
package com.example.mugloar.controller;

import com.example.mugloar.model.GameState;
import com.example.mugloar.service.GameApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mugloar.model.Mission;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameApiClient gameApiClient;

    @Test
    void startGameReturnsGameStateAsJson() throws Exception {
        when(gameApiClient.startGame()).thenReturn(
                new GameState("test123", 3, 0, 0, 0, 0, 0)
        );

        mockMvc.perform(post("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("test123"))
                .andExpect(jsonPath("$.lives").value(3))
                .andExpect(jsonPath("$.gold").value(0))
                .andExpect(jsonPath("$.turn").value(0));
    }

    @Test
    void getMessagesReturnsAvailableMissions() throws Exception {
        Mission mission = new Mission(
                "N0SjIYKD",
                "Steal sheep delivery and share some profits with the people.",
                64,
                7,
                null,
                "Walk in the park"
        );

        when(gameApiClient.getMessages("S1efis2P"))
                .thenReturn(List.of(mission));

        mockMvc.perform(get("/api/games/S1efis2P/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].adId").value("N0SjIYKD"))
                .andExpect(jsonPath("$[0].reward").value(64))
                .andExpect(jsonPath("$[0].expiresIn").value(7))
                .andExpect(jsonPath("$[0].probability")
                        .value("Walk in the park"));
    }

}
