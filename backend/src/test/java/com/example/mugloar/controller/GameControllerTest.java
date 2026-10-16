
package com.example.mugloar.controller;

import com.example.mugloar.model.GameState;
import com.example.mugloar.model.Mission;
import com.example.mugloar.model.MissionSolveResult;
import com.example.mugloar.model.ShopItem;
import com.example.mugloar.model.ShopPurchaseResult;
import com.example.mugloar.service.GameApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void getMessagesRejectsInvalidGameId() throws Exception {
        mockMvc.perform(get("/api/games/invalid!id/messages"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameApiClient);
    }

    @Test
    void solveMissionReturnsResult() throws Exception {
        MissionSolveResult result = new MissionSolveResult(
                true,
                "You successfully solved the mission!",
                3,
                13,
                13,
                0,
                1
        );

        when(gameApiClient.solveMission("S1efis2P", "N0SjIYKD"))
                .thenReturn(result);

        mockMvc.perform(post("/api/games/S1efis2P/solve/N0SjIYKD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("You successfully solved the mission!"))
                .andExpect(jsonPath("$.lives").value(3))
                .andExpect(jsonPath("$.gold").value(13))
                .andExpect(jsonPath("$.score").value(13))
                .andExpect(jsonPath("$.highScore").value(0))
                .andExpect(jsonPath("$.turn").value(1));
    }

    @Test
    void getShopReturnsAvailableItems() throws Exception {
        List<ShopItem> items = List.of(
                new ShopItem("hpot", "Healing potion", 50),
                new ShopItem("cs", "Claw Sharpening", 100)
        );

        when(gameApiClient.getShopItems("S1efis2P"))
                .thenReturn(items);

        mockMvc.perform(get("/api/games/S1efis2P/shop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("hpot"))
                .andExpect(jsonPath("$[0].name").value("Healing potion"))
                .andExpect(jsonPath("$[0].cost").value(50))
                .andExpect(jsonPath("$[1].id").value("cs"))
                .andExpect(jsonPath("$[1].cost").value(100));
    }

    @Test
    void buyShopItemReturnsPurchaseResult() throws Exception {
        ShopPurchaseResult result = new ShopPurchaseResult(
                true,
                50,
                3,
                0,
                1
        );

        when(gameApiClient.buyItem("S1efis2P", "hpot"))
                .thenReturn(result);

        mockMvc.perform(post("/api/games/S1efis2P/shop/buy/hpot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shoppingSuccess").value(true))
                .andExpect(jsonPath("$.gold").value(50))
                .andExpect(jsonPath("$.lives").value(3))
                .andExpect(jsonPath("$.level").value(0))
                .andExpect(jsonPath("$.turn").value(1));
    }
}
