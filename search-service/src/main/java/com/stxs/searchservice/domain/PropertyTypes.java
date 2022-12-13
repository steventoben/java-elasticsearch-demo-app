package com.stxs.searchservice.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PropertyTypes {
    @JsonProperty("house")
    HOUSE,
    @JsonProperty("apartment")
    APARTMENT,
    @JsonProperty("condo")
    CONDO,
    @JsonProperty("hotel")
    HOTEL
}
