<template>
  <div class="image-upload">
    <!-- 上传按钮 / 图片预览 -->
    <div class="upload-container" @click="triggerUpload">
      <template v-if="modelValue">
        <el-image
          :src="modelValue"
          fit="cover"
          class="preview-img"
          :style="previewStyle"
        >
          <template #error>
            <div class="upload-placeholder" :style="previewStyle">
              <el-icon :size="28"><PictureFilled /></el-icon>
              <span>加载失败</span>
            </div>
          </template>
        </el-image>
        <div class="upload-overlay" :style="previewStyle">
          <el-icon :size="20"><EditPen /></el-icon>
          <span>更换图片</span>
        </div>
      </template>
      <div v-else class="upload-placeholder" :style="previewStyle">
        <el-icon :size="28"><Plus /></el-icon>
        <span>{{ placeholder }}</span>
      </div>
    </div>

    <!-- 隐藏的文件输入 -->
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      style="display:none"
      @change="onFileChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, PictureFilled, EditPen } from '@element-plus/icons-vue'
import { uploadFile } from '@/api/modules/system'

const props = withDefaults(defineProps<{
  modelValue?: string
  size?: number
  width?: number
  height?: number
  placeholder?: string
  bizType?: string
}>(), {
  modelValue: '',
  size: 100,
  width: undefined as unknown as number,
  height: undefined as unknown as number,
  placeholder: '点击上传图片',
  bizType: 'image',
})

const emit = defineEmits<{
  (e: 'update:modelValue', val: string): void
}>()

const fileInputRef = ref<HTMLInputElement>()

const previewStyle = computed(() => ({
  width: props.width ? `${props.width}px` : `${props.size}px`,
  height: props.height ? `${props.height}px` : `${props.size}px`,
}))

function triggerUpload() {
  fileInputRef.value?.click()
}

async function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }

  // 检查文件大小（最大 5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }

  try {
    const res = await uploadFile(file, props.bizType, true)
    const data = res as any
    const fileUrl = data.data?.fileUrl || data.fileUrl || data.url || data.path || ''
    if (fileUrl) {
      emit('update:modelValue', fileUrl)
      ElMessage.success('上传成功')
    } else {
      ElMessage.error('上传失败：未返回文件地址')
    }
  } catch {
    ElMessage.error('上传失败')
  }

  // 重置 input 以便重复选择同一文件
  input.value = ''
}
</script>

<style scoped lang="scss">
.image-upload {
  display: inline-block;
}

.upload-container {
  position: relative;
  border-radius: var(--crm-radius-md);
  overflow: hidden;
  cursor: pointer;
  border: 1px dashed var(--crm-border);
  transition: border-color var(--crm-transition-fast);

  &:hover {
    border-color: var(--crm-primary);
    .upload-overlay { opacity: 1; }
  }
}

.preview-img {
  display: block;
  object-fit: cover;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--crm-text-secondary);
  font-size: 12px;
  background: var(--crm-bg-gray);
}

.upload-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity var(--crm-transition-fast);
}
</style>
