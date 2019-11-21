package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;

public class RechargeBalanceAgg {

    private List<RechargeBalanceEntity> rawBalances;
    private final List<RechargeBalanceEntity> deductionBalances = Lists.newArrayList();
    private final List<DeductionDetailEntity> chargeDetails = Lists.newArrayList();
    private final List<SendMsg4DeductionEntity> deductionSmses = Lists.newArrayList();

    RechargeBalanceAgg(List<RechargeBalanceEntity> rawBalances) {
        this.rawBalances = rawBalances;
    }

    public void deduction(MsgTransportBatchEntity transportBatch, List<SendMsg4DeductionEntity> deduction_smses) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(deduction_smses), "待扣费的短信列表非法...为空.");
        ArrayDeque<SendMsg4DeductionEntity> deductionQueues = Queues.newArrayDeque(deduction_smses);
        SendMsg4DeductionEntity cursor = null;
        for (RechargeBalanceEntity balance : rawBalances) {
            if (deductionQueues.isEmpty()) break;
            if (!balance.hasBlance()) continue;
            List<SendMsg4DeductionEntity> _temp_de_list = Lists.newArrayList();
            DeductionDetailEntity _chargeDetail = new DeductionDetailEntity(transportBatch, balance);
            for (; ; ) {
                if (deductionQueues.isEmpty()) break;
                cursor = deductionQueues.peekFirst();
                boolean flag = balance.deduction(cursor.getSmsCount());
                if (flag) {
                    cursor.deductionOK();
                    _temp_de_list.add(cursor);
                    deductionQueues.removeFirst();
                }
                if (!flag) break;
            }
            if (CollectionUtils.isNotEmpty(_temp_de_list)) {
                long deductionNum = _temp_de_list.stream().mapToInt(SendMsg4DeductionEntity::getSmsCount).sum();
                _chargeDetail.setDeductionNum(deductionNum);
                deductionSmses.addAll(_temp_de_list);
                chargeDetails.add(_chargeDetail);
                deductionBalances.add(balance);
            }
        }
        while (!deductionQueues.isEmpty()) {
            cursor = deductionQueues.pop();
            cursor.deductionFail();
            deductionSmses.add(cursor);
        }
    }

    public List<SendMsg4DeductionEntity> getDeductionSmses() {
        return deductionSmses;
    }

    public Optional<List<RechargeBalanceEntity>> getDeductionBalances() {
        return Optional.ofNullable(CollectionUtils.isEmpty(deductionBalances) ? null : deductionBalances);
    }

    public Optional<List<DeductionDetailEntity>> getChargeDetails() {
        return Optional.ofNullable(CollectionUtils.isEmpty(chargeDetails) ? null : chargeDetails);
    }

    @Override
    public String toString() {
        int ok = (int) deductionSmses.stream().filter(SendMsg4DeductionEntity::isDeductionOK).count();
        int fail = (int) deductionSmses.stream().filter(x -> !x.isDeductionOK()).count();
        return MoreObjects.toStringHelper(this)
                .add("chargeDetails", chargeDetails)
                .add("deductionSmses' size ", deductionSmses.size())
                .add("deductionOk' size ", ok)
                .add("deductionFail' size ", fail)
                .toString();
    }
}
