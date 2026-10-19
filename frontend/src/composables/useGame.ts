
import { computed, ref, watch } from 'vue'
import { gameApi } from '../services/gameApi'
import type { GameState } from '../types/game'
import type { Mission, MissionSolveResult } from '../types/mission'
import type { ShopItem, ShopPurchaseResult } from '../types/shop'
import { chooseAutoplayAction } from '../services/missionStrategy'

const STORAGE_KEY = 'mugloar-game'

function readSavedGame(): GameState | null {
    try {
        const saved = localStorage.getItem(STORAGE_KEY)
        if (!saved) return null

        const value: unknown = JSON.parse(saved)
        if (!value || typeof value !== 'object') return null

        const game = value as Partial<GameState>

        if (
            typeof game.gameId !== 'string' ||
            typeof game.lives !== 'number' ||
            typeof game.gold !== 'number' ||
            typeof game.level !== 'number' ||
            typeof game.score !== 'number' ||
            typeof game.highScore !== 'number' ||
            typeof game.turn !== 'number'
        ) {
            return null
        }

        return game as GameState
    } catch {
        return null
    }
}

export function useGame() {
    const game = ref<GameState | null>(readSavedGame())
    const missions = ref<Mission[]>([])
    const shopItems = ref<ShopItem[]>([])

    const loading = ref(false)
    const missionsLoading = ref(false)
    const shopLoading = ref(false)
    const buyingItemId = ref<string | null>(null)

    const error = ref('')
    const missionsError = ref('')
    const shopError = ref('')
    const shopMessage = ref('')

    const solvingMissionId = ref<string | null>(null)
    const missionOutcome = ref<MissionSolveResult | null>(null)
    const missionActionError = ref('')

    const isGameOver = computed(
        () => game.value !== null && game.value.lives <= 0)

    const autoplayRunning = ref(false)
    const autoplayStatus = ref('Autoplay is off.')

    watch(
        game,
        (value) => {
            if (value) {
                localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
            } else {
                localStorage.removeItem(STORAGE_KEY)
            }
        },
        { deep: true },
    )

    async function loadMissions(gameId: string): Promise<void> {
        if (!game.value || game.value.gameId !== gameId || game.value.lives <= 0) {
            missions.value = []
            return
        }

        missionsLoading.value = true
        missionsError.value = ''

        try {
            missions.value = await gameApi.getMissions(gameId)
        } catch (cause) {
            missionsError.value =
                cause instanceof Error ? cause.message : 'Could not load missions.'
        } finally {
            missionsLoading.value = false
        }
    }

    async function loadShopItems(gameId: string): Promise<void> {
        if (!game.value || game.value.gameId !== gameId || game.value.lives <= 0) {
            shopItems.value = []
            return
        }

        shopLoading.value = true
        shopError.value = ''

        try {
            shopItems.value = await gameApi.getShopItems(gameId)
        } catch (cause) {
            shopError.value =
                cause instanceof Error ? cause.message : 'Could not load shop items.'
        } finally {
            shopLoading.value = false
        }
    }

    async function startGame(): Promise<void> {
        loading.value = true
        error.value = ''
        missionsError.value = ''
        shopError.value = ''
        shopMessage.value = ''
        missionActionError.value = ''
        missionOutcome.value = null
        missions.value = []
        shopItems.value = []
        game.value = null

        try {
            const newGame = await gameApi.startGame()
            game.value = newGame

            if (newGame.lives > 0) {
                await Promise.all([
                    loadMissions(newGame.gameId),
                    loadShopItems(newGame.gameId),
                ])
            }
        } catch (cause) {
            error.value =
                cause instanceof Error ? cause.message : 'Could not start a game.'
        } finally {
            loading.value = false
        }
    }

    async function solveMission(mission: Mission): Promise<boolean> {
        if (!game.value || game.value.lives <= 0 || solvingMissionId.value) {
            return false
        }

        const gameId = game.value.gameId

        solvingMissionId.value = mission.adId
        missionOutcome.value = null
        missionActionError.value = ''
        shopMessage.value = ''

        try {
            const result: MissionSolveResult = await gameApi.solveMission(
                gameId,
                mission.adId,
            )

            if (!game.value || game.value.gameId !== gameId) return false

            game.value = {
                ...game.value,
                lives: result.lives,
                gold: result.gold,
                score: result.score,
                highScore: result.highScore,
                turn: result.turn,
            }

            missionOutcome.value = result

            if (result.lives > 0) {
                await Promise.all([
                    loadMissions(gameId),
                    loadShopItems(gameId),
                ])
            } else {
                missions.value = []
                shopItems.value = []
                missionsError.value = ''
                shopError.value = ''
            }
            return true
        } catch (cause) {
            missionActionError.value =
                cause instanceof Error
                    ? cause.message
                    : 'Could not confirm the mission attempt.'

            return true

        } finally {
            solvingMissionId.value = null
        }
    }

    async function buyItem(item: ShopItem): Promise<boolean> {
        if (
            !game.value ||
            game.value.lives <= 0 ||
            buyingItemId.value ||
            solvingMissionId.value
        ) {
            return false
        }

        if (game.value.gold < item.cost) {
            shopMessage.value = 'You do not have enough gold to buy this item.'
            return false
        }

        const gameId = game.value.gameId

        buyingItemId.value = item.id
        shopError.value = ''
        shopMessage.value = ''

        try {
            const result: ShopPurchaseResult = await gameApi.buyItem(
                gameId,
                item.id,
            )

            if (!game.value || game.value.gameId !== gameId) return false

            game.value = {
                ...game.value,
                gold: result.gold,
                lives: result.lives,
                level: result.level,
                turn: result.turn,
            }

            if (result.shoppingSuccess) {
                shopMessage.value = `${item.name} purchased successfully.`
            } else {
                shopMessage.value = `Could not purchase ${item.name}. Check your gold and try again.`
            }

            if (result.lives <= 0) {
                missions.value = []
                shopItems.value = []
                missionsError.value = ''
                shopError.value = ''
            } else {
                await Promise.all([
                    loadMissions(gameId),
                    loadShopItems(gameId),
                ])
            }

            return true

        } catch (cause) {
            shopError.value =
                cause instanceof Error
                    ? cause.message
                    : 'Could not confirm the purchase.'

            return false

        } finally {
            buyingItemId.value = null
        }
    }


    function stopAutoplay(reason: string): void {
        autoplayRunning.value = false
        autoplayStatus.value = reason
    }

    function pauseAutoplay(): void {
        if (!autoplayRunning.value) return

        stopAutoplay('Autoplay paused.')
    }

    function takeManualControl(): void {
        stopAutoplay('Manual control selected.')
    }

    async function startAutoplay(): Promise<void> {
        if (
            autoplayRunning.value ||
            !game.value ||
            game.value.lives <= 0
        ) {
            return
        }

        if (
            solvingMissionId.value ||
            buyingItemId.value ||
            loading.value
        ) {
            autoplayStatus.value =
                'Wait for the current action to finish before starting autoplay.'
            return
        }

        autoplayRunning.value = true
        autoplayStatus.value = 'Autoplay is running.'

        while (autoplayRunning.value) {
            const currentGame = game.value

            if (!currentGame || currentGame.lives <= 0) {
                stopAutoplay('Game over: no lives remaining.')
                break
            }

            if (missionActionError.value || shopError.value) {
                stopAutoplay(
                    'Autoplay stopped because an action could not be confirmed.',
                )
                break
            }

            if (missionsError.value || missionsLoading.value || shopLoading.value) {
                stopAutoplay(
                    'Autoplay stopped because missions or shop data could not be verified.',
                )
                break
            }

            const action = chooseAutoplayAction(
                currentGame,
                missions.value,
                shopItems.value,
            )

            if (action.type === 'stop') {
                stopAutoplay(action.reason)
                break
            }

            const previousTurn = currentGame.turn
            const previousLives = currentGame.lives

            if (action.type === 'solve') {
                const confirmed = await solveMission(action.mission)

                if (!autoplayRunning.value) break

                if (!confirmed) {
                    stopAutoplay(
                        'Autoplay stopped because the mission action failed or could not be confirmed.',
                    )
                    break
                }

                if (
                    missionActionError.value ||
                    missionsError.value ||
                    shopError.value
                ) {
                    stopAutoplay(
                        'Autoplay stopped because the latest game state could not be verified.',
                    )
                    break
                }

                if (
                    !game.value ||
                    game.value.turn <= previousTurn ||
                    game.value.lives < 0
                ) {
                    stopAutoplay(
                        'Autoplay stopped because the game state did not advance as expected.',
                    )
                    break
                }
            }

            if (action.type === 'buy') {
                const confirmed = await buyItem(action.item)

                if (!autoplayRunning.value) break

                if (!confirmed) {
                    stopAutoplay(
                        'Autoplay stopped because the purchase failed.',
                    )
                    break
                }

                if (
                    shopError.value ||
                    missionsError.value
                ) {
                    stopAutoplay(
                        'Autoplay stopped because the latest game state could not be verified.',
                    )
                    break
                }

                if (!game.value || game.value.turn <= previousTurn) {
                    stopAutoplay(
                        'Autoplay stopped because the purchase did not advance the game state.',
                    )
                    break
                }

                if (game.value.lives <= previousLives) {
                    stopAutoplay(
                        'Autoplay stopped because the healing purchase did not restore a life.',
                    )
                    break
                }
            }
        }
    }

    async function resumeGame(): Promise<void> {
        if (game.value && game.value.lives > 0) {
            await Promise.all([
                loadMissions(game.value.gameId),
                loadShopItems(game.value.gameId),
            ])
        } else {
            missions.value = []
            shopItems.value = []
        }
    }

    function retryMissions(): void {
        if (game.value && !missionsLoading.value && game.value.lives > 0) {
            void loadMissions(game.value.gameId)
        }
    }

    function retryShop(): void {
        if (game.value && !shopLoading.value && game.value.lives > 0) {
            void loadShopItems(game.value.gameId)
        }
    }

    function clearError(): void {
        error.value = ''
    }

    function endGame(): void {
        game.value = null
        missions.value = []
        shopItems.value = []
        error.value = ''
        missionsError.value = ''
        shopError.value = ''
        shopMessage.value = ''
        missionOutcome.value = null
        missionActionError.value = ''
    }

    return {
        game,
        missions,
        shopItems,
        loading,
        missionsLoading,
        shopLoading,
        buyingItemId,
        error,
        missionsError,
        shopError,
        shopMessage,
        solvingMissionId,
        missionOutcome,
        missionActionError,
        isGameOver,
        startGame,
        solveMission,
        buyItem,
        resumeGame,
        retryMissions,
        retryShop,
        clearError,
        endGame,
        autoplayRunning,
        autoplayStatus,
        startAutoplay,
        pauseAutoplay,
        takeManualControl,
    }
}
