<template>
  <div class="page-container" :class="{ 'page-container--fluid': fluid }">
    <div v-if="title || $slots.header" class="page-header">
      <div class="page-header-left">
        <h3 v-if="title" class="page-title">{{ title }}</h3>
        <span v-if="subtitle" class="page-subtitle">{{ subtitle }}</span>
        <slot name="header" />
      </div>
      <div class="page-header-right">
        <slot name="action" />
      </div>
    </div>
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  subtitle?: string
  fluid?: boolean
}>()
</script>

<style scoped lang="scss">
.page-container {
  padding: var(--crm-space-lg);
  background: var(--crm-bg-white);
  border-radius: var(--crm-radius-lg);
  box-shadow: var(--crm-shadow-sm);

  &--fluid {
    padding: 0;
    background: transparent;
    box-shadow: none;
  }
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--crm-space-lg);
  gap: var(--crm-space-base);
}
.page-header-left {
  flex: 1;
  min-width: 0;
}
.page-title {
  margin: 0;
  font-size: var(--crm-font-size-xl);
  font-weight: 600;
  color: var(--crm-text-primary);
  line-height: 1.4;
}
.page-subtitle {
  font-size: var(--crm-font-size-sm);
  color: var(--crm-text-secondary);
  margin-top: 2px;
  display: inline-block;
}
.page-header-right {
  display: flex;
  gap: var(--crm-space-sm);
  flex-shrink: 0;
  align-items: center;
}
</style>
