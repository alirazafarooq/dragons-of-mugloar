
package com.example.mugloar.controller;

import com.example.mugloar.model.GameState;
import com.example.mugloar.service.GameApiClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
@Tag(
        name = "Game Management",
        description = "Endpoints for starting and managing Dragons of Mugloar games"
)
public class GameController {

    private final GameApiClient gameApiClient;

    public GameController(GameApiClient gameApiClient) {
        this.gameApiClient = gameApiClient;
    }

    @PostMapping
    @Operation(
            summary = "Start a new game",
            description = """
            Starts a new Dragons of Mugloar game by calling the external game API.
            Returns the initial game state, including the game ID, lives, gold,
            level, score, high score, and current turn.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Game started successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "An error occurred while communicating with the game API"
            )
    })
    public GameState startGame() {
        return gameApiClient.startGame();
    }
}
