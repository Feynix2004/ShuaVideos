package org.shuavideos.entity.user;


import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.shuavideos.entity.BaseEntity;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
public class User extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String nickName;

    @Email
    private String email;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String description;

    //true为男 false为女
    private Boolean sex;

    //用户默认收藏夹id
    private Long defaultFavoritesId;

    //头像
    private Long avatar;

    @TableField(exist = false)
    private Boolean each;

    @TableField(exist = false)
    private Set<String> roleName;
}
