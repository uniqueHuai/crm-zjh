package com.crm.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long parentId;

    private String name;

    private String menuType;

    private String icon;

    private String routePath;

    private String component;

    private String permissionCode;

    private String queryParam;

    private Boolean isVisible;

    private Boolean isFrame;

    private Integer sortOrder;

    private Integer status;

    private String remark;

    @TableField(exist = false)
    private List<SysMenu> children;

    @TableField(exist = false)
    private List<SysMenu> buttons;
}
