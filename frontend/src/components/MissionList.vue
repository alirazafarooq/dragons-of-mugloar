
<script setup lang="ts">
import type { Mission } from '../types/mission'
import MissionCard from './MissionCard.vue'

defineProps<{
  missions: Mission[]
  loading: boolean
  error: string | null
  gameOver: boolean
  solvingMissionId: string | null
}>()

const emit = defineEmits<{
  solve: [adId: string]
  retry: []
}>()
</script>

<template>
  <section class="missions-section">
    <div class="section-heading">
      <div>
        <p class="eyebrow">CHOOSE YOUR CHALLENGE</p>
        <h2>Available Missions</h2>
      </div>
    </div>

    <div v-if="loading" class="loading-message">
      Loading missions...
    </div>

    <div v-else-if="error" class="error-message">
      <p>{{ error }}</p>
      <button class="secondary-button" @click="emit('retry')">
        Try Again
      </button>
    </div>

    <div v-else-if="gameOver" class="empty-message">
      Game over! You have no lives remaining.
    </div>

    <div v-else-if="missions.length === 0" class="empty-message">
      No missions are currently available.
    </div>

    <div v-else class="missions-grid">
      <MissionCard
          v-for="mission in missions"
          :key="mission.adId"
          :mission="mission"
          :solving="solvingMissionId === mission.adId"
          :disabled="gameOver"
          @solve="emit('solve', $event)"
      />
    </div>
  </section>
</template>
