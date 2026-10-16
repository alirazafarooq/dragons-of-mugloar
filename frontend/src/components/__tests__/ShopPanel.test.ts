
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ShopPanel from '../ShopPanel.vue'
import type { ShopItem } from '../../types/shop'

const items: ShopItem[] = [
    { id: 'hpot', name: 'Healing potion', cost: 50 },
    { id: 'cs', name: 'Claw Sharpening', cost: 100 },
]

const defaultProps = {
    items,
    loading: false,
    error: '',
    message: '',
    buyingItemId: null,
    gold: 150,
    disabled: false,
}

describe('ShopPanel', () => {
    it('displays shop items, prices, and available gold', () => {
        const wrapper = mount(ShopPanel, {
            props: defaultProps,
        })

        expect(wrapper.text()).toContain('Dragon Shop')
        expect(wrapper.text()).toContain('150 gold available')
        expect(wrapper.text()).toContain('Healing potion')
        expect(wrapper.text()).toContain('50 gold')
        expect(wrapper.text()).toContain('Claw Sharpening')
        expect(wrapper.text()).toContain('100 gold')
    })

    it('emits buy when the player clicks Buy item', async () => {
        const wrapper = mount(ShopPanel, {
            props: defaultProps,
        })

        await wrapper
            .get('.shop-card')
            .get('button')
            .trigger('click')

        expect(wrapper.emitted('buy')).toEqual([[items[0]]])
    })

    it('disables purchase buttons when the player has insufficient gold', () => {
        const wrapper = mount(ShopPanel, {
            props: {
                ...defaultProps,
                gold: 75,
            },
        })

        const buttons = wrapper.findAll('.shop-card button')

        expect(buttons[0].attributes('disabled')).toBeUndefined()
        expect(buttons[1].attributes('disabled')).toBeDefined()
        expect(buttons[1].text()).toContain('Not enough gold')
    })

    it('disables purchase buttons when the shop is disabled', () => {
        const wrapper = mount(ShopPanel, {
            props: {
                ...defaultProps,
                disabled: true,
            },
        })

        for (const button of wrapper.findAll('.shop-card button')) {
            expect(button.attributes('disabled')).toBeDefined()
        }
    })

    it('shows purchasing feedback for the item being purchased', () => {
        const wrapper = mount(ShopPanel, {
            props: {
                ...defaultProps,
                buyingItemId: 'hpot',
            },
        })

        const buttons = wrapper.findAll('.shop-card button')

        expect(buttons[0].text()).toContain('Purchasing...')
        expect(buttons[0].attributes('disabled')).toBeDefined()
        expect(buttons[1].attributes('disabled')).toBeDefined()
    })

    it('shows a loading message while items are loading', () => {
        const wrapper = mount(ShopPanel, {
            props: {
                ...defaultProps,
                loading: true,
                items: [],
            },
        })

        expect(wrapper.text()).toContain('Loading shop items...')
    })

    it('shows an error and emits retry when retry is clicked', async () => {
        const wrapper = mount(ShopPanel, {
            props: {
                ...defaultProps,
                error: 'Could not load shop items.',
            },
        })

        expect(wrapper.text()).toContain('Could not load shop items.')

        await wrapper.get('button').trigger('click')

        expect(wrapper.emitted('retry')).toHaveLength(1)
    })

    it('shows a message after a purchase attempt', () => {
        const wrapper = mount(ShopPanel, {
            props: {
                ...defaultProps,
                message: 'Healing potion purchased successfully!',
            },
        })

        expect(wrapper.text()).toContain(
            'Healing potion purchased successfully!',
        )
    })

    it('shows a message when no items are available', () => {
        const wrapper = mount(ShopPanel, {
            props: {
                ...defaultProps,
                items: [],
            },
        })

        expect(wrapper.text()).toContain('No shop items are available.')
    })
})
