package com.stxs.searchservice.controller;

import co.elastic.clients.elasticsearch._types.LatLonGeoLocation;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.stxs.searchservice.domain.*;
//import com.stxs.searchservice.domain.PropertyTypes;
import com.stxs.searchservice.service.IndexingService;
import com.stxs.searchservice.service.QueryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.geo.Point;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
public class SimpleController {

    @Autowired
    private IndexingService indexingService;

    @Autowired
    private QueryService queryService;

    private final ObjectMapper mapper;

    public SimpleController(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

    /*@GetMapping("/query-name")
    public String searchForName(@RequestParam("name") String searchName) throws IOException {
        SearchResponse<PropertyListing> response = queryService.searchPropertyListingForName(searchName);
        return response.toString();
    }*/

    @GetMapping("/search/price")
    public String searchPriceRange(
            @RequestParam("min") String min,
            @RequestParam("max") String max
    ) throws IOException {
        System.out.println("controller");
        BigDecimal minPrice = new BigDecimal(min);
        BigDecimal maxPrice = new BigDecimal(max);
        SearchResponse<EstateListing> searchResponse = queryService.searchPriceRange(minPrice, maxPrice);
        return searchResponse.toString();
    }
    @GetMapping("/search/distance")
    public String searchDistanceRange(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam("r") String radius
    ) throws IOException {
        System.out.println("controller");
        System.out.println(lat);
        System.out.println(lon);
        System.out.println(radius);
        SearchResponse<EstateListing> searchResponse = queryService.searchGeoRange(lat, lon, radius);
        return searchResponse.toString();
    }

    @GetMapping("/search/type")
    public String searchPropertyType(@RequestParam("type") String propertyType) throws IOException {
        System.out.println(propertyType);
        PropertyTypes type = PropertyTypes.valueOf(propertyType.toUpperCase());
        System.out.println(type);
        SearchResponse<EstateListing> response = queryService.searchPropertyType(type);
        System.out.println(response.toString());
        return response.toString();
    }
    @GetMapping("/search/types")
    public String searchPropertyTypes(@RequestParam(value = "type", required = false) List<String> propertyTypes) throws IOException {
        System.out.println(propertyTypes);
        List<PropertyTypes> types = propertyTypes.stream().map(m -> PropertyTypes.valueOf(m.toUpperCase())).toList();
        //PropertyTypes type = PropertyTypes.valueOf(propertyType.toUpperCase());
        System.out.println(types);
        SearchResponse<EstateListing> response = queryService.searchMultiplePropertyTypes(types);
        System.out.println(response.toString());
        return response.toString();
    }

    /**
     * The main search method.
     * This takes in all possible things to search for and constructs a single query
     *
     * @param mnPrice String minimum price of a property (defaults to 0)
     * @param mxPrice String maximum price of a property (defaults to 2147483647)
     * @param latitude double latitude value of origin point to perform distance query from
     * @param longitude double longitude value of origin point to perform distance query from
     * @param distance String distance from origin point in geo query
     * @param propertyTypesList List<PropertyTypes> list of which types of properties to include
     * @return
     * @throws IOException
     */
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("search")
    public List<EstateListing> search(
            @RequestParam(value = "min", required = false) String mnPrice,
            @RequestParam(value = "max", required = false) String mxPrice,
            @RequestParam("lat") double latitude,
            @RequestParam("lon") double longitude,
            @RequestParam("d") String distance,
            @RequestParam(value = "type", required = false) List<String> propertyTypesList
    ) throws IOException {

        //BigDecimal is used here since we are dealing with money
        //It's probably overkill in this situation, since our prices are basically just whole numbers
        //But it's always good to use BigDecimal when dealing with money, as floats may cause precision inaccuracies
        BigDecimal minPrice = new BigDecimal(mnPrice.isEmpty() ? String.valueOf(0) : mnPrice);
        BigDecimal maxPrice = new BigDecimal(mxPrice.isEmpty() ? String.valueOf(Integer.MAX_VALUE) : mxPrice);

        //This is kinda hacky but whatever there's only 4 enums, likely will never add more
        //Check if there were query params for 'type' passed in, if so use those values
        //If no 'type' query params were passed in, create a list of all PropertyTypes values
        //ES 'terms' query matches docs containing at least one term of a list of terms,
        //but, if no terms are passed we want docs with any terms, not none
        //So by filling the array with all PropertyTypes values, ES 'terms' query will look for
        //[house, apartment, condo, hotel, ...etc.] instead of [], essentially saying match a doc
        //with any valid PropertyTypes enum value (which should be all)
        List<PropertyTypes> propertyTypes = propertyTypesList!=null ?
                propertyTypesList
                .stream()
                .map(m -> PropertyTypes.valueOf(m.toUpperCase()))
                .toList()
                :
                Arrays.stream(PropertyTypes.values()).toList();

        SearchResponse<EstateListing> searchResponse =
                queryService.combinedSearch(minPrice, maxPrice, latitude, longitude, distance, propertyTypes);

        System.out.println(searchResponse.fields());
        HitsMetadata<EstateListing> hitsMetadata = searchResponse.hits();
        List<Hit<EstateListing>> hits = searchResponse.hits().hits();
        System.out.println(hitsMetadata);
        System.out.println(hits);
        List<EstateListing> listings = hits.stream().map(Hit::source).toList();
        //JsonNode root =

        ArrayNode jsonArray = mapper.createArrayNode();
        hits.stream().forEach(hit -> {
            EstateListingResponse response = new EstateListingResponse(hit.source());
            response.setScore(hit.score());
            JsonNode jsonHit = JsonNodeFactory.instance.pojoNode(response);
            //jsonArray.add(String.valueOf(response));
            jsonArray.add(jsonHit);
        });
        List<?> enhancedListings = hits.stream().map(h -> h.score()).toList();
        System.out.println(listings);
        System.out.println(jsonArray);
        //return  searchResponse.toString();
        return listings;
    }

    @PostMapping("/index/mylistings")
    public void indexListing(
            @RequestBody EstateListingDto listingDto
    ) throws IOException {
        EstateListing estateListing = new EstateListing();
        BeanUtils.copyProperties(listingDto, estateListing);
        System.out.println(estateListing.toString());
        System.out.println(estateListing.getPropertyType());
        System.out.println(listingDto.toString());
        System.out.println(listingDto.getPropertyType());
        System.out.println(estateListing.getPrice());
        System.out.println(listingDto.getPrice());
        indexingService.indexListing(estateListing);
    }

    @PostMapping("/index")
    public void indexDocument(@RequestBody PropertyListingDto listingDto) throws IOException {
        PropertyListing listing = new PropertyListing();
        BeanUtils.copyProperties(listingDto, listing);
        System.out.println(listingDto);
        System.out.println(listing);
        indexingService.indexResponse(listing);
        /*EstateListing estateListing = new EstateListing();
        estateListing.setPropertyType(PropertyTypes.HOUSE);
        estateListing.setLocation(new GeoPoint(10, -20));*/
    }

    @PostMapping("/index/create")
    public void createIndex() {

    }

    @DeleteMapping("/index")
    public void removeIndex(@RequestParam("i") String name) throws IOException {
        indexingService.removeIndex(name);
    }
}
