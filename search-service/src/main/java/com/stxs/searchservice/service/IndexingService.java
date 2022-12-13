package com.stxs.searchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch._types.mapping.GeoPointProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.stxs.searchservice.domain.EstateListing;
import com.stxs.searchservice.domain.PropertyListing;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.common.UUIDs;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class IndexingService {

    @Autowired
    private ElasticsearchClient client;

    public void indexResponse(PropertyListing listing) throws IOException {
        IndexRequest<PropertyListing> request = IndexRequest.of(i -> i
                .index("myindex")
                .id(String.valueOf(listing.getPid()))
                .document(listing)
        );

        IndexResponse response = client.index(request);

        System.out.println(response.toString());
        System.out.println("Indexed with version " + response.version());
        //return response;
    }



    /*public void initIndex(String indexName) throws IOException {
        XContentBuilder contentBuilder = XContentFactory.jsonBuilder();
        contentBuilder.startObject();
        {
            contentBuilder.startObject("properties");
            {
                contentBuilder.startObject("location");
                {
                    contentBuilder.field("type", "geo_point");
                }
                contentBuilder.endObject(); //close location {}
            }
            contentBuilder.endObject(); //close properties {}
        }
        contentBuilder.endObject(); //close json obj for mapping


        PutMappingRequest request = PutMappingRequest.of(contentBuilder);
        IndexSettings settings = IndexSettings.of(builder -> builder.)
        TypeMapping mapping = TypeMapping.of(builder -> builder.withJson());
        CreateIndexRequest createIndexRequest = CreateIndexRequest.of(c -> c
                .index(indexName)
        );
        client.indices().create(createIndexRequest);
    }*/

    public void indexListing(EstateListing listing) throws IOException {
        IndexRequest<EstateListing> indexRequest = IndexRequest.of(i -> i
                .index("mylistings")
                .id(listing.getId())
                .document(listing)
        );
        IndexResponse response = client.index(indexRequest);
        System.out.println(response.toString());
    }
    /*

    public void createIndexWithLocation(String indexName) throws IOException {
        CreateIndexRequest createIndexRequest = CreateIndexRequest.of(i -> i
                .index(indexName)
                .mappings()
        );
        client.indices().create(createIndexRequest);

    }*/

    /*public void addMappings(String indexName, JsonNode mappings) {
        client.indices().putMapping(i -> i
                .index(indexName)
                .properties("location", p -> p.geoPoint(
                        g -> g.
                ))
        );
    }*/

    public void removeIndex(String indexName) throws IOException {
        DeleteIndexRequest deleteIndexRequest = DeleteIndexRequest.of(i -> i
                .index(indexName)
        );
        client.indices().delete(deleteIndexRequest);
    }

}
