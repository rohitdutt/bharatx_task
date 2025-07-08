package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class SerpService {

    private final WebClient serpClient = WebClient.create("https://serpapi.com");

    public Mono<String> search(String query, String country) {
        return serpClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("engine", "google")
                        .queryParam("gl", country)
                        .queryParam("tbm", "shop")
                        .queryParam("api_key", "f63dd4f7a8d373c2659785ab4ae9c4b17e64b7759da0aaa702a8b49b949df458")
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(result -> System.out.println("SerpService search: " + query + " in " + country + " - " + result));
    }
}
