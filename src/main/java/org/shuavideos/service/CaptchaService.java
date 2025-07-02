package org.shuavideos.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shuavideos.entity.Captcha;

import java.awt.image.BufferedImage;

public interface CaptchaService extends IService<Captcha> {

    BufferedImage getCaptcha(String uuid);

    boolean validate(Captcha captcha);

}
