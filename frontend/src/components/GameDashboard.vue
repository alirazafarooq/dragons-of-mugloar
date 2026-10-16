
<script setup lang="ts">
import type { GameState } from '../types/game'

defineProps<{
  game: GameState
}>()

const emit = defineEmits<{
  end: []
}>()
</script>

<template>
  <section class="dashboard-section">
    <div class="section-heading">
      <div>
        <p class="eyebrow">YOUR ADVENTURE</p>
        <h2>Game Dashboard</h2>
      </div>

      <span
          class="status-pill"
          :class="{ 'status-game-over': game.lives <= 0 }"
      >
        {{ game.lives <= 0 ? 'Game over' : 'In progress' }}
      </span>
    </div>

    <div class="stats-grid">
      <article class="stat-card">
        <span class="stat-icon">❤️</span>
        <span class="stat-label">Lives</span>
        <strong class="stat-value">{{ game.lives }}</strong>
      </article>

      <article class="stat-card">
        <span class="stat-icon">🪙</span>
        <span class="stat-label">Gold</span>
        <strong class="stat-value">{{ game.gold }}</strong>
      </article>

      <article class="stat-card">
        <span class="stat-icon">🏆</span>
        <span class="stat-label">Score</span>
        <strong class="stat-value">{{ game.score }}</strong>
      </article>

      <article class="stat-card">
        <span class="stat-icon">📈</span>
        <span class="stat-label">High score</span>
        <strong class="stat-value">{{ game.highScore }}</strong>
      </article>

      <article class="stat-card">
        <span class="stat-icon">🐉</span>
        <span class="stat-label">Level</span>
        <strong class="stat-value">{{ game.level }}</strong>
      </article>

      <article class="stat-card">
        <span class="stat-icon">⚔️</span>
        <span class="stat-label">Turn</span>
        <strong class="stat-value">{{ game.turn }}</strong>
      </article>
    </div>

    <div class="dashboard-footer">
      <p class="game-id">
        Game ID: {{ game.gameId }}
      </p>

      <button class="secondary-button" @click="emit('end')">
        End Game
      </button>
    </div>
  </section>
</template>
