package com.legooframework.model.imchat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MemoryContactEntity extends BaseEntity<String> {

    private final List<String[]> contacts;
    private final ReadWriteLock lock;


    MemoryContactEntity() {
        super("MemoryContactEntity");
        this.contacts = Lists.newArrayList();
        this.lock = new ReentrantReadWriteLock();
    }

    void refresh(List<Map<String, Object>> mapList) {
        List<String[]> temp = Lists.newArrayList();
        mapList.forEach(x -> {
            String[] args = new String[]{
                    MapUtils.getString(x, "storeId"),
                    MapUtils.getString(x, "owner"),
                    MapUtils.getString(x, "wechatId"),
                    MapUtils.getString(x, "type"),
                    MapUtils.getString(x, "imie"),
                    MapUtils.getString(x, "nickName", null),
                    MapUtils.getString(x, "iconUrl", null),
                    MapUtils.getString(x, "ownerId", null)};
            temp.add(args);
        });
        try {
            lock.writeLock().lock();
            this.contacts.clear();
            this.contacts.addAll(temp);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Long> getStoreIdByImie(String imie) {
        if(StringUtils.isEmpty(imie)) return Optional.empty();
        try {
            lock.readLock().lock();
            Optional<String[]> optional = contacts.stream().filter(x -> StringUtils.equals(x[4], imie)).findFirst();
            return optional.map(strings -> Long.valueOf(strings[0]));
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<String[]> getContactByOwnerId(String ownerId) {
        if(CollectionUtils.isEmpty(contacts)) return Optional.empty();
        try {
            lock.readLock().lock();
            return contacts.stream().filter(x -> StringUtils.equals(x[7], ownerId)).findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<String[]> getContact(String username) {
        if(CollectionUtils.isEmpty(contacts)) return Optional.empty();
        try {
            lock.readLock().lock();
            return contacts.stream().filter(x -> StringUtils.equals(x[2], username)).findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<String> getToDeviceId(Long storeId, String accountNo) {
        Optional<String[]> exits = getContactByUser(storeId, accountNo);
        if(!exits.isPresent()) return Optional.empty();
        return Optional.ofNullable(exits.get()[4]);
    }

    public Optional<String> getToDeviceIdByWx(Long storeId, String weixiId) {
        try {
            lock.readLock().lock();
            Optional<String[]> args = contacts.stream().filter(x -> StringUtils.equals(x[0], storeId.toString())
                    && StringUtils.equals(weixiId, x[2])).findFirst();
            return args.map(strs -> strs[4]);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<String> getDeviceIdByStoreId(Long storeId) {
        if(CollectionUtils.isEmpty(contacts)) return Optional.empty();
        try {
            lock.readLock().lock();
            Optional<String[]> args = contacts.stream().filter(x -> StringUtils.equals(x[0], storeId.toString())).findFirst();
            if(args.isPresent()) return Optional.of(args.get()[4]);
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<String[]> getContactByUser(Long storeId, String accountNo) {
        if(CollectionUtils.isEmpty(contacts)) return Optional.empty();
        try {
            lock.readLock().lock();
            return contacts.stream().filter(x -> StringUtils.equals(x[0], storeId.toString()) &&
                    StringUtils.equals(x[1], accountNo)).findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<String[]> getContactByWx(Long storeId, String weixinId) {
        if(CollectionUtils.isEmpty(contacts)) return Optional.empty();
        try {
            lock.readLock().lock();
            return contacts.stream().filter(x -> StringUtils.equals(x[0], storeId.toString()) &&
                    StringUtils.equals(x[2], weixinId)).findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<String[]> getContact(Long storeId) {
        if(CollectionUtils.isEmpty(contacts)) return Optional.empty();
        try {
            lock.readLock().lock();
            return contacts.stream().filter(x -> StringUtils.equals(x[0], storeId.toString())).findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("contacts", contacts.size())
                .toString();
    }
}
