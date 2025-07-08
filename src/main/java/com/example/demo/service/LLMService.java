package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LLMService {

    private final WebClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LLMService(){
        this.webClient = WebClient.builder()
                .baseUrl("https://api.cohere.ai/v1/chat")
                .defaultHeader("Authorization", "Bearer tPDzMhjnmrWYjFTVZrCcHFS7kmMyr7yJKwkzyoFU")
                .clientConnector(
                        new ReactorClientHttpConnector(HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(40))  // Timeout for the full response
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000) // Connect timeout
                        )
                )
                .build();
    }

    public Mono<String> clarifyQuery(String originalQuery) {
        String prompt = "Clarify the user intent and convert it into a concise e-commerce product search.Provide only product name For: \"" + originalQuery + "\"";

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", "command-r",
                        "temperature", 0.3,
                        "chat_history", List.of(),
                        "message", prompt
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> resp.get("text").toString().replaceAll("(?s)```json|```", "").trim())
                .doOnNext(result -> System.out.println("LLMService clarifyQuery: " + originalQuery + " - " + result));
    }

    public Mono<String> extractAndRank(String serpJson) {

        return Mono.fromCallable(() -> {
            JsonNode root = objectMapper.readTree(serpJson);

            List<Map<String, Object>> extractedProducts = new ArrayList<>();

            if (root.has("shopping_results") && root.get("shopping_results").isArray()) {
                for (JsonNode item : root.path("shopping_results")) {
                    String link = item.path("product_link").asText(null);
                    String productName = item.path("title").asText(null);
                    String priceString = item.path("price").asText(null);

                    System.out.println("Processing item:");
                    System.out.println("Link: " + link);
                    System.out.println("Title: " + productName);
                    System.out.println("Price: " + priceString);

                    if (link != null && productName != null && priceString != null) {
                        String numericPrice = priceString.replaceAll("[^\\d.]", "");
                        if (!numericPrice.isEmpty()) {
                            extractedProducts.add(Map.of(
                                    "link", link,
                                    "price", numericPrice,
                                    "currency", "USD",
                                    "productName", productName
                            ));
                        }
                    }
                }
            }

            List<Map<String, Object>> sorted = extractedProducts.stream()
                    .sorted(Comparator.comparingDouble(p -> Double.parseDouble(p.get("price").toString())))
                    .collect(Collectors.toList());

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sorted);
        });
    }
}
