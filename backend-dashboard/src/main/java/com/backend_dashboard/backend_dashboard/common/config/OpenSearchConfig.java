package com.backend_dashboard.backend_dashboard.common.config;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

@Configuration
public class OpenSearchConfig {

    @Value("${aws.opensearch.host}")
    private String host;

//     🔥 AWS 사용 시 주석 처리 필요
//    @Value("${aws.opensearch.username}")
//    private String username;
//
//    // 🔥 AWS 사용 시 주석 처리 필요
//    @Value("${aws.opensearch.password}")
//    private String password;

    @Bean
    public RestHighLevelClient restHighLevelClient() throws Exception {


        final var provider = new BasicCredentialsProvider();
        // 🔥 AWS 사용 시 주석 처리 필요
//        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        final SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();

        RestClientBuilder builder = RestClient.builder(org.apache.http.HttpHost.create(host))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        // 🔥 AWS 사용 시 주석 처리 필요
//                        .setDefaultCredentialsProvider(provider)
                );

        return new RestHighLevelClient(builder);
    }
}
