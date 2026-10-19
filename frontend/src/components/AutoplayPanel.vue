<script setup lang="ts">
defineProps<{
  running: boolean
  status: string
  disabled: boolean
}>()

defineEmits<{
  start: []
  pause: []
  manual: []
}>()
</script>

<template>
  <section class="autoplay-panel">
    <div class="autoplay-heading">
      <div>
        <p class="eyebrow">GAME ASSISTANT</p>
        <h2>Autoplay</h2>
      </div>

      <span
          class="autoplay-indicator"
          :class="{ active: running }"
      >
        {{ running ? 'RUNNING' : 'STOPPED' }}
      </span>
    </div>

    <p class="autoplay-description">
      Automatically choose missions using the current risk strategy
      and purchase a healing potion when appropriate.
    </p>

    <p class="autoplay-status" role="status">
      {{ status }}
    </p>

    <div class="autoplay-actions">
      <button
          class="autoplay-button primary"
          :disabled="disabled || running"
          @click="$emit('start')"
      >
        Start autoplay
      </button>

      <button
          class="autoplay-button"
          :disabled="!running"
          @click="$emit('pause')"
      >
        Pause
      </button>

      <button
          class="autoplay-button"
          @click="$emit('manual')"
      >
        Manual control
      </button>
    </div>
  </section>
</template>