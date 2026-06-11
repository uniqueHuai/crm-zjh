package com.crm.sales.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.sales.entity.CrmPaymentPlan;
import com.crm.sales.mapper.CrmPaymentPlanMapper;
import com.crm.sales.service.ICrmPaymentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrmPaymentPlanServiceImpl extends ServiceImpl<CrmPaymentPlanMapper, CrmPaymentPlan>
        implements ICrmPaymentPlanService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean settle(Long id, Map<String, Object> params) {
        CrmPaymentPlan plan = getById(id);
        if (plan == null) throw new BizException(404, "回款计划不存在");
        plan.setActualAmount(new BigDecimal(params.get("actualAmount").toString()));
        plan.setPaidDate(LocalDate.parse(params.get("paidDate").toString()));
        plan.setStatus("paid");
        plan.setPaymentMethod((String) params.get("paymentMethod"));
        return updateById(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CrmPaymentPlan> batchGenerate(Long contractId, Map<String, Object> params) {
        int installments = Integer.parseInt(params.get("installments").toString());
        BigDecimal totalAmount = new BigDecimal(params.get("totalAmount").toString());
        LocalDate firstDate = LocalDate.parse(params.get("firstDate").toString());
        int intervalDays = Integer.parseInt(params.get("intervalDays").toString());
        BigDecimal perAmount = totalAmount.divide(BigDecimal.valueOf(installments), 2, java.math.RoundingMode.HALF_UP);

        List<CrmPaymentPlan> plans = new ArrayList<>();
        for (int i = 0; i < installments; i++) {
            CrmPaymentPlan plan = new CrmPaymentPlan();
            plan.setContractId(contractId);
            plan.setStage(i + 1);
            plan.setStageName("第" + (i + 1) + "期");
            plan.setExpectedAmount(i == installments - 1
                    ? totalAmount.subtract(perAmount.multiply(BigDecimal.valueOf(installments - 1)))
                    : perAmount);
            plan.setExpectedDate(firstDate.plusDays((long) i * intervalDays));
            plan.setStatus("pending");
            save(plan);
            plans.add(plan);
        }
        return plans;
    }
}
