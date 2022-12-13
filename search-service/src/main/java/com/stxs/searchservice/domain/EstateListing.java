package com.stxs.searchservice.domain;

import lombok.Builder;
import lombok.Data;
import org.elasticsearch.common.UUIDs;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Document(indexName = "mylistings")
public class EstateListing {
    @Id
    @Field(name = "id")
    private String id;
    @Field(name = "listing_name", type = FieldType.Text)
    private String listingName;
    @Field(name = "price")
    private BigDecimal price;
    @Field(name = "image_data", type = FieldType.Object)
    private ImageData imageData;
    @GeoPointField
    private GeoPoint location;
    @Field(name = "property_type", type = FieldType.Keyword)
    private PropertyTypes propertyType;
    //private String propertyType;
    public EstateListing() {
        this.id = UUIDs.randomBase64UUID();
    }
}

