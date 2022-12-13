package com.stxs.searchservice.domain;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "myindex")
public class PropertyListing {
    private Long pid;
    private String name;
    @Field(name = "kind", type = FieldType.Keyword)
    private String kind;
    private PropertyType propertyType;
}
