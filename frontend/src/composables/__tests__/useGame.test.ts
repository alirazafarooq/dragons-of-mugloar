import { beforeEach, describe, expect, it, vi } from 'vitest'
import { gameApi } from '../../services/gameApi'
import type { GameState } from '../../types/game'
import type { Mission } from '../../types/mission'
import type { ShopItem, ShopPurchaseResult } from '../../types/shop'
import { useGame } from '../useGame'

vi.mock('../../services/gameApi', () => ({
    gameApi: {
        startGame: vi.fn(),
        getMissions: vi.fn(),
        getShopItems: vi.fn(),
        solveMission: vi.fn(),
        buyItem: vi.fn(),
    },
}))

const initialGame: GameState = {
    gameId: 'game-123',
    lives: 3,
    gold: 200,
    level: 0,
    score: 0,
    highScore: 0,
    turn: 0,
}

const potion: ShopItem = {
    id: 'hpot',
    name: 'Healing potion',
    cost: 50,
}

const mission: Mission = {
    adId: 'mission-123',
    message: 'Help the village defend its treasure.',
    reward: 100,
    expiresIn: 3,
    encrypted: null,
    probability: 'Sure thing',
}

const successfulPurchase: ShopPurchaseResult = {
    shoppingSuccess: true,
    gold: 150,
    lives: 3,
    level: 0,
    turn: 1,
}

describe('useGame shop functionality', () => {
    beforeEach(() => {
        localStorage.clear()
        vi.resetAllMocks()

        // Return fresh objects so tests cannot mutate shared fixtures.
        vi.mocked(gameApi.startGame).mockResolvedValue({
            ...initialGame,
        })
        vi.mocked(gameApi.getMissions).mockResolvedValue([{ ...mission }])
        vi.mocked(gameApi.getShopItems).mockResolvedValue([{ ...potion }])
        vi.mocked(gameApi.buyItem).mockResolvedValue({
            ...successfulPurchase,
        })
    })

    async function startTestGame() {
        const game = useGame()
        await game.startGame()
        return game
    }

    it('loads shop items when a game starts', async () => {
        const game = await startTestGame()

        expect(game.game.value).toEqual(initialGame)
        expect(game.shopItems.value).toEqual([potion])
        expect(gameApi.getShopItems).toHaveBeenCalledWith('game-123')
        expect(game.shopLoading.value).toBe(false)
    })

    it('updates game state after a successful purchase', async () => {
        const game = await startTestGame()

        await game.buyItem(potion)

        expect(gameApi.buyItem).toHaveBeenCalledWith('game-123', 'hpot')
        expect(game.game.value).toMatchObject({
            gameId: 'game-123',
            gold: 150,
            lives: 3,
            level: 0,
            turn: 1,
            score: 0,
            highScore: 0,
        })
        expect(game.shopMessage.value).toBe(
            'Healing potion purchased successfully.',
        )
        expect(game.buyingItemId.value).toBeNull()
    })

    it('refreshes missions and shop items after a purchase', async () => {
        const game = await startTestGame()

        vi.mocked(gameApi.getMissions).mockClear()
        vi.mocked(gameApi.getShopItems).mockClear()

        await game.buyItem(potion)

        expect(gameApi.getMissions).toHaveBeenCalledWith('game-123')
        expect(gameApi.getShopItems).toHaveBeenCalledWith('game-123')
        expect(game.missions.value).toEqual([mission])
        expect(game.shopItems.value).toEqual([potion])
    })

    it('shows a message when a purchase is unsuccessful', async () => {
        const game = await startTestGame()

        const failedPurchase: ShopPurchaseResult = {
            shoppingSuccess: false,
            gold: 200,
            lives: 3,
            level: 0,
            turn: 1,
        }

        vi.mocked(gameApi.buyItem).mockResolvedValueOnce(failedPurchase)

        await game.buyItem(potion)

        expect(game.game.value?.gold).toBe(200)
        expect(game.game.value?.turn).toBe(1)
        expect(game.shopMessage.value).toBe(
            'Could not purchase Healing potion. Check your gold and try again.',
        )
    })

    it('does not call the API when the player has insufficient gold', async () => {
        const game = await startTestGame()

        game.game.value!.gold = 20

        await game.buyItem(potion)

        expect(gameApi.buyItem).not.toHaveBeenCalled()
        expect(game.shopMessage.value).toBe(
            'You do not have enough gold to buy this item.',
        )
        expect(game.game.value?.gold).toBe(20)
    })

    it('shows an error when the purchase request fails', async () => {
        const game = await startTestGame()

        vi.mocked(gameApi.buyItem).mockRejectedValueOnce(
            new Error('Purchase request failed'),
        )

        await game.buyItem(potion)

        expect(gameApi.buyItem).toHaveBeenCalledWith('game-123', 'hpot')
        expect(game.shopError.value).toBe('Purchase request failed')
        expect(game.buyingItemId.value).toBeNull()
    })

    it('does not purchase when no game is active', async () => {
        const game = useGame()

        await game.buyItem(potion)

        expect(gameApi.buyItem).not.toHaveBeenCalled()
    })

    it('does not purchase while a mission is being solved', async () => {
        const game = await startTestGame()

        game.solvingMissionId.value = mission.adId

        await game.buyItem(potion)

        expect(gameApi.buyItem).not.toHaveBeenCalled()
    })

    it('clears shop items when a purchase leaves the game over', async () => {
        const game = await startTestGame()

        vi.mocked(gameApi.buyItem).mockResolvedValueOnce({
            shoppingSuccess: false,
            gold: 0,
            lives: 0,
            level: 0,
            turn: 1,
        })

        await game.buyItem(potion)

        expect(gameApi.buyItem).toHaveBeenCalledWith('game-123', 'hpot')
        expect(game.game.value?.lives).toBe(0)
        expect(game.shopItems.value).toEqual([])
        expect(game.missions.value).toEqual([])
        expect(game.isGameOver.value).toBe(true)
        expect(game.buyingItemId.value).toBeNull()
    })
})