
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
import com.example.mugloar.model.Mission;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

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


    @GetMapping("/{gameId}/messages")
    @Operation(
            summary = "Get available missions",
            description = """
        Retrieves the missions currently available for the specified game.
        Each mission includes its identifier, description, reward,
        expiration time, encryption status, and estimated probability.
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Missions retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "An error occurred while retrieving missions"
            )
    })
    public List<Mission> getMessages(
            @Parameter(description = "ID of the active game", example = "S1efis2P")
            @PathVariable String gameId) {
        return gameApiClient.getMessages(gameId);
    }

}
