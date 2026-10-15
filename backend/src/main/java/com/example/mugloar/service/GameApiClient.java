
package com.example.mugloar.service;

import com.example.mugloar.model.GameState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.example.mugloar.model.Mission;
import org.springframework.core.ParameterizedTypeReference;
import java.util.List;

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


    public List<Mission> getMessages(String gameId) {
        return restClient.get()
                .uri("/{gameId}/messages", gameId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Mission>>() {});
    }

}
