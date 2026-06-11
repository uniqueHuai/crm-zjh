<template>
  <el-card shadow="never">
    <template #header>
      <div class="detail-card-header">
        <span>{{ title }}</span>
        <slot name="header-extra" />
      </div>
    </template>
    <div class="detail-grid" :style="{ gridTemplateColumns: columns }">
      <template v-for="item in items" :key="item.label">
        <div class="detail-item" :span="item.span || 1">
          <div class="detail-item-label">{{ item.label }}</div>
          <div class="detail-item-value">
            <slot :name="item.slot || item.field" :item="item">
              <template v-if="item.render">
                <component :is="item.render(item.value)" />
              </template>
              <template v-else-if="item.tag">
                <el-tag :type="item.tagType || 'info'" :size="item.tagSize || 'small'">
                  {{ item.value ?? '-' }}
                </el-tag>
              </template>
              <template v-else>
                {{ item.value ?? '-' }}
              </template>
            </slot>
          </div>
        </div>
      </template>
    </div>
    <slot />
  </el-card>
</template>

<script setup lang="ts">
import type { VNode } from 'vue'

export interface DetailItem {
  label: string
  field?: string
  value?: any
  span?: number
  slot?: string
  tag?: boolean
  tagType?: 'success' | 'warning' | 'danger' | 'info' | 'primary'
  tagSize?: 'small' | 'default' | 'large'
  render?: (value: any) => VNode | string | number
}

withDefaults(defineProps<{
  title: string
  items: DetailItem[]
  columns?: string
}>(), {
  columns: 'repeat(auto-fill, minmax(280px, 1fr))',
})
</script>

<style scoped lang="scss">
.detail-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: var(--crm-font-size-base);
}
.detail-grid {
  display: grid;
  gap: var(--crm-space-lg) var(--crm-space-xl);
}
.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  &-label {
    font-size: var(--crm-font-size-sm);
    color: var(--crm-text-secondary);
  }
  &-value {
    font-size: var(--crm-font-size-base);
    color: var(--crm-text-primary);
    word-break: break-all;
  }
}
</style>
