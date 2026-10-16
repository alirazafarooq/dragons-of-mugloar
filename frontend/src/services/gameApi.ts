
import type { GameState } from '../types/game'
import type { Mission, MissionSolveResult } from '../types/mission'

const API_BASE_URL = '/api'

function isRecord(value: unknown): value is Record<string, unknown> {
    return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function isGameState(value: unknown): value is GameState {
    if (!isRecord(value)) return false

    return (
        typeof value.gameId === 'string' &&
        typeof value.lives === 'number' &&
        typeof value.gold === 'number' &&
        typeof value.level === 'number' &&
        typeof value.score === 'number' &&
        typeof value.highScore === 'number' &&
        typeof value.turn === 'number'
    )
}

function isMission(value: unknown): value is Mission {
    if (!isRecord(value)) return false

    return (
        typeof value.adId === 'string' &&
        typeof value.message === 'string' &&
        typeof value.reward === 'number' &&
        typeof value.expiresIn === 'number' &&
        (typeof value.encrypted === 'string' || value.encrypted === null) &&
        typeof value.probability === 'string'
    )
}

function isSolveResult(value: unknown): value is MissionSolveResult {
    if (!isRecord(value)) return false

    return (
        typeof value.success === 'boolean' &&
        typeof value.message === 'string' &&
        typeof value.lives === 'number' &&
        typeof value.gold === 'number' &&
        typeof value.score === 'number' &&
        typeof value.highScore === 'number' &&
        typeof value.turn === 'number'
    )
}

async function request<T>(
    path: string,
    options: RequestInit = {},
    validate?: (value: unknown) => value is T,
): Promise<T> {
    let response: Response

    try {
        response = await fetch(`${API_BASE_URL}${path}`, options)
    } catch {
        throw new Error(
            'Cannot connect to the backend. Make sure Spring Boot is running.',
        )
    }

    if (!response.ok) {
        let message = `Request failed with HTTP ${response.status}`

        try {
            const body: unknown = await response.json()
            if (isRecord(body) && typeof body.message === 'string') {
                message += `: ${body.message}`
            }
        } catch {
            // Keep the fallback message.
        }

        throw new Error(message)
    }

    const data: unknown = await response.json()

    if (validate && !validate(data)) {
        throw new Error('The backend returned an invalid response.')
    }

    return data as T
}

export const gameApi = {
    startGame(): Promise<GameState> {
        return request('/games', { method: 'POST' }, isGameState)
    },

    getMissions(gameId: string): Promise<Mission[]> {
        return request(
            `/games/${encodeURIComponent(gameId)}/messages`,
            {},
            (value): value is Mission[] =>
                Array.isArray(value) && value.every(isMission),
        )
    },

    solveMission(gameId: string, adId: string): Promise<MissionSolveResult> {
        return request(
            `/games/${encodeURIComponent(gameId)}/solve/${encodeURIComponent(adId)}`,
            { method: 'POST' },
            isSolveResult,
        )
    },
}
