
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { gameApi } from '../gameApi'
import type { GameState } from '../../types/game'

const game: GameState = {
    gameId: 'game-123',
    lives: 3,
    gold: 50,
    level: 1,
    score: 0,
    highScore: 0,
    turn: 0,
}

describe('gameApi', () => {
    const fetchMock = vi.fn()

    beforeEach(() => {
        fetchMock.mockReset()
        vi.stubGlobal('fetch', fetchMock)
    })

    afterEach(() => {
        vi.unstubAllGlobals()
    })

    it('starts a game using POST /games', async () => {
        fetchMock.mockResolvedValue({
            ok: true,
            json: async () => game,
        })

        await expect(gameApi.startGame()).resolves.toEqual(game)

        expect(fetchMock).toHaveBeenCalledWith(
            '/api/games',
            expect.objectContaining({
                method: 'POST',
            }),
        )
    })

    it('throws a useful error for a failed response', async () => {
        fetchMock.mockResolvedValue({
            ok: false,
            status: 500,
            json: async () => ({
                message: 'Something went wrong',
            }),
        })

        await expect(gameApi.startGame()).rejects.toThrow()
    })

    it('reports a backend connection failure', async () => {
        fetchMock.mockRejectedValue(new TypeError('Failed to fetch'))

        await expect(gameApi.startGame()).rejects.toThrow()
    })
})
