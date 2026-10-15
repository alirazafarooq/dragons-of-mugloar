
package com.example.mugloar.service;

import com.example.mugloar.model.GameState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GameApiClient {

    private final RestClient restClient;

    public GameApiClient(
            RestClient.Builder builder,
            @Value("${dragons.api.base-url}") String baseUrl) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public GameState startGame() {
        return restClient.post()
                .uri("/game/start")
                .retrieve()
                .body(GameState.class);
    }
}
