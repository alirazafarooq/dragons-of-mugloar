
package com.example.mugloar.service;

import com.example.mugloar.model.GameState;
import com.example.mugloar.model.Mission;
import com.example.mugloar.model.MissionSolveResult;

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

import com.example.mugloar.model.ShopItem;
import com.example.mugloar.model.ShopPurchaseResult;

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


    public MissionSolveResult solveMission(String gameId, String adId) {
        MissionSolveResult result = callExternalApi(
                "solve mission",
                () -> restClient.post()
                        .uri("/{gameId}/solve/{adId}", gameId, adId)
                        .retrieve()
                        .body(MissionSolveResult.class)
        );

        if (result == null
                || result.success() == null
                || result.message() == null
                || result.lives() == null
                || result.gold() == null
                || result.score() == null
                || result.highScore() == null
                || result.turn() == null) {

            throw new GameApiException(
                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "External game service returned an incomplete mission result."
            );
        }

        return result;
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

    public List<ShopItem> getShopItems(String gameId) {
        List<ShopItem> items = callExternalApi(
                "retrieve shop items",
                () -> restClient.get()
                        .uri("/{gameId}/shop", gameId)
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<ShopItem>>() {})
        );

        if (items == null) {
            throw new GameApiException(
                    HttpStatus.BAD_GATEWAY,
                    "External game service returned an empty shop response."
            );
        }

        return items;
    }

    public ShopPurchaseResult buyItem(String gameId, String itemId) {
        ShopPurchaseResult result = callExternalApi(
                "purchase shop item",
                () -> restClient.post()
                        .uri("/{gameId}/shop/buy/{itemId}", gameId, itemId)
                        .retrieve()
                        .body(ShopPurchaseResult.class)
        );

        if (result == null) {
            throw new GameApiException(
                    HttpStatus.BAD_GATEWAY,
                    "External game service returned an empty purchase response."
            );
        }

        return result;
    }
}
