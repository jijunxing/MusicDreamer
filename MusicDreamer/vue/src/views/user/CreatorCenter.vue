<template>
  <div class="creator-center">
    <!-- 非创作者视图 -->
    <div v-if="!isSinger" class="non-creator-view">
      <div class="card welcome-card">
        <el-result
            icon="info"
            title="您目前还不是音乐创作者"
            sub-title="成为创作者后可以上传和管理自己的音乐作品"
        >
          <template #extra>
            <el-button
                type="primary"
                size="large"
                @click="becomeCreator"
                class="creator-btn"
            >
              成为创作者
            </el-button>
          </template>
        </el-result>
      </div>
    </div>

    <!-- 创作者视图 -->
    <div v-else class="creator-view">
      <div class="creator-header">
        <div class="header-title">
          <h2 class="title">我的音乐作品</h2>
          <el-tag type="success" size="small" class="creator-tag">创作者</el-tag>
        </div>
        <el-button
            type="primary"
            @click="handleAdd"
            icon="Plus"
            class="upload-btn"
        >
          上传新作品
        </el-button>
      </div>

      <!-- 搜索区域 -->
      <div class="search-section">
        <div class="card search-card">
          <el-input
              prefix-icon="Search"
              style="width: 300px"
              placeholder="请输入歌名"
              v-model="data.keyword"
              clearable
          />
          <div class="search-buttons">
            <el-button type="primary" @click="load" class="search-btn">查询</el-button>
            <el-button @click="reset" class="reset-btn">重置</el-button>
          </div>
        </div>
      </div>

      <!-- 音乐列表表格 -->
      <div class="music-list">
        <div class="card list-card">
          <el-table :data="data.tableData" style="width: 100%" v-loading="data.loading" class="music-table">
            <el-table-column prop="musicId" label="ID" width="70"/>
            <el-table-column prop="musicName" label="歌曲名" min-width="160"/>
            <el-table-column prop="imageUrl" label="封面" width="100">
              <template v-slot="scope">
                <el-image
                    v-if="scope.row.imageUrl"
                    :src="scope.row.imageUrl"
                    :preview-src-list="[scope.row.imageUrl]"
                    :preview-teleported="true"
                    append-to-body
                    style="width: 50px; height: 50px; border-radius: 10%; cursor: pointer"
                    fit="cover"
                    class="cover-image"
                />
              </template>
            </el-table-column>
            <el-table-column prop="tags" label="标签" min-width="180">
              <template v-slot="scope">
                <div class="tag-container">
                  <el-tag
                      v-for="tag in scope.row.tags"
                      :key="tag.id"
                      class="music-tag"
                      :type="getTagType(tag.type)"
                  >
                    {{ tag.name }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="singerName" label="歌手用户名" min-width="120"/>
            <el-table-column prop="timelength" label="时长" min-width="90">
              <template #default="scope">
                {{ formatTime(scope.row.timelength) }}
              </template>
            </el-table-column>
            <el-table-column prop="listenNumb" label="播放量" min-width="100">
              <template #default="scope">
                {{ formatPlayCount(scope.row.listenNumb) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.activation)" effect="light" size="small">
                  {{ scope.row.activation === 1 ? '激活' : '冻结' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="340">
              <template #default="scope">
                <div class="action-buttons">
                  <el-tooltip content="立即播放" placement="top">
                    <el-button type="primary" :icon="VideoPlay" circle @click="playNow(scope.row)" />
                  </el-tooltip>
                  <el-tooltip content="添加到播放列表" placement="top">
                    <el-button type="success" icon="Plus" circle @click="addToQueue(scope.row)" />
                  </el-tooltip>
                  <el-tooltip content="编辑" placement="top">
                    <el-button type="primary" :icon="Edit" circle @click="handleEdit(scope.row)" />
                  </el-tooltip>
                  <el-tooltip content="删除" placement="top">
                    <el-button type="danger" :icon="Delete" circle @click="del(scope.row.musicId)" />
                  </el-tooltip>
                  <el-popconfirm
                      title="确认要冻结这首歌曲吗?"
                      v-if="scope.row.activation === 1"
                      @confirm="freezeMusic(scope.row)"
                  >
                    <template #reference>
                      <el-button type="warning" plain class="freeze-btn">冻结</el-button>
                    </template>
                  </el-popconfirm>
                  <el-popconfirm
                      title="确认要解冻这首歌曲吗?"
                      v-if="scope.row.activation === 0"
                      @confirm="unfreezeMusic(scope.row)"
                  >
                    <template #reference>
                      <el-button type="success" plain class="unfreeze-btn">解冻</el-button>
                    </template>
                  </el-popconfirm>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 分页控制 -->
      <div class="pagination-container">
        <div class="card pagination-card" v-if="data.total">
          <el-pagination
              background
              layout="prev, pager, next, sizes, total"
              @current-change="load"
              :page-size="data.pageSize"
              v-model:current-page="data.pageNum"
              :total="data.total"
              :page-sizes="[6, 12, 24, 48]"
              @size-change="handleSizeChange"
          />
        </div>
      </div>

      <!-- 音乐信息表单 -->
      <el-dialog v-model="data.formVisible" title="音乐信息" width="50%" class="music-dialog">
        <el-form :model="data.form" label-width="100px" class="music-form">
          <div class="form-columns">
            <!-- 左列 -->
            <div class="form-left">
              <el-form-item label="歌曲名">
                <el-input v-model="data.form.musicName" autocomplete="off" />
              </el-form-item>
              <el-form-item label="歌手ID">
                <el-input
                    v-model="data.form.fromSinger"
                    autocomplete="off"
                    disabled
                />
              </el-form-item>
              <el-form-item label="封面">
                <UploadFile v-model="data.form.imageUrl" type="image" />
                <div class="form-hint">推荐尺寸：600×600像素</div>
              </el-form-item>
            </div>

            <!-- 右列 -->
            <div class="form-right">
              <el-form-item label="音频文件">
                <UploadFile v-model="data.form.musicUrl" type="music" />
                <div class="form-hint">支持MP3格式</div>
              </el-form-item>
              <el-form-item label="歌词文件">
                <UploadFile v-model="data.form.lyricsUrl" type="lrc" />
                <div class="form-hint">支持LRC格式</div>
              </el-form-item>

              <template v-for="(options, type) in groupedTags" :key="type">
                <el-form-item :label="type">
                  <el-select v-model="data.selectedTags[type]" placeholder="请选择标签" clearable>
                    <el-option
                        v-for="tag in options"
                        :key="tag.id"
                        :label="tag.name"
                        :value="tag.id"
                    />
                  </el-select>
                </el-form-item>
              </template>
            </div>
          </div>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="data.formVisible = false">取消</el-button>
            <el-button type="primary" @click="save" class="save-btn">保存</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, VideoPlay, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import UploadFile from '@/components/UploadFile.vue'
import { player } from '@/utils/player'

// 用户状态
const isSinger = ref(false)
const currentUser = ref(null)

// 音乐管理数据
const data = reactive({
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 6,
  formVisible: false,
  form: {},
  keyword: null,
  tagList: [],
  selectedTags: {},
  loading: true
})

// 检查用户身份
const checkUserRole = () => {
  const userData = localStorage.getItem('currentUser')
  if (userData) {
    try {
      currentUser.value = JSON.parse(userData)
      isSinger.value = currentUser.value.role === 'SINGER'
    } catch (e) {
      console.error('解析用户数据失败', e)
    }
  }
}

// 成为创作者
const becomeCreator = async () => {
  if (!currentUser.value) return
  try {
    currentUser.value.role = 'SINGER'
    currentUser.value.password = ''
    // 调用更新接口
    const response = await request.put('/user/update', currentUser.value)

    if (response.code === '200') {
      // 更新本地存储
      localStorage.setItem('currentUser', JSON.stringify(currentUser.value))
      isSinger.value = true
      ElMessage.success('恭喜！您已成为音乐创作者')
    } else {
      ElMessage.error(response.msg || '更新失败，请重试')
    }
  } catch (error) {
    console.error('成为创作者失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 加载标签
const loadTags = () => {
  request.get('/tag/list').then(res => {
    data.tagList = res.data
  })
}

// 加载音乐数据（仅加载当前创作者的）
const load = () => {
  if (!isSinger.value || !currentUser.value) return

  data.loading = true

  request.get('/music/selectAll', {
    params: {
      musicName: data.keyword,
      singerName: currentUser.value.username // 只获取当前创作者的歌曲
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data || []
      data.total = res.data.length
    }
    data.loading = false
  }).catch(() => {
    data.loading = false
  })
}

// 重置搜索
const reset = () => {
  data.keyword = null
  load()
}

// 打开新增表单
const handleAdd = () => {
  data.form = {
    fromSinger: currentUser.value.id // 自动填充当前创作者ID
  }
  data.selectedTags = {}
  data.formVisible = true
}

// 打开编辑表单
const handleEdit = (row) => {
  data.form = {...row}
  data.selectedTags = {}
  row.tags?.forEach(tag => {
    data.selectedTags[tag.type] = tag.id
  })
  data.formVisible = true
}

// 保存音乐信息
const save = () => {
  const tagIds = Object.values(data.selectedTags).filter(Boolean)
  const payload = {
    ...data.form,
    tags: tagIds.map(id => ({id}))
  }

  request.request({
    method: payload.musicId ? 'PUT' : 'POST',
    url: payload.musicId ? '/music/update' : '/music/addWithTags',
    data: payload
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 删除音乐
const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确认要删除吗？', '确认删除', {
    type: 'warning',
    confirmButtonClass: 'confirm-delete',
    cancelButtonClass: 'cancel-delete'
  }).then(() => {
    request.delete('/music/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {
  })
}

// 播放音乐
const playNow = (row) => {
  if (row.activation === 0) {
    ElMessage.warning('该歌曲已被冻结，无法播放！')
    return
  }
  player.play(row)
}

// 添加到播放队列
const addToQueue = (row) => {
  player.addToQueue(row)
  ElMessage.success("已添加到播放列表")
}

// 冻结音乐
const freezeMusic = (row) => {
  request.put(`/music/freeze/${row.musicId}`).then(res => {
    if (res.code === '200') {
      ElMessage.success('冻结成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 解冻音乐
const unfreezeMusic = (row) => {
  request.put(`/music/unfreeze/${row.musicId}`).then(res => {
    if (res.code === '200') {
      ElMessage.success('解冻成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 标签分组
const groupedTags = computed(() => {
  const groups = {}
  data.tagList.forEach(tag => {
    if (!groups[tag.type]) groups[tag.type] = []
    groups[tag.type].push(tag)
  })
  return groups
})

// 格式化时间（秒转分:秒）
const formatTime = (seconds) => {
  if (!seconds) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs < 10 ? '0' : ''}${secs}`
}

// 格式化播放量
const formatPlayCount = (count) => {
  if (!count) return '0'
  if (count >= 10000) return (count / 10000).toFixed(1) + '万'
  return count
}

// 获取状态标签类型
const getStatusType = (status) => {
  return status === 1 ? 'success' : 'primary'
}

// 获取标签类型样式
const getTagType = (type) => {
  const types = {
    '风格': 'success',
    '情感': 'warning',
    '场景': 'info',
    '语种': 'danger'
  }
  return types[type] || 'info'
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  data.pageSize = size
  load()
}

// 初始化
onMounted(() => {
  checkUserRole()
  if (isSinger.value) {
    loadTags()
    load()
  }
})
</script>

<style scoped>
.creator-center {
  max-width: 1200px;
  margin: 80px auto;
  padding: 20px;
  background-color: #f9fafc;
  min-height: 100vh;
}

/* 卡片样式 */
.card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border: 1px solid #eef2f7;
}

.card:hover {
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

/* 非创作者视图 */
.non-creator-view {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
}

.welcome-card {
  text-align: center;
  max-width: 600px;
  width: 100%;
  padding: 40px 30px;
  background: linear-gradient(135deg, #f9fafc 0%, #ffffff 100%);
}

.creator-btn {
  padding: 12px 36px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(120deg, #3b82f6, #6366f1);
  border: none;
}

/* 创作者视图 */
.creator-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title {
  font-size: 26px;
  font-weight: 700;
  color: #1a2a3c;
  margin: 0;
}

.creator-tag {
  font-size: 14px;
  padding: 5px 10px;
  border-radius: 20px;
}

.upload-btn {
  padding: 10px 22px;
  border-radius: 8px;
  font-weight: 500;
  background: linear-gradient(120deg, #3b82f6, #6366f1);
  border: none;
  box-shadow: 0 4px 10px rgba(76, 105, 255, 0.2);
}

/* 搜索区域 */
.search-section {
  margin-bottom: 20px;
}

.search-card {
  display: flex;
  gap: 15px;
  align-items: center;
  padding: 18px 24px;
  background: linear-gradient(135deg, #f0f4f8 0%, #f7f9fc 100%);
}

.search-buttons {
  display: flex;
  gap: 10px;
}

.search-btn {
  background: linear-gradient(120deg, #3b82f6, #6366f1);
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
}

.reset-btn {
  background-color: #f0f4f8;
  color: #5a6c8c;
  border-radius: 8px;
  padding: 10px 20px;
}

/* 音乐列表 */
.music-list {
  margin-bottom: 24px;
}

.list-card {
  padding: 0;
  overflow: hidden;
}

.music-table {
  --el-table-header-bg-color: #f7fafc;
  --el-table-row-hover-bg-color: #f9fbfd;
}

:deep(.music-table th) {
  background-color: #f0f7ff;
  color: #3b82f6;
  font-weight: 600;
}

:deep(.music-table tr) {
  transition: background-color 0.2s ease;
}

:deep(.music-table tr:hover) {
  background-color: #f5f9ff !important;
}

.cover-image {
  transition: transform 0.3s ease;
}

.cover-image:hover {
  transform: scale(1.8);
  z-index: 10;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.music-tag {
  font-size: 12px;
  border-radius: 8px;
  padding: 4px 10px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.freeze-btn,
.unfreeze-btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  margin-left: 8px;
}

.freeze-btn {
  color: #f97316;
  border-color: #ffedd5;
  background-color: #ffedd5;
}

.unfreeze-btn {
  color: #10b981;
  border-color: #d1fae5;
  background-color: #d1fae5;
}

/* 分页样式 */
.pagination-container {
  text-align: right;
}

.pagination-card {
  padding: 16px 24px;
  background-color: #f7fafc;
}

/* 表单对话框 */
.music-dialog {
  border-radius: 16px;
}

:deep(.music-dialog .el-dialog) {
  border-radius: 16px;
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
}

:deep(.music-dialog .el-dialog__header) {
  padding: 20px 24px;
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  margin: 0;
  border-radius: 16px 16px 0 0;
  color: white;
}

:deep(.music-dialog .el-dialog__title) {
  color: white;
  font-size: 20px;
  font-weight: 600;
}

:deep(.music-dialog .el-dialog__headerbtn) {
  top: 24px;
  color: white;
}

.music-form {
  padding: 0 10px;
}

.form-columns {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.form-left,
.form-right {
  flex: 1;
  min-width: 300px;
}

.form-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 5px;
}

.save-btn {
  padding: 10px 28px;
  border-radius: 8px;
  background: linear-gradient(120deg, #3b82f6, #6366f1);
  border: none;
  box-shadow: 0 4px 10px rgba(76, 105, 255, 0.25);
}

.dialog-footer {
  padding: 10px 24px 20px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .creator-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 18px;
  }

  .search-card {
    flex-direction: column;
    align-items: stretch;
  }

  .form-columns {
    flex-direction: column;
    gap: 0;
  }
}

@media (max-width: 768px) {
  .creator-center {
    padding: 12px;
  }

  .card {
    padding: 20px 15px;
  }

  .title {
    font-size: 22px;
  }

  .welcome-card {
    padding: 30px 20px;
  }

  :deep(.music-table .el-table__cell) {
    padding: 10px 0;
  }

  .action-buttons {
    flex-wrap: wrap;
    gap: 5px;
  }

  .freeze-btn, .unfreeze-btn {
    margin-left: 0;
    margin-top: 5px;
  }
}

@media (max-width: 480px) {
  .header-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .upload-btn, .creator-btn {
    width: 100%;
  }

  .music-dialog {
    width: 95%;
  }
}
</style>