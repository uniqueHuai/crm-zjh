package com.crm.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
public class SysDictItem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String typeCode;

    private String itemCode;

    private String itemValue;

    private Integer sortOrder;

    private String cssClass;

    private Boolean isDefault;

    private Integer status;

    private String remark;
}
