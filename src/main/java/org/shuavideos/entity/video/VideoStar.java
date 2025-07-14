package org.shuavideos.entity.video;

import lombok.Data;
import org.shuavideos.entity.BaseEntity;

@Data
public class VideoStar extends BaseEntity {
    private static final long serialVersionUID = 1L;


    private Long videoId;

    private Long userId;
}
