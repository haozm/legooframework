package com.legooframework.model.base.entity;

public interface Sorting {

    int getIndex();

    default int getDefIndex() {
        return 0;
    }
}
