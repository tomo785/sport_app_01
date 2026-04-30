package com.sport.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sport.api.dto.AdminLoginDTO;
import com.sport.api.dto.LoginDTO;
import com.sport.api.dto.PasswordLoginDTO;
import com.sport.api.dto.RegisterDTO;
import com.sport.api.dto.SmsCodeDTO;
import com.sport.api.vo.CheckFirstLoginVO;
import com.sport.api.vo.LoginVO;
import com.sport.api.vo.UserVO;
import com.sport.common.constant.ResultCode;
import com.sport.common.exception.BusinessException;
import com.sport.common.util.JwtUtil;
import com.sport.mapper.UserMapper;
import com.sport.model.entity.User;
import com.sport.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String SMS_CODE_KEY = "sms:code:";
    private static final int SMS_CODE_EXPIRE_MINUTES = 5;

    @Override
    public void sendSmsCode(SmsCodeDTO dto) {
        String phone = dto.getPhone();
        Integer type = dto.getType();

        // 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);

        // 存储到Redis，5分钟过期
        String key = SMS_CODE_KEY + type + ":" + phone;
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(SMS_CODE_EXPIRE_MINUTES));

        log.info("发送验证码 - 手机号: {}, 验证码: {}, 类型: {}", phone, code, type);

        // TODO: 调用短信服务发送验证码
        // smsService.send(phone, code);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        String phone = dto.getPhone();
        String code = dto.getCode();

        // 验证验证码
        if (!verifySmsCode(phone, code, 2)) {
            throw new BusinessException(ResultCode.SMS_CODE_ERROR);
        }

        // 删除已使用的验证码
        String key = SMS_CODE_KEY + "2:" + phone;
        redisTemplate.delete(key);

        // 查询或创建用户
        User user = getUserByPhone(phone);
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickname("运动爱好者" + RandomUtil.randomNumbers(4));
            user.setGender(0);
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
            log.info("新用户注册 - 用户ID: {}, 手机号: {}", user.getId(), phone);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public LoginVO loginByPassword(PasswordLoginDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();

        // 查询用户
        User user = getUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 验证密码
        String encryptedPassword = DigestUtil.md5Hex(password);
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public LoginVO register(RegisterDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();

        // 检查账号是否已存在
        User existingUser = getUserByUsername(username);
        if (existingUser != null) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }

        // 加密密码
        String encryptedPassword = DigestUtil.md5Hex(password);

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptedPassword);
        user.setNickname("运动爱好者" + RandomUtil.randomNumbers(4));
        user.setGender(0);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        log.info("用户注册成功 - 用户ID: {}, 账号: {}", user.getId(), username);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public CheckFirstLoginVO checkFirstLogin(String phone) {
        User user = getUserByPhone(phone);
        
        return CheckFirstLoginVO.builder()
                .isFirstLogin(user == null)
                .userExists(user != null)
                .build();
    }

    @Override
    public boolean verifySmsCode(String phone, String code, Integer type) {
        String key = SMS_CODE_KEY + type + ":" + phone;
        String savedCode = redisTemplate.opsForValue().get(key);
        
        if (StrUtil.isBlank(savedCode)) {
            return false;
        }
        
        return savedCode.equals(code);
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User getUserByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User getUserByOpenid(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User getUserByNickname(String nickname) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getNickname, nickname);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public LoginVO adminLogin(AdminLoginDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();

        // 查询用户（通过昵称/用户名）
        User user = getUserByNickname(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 检查是否是管理员
        if (user.getRole() == null || user.getRole() != 2) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 验证密码
        //String encryptedPassword = DigestUtil.md5Hex(password);
        String encryptedPassword = password;
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());

        log.info("管理员登录成功 - 用户ID: {}, 用户名: {}", user.getId(), username);

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public User createUser(User user) {
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    @Override
    public void updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 校验旧密码
        String encryptedOldPassword = DigestUtil.md5Hex(oldPassword);
        if (user.getPassword() != null && !encryptedOldPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 更新新密码
        String encryptedNewPassword = DigestUtil.md5Hex(newPassword);
        user.setPassword(encryptedNewPassword);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
