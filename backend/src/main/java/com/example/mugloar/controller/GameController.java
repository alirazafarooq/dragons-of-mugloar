
package com.example.mugloar.controller;

import com.example.mugloar.model.GameState;
import com.example.mugloar.model.Mission;
import com.example.mugloar.service.GameApiClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@Validated
@Tag(
        name = "Game Management",
        description = "Endpoints for starting and retrieving game information"
)
public class GameController {

    private final GameApiClient gameApiClient;

    public GameController(GameApiClient gameApiClient) {
        this.gameApiClient = gameApiClient;
    }

    @PostMapping
    @Operation(
            summary = "Start a new game",
            description = "Starts a new game and returns its initial state."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Game started successfully"
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "External game service returned an unexpected response"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "External game service is unavailable"
            )
    })
    public GameState startGame() {
        return gameApiClient.startGame();
    }

    @GetMapping("/{gameId}/messages")
    @Operation(
            summary = "Get available missions",
            description = "Retrieves available missions for the specified game."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Missions retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Game ID has an invalid format"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Game was not found by the external service"
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "External game service returned an unexpected response"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "External game service is unavailable"
            )
    })
    public List<Mission> getMessages(
            @Parameter(
                    description = "Game ID containing 1–64 letters, digits, underscores, or hyphens",
                    example = "Pfofn50V"
            )
            @PathVariable
            @Pattern(
                    regexp = "^[A-Za-z0-9_-]{1,64}$",
                    message = "Game ID must contain only letters, digits, underscores, or hyphens and be 1–64 characters long."
            )
            String gameId
    ) {
        return gameApiClient.getMessages(gameId);
    }
}
