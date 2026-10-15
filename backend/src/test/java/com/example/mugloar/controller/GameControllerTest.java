
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
}
