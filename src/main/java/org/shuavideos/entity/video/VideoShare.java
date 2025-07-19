package org.shuavideos.entity.video;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.shuavideos.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = false)
public class VideoShare extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long videoId;

    private Long userId;

    private String ip;
}
