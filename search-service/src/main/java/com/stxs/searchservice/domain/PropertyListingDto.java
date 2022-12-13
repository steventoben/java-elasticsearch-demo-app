package com.stxs.searchservice.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PropertyListingDto {
    private Long pid;
    private String name;
    private String kind;
    @JsonProperty("propertyType")
    private PropertyType propertyType;
}
