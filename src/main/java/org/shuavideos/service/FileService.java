package org.shuavideos.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shuavideos.entity.File;

public interface FileService extends IService<File> {

    Long save(String fileKey,Long userId);

    /**
     * 获取文件真实URL
     * @param fileId 文件id
     * @return
     */
    File getFileTrustUrl(Long fileId);


    /**
     * 根据视频id生成图片
     * @param fileId
     * @return
     */
    Long generatePhoto(Long fileId, Long userId);
}
