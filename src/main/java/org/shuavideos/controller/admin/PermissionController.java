package org.shuavideos.controller.admin;


import org.shuavideos.holder.UserHolder;
import org.shuavideos.service.user.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/authorize/permission")
public class PermissionController {


    @Autowired
    private PermissionService permissionService;

    /**
     * 初始化菜单
     * @return
     */
    @GetMapping("/initMenu")
    public Map<String, Object> initMenu(){
        Map<String, Object> data = permissionService.initMenu(UserHolder.get());
        return data;
    }
}
