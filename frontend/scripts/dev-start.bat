@echo off
chcp 65001 >nul
echo ==========================================
echo    run 运动App 前端开发服务器启动器
echo ==========================================
echo.
echo  默认账号信息:
echo    管理员: qwen / qwerty
echo    游客模式: 无需账号
echo.
echo home 页面导航:
echo    首页 - 运动统计和快捷入口
echo    个人中心 - 用户信息、退出登录
echo.
echo url 访问地址: http://localhost:5173
echo.
echo ==========================================
echo.
npm run dev:h5
pause