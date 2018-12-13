package com.legooframework.model.customer.entity;

import com.google.common.collect.Sets;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.dict.dto.KvDictDto;
import com.legooframework.model.organization.entity.StoreEntity;
import com.legooframework.model.wechat.entity.WechatAccountEntity;
import org.apache.commons.collections4.MapUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class Customers {

    /**
     * 将微信账号列表转换为顾客列表
     *
     * @param store
     * @param wechatAccounts
     * @return
     */
    public static Collection<CustomerEntity> convertWechatAccounts(StoreEntity store,
                                                                   Collection<WechatAccountEntity> wechatAccounts) {
        Objects.requireNonNull(wechatAccounts, "入参wecharAccounts不能为null");
        Set<CustomerEntity> customers = Sets.newHashSet();
        if (wechatAccounts.isEmpty())
            return customers;
        wechatAccounts.forEach(x -> {
            CustomerId cid = new CustomerId(x, store);
            String name = x.getNickName();
            String remark = x.getConRemark();
            String iconUrl = x.getIconUrl();
            KvDictDto sex = new KvDictDto("9", "保密", 0, "SEX");
            CustomerEntity customer = new CustomerEntity(cid, name, remark, iconUrl, sex, LoginContextHolder.get());
            customers.add(customer);
        });
        return customers;
    }

    public static Collection<CustomerEntity> convertWechatAccountMaps(List<Map<String, Object>> maps) {
        Set<CustomerEntity> customers = Sets.newHashSet();
        if (maps.isEmpty())
            return customers;
        maps.stream().forEach(x -> {
            Long companyId = MapUtils.getLong(x, "company_id");
            Long storeId = MapUtils.getLong(x, "store_id");
            Long id = MapUtils.getLong(x, "id");
            Channel channel = Channel.TYPE_WEIXIN;
            String name = MapUtils.getString(x, "nickname");
            String remark = MapUtils.getString(x, "conremark");
            String iconUrl = MapUtils.getString(x, "iconUrl");
            KvDictDto sex = new KvDictDto("9", "保密", 0, "SEX");
            CustomerId cid = new CustomerId(id, channel, storeId);
            CustomerEntity customer = new CustomerEntity(cid, name, remark, iconUrl, sex, companyId, LoginContextHolder.get());
            customers.add(customer);
        });
        return customers;
    }
}
