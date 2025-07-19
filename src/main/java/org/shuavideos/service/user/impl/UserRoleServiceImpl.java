package org.shuavideos.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shuavideos.entity.user.UserRole;
import org.shuavideos.mapper.user.UserRoleMapper;
import org.shuavideos.service.user.UserRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
}
