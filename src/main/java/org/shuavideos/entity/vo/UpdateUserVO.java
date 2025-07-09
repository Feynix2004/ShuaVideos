package org.shuavideos.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateUserVO {

    @NotBlank(message = "昵称不可为空")
    private String nickName;

    private Long userId;

    private Long avatar;

    private Boolean sex;

    private String description;

    @NotNull
    private Long defaultFavoritesId;

}
