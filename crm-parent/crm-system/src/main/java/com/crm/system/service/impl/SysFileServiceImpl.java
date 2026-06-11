package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.common.enums.ResultCode;
import com.crm.framework.storage.FileStorage;
import com.crm.system.entity.SysFile;
import com.crm.system.mapper.SysFileMapper;
import com.crm.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {

    private final FileStorage fileStorage;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFile uploadFile(MultipartFile file, String bizType, Boolean isPublic) {
        if (file.isEmpty()) {
            throw new BizException(ResultCode.INVALID_PARAM, "文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")) : "";
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 写入对象存储
        String fileUrl;
        try {
            fileUrl = fileStorage.store(fileName, file.getBytes(), file.getContentType());
        } catch (Exception e) {
            throw new RuntimeException("文件存储失败", e);
        }

        SysFile sysFile = new SysFile();
        sysFile.setFileName(fileName);
        sysFile.setOriginalName(originalName);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(file.getContentType());
        sysFile.setFileUrl(fileUrl);
        sysFile.setStorageType(fileStorage.getType());
        sysFile.setBizType(bizType);
        sysFile.setIsPublic(isPublic != null && isPublic);
        save(sysFile);
        return sysFile;
    }

    @Override
    public String getDownloadUrl(Long id, Long expiresIn) {
        SysFile sysFile = getById(id);
        if (sysFile == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文件不存在");
        }
        return fileStorage.getDownloadUrl(sysFile.getFileUrl(), expiresIn != null ? expiresIn : 3600);
    }

    @Override
    public InputStreamResource downloadFile(Long id) {
        SysFile sysFile = getById(id);
        if (sysFile == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文件不存在");
        }
        try {
            return new InputStreamResource(fileStorage.getContent(sysFile.getFileUrl()));
        } catch (Exception e) {
            throw new RuntimeException("文件读取失败", e);
        }
    }

    @Override
    public List<SysFile> selectByBiz(String bizType, Long bizId) {
        return lambdaQuery()
                .eq(SysFile::getBizType, bizType)
                .eq(bizId != null, SysFile::getBizId, bizId)
                .orderByDesc(SysFile::getCreatedAt)
                .list();
    }
}
