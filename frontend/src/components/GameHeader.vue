
<script setup lang="ts">
defineProps<{
  loading: boolean
}>()

const emit = defineEmits<{
  start: []
}>()
</script>

<template>
  <header class="hero">
    <div class="hero-content">
      <p class="hero-eyebrow">A DRAGON-SIZED CHALLENGE</p>

      <h1>Dragons of Mugloar</h1>

      <p class="hero-description">
        Lead your dragon, choose your missions wisely, and build your
        fortune. Every decision shapes your adventure.
      </p>

      <button
          class="primary-button start-game-button"
          type="button"
          :disabled="loading"
          @click="emit('start')"
      >
        {{ loading ? 'Starting Game...' : 'Start New Game' }}
      </button>
    </div>
  </header>
</template>
