package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISysFileService extends IService<SysFile> {

    SysFile uploadFile(MultipartFile file, String bizType, Boolean isPublic);

    /** 获取文件下载 URL */
    String getDownloadUrl(Long id, Long expiresIn);

    /** 获取文件内容流 */
    org.springframework.core.io.InputStreamResource downloadFile(Long id);

    List<SysFile> selectByBiz(String bizType, Long bizId);
}
