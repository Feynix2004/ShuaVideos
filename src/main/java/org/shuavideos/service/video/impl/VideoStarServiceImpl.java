package org.shuavideos.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shuavideos.entity.video.VideoStar;
import org.shuavideos.mapper.video.VideoStarMapper;
import org.shuavideos.service.video.VideoStarService;
import org.springframework.stereotype.Service;

@Service
public class VideoStarServiceImpl extends ServiceImpl<VideoStarMapper, VideoStar> implements VideoStarService {
    @Override
    public boolean starVideo(VideoStar videoStar) {
        synchronized ((String.valueOf(videoStar.getVideoId())+String.valueOf(videoStar.getUserId())).intern()){
            try {
                // 添加概率
                this.save(videoStar);
            }catch (Exception e){
                // 存在则取消点赞
                this.remove(new LambdaQueryWrapper<VideoStar>().eq(VideoStar::getVideoId,videoStar.getVideoId()).eq(VideoStar::getUserId,videoStar.getUserId()));
                return false;
            }
            return true;
        }

    }
}
