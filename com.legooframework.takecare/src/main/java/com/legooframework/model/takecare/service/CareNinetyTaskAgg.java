package com.legooframework.model.takecare.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.covariant.entity.SendChannel;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.entity.WxUserEntity;
import com.legooframework.model.covariant.service.MemberAgg;
import com.legooframework.model.takecare.entity.CareHisRecordEntity;
import com.legooframework.model.takecare.entity.CareNinetyTaskEntity;
import com.legooframework.model.takecare.entity.CareRecordEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CareNinetyTaskAgg {

    private CareNinetyTaskEntity task;
    private String errorMsg;
    private MemberAgg memberAgg;
    private String srcMsgTemp;
    private String targetContent;
    private Collection<SendChannel> channels;
    private String[] imgs;

    void setImgs(String[] imgs) {
        this.imgs = imgs;
    }

    public boolean isOnlyWx() {
        return this.channels.size() == 1 && this.channels.contains(SendChannel.WECHAT);
    }

    List<CareRecordEntity> getCareRecord(UserAuthorEntity user) {
        List<CareRecordEntity> logs = Lists.newArrayList();
        if (memberAgg == null) {
            logs.add(CareRecordEntity.errorNinetyCare4Member(this, user));
            return logs;
        }
        for (SendChannel ch : channels) {
            if (SendChannel.SMS == ch) {
                if (Strings.isNullOrEmpty(targetContent)) {
                    logs.add(CareRecordEntity.errorNinetyCare4Member(this, user));
                } else {
                    logs.add(CareRecordEntity.sendSmsNinetyCare4Member(this, user));
                }
            } else if (SendChannel.WECHAT == ch && memberAgg.getWxUser().isPresent()) {
                if (Strings.isNullOrEmpty(targetContent)) {
                    logs.add(CareRecordEntity.errorNinetyCare4Member(this, user));
                } else {
                    logs.add(CareRecordEntity.wxNinetyCare4Member(this, user, imgs));
                }
            } else if (SendChannel.CANCEL == ch) {
                logs.add(CareRecordEntity.cancelNinetyCare4Member(this, user));
            } else {
                logs.add(CareRecordEntity.manualNinetyCare4Member(this, user));
            }
        }
        return logs;
    }

    List<CareHisRecordEntity> getCareHisRecord(UserAuthorEntity user) {
        List<CareHisRecordEntity> logs = Lists.newArrayList();
        if (memberAgg == null) {
            logs.add(CareHisRecordEntity.errorCareNinety4Member(this, user));
            return logs;
        }
        for (SendChannel ch : channels) {
            if (SendChannel.SMS == ch) {
                if (Strings.isNullOrEmpty(targetContent)) {
                    logs.add(CareHisRecordEntity.errorCareNinety4Member(this, user));
                } else {
                    logs.add(CareHisRecordEntity.smsCareNinety4Member(this, user));
                }
            } else if (SendChannel.WECHAT == ch && memberAgg.getWxUser().isPresent()) {
                if (Strings.isNullOrEmpty(targetContent)) {
                    logs.add(CareHisRecordEntity.errorCareNinety4Member(this, user));
                } else {
                    logs.add(CareHisRecordEntity.wxBirthdayCare4Member(this, user));
                }
            } else if (SendChannel.CANCEL == ch) {
                logs.add(CareHisRecordEntity.cancelCareNinety4Member(this, user));
            } else {
                logs.add(CareHisRecordEntity.offlineCareNinety4Member(this, user));
            }
        }
        return logs;
    }

    void setChannels(Collection<SendChannel> channels) {
        this.channels = channels;
    }

    CareNinetyTaskAgg(CareNinetyTaskEntity task) {
        this.task = task;
    }

    boolean hasError() {
        return !Strings.isNullOrEmpty(errorMsg);
    }

    void setMemberAgg(MemberAgg memberAgg) {
        this.memberAgg = memberAgg;
    }

    void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public CareNinetyTaskEntity getTask() {
        return task;
    }

    Integer getTaskId() {
        return this.task.getId();
    }

    public Optional<WxUserEntity> getWxUser() {
        if (null == memberAgg) return Optional.empty();
        return this.memberAgg.getWxUser();
    }

    void setTask(CareNinetyTaskEntity task) {
        if (this.task.getId().equals(task.getId()))
            this.task = task;
    }

    public Optional<Integer> getMemberId() {
        return Optional.ofNullable(memberAgg == null ? null : memberAgg.getMember().getId());
    }

    public Optional<String> getPhone() {
        return Optional.ofNullable(memberAgg == null ? null : memberAgg.getMember().getPhone());
    }

    public Optional<String> getMemberName() {
        return Optional.ofNullable(memberAgg == null ? null : memberAgg.getMember().getName());
    }

    String getSrcMsgTemp() {
        return srcMsgTemp;
    }

    void setSrcMsgTemp(String srcMsgTemp) {
        this.srcMsgTemp = srcMsgTemp;
    }

    public String getTargetContent() {
        return targetContent;
    }

    void setTargetContent(String targetContent) {
        this.targetContent = targetContent;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> params = task.toViewMap();
        params.put("errorMsg", errorMsg);
        params.put("code", errorMsg == null ? "0000" : "9999");
        if (memberAgg != null) {
            params.putAll(memberAgg.toViewMap());
        }
        params.put("srcContent", srcMsgTemp);
        params.put("targetContent", targetContent);
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("task", task)
                .add("errorMsg", errorMsg)
                .add("memberAgg", memberAgg == null ? null : memberAgg.getMember().getName())
                .add("srcMsgTemp", srcMsgTemp)
                .add("targetContent", targetContent)
                .toString();
    }
}
