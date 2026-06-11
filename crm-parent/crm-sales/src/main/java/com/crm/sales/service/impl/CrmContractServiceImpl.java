package com.crm.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.sales.entity.CrmContract;
import com.crm.sales.entity.CrmContractItem;
import com.crm.sales.mapper.CrmContractItemMapper;
import com.crm.sales.mapper.CrmContractMapper;
import com.crm.sales.service.ICrmContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrmContractServiceImpl extends ServiceImpl<CrmContractMapper, CrmContract> implements ICrmContractService {

    private final CrmContractMapper contractMapper;
    private final CrmContractItemMapper contractItemMapper;

    @Override
    public IPage<CrmContract> selectPageWithCondition(Page<CrmContract> page, String keywords, Long customerId, String status,
                                                       String startDate, String endDate) {
        return contractMapper.selectPageWithCondition(page, keywords, customerId, status, startDate, endDate);
    }

    @Override
    public CrmContract selectWithItems(Long id) {
        CrmContract contract = getById(id);
        if (contract == null) throw new BizException(404, "合同不存在");
        contract.setItems(contractItemMapper.selectList(
                new LambdaQueryWrapper<CrmContractItem>()
                        .eq(CrmContractItem::getContractId, id)));
        return contract;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sign(Long id, Map<String, Object> params) {
        CrmContract contract = getById(id);
        if (contract == null) throw new BizException(404, "合同不存在");
        return lambdaUpdate().eq(CrmContract::getId, id)
                .set(CrmContract::getStatus, "signed")
                .set(CrmContract::getSignType, params.get("signType"))
                .set(CrmContract::getPlatform, params.get("platform"))
                .set(CrmContract::getSignUrl, params.get("signUrl"))
                .set(CrmContract::getSignedAt, LocalDateTime.now())
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long id, Map<String, Object> params) {
        CrmContract contract = getById(id);
        if (contract == null) throw new BizException(404, "合同不存在");
        return lambdaUpdate().eq(CrmContract::getId, id)
                .set(CrmContract::getStatus, "cancelled")
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long renewal(Long id, Map<String, Object> params) {
        CrmContract old = getById(id);
        if (old == null) throw new BizException(404, "合同不存在");
        CrmContract newContract = new CrmContract();
        newContract.setCustomerId(old.getCustomerId());
        newContract.setTitle(old.getTitle());
        newContract.setTotalAmount(old.getTotalAmount());
        save(newContract);
        return newContract.getId();
    }
}
