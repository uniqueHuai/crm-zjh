<template>
  <el-dialog
    :model-value="visible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    @update:model-value="emit('update:visible', $event)"
    @open="handleOpen"
    @closed="handleClosed"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-width="labelWidth"
      :label-position="labelPosition"
      v-bind="$attrs"
    >
      <slot :form="formData" />
    </el-form>
    <template #footer>
      <el-button @click="emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

const props = withDefaults(defineProps<{
  visible: boolean
  title?: string
  width?: string | number
  labelWidth?: string | number
  labelPosition?: 'left' | 'right' | 'top'
  rules?: FormRules
  submitting?: boolean
  initialData?: Record<string, any>
}>(), {
  title: '表单',
  width: '600px',
  labelWidth: '100px',
  labelPosition: 'right' as const,
  rules: () => ({}),
  submitting: false,
  initialData: () => ({}),
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  submit: [formData: Record<string, any>, done: () => void]
}>()

const formRef = ref<FormInstance>()
const formData = ref<Record<string, any>>({})

function handleOpen() {
  formData.value = { ...props.initialData }
  formRef.value?.clearValidate()
}

function handleClosed() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  emit('submit', formData.value, () => {
    emit('update:visible', false)
  })
}

function resetForm() {
  formRef.value?.resetFields()
}

function setFields(data: Record<string, any>) {
  formData.value = { ...data }
}

defineExpose({ resetForm, setFields })
</script>
