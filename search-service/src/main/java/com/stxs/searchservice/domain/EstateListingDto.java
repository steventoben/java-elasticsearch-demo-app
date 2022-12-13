package com.stxs.searchservice.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@Data
public class EstateListingDto {
    //@JsonProperty("id")
    //private String id;
    @JsonProperty("listing_name")
    private String listingName;
    @JsonProperty("price")
    private BigDecimal price;
    @JsonProperty("image_data")
    private ImageData imageData;
    //@JsonProperty("location")
    private GeoPoint location;
    @JsonProperty("property_type")
    private PropertyTypes propertyType;
}
