
package com.example.mugloar.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of attempting a mission")
public record MissionSolveResult(

        @Schema(
                description = "Whether the mission was solved successfully",
                example = "true"
        )
        Boolean success,

        @Schema(
                description = "Message describing the mission outcome",
                example = "You successfully solved the mission!"
        )
        String message,

        @Schema(description = "Lives remaining after the attempt", example = "3")
        Integer lives,

        @Schema(description = "Gold after the attempt", example = "13")
        Integer gold,

        @Schema(description = "Score after the attempt", example = "13")
        Integer score,

        @Schema(description = "High score after the attempt", example = "0")
        Integer highScore,

        @Schema(description = "Current game turn", example = "1")
        Integer turn
) {}
