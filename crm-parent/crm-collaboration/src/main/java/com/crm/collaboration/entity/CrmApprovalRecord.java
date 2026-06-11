package com.crm.collaboration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("coll_approval_node_record")
public class CrmApprovalRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long instanceId;

    private Long stepId;

    private String stepName;

    private Long approverId;

    private String action;

    private String comment;

    private String signatureImage;

    private LocalDateTime actedAt;
}
