package com.crm.mall.controller;

import com.crm.common.model.R;
import com.crm.mall.entity.CrmCartItem;
import com.crm.mall.mp.MpContext;
import com.crm.mall.service.ICrmCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "小程序端购物车")
@RestController
@RequestMapping("/app/cart")
@RequiredArgsConstructor
public class CrmAppCartController {

    private final ICrmCartService cartService;

    @Operation(summary = "购物车列表")
    @GetMapping
    public R<List<CrmCartItem>> list() {
        return R.ok(cartService.listByCustomerId(MpContext.getCustomerId()));
    }

    @Operation(summary = "添加购物车")
    @PostMapping
    public R<Void> add(@RequestBody Map<String, Object> body) {
        Long skuId = Long.valueOf(body.get("skuId").toString());
        Integer quantity = body.containsKey("quantity") ? Integer.valueOf(body.get("quantity").toString()) : 1;
        cartService.addItem(MpContext.getCustomerId(), skuId, quantity);
        return R.ok();
    }

    @Operation(summary = "修改数量")
    @PutMapping("/{id}/quantity")
    public R<Void> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        cartService.updateQuantity(id, MpContext.getCustomerId(), body.get("quantity"));
        return R.ok();
    }

    @Operation(summary = "选中/取消选中")
    @PutMapping("/{id}/selected")
    public R<Void> toggleSelected(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        cartService.toggleSelected(id, MpContext.getCustomerId(), body.get("selected"));
        return R.ok();
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        cartService.deleteItem(id, MpContext.getCustomerId());
        return R.ok();
    }
}
