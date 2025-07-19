package org.shuavideos.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shuavideos.entity.user.Permission;

import java.util.Map;

public interface PermissionService extends IService<Permission> {
    Map<String, Object> initMenu(Long userId);
}
