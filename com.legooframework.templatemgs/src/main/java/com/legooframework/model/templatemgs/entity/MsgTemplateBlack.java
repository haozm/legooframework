package com.legooframework.model.templatemgs.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MsgTemplateBlack {

    private Integer companyId, storeId;
    private List<String> blackList;

    MsgTemplateBlack(ResultSet res) {
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            String _blackList = ResultSetUtil.getOptString(res, "blackList", null);
            if (Strings.isNullOrEmpty(_blackList)) {
                this.blackList = Lists.newArrayList();
            } else {
                this.blackList = Lists.newArrayList(StringUtils.split(_blackList, ','));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore MsgTemplateBlack has SQLException", e);
        }
    }

    Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", this.companyId);
        params.put("storeId", this.storeId);
        if (CollectionUtils.isEmpty(this.blackList)) {
            params.put("blackList", null);
        } else {
            params.put("blackList", StringUtils.join(this.blackList, ','));
        }
        return params;
    }

    MsgTemplateBlack(LoginContext user, MsgTemplateEntity template) {
        this.companyId = user.getTenantId().intValue();
        this.storeId = user.getStoreId();
        this.blackList = Lists.newArrayList(template.getId());
    }

    Optional<MsgTemplateBlack> blacked(MsgTemplateEntity template) {
        if (this.blackList.contains(template.getId())) return Optional.empty();
        this.blackList.add(template.getId());
        return Optional.of(this);
    }

    Optional<MsgTemplateBlack> unblacked(MsgTemplateEntity template) {
        if (!this.blackList.contains(template.getId())) return Optional.empty();
        this.blackList.remove(template.getId());
        return Optional.of(this);
    }

    Integer getCompanyId() {
        return companyId;
    }

    Integer getStoreId() {
        return storeId;
    }

    List<String> getBlackList() {
        return blackList;
    }

    String getBlackList4Save() {
        return CollectionUtils.isEmpty(this.blackList) ? null : StringUtils.join(this.blackList, ',');
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("blackList", blackList)
                .toString();
    }
}
