
package com.example.mugloar.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current state of a Dragons of Mugloar game")
public record GameState(

        @Schema(description = "Unique identifier assigned to the game",
                example = "S1efis2P")
        String gameId,

        @Schema(description = "Number of lives remaining", example = "3")
        int lives,

        @Schema(description = "Gold currently available", example = "0")
        int gold,

        @Schema(description = "Current game level", example = "0")
        int level,

        @Schema(description = "Current score", example = "0")
        int score,

        @Schema(description = "Highest score recorded", example = "0")
        int highScore,

        @Schema(description = "Current turn number", example = "0")
        int turn
) {}
