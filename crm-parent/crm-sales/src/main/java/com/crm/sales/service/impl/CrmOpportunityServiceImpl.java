package com.crm.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.customer.entity.CrmCustomer;
import com.crm.customer.service.ICrmCustomerService;
import com.crm.sales.entity.CrmOpportunity;
import com.crm.sales.entity.CrmOpportunityParticipant;
import com.crm.sales.entity.CrmOpportunityStage;
import com.crm.sales.mapper.CrmOpportunityMapper;
import com.crm.sales.mapper.CrmOpportunityParticipantMapper;
import com.crm.sales.service.ICrmOpportunityService;
import com.crm.sales.service.ICrmOpportunityStageService;
import com.crm.system.entity.SysUser;
import com.crm.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrmOpportunityServiceImpl extends ServiceImpl<CrmOpportunityMapper, CrmOpportunity>
        implements ICrmOpportunityService {

    private final CrmOpportunityMapper opportunityMapper;
    private final CrmOpportunityParticipantMapper participantMapper;
    private final ICrmCustomerService customerService;
    private final ISysUserService userService;
    private final ICrmOpportunityStageService stageService;

    @Override
    public IPage<CrmOpportunity> selectPageWithCondition(Page<CrmOpportunity> page, Long stageId, Long customerId,
                                                          Long ownerId, String keywords, Boolean isExpired) {
        return opportunityMapper.selectPageWithCondition(page, keywords, stageId, customerId, ownerId);
    }

    @Override
    public CrmOpportunity selectWithDetail(Long id) {
        CrmOpportunity opp = getById(id);
        if (opp == null) throw new BizException(404, "商机不存在");
        List<CrmOpportunityParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<CrmOpportunityParticipant>()
                        .eq(CrmOpportunityParticipant::getOpportunityId, id));
        opp.setParticipantIds(participants.stream().map(CrmOpportunityParticipant::getUserId).collect(Collectors.toList()));
        return opp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStage(Long id, Long stageId, String remark) {
        CrmOpportunity opp = getById(id);
        if (opp == null) throw new BizException(404, "商机不存在");
        return lambdaUpdate().eq(CrmOpportunity::getId, id)
                .set(CrmOpportunity::getStageId, stageId)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean win(Long id, Map<String, Object> params) {
        CrmOpportunity opp = getById(id);
        if (opp == null) throw new BizException(404, "商机不存在");
        lambdaUpdate().eq(CrmOpportunity::getId, id)
                .set(CrmOpportunity::getFinalAmount, params.get("finalAmount"))
                .set(CrmOpportunity::getContractId, params.get("contractId"))
                .update();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lose(Long id, Map<String, Object> params) {
        CrmOpportunity opp = getById(id);
        if (opp == null) throw new BizException(404, "商机不存在");
        lambdaUpdate().eq(CrmOpportunity::getId, id)
                .set(CrmOpportunity::getLoseReason, params.get("loseReason"))
                .set(CrmOpportunity::getLoseReasonDetail, params.get("loseReasonDetail"))
                .set(CrmOpportunity::getCompetitor, params.get("competitor"))
                .update();
        return true;
    }

    @Override
    public List<Map<String, Object>> pipeline() {
        List<CrmOpportunity> all = lambdaQuery().list();
        if (all.isEmpty()) return Collections.emptyList();

        // batch load display names
        Set<Long> customerIds = all.stream().map(CrmOpportunity::getCustomerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> ownerIds = all.stream().map(CrmOpportunity::getOwnerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> stageIds = all.stream().map(CrmOpportunity::getStageId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> customerNameMap = customerIds.isEmpty() ? Collections.emptyMap() :
                customerService.lambdaQuery().in(CrmCustomer::getId, customerIds).list()
                        .stream().collect(Collectors.toMap(CrmCustomer::getId, CrmCustomer::getName));

        Map<Long, CrmOpportunityStage> stageMap = stageIds.isEmpty() ? Collections.emptyMap() :
                stageService.lambdaQuery().in(CrmOpportunityStage::getId, stageIds).list()
                        .stream().collect(Collectors.toMap(CrmOpportunityStage::getId, Function.identity()));

        Map<Long, String> ownerNameMap = ownerIds.isEmpty() ? Collections.emptyMap() :
                userService.lambdaQuery().in(SysUser::getId, ownerIds).list()
                        .stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));

        for (CrmOpportunity opp : all) {
            opp.setCustomerName(customerNameMap.getOrDefault(opp.getCustomerId(), ""));
            CrmOpportunityStage s = stageMap.get(opp.getStageId());
            opp.setStageName(s != null ? s.getName() : "");
            opp.setOwnerName(ownerNameMap.getOrDefault(opp.getOwnerId(), ""));
            opp.setProbability(s != null ? s.getProbability() : null);
        }

        // sort stages by sortOrder, group opportunities
        List<Long> sortedStageIds = stageMap.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().getSortOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Map<Long, List<CrmOpportunity>> grouped = all.stream()
                .filter(o -> o.getStageId() != null)
                .collect(Collectors.groupingBy(CrmOpportunity::getStageId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long stageId : sortedStageIds) {
            List<CrmOpportunity> opps = grouped.getOrDefault(stageId, Collections.emptyList());
            CrmOpportunityStage stageDef = stageMap.get(stageId);
            Map<String, Object> stage = new HashMap<>();
            stage.put("stageId", stageId);
            stage.put("stageName", stageDef != null ? stageDef.getName() : "");
            stage.put("category", stageDef != null ? stageDef.getCategory() : "open");
            stage.put("opportunities", opps);
            stage.put("count", opps.size());
            stage.put("totalAmount", opps.stream()
                    .mapToDouble(o -> o.getExpectedAmount() != null ? o.getExpectedAmount().doubleValue() : 0.0)
                    .sum());
            result.add(stage);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addParticipants(Long id, List<Long> userIds) {
        for (Long userId : userIds) {
            CrmOpportunityParticipant p = new CrmOpportunityParticipant();
            p.setOpportunityId(id);
            p.setUserId(userId);
            try { participantMapper.insert(p); } catch (Exception ignored) {}
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeParticipants(Long id, List<Long> userIds) {
        for (Long userId : userIds) {
            participantMapper.delete(new LambdaQueryWrapper<CrmOpportunityParticipant>()
                    .eq(CrmOpportunityParticipant::getOpportunityId, id)
                    .eq(CrmOpportunityParticipant::getUserId, userId));
        }
        return true;
    }
}
