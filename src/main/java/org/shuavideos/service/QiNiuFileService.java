package org.shuavideos.service;

import com.qiniu.storage.model.FileInfo;

public interface QiNiuFileService extends FileCloudService {
    /**
     * 获取文件信息
     * @param url
     * @return
     */
    FileInfo getFileInfo(String url);
}
