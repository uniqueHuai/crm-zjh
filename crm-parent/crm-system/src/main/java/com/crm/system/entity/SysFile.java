package com.crm.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFile extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String fileName;

    private String originalName;

    private Long fileSize;

    private String fileType;

    private String fileUrl;

    private String thumbnailUrl;

    private String storageType;

    private String bizType;

    private Long bizId;

    private String md5Hash;

    private Boolean isPublic;
}
