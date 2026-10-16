
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import GameDashboard from '../GameDashboard.vue'
import type { GameState } from '../../types/game'

const game: GameState = {
    gameId: 'game-123',
    lives: 3,
    gold: 50,
    level: 2,
    score: 150,
    highScore: 200,
    turn: 4,
}

describe('GameDashboard', () => {
    it('renders all game statistics', () => {
        const wrapper = mount(GameDashboard, {
            props: { game },
        })

        expect(wrapper.text()).toContain('Lives')
        expect(wrapper.text()).toContain('Gold')
        expect(wrapper.text()).toContain('Score')
        expect(wrapper.text()).toContain('High score')
        expect(wrapper.text()).toContain('Level')
        expect(wrapper.text()).toContain('Turn')

        expect(wrapper.text()).toContain('150')
        expect(wrapper.text()).toContain('50')
        expect(wrapper.text()).toContain('200')
    })
})
