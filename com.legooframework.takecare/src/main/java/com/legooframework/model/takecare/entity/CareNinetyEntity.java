package com.legooframework.model.takecare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.covariant.entity.SendChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CareNinetyEntity extends BaseEntity<Integer> {

    private SendChannel followUpWay;
    // 计划状态：1 - 未开始，2 - 已完成，3 - 已审核，4 - 进行中  5 取消
    private int planState;
    // 计划节点
    private Integer taskIndex;
    private Integer storeId, memberId, operateType;
    // 会员反馈,    // 导购总结
    private String memberFeedback, shoppingGuideSummary, followUpContent;
    // 是否跨店1 - 是， 0 - 否
    private boolean acrossStore;
    // 计划时间  、 更近之间、船舰时间、更新时间
    private LocalDateTime planPerformTime, followUpTime, createTime, updateTime;
    private final List<Integer> taskIds;
    private List<CareNinetyTaskEntity> details;

    void setDetails(List<CareNinetyTaskEntity> details) {
        if (CollectionUtils.isEmpty(this.details)) this.details = details;
    }

    CareNinetyEntity(Integer id, ResultSet resultSet) throws RuntimeException {
        super(id);
        try {
            int followUpWay = resultSet.getInt("followUpWay");
            if (followUpWay == 1) {
                this.followUpWay = SendChannel.SMS;
            } else if (followUpWay == 2) {
                this.followUpWay = SendChannel.CALLPHONE;
            } else if (followUpWay == 3) {
                this.followUpWay = SendChannel.WECHAT;
            } else {
                this.followUpWay = SendChannel.OFFLINE;
            }
            this.memberId = resultSet.getInt("member_id");
            this.storeId = resultSet.getInt("store_id");
            this.operateType = resultSet.getInt("operateType");
            this.planState = resultSet.getInt("planState");
            this.taskIndex = resultSet.getInt("planNode");
            this.followUpContent = resultSet.getString("followUpContent");
            this.memberFeedback = resultSet.getString("memberFeedback");
            this.shoppingGuideSummary = resultSet.getString("shoppingGuideSummary");
            this.acrossStore = resultSet.getInt("acrossStore") == 1;
            this.createTime = resultSet.getTimestamp("createTime") == null ? LocalDateTime.now()
                    : LocalDateTime.fromDateFields(resultSet.getTimestamp("createTime"));
            this.updateTime = resultSet.getTimestamp("updateTime") == null ? LocalDateTime.now()
                    : LocalDateTime.fromDateFields(resultSet.getTimestamp("updateTime"));
            this.followUpTime = resultSet.getTimestamp("followUpTime") == null ? LocalDateTime.now()
                    : LocalDateTime.fromDateFields(resultSet.getTimestamp("followUpTime"));
            this.planPerformTime = resultSet.getTimestamp("planPerformTime") == null ? LocalDateTime.now()
                    : LocalDateTime.fromDateFields(resultSet.getTimestamp("planPerformTime"));
            String taskIds = resultSet.getString("taskIds");
            if (Strings.isNullOrEmpty(taskIds)) {
                this.taskIds = null;
            } else {
                this.taskIds = Stream.of(StringUtils.split(taskIds, ',')).mapToInt(Integer::parseInt).boxed()
                        .collect(Collectors.toList());
            }
        } catch (SQLException e) {
            throw new RuntimeException("还原对象 CareNinetyEntity 发生异常", e);
        }
    }

    CareNinetyTaskEntity loadByTaskId(Integer taskId) {
        Optional<CareNinetyTaskEntity> optional = this.details.stream().filter(x -> x.getId().equals(taskId)).findFirst();
        Preconditions.checkState(optional.isPresent(), "不存在 taskId=%s 对应的子任务", taskId);
        return optional.get();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("followUpWay", followUpWay)
                .add("planState", planState)
                .add("planNodeId", taskIndex)
                .add("storeId", storeId)
                .add("memberId", memberId)
                .add("shoppingGuideSummary", shoppingGuideSummary)
                .add("acrossStore", acrossStore)
                .add("planPerformTime", planPerformTime)
                .add("followUpTime", followUpTime)
                .add("updateTime", updateTime)
                .add("taskIds", taskIds)
                .toString();
    }

}
