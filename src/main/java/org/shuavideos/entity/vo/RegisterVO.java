package org.shuavideos.entity.vo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class RegisterVO {

    @Email(message = "邮箱不可为空")
    private String email;

    @NotBlank(message = "密码不可为空")
    private  String password;
    @NotBlank(message = "验证码不可为空")
    private String code;
    @NotBlank(message = "验证码不可为空")
    private String uuid;

    @NotBlank(message = "用户名不可为空")
    private String nickName;

}
