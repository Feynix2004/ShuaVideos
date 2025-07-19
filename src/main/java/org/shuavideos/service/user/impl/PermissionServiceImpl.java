package org.shuavideos.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shuavideos.authority.AuthorityUtils;
import org.shuavideos.entity.user.*;
import org.shuavideos.mapper.user.PermissionMapper;
import org.shuavideos.service.user.PermissionService;
import org.shuavideos.service.user.RolePermissionService;
import org.shuavideos.service.user.UserRoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {



    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RolePermissionService rolePermissionService;

    // Permission比较器
    private  class PermissionComparator implements Comparator<Permission> {

        @Override
        public int compare(Permission o1, Permission o2) {
            return -o1.getSort()-o2.getSort();
        }
    }
    @Override
    public Map<String, Object> initMenu(Long uId) {


        // 创建返回结果map
        Map<String, Object> data = new HashMap<>();
        List<Menu> menus = new ArrayList<>();
        List<Menu> parentMenu = new ArrayList<>();

        // 封装权限集合
        Set<String> set = new HashSet<>();

        // 根据当期用户获取菜单

        // 根据用户id查询对应的角色id
        List<Long> rIds = userRoleService.list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId,uId).select(UserRole::getRoleId)).stream().map(UserRole::getRoleId).collect(Collectors.toList());

        if (ObjectUtils.isEmpty(rIds)){
            return Collections.EMPTY_MAP;
        }
        // 根据角色查询对应的权限id
        List<Integer> pIds = rolePermissionService.list(new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, rIds).select(RolePermission::getPermissionId)).stream().map(RolePermission::getPermissionId).collect(Collectors.toList());

        // 根据权限id查出权限
        // 查出所有权限-->转成对应的菜单对象
        list(new LambdaQueryWrapper<Permission>().in(Permission::getId,pIds))
                .stream()
                .sorted(new PermissionComparator())
                .forEach(permission -> {
                    Menu menu = new Menu();
                    BeanUtils.copyProperties(permission,menu);
                    menu.setTitle(permission.getName());
                    menus.add(menu);
                });
        // list转树形结构
        // 1. 先找到根节点
        for (Menu menu : menus) {
            // 校验是根节点以及根节点不为按钮的节点
            if (menu.getPId().compareTo(0L) == 0 && menu.getIsMenu()!=1) {

                parentMenu.add(menu);
            }
        }

        // 根据根节点找到子节点
        for (Menu menu : parentMenu) {
            findChild(menu,menus,set);
        }

        // 保存用户权限
        AuthorityUtils.setAuthority(uId,set);

        MenuKey menuKey1 = new MenuKey();
        MenuKey menuKey2 = new MenuKey();
        menuKey1.setTitle("首页");
        menuKey1.setHref("page/welcome.html?t=1");
        menuKey2.setTitle("一路顺风鸭");
        menuKey2.setHref("/index.html");
        data.put("menuInfo",parentMenu);
        data.put("homeInfo",menuKey1);
        data.put("logoInfo",menuKey2);
        return data;
    }

    private Menu findChild(Menu menu, List<Menu> menus, Set<String> set) {

        menu.setChild(new ArrayList<Menu>());
        for (Menu m : menus) {
            if (!ObjectUtils.isEmpty(m.getPath())){
                set.add(m.getPath());
            }
            if (m.getIsMenu()!=1){
                if (menu.getId().compareTo(m.getPId()) ==0 ) {
                    // 递归调用该方法
                    menu.getChild().add(findChild(m,menus,set));
                }
            }
        }
        return menu;
    }
}
