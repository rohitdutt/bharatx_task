package com.example.demo.controller;

import com.example.demo.service.LLMService;
import com.example.demo.service.SerpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
class PriceController {

    @Autowired
    private LLMService llmService;

    @Autowired
    private SerpService serpService;

    @PostMapping("/search")
    public Mono<String> search(@RequestBody Map<String, String> request) {
        String country = request.getOrDefault("country", "US");
        String query = request.get("query");

        return llmService.clarifyQuery(query)
                .flatMap(clarifiedQuery -> serpService.search(query, country))
                .flatMap(llmService::extractAndRank);
    }
}