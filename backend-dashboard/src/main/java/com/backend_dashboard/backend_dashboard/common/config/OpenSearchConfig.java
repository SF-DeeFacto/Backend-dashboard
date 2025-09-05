package com.backend_dashboard.backend_dashboard.common.config;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;

import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchConfig {

    @Value("${aws.opensearch.host}")
    private String host;

    @Value("${aws.opensearch.port}")
    private int port;

    @Value("${aws.opensearch.scheme}")
    private String scheme;

    @Value("${aws.opensearch.username}")
    private String username;

    @Value("${aws.opensearch.password}")
    private String password;

    @Bean
    public OpenSearchClient openSearchClient() {
        final BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
        );

        RestClientBuilder builder = RestClient.builder(
                        new org.apache.http.HttpHost(host, port, scheme))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credsProv));

        RestClientTransport transport = new RestClientTransport(
                builder.build(),
                new JacksonJsonpMapper()
        );

        return new OpenSearchClient(transport);
    }
}
