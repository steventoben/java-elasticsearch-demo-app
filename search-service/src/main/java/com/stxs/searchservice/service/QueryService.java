package com.stxs.searchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.GeoDistanceType;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.LatLonGeoLocation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.json.JsonData;
import com.stxs.searchservice.domain.EstateListing;
import com.stxs.searchservice.domain.PropertyListing;
import com.stxs.searchservice.domain.PropertyTypes;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class QueryService {
    WebClient webClient = WebClient
            .create("localhost:9200");
            //.post().bodyValue().retrieve().bodyToMono()

    @Autowired
    ElasticsearchClient client;

    public SearchResponse<EstateListing> searchPriceRange(BigDecimal minPrice, BigDecimal maxPrice) throws IOException {
        System.out.println("service");
        System.out.println(minPrice.toString());
        System.out.println(maxPrice.toString());
        RangeQuery rangeQuery = RangeQuery.of(r -> r
                .field("price")
                .gte(JsonData.of(minPrice.toString()))
                .lte(JsonData.of(maxPrice.toString()))
        );
        SearchResponse<EstateListing> searchResponse = client.search(
                SearchRequest.of(
                        s -> s
                                .index("listingsx")
                                .query(q ->
                                        q.range(
                                                r -> r
                                                        .field("price")
                                                        .gte(JsonData.of(minPrice.toString()))
                                                        .lte(JsonData.of(maxPrice.toString()))
                                        ))
                ),
                EstateListing.class
        );
        TotalHits totalHits = searchResponse.hits().total();
        List<Hit<EstateListing>> hits = searchResponse.hits().hits();
        for (Hit<EstateListing> hit: hits) {
            System.out.println(hit.source());
        }
        return searchResponse;
    }

    public SearchResponse<EstateListing> searchGeoRange(double lat, double lon, String distance) throws IOException {
        System.out.println(lat);
        System.out.println(lon);
        System.out.println(distance);
        GeoLocation geoLocation = new GeoLocation.Builder()
                .latlon(c ->
                        c.lat(lat).lon(lon)
                ).build();
        System.out.println(geoLocation.toString());
        GeoDistanceQuery geoDistanceQuery = new GeoDistanceQuery.Builder()
                .field("location")
                .distance(distance)
                .location(g -> g.latlon(LatLonGeoLocation.of(
                        l -> l.lat(lat).lon(lon)
                )))
                .distanceType(GeoDistanceType.Arc)
                .build();
        /*SearchResponse<EstateListing> searchResponse = client.search(
                SearchRequest.of(
                        s -> s.index("listingsx")
                                .query(q ->
                                        q.geoDistance(
                                                builder -> builder
                                                        .field("location")
                                                        .location(
                                                                g -> g.coords(List.of(lat, lon))
                                                        ).distance(
                                                                distance
                                                        )
                                        ))
                ),
                EstateListing.class
        );*/
        System.out.println(geoDistanceQuery.toString());
        GeoDistanceQueryBuilder geoDistanceQueryBuilder = QueryBuilders
                .geoDistanceQuery("geoPoint")
                .point(lat, lon)
                .distance(distance, DistanceUnit.MILES);
        System.out.println(geoDistanceQueryBuilder.toString());
        Reader stringReader = new StringReader("{}");
        /*XContentBuilder locationBuilder = XContentFactory.jsonBuilder();
        locationBuilder.startArray();
        {
            locationBuilder.array("origin", lon, lat);
        }*/
        Reader locationJson = new StringReader(String.format("[%s, %s]", lon, lat));
        JsonData locJson = JsonData.from(locationJson);
        JsonData distanceJson = JsonData.fromJson(distance);
        JsonData distanceJson2 = JsonData.of(distance);
        System.out.println(distanceJson);
        System.out.println(distanceJson2);
        DistanceFeatureQuery distanceFeatureQuery = DistanceFeatureQuery.of(builder -> builder
                .field("location")
                .pivot(distanceJson2)
                .origin(locJson)
        );
        System.out.println(locationJson);
        System.out.println(locJson);
        System.out.println(distance);
        System.out.println(distanceFeatureQuery);
        System.out.println("searching....");
        SearchResponse<EstateListing> searchResponse = client.search(SearchRequest.of(
                s -> s
                        .index("mylistings")
                        .query(q -> q
                                .bool(b -> b
                                        .must(m -> m.matchAll(
                                                a -> a.withJson(stringReader)
                                        ))
                                        .should(s0 -> s0
                                                        /*.geoDistance(d -> d
                                                                .field("location")
                                                                .location(geoLocation)
                                                                .distance(distance)
                                                        )*/
                                                /*.distanceFeature(d -> d
                                                        .field("location")
                                                        .pivot(JsonData.fromJson(distance))
                                                        .origin(JsonData.of(geoLocation))
                                                )*/
                                                .distanceFeature(distanceFeatureQuery)
                                        )
                                )
                        )
        ), EstateListing.class);
        System.out.println(searchResponse.toString());
        TotalHits totalHits = searchResponse.hits().total();
        List<Hit<EstateListing>> hits = searchResponse.hits().hits();
        for (Hit<EstateListing> hit: hits) {
            EstateListing listing = hit.source();
            System.out.println(listing);
        }
        return searchResponse;
    }

    public SearchResponse<EstateListing> searchPropertyType(PropertyTypes pType) throws IOException {
        Reader stringReader = new StringReader("{}");
        System.out.println(pType);
        System.out.println(pType.toString());
        TermQuery termQuery = TermQuery.of(t -> t.field("propertyType").value(pType.toString()).caseInsensitive(true));
        SearchResponse<EstateListing> searchResponse = client.search(SearchRequest.of(s -> s
                .index("mylistings")
                .query(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .matchAll(builder -> builder.withJson(stringReader))
                                )

                                .filter(f -> f
                                        .term(termQuery)
                                )
                        )
                )
        ), EstateListing.class);
        System.out.println(searchResponse.toString());
        TotalHits totalHits = searchResponse.hits().total();
        List<Hit<EstateListing>> hits = searchResponse.hits().hits();
        for (Hit<EstateListing> hit: hits) {
            EstateListing listing = hit.source();
            System.out.println(listing);
        }
        return searchResponse;
    }
    public SearchResponse<EstateListing> searchMultiplePropertyTypes(List<PropertyTypes> pTypes) throws IOException {
        Reader stringReader = new StringReader("{}");
        System.out.println(pTypes);
        System.out.println(pTypes.toString());
        if(!pTypes.isEmpty()) {
            System.out.printf("toString: %s", pTypes.stream().findFirst().get().toString());
            System.out.printf("name: %s", pTypes.stream().findFirst());
            System.out.printf("toString: %s", pTypes.get(0).toString());
            System.out.printf("name: %s", pTypes.get(0).name());
            System.out.printf("all: %s", pTypes.get(0));
        } else {
            System.out.println("Types list is empty");
        }
        List<FieldValue> fieldValueList = pTypes.stream().map(m -> FieldValue.of(builder -> builder.stringValue(m.name().toLowerCase()))).toList();
        System.out.println(fieldValueList);
        System.out.println(fieldValueList.toString());
        TermsQueryField termsQueryField = TermsQueryField.of(t -> t.value(fieldValueList));
        System.out.println(termsQueryField);
        System.out.println(termsQueryField.toString());
        TermsQuery termsQuery = TermsQuery.of(builder -> builder.field("propertyType").terms(termsQueryField));
        System.out.println(termsQuery);
        System.out.println(termsQuery.toString());
        //TermQuery termQuery = TermQuery.of(t -> t.field("propertyType").value(pType.toString()).caseInsensitive(true));
        SearchResponse<EstateListing> searchResponse = client.search(SearchRequest.of(s -> s
                .index("mylistings")
                .query(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .terms(termsQuery)
                                )
                        )
                        //.terms(termsQuery)
                )
        ), EstateListing.class);
        System.out.println(searchResponse.toString());
        TotalHits totalHits = searchResponse.hits().total();
        List<Hit<EstateListing>> hits = searchResponse.hits().hits();
        for (Hit<EstateListing> hit: hits) {
            EstateListing listing = hit.source();
            System.out.println(listing);
        }
        return searchResponse;
    }

    /*public SearchResponse<PropertyListing> searchPropertyListingForName(String listingName) throws IOException {
        SearchResponse<PropertyListing> searchResponse = client.search(
                SearchRequest.of(
                        s -> s
                                .index("myindex")
                                .query(q -> q
                                        .match(t -> t
                                                .field("name")
                                                .query(listingName)
                                        )
                                )
                ),
                PropertyListing.class
        );
        TotalHits totalHits = searchResponse.hits().total();
        boolean isExactResult = totalHits.relation() == TotalHitsRelation.Eq;

        if(isExactResult) {
            System.out.println("There are " + totalHits.value() + " results");
        } else {
            System.out.println("There are more than " + totalHits.value() + " results");
        }

        List<Hit<PropertyListing>> hits = searchResponse.hits().hits();

        for (Hit<PropertyListing> hit: hits) {
            PropertyListing listing = hit.source();
            System.out.println("Found property listing " + hit.id() +  " , " + listing.getPid() + " , " + listing.getName() + " with score of " + hit.score());
        }
        return searchResponse;
    }*/

    public SearchResponse<EstateListing> combinedSearch(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            double lat,
            double lon,
            String distance,
            List<PropertyTypes> propertyTypes
    ) throws IOException {

        //ES match_all query looks like:
        // "match_all": {}
        // so create an empty object(empty curly braces) in json to use as the value
        Reader matchAllJsonValue = new StringReader("{}");

        //construct a RangeQuery match price in between minPrice and MaxPrice
        //BigDecimal to String is used as type for price because it doesn't lose precision like floats/doubles
        //Although in this use case that likely wouldn't be a problem since prices don't need lots of decimal precision
        RangeQuery priceRangeQuery = RangeQuery.of(r -> r
                .field("price")
                .gte(JsonData.of(minPrice.toString()))
                .lte(JsonData.of(maxPrice.toString()))
        );

        System.out.println(priceRangeQuery);

        //Create JSON value of GeoPoint
        //can be represented by an array (tuple) where first element is longitude, last element is latitude
        //So the string value looks like:
        // [lon, lat]
        //using lon and lat parameters of this method
        //Java doesn't have template literals yet, so String.format is the closest thing as of JDK 17
        Reader locationLatLonJson = new StringReader(String.format("[%s, %s]", lon, lat));
        //Create the actual JsonData from the reader
        JsonData latLonJson = JsonData.from(locationLatLonJson);

        //Turn the distance parameter to JsonData
        JsonData locationDistanceJson = JsonData.of(distance);

        //Create query for distance_feature
        //This query is used in "should" query
        //This adjusts relevance score based on how far away the property is from the provided coordinates
        DistanceFeatureQuery distanceFeatureQuery = DistanceFeatureQuery.of(d -> d
                .field("location")
                .pivot(locationDistanceJson)
                .origin(latLonJson)
        );

        System.out.println(distanceFeatureQuery);


        //Use the list of property types to create a new list of corresponding FieldValue
        //ES stores the terms in all lowercase, so convert the uppercase enum name to lowercase
        List<FieldValue> typesList = propertyTypes
                .stream()
                .map(m -> FieldValue.of(m.toString().toLowerCase()))
                .toList();

        //Use the list of FieldValue to set up the values/terms to use in TermsQuery
        TermsQueryField termsQueryField = TermsQueryField.of(t -> t
                .value(typesList)
        );

        //The actual TermsQuery, searching on "propertyType" field
        //Match a listing if its propertyType matches any of the terms in the list provided
        TermsQuery termsQuery = TermsQuery.of(t -> t
                .field("propertyType")
                .terms(termsQueryField)
        );

        System.out.println(termsQuery);

        SearchResponse<EstateListing> searchResponse = client.search(SearchRequest.of(s -> s
                .index("mylistings")
                .query(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .matchAll(builder -> builder.withJson(matchAllJsonValue))
                                )
                                .should(builder -> builder
                                        .distanceFeature(distanceFeatureQuery)
                                )
                                .filter(f -> f
                                        .range(priceRangeQuery)
                                )
                                .filter(f -> f
                                        .terms(termsQuery)
                                )
                        )
                )),
                EstateListing.class
        );

        TotalHits totalHits = searchResponse.hits().total();

        System.out.printf("Total hits: %s%n", totalHits);

        List<Hit<EstateListing>> hits = searchResponse.hits().hits();

        for(Hit<EstateListing> hit : hits) {
            EstateListing listing = hit.source();
            System.out.println(listing);
        }

        System.out.println(searchResponse.toString());

        return searchResponse;
    }

}
