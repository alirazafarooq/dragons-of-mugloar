
<script setup lang="ts">
import type { ShopItem } from '../types/shop'

defineProps<{
  items: ShopItem[]
  loading: boolean
  error: string
  message: string
  buyingItemId: string | null
  gold: number
  disabled: boolean
}>()

const emit = defineEmits<{
  buy: [item: ShopItem]
  retry: []
}>()
</script>

<template>
  <section class="shop-section">
    <div class="section-heading">
      <div>
        <p class="eyebrow">EQUIP YOUR DRAGON</p>
        <h2>Dragon Shop</h2>
      </div>

      <span class="status-pill">
        🪙 {{ gold }} gold available
      </span>
    </div>

    <p class="shop-description">
      Purchase items to help your dragon on its adventure.
    </p>

    <p v-if="message" class="shop-feedback" role="status">
      {{ message }}
    </p>

    <section v-if="error" class="error-message" role="alert">
      <p>{{ error }}</p>
      <button
          class="secondary-button"
          :disabled="loading"
          @click="emit('retry')"
      >
        Retry
      </button>
    </section>

    <p v-else-if="loading" class="loading-message">
      Loading shop items...
    </p>

    <p v-else-if="items.length === 0" class="loading-message">
      No shop items are available.
    </p>

    <div v-else class="shop-grid">
      <article
          v-for="item in items"
          :key="item.id"
          class="shop-card"
      >
        <div class="shop-item-details">
          <h3>{{ item.name }}</h3>
          <p class="shop-item-price">🪙 {{ item.cost }} gold</p>
        </div>

        <button
            class="primary-button"
            :disabled="
            disabled ||
            buyingItemId !== null ||
            gold < item.cost
          "
            @click="emit('buy', item)"
        >
          <template v-if="buyingItemId === item.id">
            Purchasing...
          </template>
          <template v-else-if="gold < item.cost">
            Not enough gold
          </template>
          <template v-else>
            Buy item
          </template>
        </button>
      </article>
    </div>
  </section>
</template>
