
package com.example.mugloar.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A mission available in the current game")
public record Mission(

        @Schema(description = "Unique identifier used to select a mission",
                example = "N0SjIYKD")
        String adId,

        @Schema(description = "Description of the mission",
                example = "Steal sheep delivery and share some profits with the people")
        String message,

        @Schema(description = "Gold reward for completing the mission",
                example = "64")
        int reward,

        @Schema(description = "Number of turns before the mission expires",
                example = "7")
        int expiresIn,

        @Schema(description = "Encrypted mission message, if present",
                example = "null", nullable = true)
        String encrypted,

        @Schema(description = "Estimated mission difficulty",
                example = "Walk in the park")
        String probability
) {}
