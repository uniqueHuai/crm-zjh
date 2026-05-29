package com.crm.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String password;

    private String salt;

    private String realName;

    private String nickname;

    private String avatar;

    private String phone;

    private String email;

    private Integer gender;

    private Long deptId;

    private String post;

    private Integer status;

    private String lastLoginIp;

    private LocalDateTime lastLoginAt;

    private Integer pwdErrorCount;

    private LocalDateTime pwdUpdatedAt;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private java.util.List<Long> roleIds;
}
