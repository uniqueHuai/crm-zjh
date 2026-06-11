package com.crm.mall.controller;

import com.crm.common.model.R;
import com.crm.mall.entity.CrmAddress;
import com.crm.mall.mp.MpContext;
import com.crm.mall.service.ICrmAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "小程序端地址")
@RestController
@RequestMapping("/app/addresses")
@RequiredArgsConstructor
public class CrmAppAddressController {

    private final ICrmAddressService addressService;

    @Operation(summary = "地址列表")
    @GetMapping
    public R<List<CrmAddress>> list() {
        return R.ok(addressService.listByCustomerId(MpContext.getCustomerId()));
    }

    @Operation(summary = "新增地址")
    @PostMapping
    public R<Void> create(@RequestBody CrmAddress address) {
        address.setCustomerId(MpContext.getCustomerId());
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressService.setDefault(null, MpContext.getCustomerId());
        }
        addressService.save(address);
        return R.ok();
    }

    @Operation(summary = "编辑地址")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmAddress address) {
        address.setId(id);
        address.setCustomerId(MpContext.getCustomerId());
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressService.setDefault(id, MpContext.getCustomerId());
        }
        addressService.updateById(address);
        return R.ok();
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long customerId = MpContext.getCustomerId();
        boolean removed = addressService.lambdaUpdate()
                .eq(CrmAddress::getId, id)
                .eq(CrmAddress::getCustomerId, customerId)
                .remove();
        if (!removed) {
            return R.failed(404, "地址不存在或无权删除");
        }
        return R.ok();
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/{id}/default")
    public R<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id, MpContext.getCustomerId());
        return R.ok();
    }
}
