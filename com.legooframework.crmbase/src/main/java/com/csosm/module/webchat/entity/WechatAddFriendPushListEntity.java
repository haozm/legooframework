package com.csosm.module.webchat.entity;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.*;

public class WechatAddFriendPushListEntity extends BaseEntity<Long> {

    private final Integer userSize, storeId, companyId;
    private int status;
    private DateTime receiveDate;
    private Set<PushInfo> pushInfos;
    private final String uuid;

    WechatAddFriendPushListEntity(Integer storeId, Integer companyId, String[] datas) {
        super(-1L);
        this.userSize = datas.length;
        this.storeId = storeId;
        this.companyId = companyId;
        this.status = 0;
        this.pushInfos = Sets.newHashSet();
        this.uuid = UUID.randomUUID().toString();
        for (String data : datas) {
            String[] args = StringUtils.split(data, ':');
            this.pushInfos.add(new PushInfo(Long.valueOf(args[0]), Integer.valueOf(args[1])));
        }
    }

    WechatAddFriendPushListEntity(Long id, Date createTime, Integer userSize, Integer storeId,
                                  Integer companyId, int status, DateTime receiveDate,
                                  Map<String, String> pushInfos, String uuid) {
        super(id, -1, createTime);
        this.userSize = userSize;
        this.storeId = storeId;
        this.companyId = companyId;
        this.status = status;
        this.uuid = uuid;
        this.receiveDate = receiveDate;
        this.pushInfos = Sets.newHashSet();
        for (Map.Entry<String, String> entry : pushInfos.entrySet())
            this.pushInfos.add(new PushInfo(Long.valueOf(entry.getKey()), Integer.valueOf(entry.getValue())));
    }

    public Integer getUserSize() {
        return userSize;
    }

    public Set<PushInfo> getPushInfos() {
        return pushInfos;
    }

    public Collection<Long> getAddIds() {
        List<Long> ids = Lists.newArrayList();
        for (PushInfo $it : this.pushInfos) ids.add($it.getAddId());
        return ids;
    }

    public String getUuid() {
        return uuid;
    }

    public Collection<Integer> getMemberIds() {
        List<Integer> ids = Lists.newArrayList();
        for (PushInfo $it : this.pushInfos) ids.add($it.getMemberId());
        return ids;
    }

    public String getPushInfosToString() {
        return Joiner.on(',').join(pushInfos);
    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("storeId", storeId);
        data.put("companyId", companyId);
        data.put("status", status);
        data.put("createTime", getCreateTime());
        data.put("receiveDate", receiveDate);
        data.put("pushInfos", Joiner.on(',').join(pushInfos));
        return data;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public int getStatus() {
        return status;
    }

    public DateTime getReceiveDate() {
        return receiveDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WechatAddFriendPushListEntity that = (WechatAddFriendPushListEntity) o;
        return status == that.status &&
                Objects.equal(userSize, that.userSize) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(receiveDate, that.receiveDate) &&
                SetUtils.isEqualSet(pushInfos, that.pushInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), userSize, storeId, companyId, status, receiveDate, pushInfos);
    }

    public class PushInfo {
        private final Long addId;
        private final Integer memberId;

        public PushInfo(Long addId, Integer memberId) {
            this.addId = addId;
            this.memberId = memberId;
        }

        public Long getAddId() {
            return addId;
        }

        public Integer getMemberId() {
            return memberId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PushInfo pushInfo = (PushInfo) o;
            return Objects.equal(addId, pushInfo.addId) &&
                    Objects.equal(memberId, pushInfo.memberId);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(addId, memberId);
        }

        @Override
        public String toString() {
            return String.format("%s:%s", addId, memberId);
        }
    }
}
