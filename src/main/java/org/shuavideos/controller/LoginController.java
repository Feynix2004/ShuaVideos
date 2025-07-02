package org.shuavideos.controller;

import org.shuavideos.entity.Captcha;
import org.shuavideos.entity.user.User;
import org.shuavideos.entity.vo.FindPWVO;
import org.shuavideos.entity.vo.RegisterVO;
import org.shuavideos.service.LoginService;
import org.shuavideos.service.user.UserService;
import org.shuavideos.util.JwtUtils;
import org.shuavideos.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping("/luckyjourney/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserService userService;


    @PostMapping
    public R login(@RequestBody @Validated User user){
        user = loginService.login(user);
        //登录成功，生成token
        String token = JwtUtils.getJwtToken(user.getId(), user.getNickName());
        final HashMap<Object, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("name", user.getNickName());
        map.put("user", user);
        return R.ok().data(map);
    }
    /**
     * 获取图形验证码
     * @param response
     * @param uuId
     * @throws IOException
     */
    @GetMapping("/captcha.jpg/{uuId}")
    public void captcha(HttpServletResponse response, @PathVariable String uuId) throws IOException {
        loginService.captcha(uuId,response);

    }

    @PostMapping("/getCode")
    public R getCode(@RequestBody @Validated Captcha captcha){
        if(!loginService.getCode(captcha)){
            return R.error().message("验证码错误");
        }
        return R.ok().message("发生成功，请耐心等待");
    }

    @PostMapping("/check")
    public  R check(String email, Integer code){
        loginService.checkCode(email, code);
        return R.ok().message("验证成功");
    }

    @PostMapping("/register")
    public R register(@RequestBody @Validated RegisterVO registerVO){
        if(!loginService.register(registerVO)){
            return R.error().message("注册失败,验证码错误");
        }
        return R.ok().message("注册成功");

    }

    @PostMapping("/findPassword")
    public R findPassword(@RequestBody @Validated FindPWVO findPWVO, HttpServletResponse response){
        final Boolean b = loginService.findPassword(findPWVO);
        return R.ok().message(b? "修改成功": "修改失败，验证码不正确");

    }


}
