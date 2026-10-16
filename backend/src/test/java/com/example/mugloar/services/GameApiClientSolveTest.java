
package com.example.mugloar.service;

import com.example.mugloar.model.MissionSolveResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GameApiClientSolveTest {

    private MockRestServiceServer mockServer;
    private GameApiClient gameApiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        mockServer = MockRestServiceServer.bindTo(builder).build();

        gameApiClient = new GameApiClient(
                builder,
                "https://dragonsofmugloar.com/api/v2"
        );
    }

    @Test
    void solveMissionSendsPostAndMapsResponse() {
        mockServer.expect(
                        requestTo(
                                "https://dragonsofmugloar.com/api/v2/S1efis2P/solve/N0SjIYKD"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                    {
                      "success": true,
                      "lives": 3,
                      "gold": 13,
                      "score": 13,
                      "highScore": 0,
                      "turn": 1,
                      "message": "You successfully solved the mission!"
                    }
                    """, MediaType.APPLICATION_JSON));

        MissionSolveResult result = gameApiClient.solveMission(
                "S1efis2P",
                "N0SjIYKD"
        );

        assertEquals(Boolean.TRUE, result.success());
        assertEquals(
                "You successfully solved the mission!",
                result.message()
        );
        assertEquals(3, result.lives());
        assertEquals(13, result.gold());
        assertEquals(13, result.score());
        assertEquals(0, result.highScore());
        assertEquals(1, result.turn());

        mockServer.verify();
    }

    @Test
    void solveMissionMapsUnsuccessfulAttempt() {
        mockServer.expect(
                        requestTo(
                                "https://dragonsofmugloar.com/api/v2/S1efis2P/solve/N0SjIYKD"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                    {
                      "success": false,
                      "lives": 2,
                      "gold": 13,
                      "score": 13,
                      "highScore": 0,
                      "turn": 1,
                      "message": "The mission failed."
                    }
                    """, MediaType.APPLICATION_JSON));

        MissionSolveResult result = gameApiClient.solveMission(
                "S1efis2P",
                "N0SjIYKD"
        );

        assertEquals(Boolean.FALSE, result.success());
        assertEquals("The mission failed.", result.message());
        assertEquals(2, result.lives());
        assertEquals(1, result.turn());

        mockServer.verify();
    }
}
