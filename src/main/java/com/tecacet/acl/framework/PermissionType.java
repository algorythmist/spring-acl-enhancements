package com.tecacet.acl.framework;

public enum PermissionType {
    READ(1), WRITE(2);

    private final int type;

    PermissionType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
