package org.shuavideos.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shuavideos.entity.user.Role;
import org.shuavideos.entity.user.Tree;
import org.shuavideos.entity.vo.AssignRoleVO;
import org.shuavideos.entity.vo.AuthorityVO;
import org.shuavideos.util.R;

import java.util.List;

public interface RoleService extends IService<Role> {
    List<Tree> tree();

    R removeRole(String id);

    R gavePermission(AuthorityVO authorityVO);

    R gaveRole(AssignRoleVO assignRoleVO);
}
