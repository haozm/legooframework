package com.legooframework.model.core.base.entity;

public interface Sorting {

    int getIndex();

    default int getDefIndex() {
        return 0;
    }
}
