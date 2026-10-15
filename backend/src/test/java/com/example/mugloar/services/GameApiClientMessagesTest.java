
package com.example.mugloar.service;

import com.example.mugloar.model.Mission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GameApiClientMessagesTest {

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
    void getMessagesReturnsMissionList() {
        mockServer.expect(
                        requestTo(
                                "https://dragonsofmugloar.com/api/v2/S1efis2P/messages"
                        )
                )
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [
                          {
                            "adId": "N0SjIYKD",
                            "message": "Steal sheep delivery and share some profits with the people.",
                            "reward": 64,
                            "expiresIn": 7,
                            "encrypted": null,
                            "probability": "Walk in the park"
                          }
                        ]
                        """, MediaType.APPLICATION_JSON));

        List<Mission> missions =
                gameApiClient.getMessages("S1efis2P");

        assertEquals(1, missions.size());

        Mission mission = missions.get(0);

        assertEquals("N0SjIYKD", mission.adId());
        assertEquals(
                "Steal sheep delivery and share some profits with the people.",
                mission.message()
        );
        assertEquals(64, mission.reward());
        assertEquals(7, mission.expiresIn());
        assertEquals(null, mission.encrypted());
        assertEquals("Walk in the park", mission.probability());

        mockServer.verify();
    }

    @Test
    void getMessagesReturnsEmptyListWhenNoMissionsAvailable() {
        mockServer.expect(
                        requestTo(
                                "https://dragonsofmugloar.com/api/v2/S1efis2P/messages"
                        )
                )
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        List<Mission> missions =
                gameApiClient.getMessages("S1efis2P");

        assertEquals(0, missions.size());

        mockServer.verify();
    }
}
