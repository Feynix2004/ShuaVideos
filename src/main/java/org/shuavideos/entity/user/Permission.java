package org.shuavideos.entity.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.shuavideos.entity.BaseEntity;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Permission extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long pId;

    private String path;

    private String href;

    private String icon;

    private String name;

    private Integer isMenu;

    private String target;

    private Integer sort;

    private Integer state;

    @TableField(exist = false)
    private List<Permission> children;

}
