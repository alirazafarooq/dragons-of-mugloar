
<script setup lang="ts">
defineProps<{
  success: boolean
  message: string
}>()
</script>

<template>
  <p
      class="mission-outcome"
      :class="{
      'mission-outcome--success': success,
      'mission-outcome--failure': !success,
    }"
      role="status"
  >
    {{ message }}
  </p>
</template>
