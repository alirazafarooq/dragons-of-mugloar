
package com.example.mugloar.service;

import com.example.mugloar.model.GameState;
import com.example.mugloar.model.Mission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.function.Supplier;

@Service
public class GameApiClient {

    private static final Logger log =
            LoggerFactory.getLogger(GameApiClient.class);

    private final RestClient restClient;

    public GameApiClient(
            RestClient.Builder builder,
            @Value("${dragons.api.base-url}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public GameState startGame() {
        GameState state = callExternalApi(
                "start game",
                () -> restClient.post()
                        .uri("/game/start")
                        .retrieve()
                        .body(GameState.class)
        );

        if (state == null) {
            throw new GameApiException(
                    HttpStatus.BAD_GATEWAY,
                    "External game service returned an empty game state."
            );
        }

        return state;
    }

    public List<Mission> getMessages(String gameId) {
        List<Mission> missions = callExternalApi(
                "retrieve missions",
                () -> restClient.get()
                        .uri("/{gameId}/messages", gameId)
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<Mission>>() {})
        );

        if (missions == null) {
            throw new GameApiException(
                    HttpStatus.BAD_GATEWAY,
                    "External game service returned an empty response."
            );
        }

        return missions;
    }

    private <T> T callExternalApi(
            String operation,
            Supplier<T> request
    ) {
        try {
            return request.get();

        } catch (RestClientResponseException ex) {
            int upstreamStatus = ex.getStatusCode().value();

            if (upstreamStatus == 404) {
                log.warn(
                        "External API returned 404 during {}",
                        operation
                );

                throw new GameApiException(
                        HttpStatus.NOT_FOUND,
                        "Game was not found by the external service.",
                        ex
                );
            }

            if (upstreamStatus == 429) {
                log.warn(
                        "External API rate limit reached during {}",
                        operation
                );

                throw new GameApiException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "External game service is temporarily unavailable.",
                        ex
                );
            }

            log.error(
                    "External API returned HTTP {} during {}",
                    upstreamStatus,
                    operation,
                    ex
            );

            throw new GameApiException(
                    HttpStatus.BAD_GATEWAY,
                    "External game service returned an unexpected response.",
                    ex
            );

        } catch (ResourceAccessException ex) {
            log.error(
                    "Could not connect to external API during {}",
                    operation,
                    ex
            );

            throw new GameApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not connect to the external game service.",
                    ex
            );

        } catch (RestClientException ex) {
            log.error(
                    "Failed to process external API response during {}",
                    operation,
                    ex
            );

            throw new GameApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not process the external game service response.",
                    ex
            );
        }
    }
}
