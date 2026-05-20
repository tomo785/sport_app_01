package com.sport.common.util;

import cn.hutool.crypto.symmetric.AES;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 微信加密数据解密工具
 * 用于解密微信运动、用户信息等加密数据
 */
@Slf4j
public class WeChatDecryptUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 解密微信运动数据
     *
     * @param encryptedData 加密数据
     * @param sessionKey    会话密钥
     * @param iv            加密算法初始向量
     * @return 解密后的 JSON 字符串
     */
    public static String decrypt(String encryptedData, String sessionKey, String iv) {
        try {
            byte[] key = Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            byte[] data = Base64.getDecoder().decode(encryptedData);

            // Hutool AES CBC 模式，使用 PKCS5Padding（与 PKCS7Padding 等价）
            AES aes = new AES("CBC", "PKCS5Padding", key, ivBytes);
            byte[] decrypted = aes.decrypt(data);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("微信数据解密失败", e);
            throw new RuntimeException("解密失败：" + e.getMessage());
        }
    }

    /**
     * 从解密后的微信运动数据中提取今日步数
     *
     * @param decryptedData 解密后的 JSON 字符串
     * @return 今日步数，提取失败返回 0
     */
    public static int extractTodaySteps(String decryptedData) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(decryptedData);
            JsonNode stepInfoList = root.get("stepInfoList");
            if (stepInfoList == null || !stepInfoList.isArray()) {
                return 0;
            }

            // stepInfoList 最后一项通常是今日数据
            JsonNode todayNode = stepInfoList.get(stepInfoList.size() - 1);
            if (todayNode != null && todayNode.has("step")) {
                return todayNode.get("step").asInt(0);
            }
            return 0;
        } catch (Exception e) {
            log.error("提取步数失败", e);
            return 0;
        }
    }
}
