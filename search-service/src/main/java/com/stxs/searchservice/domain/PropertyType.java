package com.stxs.searchservice.domain;

public enum PropertyType {
    HOUSE("house"), APARTMENT("apartment"), CONDO("condo"), HOTEL("hotel");
    private final String propertyType;
    PropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getPropertyType() {
        return propertyType;
    }
}
