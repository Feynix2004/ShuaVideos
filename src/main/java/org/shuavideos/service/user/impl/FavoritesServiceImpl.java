package org.shuavideos.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shuavideos.entity.user.Favorites;
import org.shuavideos.entity.user.FavoritesVideo;
import org.shuavideos.exception.BaseException;
import org.shuavideos.mapper.user.FavoritesMapper;
import org.shuavideos.service.user.FavoritesService;
import org.shuavideos.service.user.FavoritesVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.filter.OrderedHiddenHttpMethodFilter;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements FavoritesService {

    @Autowired
    FavoritesVideoService favoritesVideoService;


    @Override
    public void remove(Long id, Long userId) {
        //不能删除默认收藏夹
        final Favorites favorites = getOne(new LambdaQueryWrapper<Favorites>().eq(Favorites::getId,id).eq(Favorites::getUserId, userId));
        if(favorites.getName().equals("默认收藏夹")){
            throw new BaseException("默认收藏夹不允许被删除");
        }
        final boolean result = remove(new LambdaQueryWrapper<Favorites>().eq(Favorites::getId, id).eq(Favorites::getUserId, userId));
        if(result){
            favoritesVideoService.remove(new LambdaQueryWrapper<FavoritesVideo>().eq(FavoritesVideo::getFavoritesId, id));
        }else{
            throw new BaseException("你小子想删别人收藏夹？");
        }
    }

    @Override
    public List<Favorites> listByUserId(Long uerId) {
        return Collections.emptyList();
    }

    @Override
    public List<Long> listVideoIds(Long favoritesId, Long userId) {
        return Collections.emptyList();
    }

    @Override
    public boolean favorites(Long fId, Long vId) {
        return false;
    }

    @Override
    public Boolean favoritesState(Long videoId, Long userId) {
        return null;
    }

    @Override
    public void exist(Long userId, Long defaultFavoritesId) {

    }
}
