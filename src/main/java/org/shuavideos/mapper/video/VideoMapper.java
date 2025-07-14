package org.shuavideos.mapper.video;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.shuavideos.entity.video.Video;

import java.util.List;

public interface VideoMapper extends BaseMapper<Video> {

    @Select("SELECT id,share_count,history_count,start_count,favorites_count,gmt_created,title FROM video WHERE id > " +
            "#{id} and open = 0 and audit_status = 0 and DATEDIFF(gmt_created,NOW())<=0 AND DATEDIFF(gmt_created,NOW())>- #{days} limit #{limit}")
    List<Video> selectNDaysAgeVideo(@Param("id") long id, @Param("days") int days, @Param("limit") int limit);
}
