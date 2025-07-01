<template>
  <div>
    <div v-if="type === 'image'" class="image-upload-box" @click="triggerInput">
      <input ref="fileInput" type="file" :accept="acceptType" style="display:none" @change="handleFileChange" />
      <div v-if="imageUrl" class="image-preview">
        <img :src="imageUrl" alt="图片预览" />
      </div>
      <div v-else class="upload-placeholder">
        <span>点击上传图片</span>
      </div>
    </div>
    <div v-else class="mp3-upload-box">
      <el-upload
          :action="uploadUrl"
          :headers="headers"
          :data="{ type }"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :on-success="onSuccess"
          :on-error="onError"
          :accept="acceptType"
      >
        <el-button type="primary">
          上传{{ type === 'music' ? '音频' : type === 'lrc' ? '歌词 (.lrc)' : '文件' }}
        </el-button>
      </el-upload>

      <div v-if="fileUrl" class="file-link">
        <a :href="fileUrl" target="_blank">已上传文件，点击查看</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,watch,computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  type: { type: String, default: 'other' }, // image, music, other
  modelValue: String
})
const emit = defineEmits(['update:modelValue'])

const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'
const headers = { Authorization: `Bearer ${localStorage.getItem('jwt_token') || ''}`}
const imageUrl = ref(props.modelValue || '')
const fileUrl = ref(props.modelValue || '')
const fileInput = ref(null)
// 监听父组件数据变化，确保子组件数据同步
watch(() => props.modelValue, (newValue) => {
  if (props.type === 'image') {
    imageUrl.value = newValue || ''
  } else {
    fileUrl.value = newValue || ''
  }
})
const triggerInput = () => {
  fileInput.value && fileInput.value.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  // 类型校验
  const validImageTypes = ['image/jpeg', 'image/png', 'image/jpg']
  if (props.type === 'image' && !validImageTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG 格式图片')
    return
  }
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

const acceptType = computed(() => {
  switch (props.type) {
    case 'image': return 'image/jpeg,image/png,image/jpg'
    case 'music': return '.mp3'
    case 'lrc': return '.lrc'
    default: return '*/*'
  }
})

const beforeUpload = (file) => {
  if (file.size > 20 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 20MB')
    return false
  }
  const isImage = props.type === 'image' && ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  const isMP3 = props.type === 'music' && file.type === 'audio/mpeg'
  const isLRC = props.type === 'lrc' && file.name.toLowerCase().endsWith('.lrc')

  if (!isImage && !isMP3 && !isLRC) {
    ElMessage.error('仅支持上传 JPG、PNG 图片、 MP3 音频文件或LRC 歌词文件')
    return false
  }

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
  width: 120px;
  height: 120px;
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
.mp3-upload-box {
  margin: 10px 0;
}
.file-link {
  margin-top: 10px;
}
</style> 