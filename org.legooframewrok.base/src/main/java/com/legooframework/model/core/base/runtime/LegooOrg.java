package com.legooframework.model.core.base.runtime;

import java.util.Collection;
import java.util.Optional;

public interface LegooOrg {

    Long getId();

    String getName();

    Optional<Collection<String>> getDeviceIds();

    boolean isCompany();

    boolean isStore();
}
