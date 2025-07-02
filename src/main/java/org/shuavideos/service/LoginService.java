package org.shuavideos.service;

import org.shuavideos.entity.Captcha;
import org.shuavideos.entity.user.User;
import org.shuavideos.entity.vo.FindPWVO;
import org.shuavideos.entity.vo.RegisterVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface LoginService {


    User login(User user);



    void captcha(String uuId, HttpServletResponse response) throws IOException;


    Boolean getCode(Captcha captcha);

    Boolean checkCode(String email, Integer code);

    Boolean register(RegisterVO registerVO);

    Boolean findPassword(FindPWVO findPWVO);
}
