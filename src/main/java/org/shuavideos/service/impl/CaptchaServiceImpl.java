package org.shuavideos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.code.kaptcha.Producer;
import org.shuavideos.constant.RedisConstant;
import org.shuavideos.entity.Captcha;
import org.shuavideos.exception.BaseException;
import org.shuavideos.mapper.CaptchaMapper;
import org.shuavideos.service.CaptchaService;
import org.shuavideos.service.EmailService;
import org.shuavideos.util.DateUtil;
import org.shuavideos.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.Date;

@Service
public class CaptchaServiceImpl extends ServiceImpl<CaptchaMapper, Captcha> implements CaptchaService {

    @Autowired
    private Producer producer;
    @Autowired
    private RedisCacheUtil redisCacheUtil;
    @Autowired
    private EmailService emailService;


    @Override
    public BufferedImage getCaptcha(String uuid) {
        String code = this.producer.createText();
        Captcha captcha = new Captcha();
        captcha.setUuid(uuid);
        captcha.setCode(code);
        captcha.setExpireTime(DateUtil.addDateMinutes(new Date(), 5));
        this.save(captcha);
        return producer.createImage(code);
    }

    @Override
    public boolean validate(Captcha captcha) {
        String email = captcha.getEmail();
        final String code1 = captcha.getCode();
        captcha = this.getOne(new LambdaQueryWrapper<Captcha>().eq(Captcha::getUuid, captcha.getUuid()));
        if(captcha == null) throw new BaseException("uuid为空");
        this.remove(new LambdaQueryWrapper<Captcha>().eq(Captcha::getUuid, captcha.getUuid()));
        if(!captcha.getCode().equals(code1)){
            throw new BaseException("code错误");
        }
        if(captcha.getExpireTime().getTime()<=System.currentTimeMillis()) throw new BaseException("uuid过期");

        String code = getSixCode();
        redisCacheUtil.set(RedisConstant.EMAIL_CODE+email, code, RedisConstant.EMAIL_CODE_TIME);
        emailService.send(email, "注册验证码:"+code+",验证码5分钟之内有效");
        return true;
    }


    public static String getSixCode(){
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<6; i++){
            int code = (int)(Math.random()*10);
            builder.append(code);
        }
        return builder.toString();
    }
}
