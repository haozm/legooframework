package com.csosm.module.webchat.event;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.event.BusEvent;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.webchat.entity.DevicesEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

public class StoreBildDeviceEvent implements BusEvent {

    private final DevicesEntity device;
    private final StoreEntity store;
    private final LoginUserContext userContext;

    public StoreBildDeviceEvent(DevicesEntity device, StoreEntity store, LoginUserContext userContext) {
        this.device = device;
        this.store = store;
        this.userContext = userContext;
    }

    @Override
    public void setLoginUser(LoginUserContext user) {

    }

    public DevicesEntity getDevice() {
        return device;
    }

    public StoreEntity getStore() {
        return store;
    }

    public Optional<LoginUserContext> getUserContext() {
        return Optional.fromNullable(userContext);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("device", device)
                .add("store", store)
                .toString();
    }
}
