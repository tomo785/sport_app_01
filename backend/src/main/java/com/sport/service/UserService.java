package com.sport.service;

import com.sport.api.dto.AdminLoginDTO;
import com.sport.api.dto.LoginDTO;
import com.sport.api.dto.PasswordLoginDTO;
import com.sport.api.dto.RegisterDTO;
import com.sport.api.dto.SmsCodeDTO;
import com.sport.api.vo.CheckFirstLoginVO;
import com.sport.api.vo.LoginVO;
import com.sport.api.vo.UserVO;
import com.sport.model.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 发送短信验证码
     */
    void sendSmsCode(SmsCodeDTO dto);

    /**
     * 手机号验证码登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 密码登录
     */
    LoginVO loginByPassword(PasswordLoginDTO dto);

    /**
     * 管理员登录
     */
    LoginVO adminLogin(AdminLoginDTO dto);

    /**
     * 用户注册
     */
    LoginVO register(RegisterDTO dto);

    /**
     * 检查是否首次登录
     */
    CheckFirstLoginVO checkFirstLogin(String phone);

    /**
     * 验证验证码
     */
    boolean verifySmsCode(String phone, String code, Integer type);

    /**
     * 根据ID获取用户信息
     */
    User getUserById(Long userId);

    /**
     * 根据手机号获取用户信息
     */
    User getUserByPhone(String phone);

    /**
     * 根据微信OpenID获取用户信息
     */
    User getUserByOpenid(String openid);

    /**
     * 根据昵称获取用户信息
     */
    User getUserByNickname(String nickname);

    /**
     * 根据账号获取用户信息
     */
    User getUserByUsername(String username);

    /**
     * 创建用户
     */
    User createUser(User user);

    /**
     * 更新用户信息
     */
    void updateUser(User user);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 转换为VO
     */
    UserVO toVO(User user);
}
