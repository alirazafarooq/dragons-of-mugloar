
import type { Mission } from '../types/mission'
import type { GameState } from '../types/game'
import type { ShopItem } from '../types/shop'

export type AutoplayAction =
    | { type: 'stop'; reason: string }
    | { type: 'buy'; item: ShopItem; reason: string }
    | { type: 'solve'; mission: Mission; reason: string }

const HEALING_ITEM_ID = 'hpot'
const HEALING_COST = 50

function missionRisk(mission: Mission): number {
    const probability = mission.probability.trim().toLowerCase()

    const knownRisk: Record<string, number> = {
        'sure thing': 1,
        'piece of cake': 2,
        'walk in the park': 2,
        'quite likely': 3,
        likely: 3,
        gamble: 5,
        'playing with fire': 7,
        risky: 7,
        impossible: 10,
        'suicide mission': 10,
    }

    let risk = knownRisk[probability] ?? 5

    // Treat suspiciously high rewards cautiously.
    if (probability === 'sure thing' && mission.reward >= 130) {
        risk = Math.max(risk, 9)
    }

    if (probability === 'piece of cake' && mission.reward >= 150) {
        risk = Math.max(risk, 8)
    }

    return risk
}

export function chooseAutoplayAction(
    game: GameState,
    missions: Mission[],
    shopItems: ShopItem[],
): AutoplayAction {
    if (game.lives <= 0) {
        return {
            type: 'stop',
            reason: 'No lives remaining.',
        }
    }

    // Prefer healing when only one life remains and the potion is affordable.
    if (game.lives === 1 && game.gold >= HEALING_COST) {
        const healingPotion = shopItems.find(
            item => item.id === HEALING_ITEM_ID,
        )

        if (
            healingPotion &&
            healingPotion.cost <= game.gold
        ) {
            return {
                type: 'buy',
                item: healingPotion,
                reason: 'Only one life remains; attempting to heal before the next mission.',
            }
        }
    }

    if (missions.length === 0) {
        return {
            type: 'stop',
            reason: 'No missions are currently available.',
        }
    }

    const ranked = missions
        .map(mission => ({
            mission,
            risk: missionRisk(mission),
        }))
        .sort((a, b) => {
            // Prefer lower risk first.
            if (a.risk !== b.risk) {
                return a.risk - b.risk
            }

            // For equal risk, prefer the higher reward.
            return b.mission.reward - a.mission.reward
        })

    // Always choose the best available mission, even if all are risky.
    const selected = ranked[0]

    return {
        type: 'solve',
        mission: selected.mission,
        reason: `Selected the best available mission (risk score ${selected.risk}/10).`,
    }
}
