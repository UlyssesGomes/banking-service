package com.mybanking.agencies.enums;

public enum MetersEnums {
    AGENCY_NOT_ADDED_COUNT("agency_not_added_count"),
    AGENCY_ADDED_COUNT("agency_added_count"),
    CACHE_HIT("cache_hit"),
    CACHE_MISSED("cache_missed");

    private final String value;

    MetersEnums(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
