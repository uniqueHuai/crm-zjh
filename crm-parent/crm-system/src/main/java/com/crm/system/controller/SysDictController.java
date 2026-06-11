package com.crm.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.exception.BizException;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.system.entity.SysDictItem;
import com.crm.system.entity.SysDictType;
import com.crm.system.service.ISysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "数据字典", description = "字典类型与字典项管理")
@RestController
@RequiredArgsConstructor
public class SysDictController {

    private final ISysDictTypeService dictTypeService;

    /* ========== 字典类型 CRUD ========== */

    @Operation(summary = "字典类型分页")
    @GetMapping("/dict-types")
    @PreAuthorize("hasAuthority('system:dict:list')")
    public R<IPage<SysDictType>> pageTypes(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "20") long size,
                                            @RequestParam(required = false) String keywords) {
        return R.ok(dictTypeService.selectPageWithCondition(new Page<>(page, size), keywords));
    }

    @Operation(summary = "新增字典类型")
    @PostMapping("/dict-types")
    @PreAuthorize("hasAuthority('system:dict:create')")
    @OperationLog(module = "system", action = "create", description = "新增字典类型")
    public R<Void> createType(@RequestBody SysDictType dictType) {
        dictTypeService.save(dictType);
        return R.ok();
    }

    @Operation(summary = "字典类型详情")
    @GetMapping("/dict-types/{id}")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public R<SysDictType> getType(@PathVariable Long id) {
        return R.ok(dictTypeService.getById(id));
    }

    @Operation(summary = "更新字典类型")
    @PutMapping("/dict-types/{id}")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @OperationLog(module = "system", action = "update", description = "更新字典类型")
    public R<Void> updateType(@PathVariable Long id, @RequestBody SysDictType dictType) {
        dictType.setId(id);
        dictTypeService.updateById(dictType);
        return R.ok();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/dict-types/{id}")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @OperationLog(module = "system", action = "delete", description = "删除字典类型")
    public R<Void> deleteType(@PathVariable Long id) {
        SysDictType type = dictTypeService.getById(id);
        if (type != null && dictTypeService.hasItems(type.getTypeCode())) {
            throw new BizException(409002, "存在字典项，禁止删除");
        }
        dictTypeService.removeById(id);
        return R.ok();
    }

    /* ========== 字典项 CRUD ========== */

    @Operation(summary = "获取字典项列表")
    @GetMapping("/dict-types/{typeCode}/items")
    @PreAuthorize("hasAuthority('system:dict:list')")
    public R<Map<String, List<SysDictItem>>> getItems(@PathVariable String typeCode) {
        return R.ok(Map.of("records", dictTypeService.selectItemsByTypeCode(typeCode)));
    }

    @Operation(summary = "新增字典项")
    @PostMapping("/dict-types/{typeCode}/items")
    @PreAuthorize("hasAuthority('system:dict:create')")
    @OperationLog(module = "system", action = "create", description = "新增字典项")
    public R<Void> createItem(@PathVariable String typeCode, @RequestBody SysDictItem item) {
        item.setTypeCode(typeCode);
        dictTypeService.createDictItem(item);
        return R.ok();
    }

    @Operation(summary = "更新字典项")
    @PutMapping("/dict-items/{id}")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @OperationLog(module = "system", action = "update", description = "更新字典项")
    public R<Void> updateItem(@PathVariable Long id, @RequestBody SysDictItem item) {
        item.setId(id);
        dictTypeService.updateDictItem(item);
        return R.ok();
    }

    @Operation(summary = "删除字典项")
    @DeleteMapping("/dict-items/{id}")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @OperationLog(module = "system", action = "delete", description = "删除字典项")
    public R<Void> deleteItem(@PathVariable Long id) {
        dictTypeService.removeDictItem(id);
        return R.ok();
    }

    /* ========== 批量获取 ========== */

    @Operation(summary = "批量获取字典（前端一次性加载）")
    @GetMapping("/dicts")
    public R<Map<String, List<SysDictItem>>> getDicts(@RequestParam("codes") String codes) {
        return R.ok(dictTypeService.selectDictByCodes(codes.split(",")));
    }
}
