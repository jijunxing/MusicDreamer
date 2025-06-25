<template>
  <div>
    <div v-if="type === 'image'" class="image-upload-box" @click="triggerInput">
      <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleFileChange" />
      <div v-if="imageUrl" class="image-preview">
        <img :src="imageUrl" alt="图片预览" />
      </div>
      <div v-else class="upload-placeholder">
        <span>点击上传图片</span>
      </div>
    </div>
    <div v-else class="file-upload-box">
      <el-upload
        :action="uploadUrl"
        :headers="headers"
        :data="{ type }"
        :show-file-list="false"
        :before-upload="beforeUpload"
        :on-success="onSuccess"
        :on-error="onError"
        :with-credentials="false"
      >
        <el-button type="primary">上传文件</el-button>
      </el-upload>
      <div v-if="fileUrl" class="file-link">
        <a :href="fileUrl" target="_blank">已上传文件，点击查看</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  type: { type: String, default: 'other' }, // image, music, other
  modelValue: String
})
const emit = defineEmits(['update:modelValue'])

const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'
const headers = { }
const imageUrl = ref(props.modelValue || '')
const fileUrl = ref(props.modelValue || '')
const fileInput = ref(null)

const triggerInput = () => {
  fileInput.value && fileInput.value.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', props.type)
  try {
    const { data } = await request.post('/files/upload', formData)
    imageUrl.value = data
    emit('update:modelValue', data)
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const beforeUpload = (file) => {
  // 可扩展校验
  return true
}
const onSuccess = (res) => {
  fileUrl.value = res.data
  emit('update:modelValue', res.data)
  ElMessage.success('上传成功')
}
const onError = () => {
  ElMessage.error('上传失败')
}
</script>

<style scoped>
.image-upload-box {
  width: 240px;
  height: 240px;
  border: 2px dashed #d9d9d9;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  background: #fafafa;
}
.upload-placeholder {
  color: #aaa;
  font-size: 18px;
}
.image-preview img {
  max-width: 100%;
  max-height: 100%;
  display: block;
}
.file-upload-box {
  margin: 10px 0;
}
.file-link {
  margin-top: 10px;
}
</style> 