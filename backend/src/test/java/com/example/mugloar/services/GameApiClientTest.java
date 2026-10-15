
package com.example.mugloar.service;

import com.example.mugloar.model.GameState;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GameApiClientTest {

    private static final String BASE_URL =
            "https://dragonsofmugloar.com/api/v2";

    @Test
    void startGameSendsPostRequestAndReturnsGameState() {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        GameApiClient client = new GameApiClient(builder, BASE_URL);

        server.expect(requestTo(BASE_URL + "/game/start"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        """
                        {
                            "gameId": "test123",
                            "lives": 3,
                            "gold": 0,
                            "level": 0,
                            "score": 0,
                            "highScore": 0,
                            "turn": 0
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        GameState response = client.startGame();

        assertEquals("test123", response.gameId());
        assertEquals(3, response.lives());
        assertEquals(0, response.gold());
        assertEquals(0, response.level());
        assertEquals(0, response.score());
        assertEquals(0, response.highScore());
        assertEquals(0, response.turn());

        server.verify();
    }
}
