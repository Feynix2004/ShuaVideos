package org.shuavideos.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shuavideos.entity.user.RolePermission;
import org.shuavideos.mapper.user.RolePermissionMapper;
import org.shuavideos.service.user.RolePermissionService;
import org.springframework.stereotype.Service;

@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission>implements RolePermissionService {
}
