<template>
  <el-card shadow="hover" class="stat-card" @click="$emit('click')">
    <div class="stat-card-body">
      <div v-if="icon" class="stat-card-icon" :style="{ background: iconBg }">
        <el-icon :size="24"><component :is="icon" /></el-icon>
      </div>
      <div class="stat-card-info">
        <div class="stat-card-value" :style="{ color: valueColor }">{{ value }}</div>
        <div class="stat-card-label">{{ label }}</div>
        <div v-if="trend" class="stat-card-trend" :class="trendDir">
          <el-icon :size="12">
            <Top v-if="trendDir === 'up'" />
            <Bottom v-else-if="trendDir === 'down'" />
          </el-icon>
          {{ trend }}
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import type { Component } from 'vue'

withDefaults(defineProps<{
  value: string | number
  label: string
  icon?: string | Component
  iconBg?: string
  valueColor?: string
  trend?: string
  trendDir?: 'up' | 'down'
}>(), {
  iconBg: 'var(--crm-primary-lighter)',
  valueColor: 'var(--crm-primary)',
  trendDir: 'up' as const,
})

defineEmits<{
  click: []
}>()
</script>

<style scoped lang="scss">
.stat-card {
  cursor: pointer;
  :deep(.el-card__body) {
    padding: var(--crm-space-lg);
  }
  &-body {
    display: flex;
    align-items: center;
    gap: var(--crm-space-base);
  }
  &-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--crm-radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    color: var(--crm-primary);
  }
  &-info {
    flex: 1;
    min-width: 0;
  }
  &-value {
    font-size: 26px;
    font-weight: 700;
    line-height: 1.2;
    white-space: nowrap;
  }
  &-label {
    font-size: var(--crm-font-size-sm);
    color: var(--crm-text-secondary);
    margin-top: 2px;
  }
  &-trend {
    font-size: var(--crm-font-size-xs);
    margin-top: 4px;
    display: inline-flex;
    align-items: center;
    gap: 2px;
    &.up { color: var(--crm-success); }
    &.down { color: var(--crm-danger); }
  }
}
</style>
