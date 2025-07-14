package org.shuavideos.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shuavideos.entity.user.UserSubscribe;
import org.shuavideos.mapper.user.UserSubscribeMapper;
import org.shuavideos.service.user.UserSubscribeService;
import org.springframework.stereotype.Service;

@Service
public class UserSubscribeServiceImpl extends ServiceImpl<UserSubscribeMapper, UserSubscribe> implements UserSubscribeService {
}
