
<script setup lang="ts">
import type { Mission } from '../types/mission'

defineProps<{
  mission: Mission
  solving: boolean
  disabled: boolean
}>()

const emit = defineEmits<{
  solve: [adId: string]
}>()
</script>

<template>
  <article class="mission-card">
    <div class="mission-card-header">
      <span class="mission-label">MISSION</span>

      <span v-if="mission.encrypted" class="encrypted-badge">
        Encrypted
      </span>
    </div>

    <p class="mission-message">
      {{ mission.message }}
    </p>

    <div class="mission-details">
      <div class="mission-detail">
        <span class="detail-label">Gold reward</span>
        <strong>🪙 {{ mission.reward }} gold</strong>
      </div>

      <div class="mission-detail">
        <span class="detail-label">Expires in</span>
        <strong>{{ mission.expiresIn }} turns</strong>
      </div>

      <div class="mission-detail">
        <span class="detail-label">Estimated probability</span>
        <strong>{{ mission.probability }}</strong>
      </div>
    </div>

    <p class="mission-id">
      Mission ID: {{ mission.adId }}
    </p>

    <button
        class="primary-button"
        :disabled="disabled || solving"
        @click="emit('solve', mission.adId)"
    >
      {{ solving ? 'Attempting...' : 'Attempt Mission' }}
    </button>
  </article>
</template>
