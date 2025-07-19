package org.shuavideos.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shuavideos.authority.Authority;
import org.shuavideos.config.LocalCache;
import org.shuavideos.entity.Setting;
import org.shuavideos.entity.json.SettingScoreJson;
import org.shuavideos.service.SettingService;
import org.shuavideos.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/setting")
public class AdminSettingController {

    @Autowired
    private SettingService settingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    @Authority("admin:setting:get")
    public R get() throws JsonProcessingException {
        final Setting setting = settingService.list(null).get(0);
        final SettingScoreJson settingScoreJson = objectMapper.readValue(setting.getAuditPolicy(), SettingScoreJson.class);
        setting.setSettingScoreJson(settingScoreJson);
        return R.ok().data(setting);
    }


    @PutMapping
    @Authority("admin:setting:update")
    public R update(@RequestBody @Validated Setting setting){
        settingService.updateById(setting);
        for (String s : setting.getAllowIp().split(",")) {
            LocalCache.put(s,true);
        }
        return R.ok().message("修改成功");
    }
}
