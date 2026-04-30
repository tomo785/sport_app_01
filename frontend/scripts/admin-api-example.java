/**
 * 管理员登录后端接口示例 (Spring Boot)
 * 供后端开发人员参考实现
 */

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 管理员登录
     * POST /api/v1/admin/login
     */
    @PostMapping("/login")
    public Result<AdminLoginVO> login(@RequestBody @Valid AdminLoginDTO loginDTO) {
        // 1. 查询管理员
        Admin admin = adminService.findByUsername(loginDTO.getUsername());
        if (admin == null || admin.getStatus() != 1) {
            return Result.error("账号或密码错误");
        }

        // 2. 验证密码 (BCrypt)
        if (!BCrypt.checkpw(loginDTO.getPassword(), admin.getPassword())) {
            return Result.error("账号或密码错误");
        }

        // 3. 更新登录信息
        adminService.updateLoginInfo(admin.getId(), request.getRemoteAddr());

        // 4. 生成 JWT Token
        String token = jwtTokenUtil.generateToken(admin.getId(), admin.getUsername(), "admin");

        // 5. 返回数据
        AdminLoginVO vo = new AdminLoginVO();
        vo.setToken(token);
        vo.setId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setNickname(admin.getNickname());
        vo.setAvatar(admin.getAvatar());
        vo.setRole(admin.getRole());

        return Result.success(vo);
    }
}

/**
 * 登录请求 DTO
 */
@Data
public class AdminLoginDTO {
    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}

/**
 * 登录响应 VO
 */
@Data
public class AdminLoginVO {
    private String token;
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
}
