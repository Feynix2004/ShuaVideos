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
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<Favorites> listByUserId(Long userId) {
        // 查出收藏夹id
        final List<Favorites> favorites = list(new LambdaQueryWrapper<Favorites>().eq(Favorites::getUserId, userId));
        if (ObjectUtils.isEmpty(favorites)) return Collections.EMPTY_LIST;
        // 根据收藏夹id获取对应数
        final List<Long> fIds = favorites.stream().map(Favorites::getId).collect(Collectors.toList());
        final Map<Long, Long> fMap = favoritesVideoService.list(new LambdaQueryWrapper<FavoritesVideo>().in(FavoritesVideo::getFavoritesId, fIds))
                .stream().collect(Collectors.groupingBy(FavoritesVideo::getFavoritesId,
                        Collectors.counting()));
        // 计算对应视频总数
        for (Favorites favorite : favorites) {
            final Long videoCount = fMap.get(favorite.getId());
            favorite.setVideoCount(videoCount == null ? 0 :videoCount);
        }

        return favorites;
    }

    @Override
    public List<Long> listVideoIds(Long favoritesId, Long userId) {
        return Collections.emptyList();
    }

    @Override
    public boolean favorites(Long fId, Long vId, Long uId) {
        try {
            final FavoritesVideo favoritesVideo = new FavoritesVideo();
            favoritesVideo.setFavoritesId(fId);
            favoritesVideo.setVideoId(vId);
            favoritesVideo.setUserId(uId);
            favoritesVideoService.save(favoritesVideo);

        }catch (Exception e){
            favoritesVideoService.remove(new LambdaQueryWrapper<FavoritesVideo>().eq(FavoritesVideo::getFavoritesId, fId)
                    .eq(FavoritesVideo::getVideoId, vId).eq(FavoritesVideo::getUserId, uId));
            return false;
        }
        return true;
    }

    @Override
    public Boolean favoritesState(Long videoId, Long userId) {
        return null;
    }

    @Override
    public void exist(Long userId, Long fId) {
        final int count = count(new LambdaQueryWrapper<Favorites>().eq(Favorites::getUserId, userId).eq(Favorites::getId, fId));
        if (count == 0){
            throw new BaseException("收藏夹选择错误");
        }
    }
}
