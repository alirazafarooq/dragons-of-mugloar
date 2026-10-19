
import { describe, expect, it } from 'vitest'
import { chooseAutoplayAction } from '../missionStrategy'
import type { GameState } from '../../types/game'
import type { Mission } from '../../types/mission'
import type { ShopItem } from '../../types/shop'

const game: GameState = {
    gameId: 'test-game',
    lives: 3,
    gold: 100,
    level: 1,
    score: 0,
    highScore: 0,
    turn: 1,
}

function makeMission(overrides: Partial<Mission> = {}): Mission {
    return {
        adId: 'mission-1',
        message: 'Test mission',
        reward: 100,
        expiresIn: 3,
        encrypted: null,
        probability: 'Sure thing',
        ...overrides,
    }
}

const potion: ShopItem = {
    id: 'hpot',
    name: 'Healing potion',
    cost: 50,
}

describe('chooseAutoplayAction', () => {
    it('continues playing when the score reaches 1000', () => {
        const action = chooseAutoplayAction(
            { ...game, score: 1000 },
            [makeMission()],
            [],
        )

        expect(action.type).toBe('solve')
    })

    it('stops when no lives remain', () => {
        const action = chooseAutoplayAction(
            { ...game, lives: 0 },
            [makeMission()],
            [potion],
        )

        expect(action.type).toBe('stop')
    })

    it('buys a healing potion when one life remains and it is affordable', () => {
        const action = chooseAutoplayAction(
            { ...game, lives: 1, gold: 50 },
            [makeMission()],
            [potion],
        )

        expect(action.type).toBe('buy')

        if (action.type === 'buy') {
            expect(action.item.id).toBe('hpot')
        }
    })

    it('chooses a safer mission over a suspicious high-reward mission', () => {
        const trap = makeMission({
            adId: 'trap',
            reward: 160,
            probability: 'Sure thing',
        })

        const safer = makeMission({
            adId: 'safer',
            reward: 80,
            probability: 'Sure thing',
        })

        const action = chooseAutoplayAction(
            game,
            [trap, safer],
            [],
        )

        expect(action.type).toBe('solve')

        if (action.type === 'solve') {
            expect(action.mission.adId).toBe('safer')
        }
    })

    it('chooses a mission even when all available missions are extremely risky', () => {
        const action = chooseAutoplayAction(
            game,
            [
                makeMission({
                    adId: 'risky-1',
                    probability: 'Impossible',
                }),
                makeMission({
                    adId: 'risky-2',
                    probability: 'Suicide mission',
                }),
            ],
            [],
        )

        expect(action.type).toBe('solve')
    })

    it('stops when there are no missions available', () => {
        expect(
            chooseAutoplayAction(game, [], []).type,
        ).toBe('stop')
    })
})
