<template>
  <div class="crud-table">
    <!-- Search Form -->
    <el-collapse-transition>
      <div v-if="showSearch" class="crud-search">
        <el-form :model="searchData" :inline="true" size="default" @keyup.enter="handleSearch">
          <slot name="search" :form="searchData" />
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-collapse-transition>

    <!-- Toolbar -->
    <div class="crud-toolbar">
      <div class="crud-toolbar-left">
        <slot name="toolbar-left" />
      </div>
      <div class="crud-toolbar-right">
        <slot name="toolbar-right" />
        <el-button
          v-if="$slots.search"
          :text="true"
          @click="showSearch = !showSearch"
        >
          <el-icon><Search /></el-icon>
          {{ showSearch ? '隐藏搜索' : '搜索' }}
        </el-button>
        <el-tooltip content="刷新">
          <el-button :text="true" @click="emit('refresh')">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- Table -->
    <el-table
      :data="data"
      :border="border"
      :stripe="stripe"
      :loading="loading"
      :max-height="maxHeight"
      :size="size"
      :highlight-current-row="true"
      @selection-change="emit('selection-change', $event)"
      v-bind="$attrs"
    >
      <slot />
      <template #empty>
        <el-empty description="暂无数据" :image-size="80" />
      </template>
    </el-table>

    <!-- Pagination -->
    <div v-if="showPagination" class="crud-pagination">
      <el-pagination
        v-model:current-page="currentPageModel"
        v-model:page-size="pageSizeModel"
        :page-sizes="pageSizes"
        :total="total"
        :layout="layout"
        background
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, useSlots } from 'vue'

const slots = useSlots()

interface CrudTableProps {
  data: any[]
  loading?: boolean
  total?: number
  page?: number
  pageSize?: number
  showPagination?: boolean
  border?: boolean
  stripe?: boolean
  maxHeight?: string | number
  size?: 'large' | 'default' | 'small'
  pageSizes?: number[]
  layout?: string
}

const props = withDefaults(defineProps<CrudTableProps>(), {
  data: () => [],
  loading: false,
  total: 0,
  page: 1,
  pageSize: 20,
  showPagination: true,
  border: true,
  stripe: true,
  maxHeight: undefined,
  size: 'default' as const,
  pageSizes: () => [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
})

const emit = defineEmits<{
  refresh: []
  search: [form: Record<string, any>]
  'update:page': [page: number]
  'update:page-size': [size: number]
  'page-change': [page: number]
  'size-change': [size: number]
  'selection-change': [selection: any[]]
}>()

const currentPageModel = computed({
  get: () => props.page,
  set: (val: number) => { emit('update:page', val); emit('page-change', val) },
})

const pageSizeModel = computed({
  get: () => props.pageSize,
  set: (val: number) => { emit('update:page-size', val); emit('size-change', val) },
})

const showSearch = ref(false)
const searchData = ref<Record<string, any>>({})

function handleSearch() {
  emit('search', searchData.value)
}

function handleReset() {
  searchData.value = {}
  emit('search', {})
}
</script>

<style scoped lang="scss">
.crud-table {
  .crud-search {
    margin-bottom: var(--crm-space-base);
    .el-form {
      display: flex;
      flex-wrap: wrap;
      gap: 0;
      .el-form-item {
        margin-bottom: 0;
      }
    }
  }
  .crud-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--crm-space-base);
    gap: var(--crm-space-sm);
  }
  .crud-toolbar-left,
  .crud-toolbar-right {
    display: flex;
    align-items: center;
    gap: var(--crm-space-sm);
  }
  .crud-pagination {
    display: flex;
    justify-content: flex-end;
    padding-top: var(--crm-space-base);
  }
}
</style>
