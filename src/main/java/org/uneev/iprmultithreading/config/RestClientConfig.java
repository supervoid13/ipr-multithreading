package org.uneev.iprmultithreading.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.uneev.iprmultithreading.service.FacePhotoExtractorService;
import org.uneev.iprmultithreading.service.FacePhotoSearchService;
import org.uneev.iprmultithreading.service.FingerprintExtractorService;
import org.uneev.iprmultithreading.service.FingerprintSearchService;

import java.net.http.HttpClient;

@Configuration
class RestClientConfig {

    @Bean
    public RestClient fingerprintExtractorRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .baseUrl("http://localhost:8082")
                .build();
    }

    @Bean
    public RestClient facePhotoExtractorRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .baseUrl("http://localhost:8083")
                .build();
    }

    @Bean
    public RestClient fingerprintSearcherRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .baseUrl("http://localhost:8084")
                .build();
    }

    @Bean
    public RestClient facePhotoSearcherRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .baseUrl("http://localhost:8085")
                .build();
    }

    @Bean
    public FingerprintExtractorService fingerprintExtractorService(
            @Qualifier("fingerprintExtractorRestClient") RestClient fingerprintExtractorRestClient
    ) {
        RestClientAdapter adapter = RestClientAdapter.create(fingerprintExtractorRestClient);
        HttpServiceProxyFactory factory =HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(FingerprintExtractorService.class);
    }

    @Bean
    public FacePhotoExtractorService facePhotoExtractorService(
            @Qualifier("facePhotoExtractorRestClient") RestClient facePhotoExtractorRestClient
    ) {
        RestClientAdapter adapter = RestClientAdapter.create(facePhotoExtractorRestClient);
        HttpServiceProxyFactory factory =HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(FacePhotoExtractorService.class);
    }

    @Bean
    public FingerprintSearchService fingerprintSearchService(
            @Qualifier("fingerprintSearcherRestClient") RestClient fingerprintSearcherRestClient
    ) {
        RestClientAdapter adapter = RestClientAdapter.create(fingerprintSearcherRestClient);
        HttpServiceProxyFactory factory =HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(FingerprintSearchService.class);
    }

    @Bean
    public FacePhotoSearchService facePhotoSearchService(
            @Qualifier("facePhotoSearcherRestClient") RestClient facePhotoSearcherRestClient
    ) {
        RestClientAdapter adapter = RestClientAdapter.create(facePhotoSearcherRestClient);
        HttpServiceProxyFactory factory =HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(FacePhotoSearchService.class);
    }
}
