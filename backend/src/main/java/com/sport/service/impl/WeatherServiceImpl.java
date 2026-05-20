package com.sport.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sport.api.vo.WeatherNowVO;
import com.sport.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * 天气服务 — 代理天气 API（JWT Bearer 鉴权）
 */
@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${weather.api-key}")
    private String apiKey;

    @Value("${weather.base-url}")
    private String baseUrl;

    @Value("${weather.geo-url}")
    private String geoUrl;

    @Value("${weather.aqi-url}")
    private String aqiUrl;

    @Value("${weather.indices-url}")
    private String indicesUrl;

    public WeatherServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public WeatherNowVO getNowWeather(BigDecimal lat, BigDecimal lng) {
        WeatherNowVO vo = new WeatherNowVO();

        // 坐标格式: lng,lat（保留最多两位小数）
        String location = lng.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                + "," + lat.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();

        log.info("===== 天气查询开始: lat={}, lng={}, location={}", lat, lng, location);
        log.info("API Host: base={}, geo={}, aqi={}, indices={}", baseUrl, geoUrl, aqiUrl, indicesUrl);

        // X-QW-Api-Key 鉴权公共 headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-QW-Api-Key", apiKey);
        headers.set("Accept-Encoding", "gzip");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 1. 实时天气
        try {
            String weatherUrl = baseUrl + "/v7/weather/now?location=" + location;
            log.info("[天气API] 请求: GET {}", weatherUrl);
            ResponseEntity<byte[]> weatherResp = restTemplate.exchange(weatherUrl, HttpMethod.GET, entity, byte[].class);
            log.info("[天气API] 响应: HTTP {}", weatherResp.getStatusCode().value());
            if (weatherResp.getBody() != null) {
                JsonNode root = readJsonBody(weatherResp.getBody());
                String code = root.path("code").asText();
                if ("200".equals(code)) {
                    JsonNode now = root.path("now");
                    vo.setTemp(now.path("temp").asText());
                    vo.setFeelsLike(now.path("feelsLike").asText());
                    vo.setText(now.path("text").asText());
                    vo.setIcon(now.path("icon").asText());
                    vo.setWindDir(now.path("windDir").asText());
                    log.info("[天气API] 成功: temp={}, text={}", vo.getTemp(), vo.getText());
                } else {
                    log.warn("[天气API] 返回异常code={}", code);
                }
            }
        } catch (RestClientResponseException e) {
            log.error("[天气API] HTTP调用失败: status={}, body={}", e.getStatusCode().value(), decodeErrorBody(e));
        } catch (Exception e) {
            log.error("[天气API] 调用失败: {}", e.getMessage());
        }

        // 2. 空气质量 AQI（path 参数: /airquality/v1/current/{lat}/{lng}）
        try {
            String latStr = lat.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            String lngStr = lng.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            String airUrl = aqiUrl + "/airquality/v1/current/" + latStr + "/" + lngStr;
            log.info("[AQI API] 请求: GET {}", airUrl);
            ResponseEntity<byte[]> airResp = restTemplate.exchange(airUrl, HttpMethod.GET, entity, byte[].class);
            log.info("[AQI API] 响应: HTTP {}", airResp.getStatusCode().value());
            if (airResp.getBody() != null) {
                JsonNode root = readJsonBody(airResp.getBody());
                JsonNode indexes = root.path("indexes");
                if (indexes.isArray()) {
                    JsonNode fallback = indexes.size() > 0 ? indexes.get(0) : null;
                    for (JsonNode idx : indexes) {
                        if ("qaqi".equals(idx.path("code").asText())) {
                            vo.setAqi(idx.path("aqiDisplay").asText());
                            vo.setAqiLevel(idx.path("category").asText());
                            JsonNode color = idx.path("color");
                            vo.setAqiColor(String.format("#%02x%02x%02x",
                                    color.path("red").asInt(),
                                    color.path("green").asInt(),
                                    color.path("blue").asInt()));
                            break;
                        }
                    }
                    if (vo.getAqi() == null && fallback != null) {
                        fillAqi(vo, fallback);
                    }
                }
                log.info("[AQI API] 成功: aqi={}, level={}", vo.getAqi(), vo.getAqiLevel());
            }
        } catch (RestClientResponseException e) {
            log.warn("[AQI API] HTTP调用失败(空气质量数据可能需要额外订阅): status={}, body={}", e.getStatusCode().value(), decodeErrorBody(e));
        } catch (Exception e) {
            log.warn("[AQI API] 调用失败(空气质量数据可能需要额外订阅): {}", e.getMessage());
        }

        // 3. 城市名（反查地理位置）
        try {
            String cityUrl = geoUrl + "/geo/v2/city/lookup?location=" + location;
            log.info("[Geo API] 请求: GET {}", cityUrl);
            ResponseEntity<byte[]> cityResp = restTemplate.exchange(cityUrl, HttpMethod.GET, entity, byte[].class);
            log.info("[Geo API] 响应: HTTP {}", cityResp.getStatusCode().value());
            if (cityResp.getBody() != null) {
                JsonNode root = readJsonBody(cityResp.getBody());
                String code = root.path("code").asText();
                if ("200".equals(code)) {
                    JsonNode cityList = root.path("location");
                    if (cityList.isArray() && cityList.size() > 0) {
                        vo.setCity(cityList.get(0).path("name").asText());
                    }
                }
                log.info("[Geo API] 成功: city={}", vo.getCity());
            }
        } catch (RestClientResponseException e) {
            log.warn("[Geo API] HTTP调用失败: status={}, body={}", e.getStatusCode().value(), decodeErrorBody(e));
        } catch (Exception e) {
            log.warn("[Geo API] 调用失败: {}", e.getMessage());
        }

        // 4. 运动指数（type=1 为运动指数）
        try {
            String idxUrl = indicesUrl + "/v7/indices/1d?type=1&location=" + location;
            log.info("[指数API] 请求: GET {}", idxUrl);
            ResponseEntity<byte[]> idxResp = restTemplate.exchange(idxUrl, HttpMethod.GET, entity, byte[].class);
            log.info("[指数API] 响应: HTTP {}", idxResp.getStatusCode().value());
            if (idxResp.getBody() != null) {
                JsonNode root = readJsonBody(idxResp.getBody());
                String code = root.path("code").asText();
                if ("200".equals(code)) {
                    JsonNode daily = root.path("daily");
                    if (daily.isArray() && daily.size() > 0) {
                        JsonNode first = daily.get(0);
                        vo.setSportLevel(first.path("level").asInt());
                        vo.setSportCategory(first.path("category").asText());
                        vo.setSportText(first.path("text").asText());
                    }
                }
                log.info("[指数API] 成功: sport={} {}", vo.getSportCategory(), vo.getSportText());
            }
        } catch (RestClientResponseException e) {
            log.warn("[指数API] HTTP调用失败: status={}, body={}", e.getStatusCode().value(), decodeErrorBody(e));
        } catch (Exception e) {
            log.warn("[指数API] 调用失败: {}", e.getMessage());
        }

        // 降级：API 失败时填充默认值
        if (vo.getCity() == null || vo.getCity().isEmpty()) {
            vo.setCity("杭州");
        }
        if (vo.getTemp() == null) {
            vo.setTemp("--");
            vo.setFeelsLike("--");
            vo.setText("数据加载失败");
        }

        log.info("===== 天气查询结束: temp={}, text={}, city={}, aqi={}, sport={}",
                vo.getTemp(), vo.getText(), vo.getCity(), vo.getAqi(), vo.getSportCategory());
        return vo;
    }

    private JsonNode readJsonBody(byte[] body) throws IOException {
        return objectMapper.readTree(decodeBody(body));
    }

    private void fillAqi(WeatherNowVO vo, JsonNode index) {
        vo.setAqi(index.path("aqiDisplay").asText());
        vo.setAqiLevel(index.path("category").asText());
        JsonNode color = index.path("color");
        vo.setAqiColor(String.format("#%02x%02x%02x",
                color.path("red").asInt(),
                color.path("green").asInt(),
                color.path("blue").asInt()));
    }

    private String decodeErrorBody(RestClientResponseException e) {
        String body = decodeBody(e.getResponseBodyAsByteArray());
        if (body.length() > 500) {
            return body.substring(0, 500) + "...";
        }
        return body;
    }

    private String decodeBody(byte[] body) {
        if (body == null || body.length == 0) {
            return "";
        }
        try {
            if (isGzip(body)) {
                try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(body))) {
                    return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (IOException e) {
            log.warn("Gzip response decode failed, fallback to UTF-8 text: {}", e.getMessage());
        }
        return new String(body, StandardCharsets.UTF_8);
    }

    private boolean isGzip(byte[] body) {
        return body.length >= 2 && (body[0] & 0xff) == 0x1f && (body[1] & 0xff) == 0x8b;
    }
}
