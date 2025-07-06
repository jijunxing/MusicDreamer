<template>
  <div class="song-library">
    <!-- 搜索和筛选区域 -->
    <div class="toolbar">
      <el-input
          v-model="searchKeyword"
          placeholder="搜索歌曲/歌手"
          class="search-input"
          clearable
          @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select v-model="sortField" placeholder="排序方式" @change="fetchSongs">
        <el-option label="最新添加" value="createTime"></el-option>
        <el-option label="播放量" value="listenNumb"></el-option>
        <el-option label="歌曲名称" value="musicName"></el-option>
      </el-select>
    </div>

    <!-- 歌曲列表 -->
    <div class="song-list">
      <div v-for="song in songs" :key="song.musicId" class="song-item">
        <div class="song-cover" @click="playMusic(song)">
          <img :src="song.imageUrl" alt="歌曲封面" />
          <div class="play-overlay">
            <el-icon><VideoPlay /></el-icon>
          </div>
        </div>

        <div class="song-info">
          <div class="song-name">{{ song.musicName }}</div>
          <div class="singer">{{ song.singerName }}</div>
          <div class="song-meta">
            <span><el-icon><Clock /></el-icon> {{ formatDuration(song.timelength) }}</span>
            <span><el-icon><Headset /></el-icon> {{ song.listenNumb || 0 }}</span>
          </div>
        </div>

        <div class="song-actions">
          <el-button type="primary" circle @click.stop="playMusic(song)">
            <el-icon><VideoPlay /></el-icon>
          </el-button>
          <el-button type="info" circle @click.stop="addToQueue(song)">
            <el-icon><Plus /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页控件 -->
    <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalSongs"
        layout="prev, pager, next"
        @current-change="handlePageChange"
    />

    <!-- 空状态处理 -->
    <el-empty v-if="songs.length === 0" description="未找到相关歌曲" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, VideoPlay, Plus, Clock, Headset } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 数据状态
const songs = ref([])
const searchKeyword = ref('')
const sortField = ref('createTime')
const currentPage = ref(1)
const pageSize = ref(20)
const totalSongs = ref(0)

// 获取歌曲数据
const fetchSongs = async () => {
  try {
    const response = await request.get('/music/selectPage', {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        musicName: searchKeyword.value,
        orderBy: `${sortField.value} desc`
      }
    })

    if (response.code === '200') {
      songs.value = response.data.list || []
      totalSongs.value = response.data.total || 0
    } else {
      ElMessage.error('获取歌曲数据失败: ' + response.msg)
    }
  } catch (error) {
    console.error('获取歌曲数据异常:', error)
    ElMessage.error('数据加载失败，请重试')
  }
}

// 搜索处理（带防抖）
let searchTimer = null
const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    fetchSongs()
  }, 500)
}

// 分页变化处理
const handlePageChange = (newPage) => {
  currentPage.value = newPage
  fetchSongs()
}

// 格式化歌曲时长
const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 播放歌曲
const playMusic = (song) => {
  if (song.activation === 0) {
    ElMessage.warning('该歌曲已被冻结，无法播放！')
    return
  }
  player.play(song)
}

// 添加到播放队列
const addToQueue = (song) => {
  if (song.activation === 0) {
    ElMessage.warning('该歌曲已被冻结，无法添加！')
    return
  }
  player.addToQueue(song)
  ElMessage.success(`"${song.musicName}" 已添加到播放队列`)
}

onMounted(fetchSongs)
</script>

<style scoped>
.song-library {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

.song-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.song-item {
  display: flex;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.song-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.song-cover {
  position: relative;
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  cursor: pointer;
}

.song-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.song-cover:hover .play-overlay {
  opacity: 1;
}

.play-overlay .el-icon {
  color: white;
  font-size: 32px;
}

.song-info {
  flex: 1;
  padding: 15px;
  min-width: 0; /* 防止内容溢出 */
}

.song-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.singer {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
}

.song-meta .el-icon {
  margin-right: 4px;
}

.song-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 15px;
  gap: 10px;
}

.el-pagination {
  justify-content: center;
  margin-top: 30px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .song-list {
    grid-template-columns: repeat(auto-fill, minmax(100%, 1fr));
  }

  .toolbar {
    flex-direction: column;
  }

  .search-input {
    max-width: 100%;
  }
}
</style>