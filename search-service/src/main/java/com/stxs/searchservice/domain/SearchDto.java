package com.stxs.searchservice.domain;

//import org.springframework.data.elasticsearch.core.Range;

public class SearchDto {
    private String propertyType;//multi
    private Range priceRange;
    private Double distance;
    private Integer beds;
}
class Range {
    private Integer from;
    private Integer to;
}