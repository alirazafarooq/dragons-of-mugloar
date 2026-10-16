
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import MissionCard from '../MissionCard.vue'
import type { Mission } from '../../types/mission'

const mission: Mission = {
    adId: 'mission-123',
    message: 'Help the village defend its treasure.',
    reward: 100,
    expiresIn: 3,
    encrypted: null,
    probability: 'Sure thing',
}

describe('MissionCard', () => {
    it('displays mission details', () => {
        const wrapper = mount(MissionCard, {
            props: {
                mission,
                solving: false,
                disabled: false,
            },
        })

        expect(wrapper.text()).toContain(mission.message)
        expect(wrapper.text()).toContain('100 gold')
        expect(wrapper.text()).toContain('Sure thing')
        expect(wrapper.text()).toContain('mission-123')
    })

    it('emits solve when the player attempts a mission', async () => {
        const wrapper = mount(MissionCard, {
            props: {
                mission,
                solving: false,
                disabled: false,
            },
        })

        await wrapper.get('button').trigger('click')

        expect(wrapper.emitted('solve')).toEqual([['mission-123']])
    })

    it('disables the button when a mission is being solved', () => {
        const wrapper = mount(MissionCard, {
            props: {
                mission,
                solving: true,
                disabled: false,
            },
        })

        expect(wrapper.get('button').attributes('disabled')).toBeDefined()
        expect(wrapper.text()).toContain('Attempting...')
    })

    it('does not allow attempts when disabled', async () => {
        const wrapper = mount(MissionCard, {
            props: {
                mission,
                solving: false,
                disabled: true,
            },
        })

        expect(wrapper.get('button').attributes('disabled')).toBeDefined()

        await wrapper.get('button').trigger('click')

        expect(wrapper.emitted('solve')).toBeUndefined()
    })
})
