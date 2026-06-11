package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.entity.SysDept;

import java.util.List;

public interface ISysDeptService extends IService<SysDept> {

    List<SysDept> selectDeptTree();

    boolean createDept(SysDept dept);

    boolean updateDept(SysDept dept);

    boolean hasChildrenOrUser(Long deptId);

    List<Long> selectChildDeptIds(Long deptId);
}
