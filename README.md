# java-elasticsearch-demo-app

This is a Java app I made mainly to play around with the new ElasticSearch Java Client.

This app uses Spring Boot, but does not use the Spring Data ElasticSearch client, as it used the deprecated RestHighLevelClient for interacting with ElasticSearch at the time of development (Spring Boot 2.7.4).

Docker Compose is used to run an ElasticSearch 7.17.6 cluster of 3 nodes.

There is a front-end NextJs/React app that makes use of this app, the demo is still a WIP.
