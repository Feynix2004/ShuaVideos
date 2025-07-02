package org.shuavideos.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shuavideos.entity.user.FavoritesVideo;
import org.shuavideos.mapper.user.FavoritesVideoMapper;
import org.shuavideos.service.user.FavoritesVideoService;
import org.springframework.stereotype.Service;

@Service
public class FavoritesVideoServiceImpl extends ServiceImpl<FavoritesVideoMapper, FavoritesVideo> implements FavoritesVideoService {

}
