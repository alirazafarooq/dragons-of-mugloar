
package com.example.mugloar.service;

import com.example.mugloar.model.ShopItem;
import com.example.mugloar.model.ShopPurchaseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GameApiClientShopTest {

    private MockRestServiceServer mockServer;
    private GameApiClient gameApiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        mockServer = MockRestServiceServer.bindTo(builder).build();

        gameApiClient = new GameApiClient(
                builder,
                "https://dragonsofmugloar.com/api/v2"
        );
    }

    @Test
    void getShopItemsReturnsAvailableItems() {
        mockServer.expect(
                        requestTo(
                                "https://dragonsofmugloar.com/api/v2/S1efis2P/shop"
                        )
                )
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [
                          {
                            "id": "hpot",
                            "name": "Healing potion",
                            "cost": 50
                          },
                          {
                            "id": "cs",
                            "name": "Claw Sharpening",
                            "cost": 100
                          }
                        ]
                        """, MediaType.APPLICATION_JSON));

        List<ShopItem> items =
                gameApiClient.getShopItems("S1efis2P");

        assertEquals(2, items.size());

        assertEquals("hpot", items.get(0).id());
        assertEquals("Healing potion", items.get(0).name());
        assertEquals(50, items.get(0).cost());

        assertEquals("cs", items.get(1).id());
        assertEquals("Claw Sharpening", items.get(1).name());
        assertEquals(100, items.get(1).cost());

        mockServer.verify();
    }

    @Test
    void buyItemSendsPostAndMapsSuccessfulPurchase() {
        mockServer.expect(
                        requestTo(
                                "https://dragonsofmugloar.com/api/v2/S1efis2P/shop/buy/hpot"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "shoppingSuccess": true,
                          "gold": 50,
                          "lives": 3,
                          "level": 0,
                          "turn": 1
                        }
                        """, MediaType.APPLICATION_JSON));

        ShopPurchaseResult result = gameApiClient.buyItem(
                "S1efis2P",
                "hpot"
        );

        assertTrue(result.shoppingSuccess());
        assertEquals(50, result.gold());
        assertEquals(3, result.lives());
        assertEquals(0, result.level());
        assertEquals(1, result.turn());

        mockServer.verify();
    }

    @Test
    void buyItemMapsUnsuccessfulPurchase() {
        mockServer.expect(
                        requestTo(
                                "https://dragonsofmugloar.com/api/v2/S1efis2P/shop/buy/hpot"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "shoppingSuccess": false,
                          "gold": 0,
                          "lives": 3,
                          "level": 0,
                          "turn": 1
                        }
                        """, MediaType.APPLICATION_JSON));

        ShopPurchaseResult result = gameApiClient.buyItem(
                "S1efis2P",
                "hpot"
        );

        org.junit.jupiter.api.Assertions.assertFalse(
                result.shoppingSuccess()
        );
        assertEquals(0, result.gold());
        assertEquals(3, result.lives());
        assertEquals(1, result.turn());

        mockServer.verify();
    }
}
