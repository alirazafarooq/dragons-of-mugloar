
import { computed, ref, watch } from 'vue'
import { gameApi } from '../services/gameApi'
import type { GameState } from '../types/game'
import type { Mission, MissionSolveResult } from '../types/mission'

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
    const loading = ref(false)
    const missionsLoading = ref(false)
    const error = ref('')
    const missionsError = ref('')
    const solvingMissionId = ref<string | null>(null)
    const missionOutcome = ref<MissionSolveResult | null>(null)
    const missionActionError = ref('')

    const isGameOver = computed(
        () => game.value !== null && game.value.lives <= 0,
    )

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

    async function startGame(): Promise<void> {
        loading.value = true
        error.value = ''
        missionsError.value = ''
        missionActionError.value = ''
        missionOutcome.value = null
        missions.value = []
        game.value = null

        try {
            const newGame = await gameApi.startGame()
            game.value = newGame

            if (newGame.lives > 0) {
                await loadMissions(newGame.gameId)
            }
        } catch (cause) {
            error.value =
                cause instanceof Error ? cause.message : 'Could not start a game.'
        } finally {
            loading.value = false
        }
    }

    async function solveMission(mission: Mission): Promise<void> {
        if (!game.value || game.value.lives <= 0 || solvingMissionId.value) {
            return
        }

        const gameId = game.value.gameId

        solvingMissionId.value = mission.adId
        missionOutcome.value = null
        missionActionError.value = ''

        try {
            const result: MissionSolveResult = await gameApi.solveMission(
                gameId,
                mission.adId,
            )

            if (!game.value || game.value.gameId !== gameId) return

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
                await loadMissions(gameId)
            } else {
                missions.value = []
                missionsError.value = ''
            }
        } catch (cause) {
            missionActionError.value =
                cause instanceof Error
                    ? cause.message
                    : 'Could not confirm the mission attempt.'
        } finally {
            solvingMissionId.value = null
        }
    }

    async function resumeGame(): Promise<void> {
        if (game.value && game.value.lives > 0) {
            await loadMissions(game.value.gameId)
        } else {
            missions.value = []
        }
    }

    function retryMissions(): void {
        if (game.value && !missionsLoading.value && game.value.lives > 0) {
            void loadMissions(game.value.gameId)
        }
    }

    function clearError(): void {
        error.value = ''
    }

    function endGame(): void {
        game.value = null
        missions.value = []
        error.value = ''
        missionsError.value = ''
        missionOutcome.value = null
        missionActionError.value = ''
    }

    return {
        game,
        missions,
        loading,
        missionsLoading,
        error,
        missionsError,
        solvingMissionId,
        missionOutcome,
        missionActionError,
        isGameOver,
        startGame,
        solveMission,
        resumeGame,
        retryMissions,
        clearError,
        endGame,
    }
}
