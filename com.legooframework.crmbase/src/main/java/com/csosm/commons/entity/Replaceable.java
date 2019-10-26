package com.csosm.commons.entity;

import com.csosm.module.base.entity.StoreEntity;

import java.util.Map;

public interface Replaceable {

    Map<String, String> toSmsMap(StoreEntity store);

}
