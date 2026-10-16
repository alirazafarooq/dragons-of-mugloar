package com.example.mugloar.model;

public record ShopPurchaseResult(
        boolean shoppingSuccess,
        int gold,
        int lives,
        int level,
        int turn
) {
}