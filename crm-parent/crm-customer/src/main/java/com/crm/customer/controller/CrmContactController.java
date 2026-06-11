package com.crm.customer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.exception.BizException;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.customer.entity.CrmContact;
import com.crm.customer.service.ICrmContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "联系人管理", description = "联系人 CRUD、设置主要联系人、生日提醒")
@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class CrmContactController {

    private final ICrmContactService contactService;

    @Operation(summary = "联系人列表")
    @GetMapping
    @PreAuthorize("hasAuthority('customer:contact:list')")
    public R<IPage<CrmContact>> list(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "20") long size,
                                      @RequestParam(required = false) String keywords,
                                      @RequestParam(required = false) Long customerId,
                                      @RequestParam(required = false) Boolean isDecisionMaker) {
        return R.ok(contactService.selectPageWithCondition(new Page<>(page, size), keywords, customerId, isDecisionMaker));
    }

    @Operation(summary = "新增联系人")
    @PostMapping
    @PreAuthorize("hasAuthority('customer:contact:create')")
    @OperationLog(module = "customer", action = "create", description = "新增联系人")
    public R<Void> create(@RequestBody CrmContact contact) {
        contactService.save(contact);
        return R.ok();
    }

    @Operation(summary = "更新联系人")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:contact:edit')")
    @OperationLog(module = "customer", action = "update", description = "更新联系人")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmContact contact) {
        contact.setId(id);
        contactService.updateById(contact);
        return R.ok();
    }

    @Operation(summary = "删除联系人")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:contact:delete')")
    @OperationLog(module = "customer", action = "delete", description = "删除联系人")
    public R<Void> delete(@PathVariable Long id) {
        if (!contactService.removeById(id)) {
            throw new BizException(404, "联系人不存在");
        }
        return R.ok();
    }

    @Operation(summary = "设置主要联系人")
    @PutMapping("/{id}/set-primary")
    @PreAuthorize("hasAuthority('customer:contact:edit')")
    @OperationLog(module = "customer", action = "update", description = "设置主要联系人")
    public R<Void> setPrimary(@PathVariable Long id) {
        contactService.setPrimary(id);
        return R.ok();
    }

    @Operation(summary = "联系人生日提醒")
    @GetMapping("/upcoming-birthdays")
    @PreAuthorize("hasAuthority('customer:contact:list')")
    public R<List<CrmContact>> upcomingBirthdays(@RequestParam(defaultValue = "7") int days) {
        return R.ok(contactService.selectUpcomingBirthdays(days));
    }
}
