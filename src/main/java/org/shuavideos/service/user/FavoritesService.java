package org.shuavideos.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shuavideos.entity.user.Favorites;

import java.util.List;

public interface FavoritesService extends IService<Favorites> {

    void remove(Long id, Long userId);


    List<Favorites> listByUserId(Long uerId);


    List<Long> listVideoIds(Long favoritesId, Long userId);

    /**
     * 收藏视频
     *
     * @param fId
     * @param vId
     * @param uId
     */
    boolean favorites(Long fId, Long vId, Long uId);

    Boolean favoritesState(Long videoId, Long userId);

    void exist(Long userId, Long defaultFavoritesId);

}
