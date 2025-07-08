<template>
  <div class="ranked-songs-page">
    <!-- 榜单头部 - 添加动态背景和视觉层次 -->
    <div class="rank-banner" :style="{ backgroundImage: `linear-gradient(135deg, rgba(14, 22, 33, 0.85), rgba(22, 34, 49, 0.85)), url(${currentRank.cover})` }">
      <div class="rank-header">
        <div class="cover-container">
          <img class="rank-image" :src="currentRank.cover" alt="榜单封面" />
          <div class="cd-effect"></div>
        </div>

        <div class="rank-info">
          <h2 class="rank-title">{{ currentRank.title }}</h2>

          <!-- 榜单切换按钮组 -->
          <div class="rank-switcher">
            <div
                v-for="rank in ranks"
                :key="rank.key"
                class="rank-tab"
                :class="{ active: rank.key === currentRank.key }"
                @click="switchRank(rank)"
            >
              {{ rank.title }}
            </div>
          </div>

          <!-- 播放控制区域 -->
          <div class="play-controls">
            <el-button
                type="primary"
                @click="playAll"
                class="play-all-btn"
            >
              <el-icon><VideoPlay /></el-icon> 全部播放
            </el-button>
            <span class="song-count">{{ musicList.length }} 首歌曲</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 榜单表格 - 添加卡片式设计和播放状态反馈 -->
    <div class="music-table-container">
      <el-table
          :data="musicList"
          class="music-table"
          v-loading="loading"
          :row-class-name="getRowClassName"
      >
        <el-table-column type="index" label="#" width="50">
          <template #default="{ row, $index }">
            <div class="rank-number" :class="{ top3: $index < 3 }">
              {{ $index + 1 }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="歌曲信息" min-width="300">
          <template #default="{ row }">
            <div class="music-cell">
              <div class="cover-container">
                <img :src="row.imageUrl" class="music-cover" />
                <div class="play-icon" @click="play(row)">
                  <el-icon><VideoPlay /></el-icon>
                </div>
                <div v-if="player.current!=null && player.current.musicId === row.musicId" class="playing-indicator">
                  <div class="sound-wave">
                    <span></span><span></span><span></span>
                  </div>
                </div>
              </div>
              <div class="music-meta">
                <div class="music-name">{{ row.musicName }}</div>
                <div class="singer-name">{{ row.singerName }}</div>
                <div class="music-tags">
                  <el-tag
                      v-for="(tag, index) in row.tags?.slice(0, 2)"
                      :key="index"
                      size="small"
                      class="tag"
                  >
                    {{ tag.name }}
                  </el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="时长" width="80">
          <template #default="{ row }">
            <div class="duration">
              {{ formatDuration(row.timelength) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="播放量" width="120">
          <template #default="{ row }">
            <div class="play-count">
              <el-icon><Headset /></el-icon>
              <span v-if="row.listenNumb > 10000">
                {{ (row.listenNumb / 10000).toFixed(1) }}万
              </span>
              <span v-else>{{ row.listenNumb || 0 }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <div class="music-actions">
              <el-tooltip content="播放" placement="top">
                <el-button
                    circle
                    class="action-btn"
                    @click="play(row)"
                    :type="player.current!=null && player.current.musicId === row.musicId ? 'primary' : ''"
                >
                  <el-icon><VideoPlay /></el-icon>
                </el-button>
              </el-tooltip>

              <el-tooltip content="添加到队列" placement="top">
                <el-button
                    circle
                    class="action-btn"
                    @click="addToQueue(row)"
                >
                  <el-icon><Plus /></el-icon>
                </el-button>
              </el-tooltip>

              <el-tooltip content="更多操作" placement="top">
                <el-button
                    circle
                    class="action-btn"
                >
                  <el-icon><More /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { player } from '@/utils/player'
import {
  VideoPlay, Headset, Plus, More
} from '@element-plus/icons-vue'

// 数据响应
const musicList = ref([])
const loading = ref(false)

// 榜单配置
const ranks = [
  {
    key: 'hot',
    title: '热歌榜',
    cover: 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?q=80',
    fetcher: async () => {
      const res = await request.get('/music/selectAll')
      return res.code === '200'
          ? (res.data || []).sort((a, b) => (b.listenNumb || 0) - (a.listenNumb || 0))
          : []
    },
  },
  {
    key: 'new',
    title: '新歌榜',
    cover: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80',
    fetcher: async () => {
      const res = await request.get('/music/selectAll')
      return res.code === '200'
          ? (res.data || []).sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
          : []
    },
  },
  {
    key: 'original',
    title: '原创榜',
    cover: 'https://images.unsplash.com/photo-1453733190371-0a9bedd82893?q=80',
    fetcher: async () => {
      const res = await request.get('/music/selectAll')
      return res.code === '200'
          ? (res.data || []).filter(song => song.tags?.some(tag => tag.name === '国语'))
          : []
    },
  }
]

const currentRank = ref(ranks[0]) // 默认是热歌榜

// 计算当前播放歌曲
const currentPlayingId = computed(() => player.current?.musicId || null)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    musicList.value = await currentRank.value.fetcher()
  } catch (e) {
    ElMessage.error('加载榜单失败')
  }
  loading.value = false
}

// 切换榜单
const switchRank = async (rank) => {
  currentRank.value = rank
  await loadData()
}

// 播放歌曲
const play = (music) => {
  if (music.activation === 0) {
    ElMessage.warning('该歌曲已被冻结，无法播放')
    return
  }
  player.play(music)
}

// 添加到队列
const addToQueue = (music) => {
  if (music.activation === 0) {
    ElMessage.warning('该歌曲已被冻结，无法添加')
    return
  }
  player.addToQueue(music)
  ElMessage.success(`已添加：${music.musicName}`)
}

// 播放全部
const playAll = () => {
  const available = musicList.value.filter((m) => m.activation !== 0)
  if (available.length === 0) {
    ElMessage.warning('没有可播放的歌曲')
    return
  }
  player.playList(available)
  ElMessage.success('开始播放榜单全部歌曲')
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 为当前播放歌曲添加特殊类名
const getRowClassName = ({ row }) => {
  if (currentPlayingId.value && row.musicId === currentPlayingId.value) {
    return 'playing-row'
  }
  return ''
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.ranked-songs-page {
  max-width: 1200px;
  margin: 60px auto;
  padding: 0;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  background: #fff;
}

.rank-banner {
  padding: 40px 30px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.rank-header {
  display: flex;
  align-items: center;
  gap: 30px;
  position: relative;
  z-index: 2;
}

.cover-container {
  position: relative;
  width: 220px;
  height: 220px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.3);

  .rank-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .cd-effect {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: radial-gradient(circle, transparent 60%, rgba(0, 0, 0, 0.7) 150%);
    border: 15px solid rgba(255, 255, 255, 0.05);
    box-sizing: border-box;
  }
}

.rank-info {
  flex: 1;
  color: #fff;

  .rank-title {
    font-size: 36px;
    font-weight: 800;
    margin-bottom: 15px;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  }
}

.rank-switcher {
  display: flex;
  gap: 12px;
  margin-bottom: 25px;

  .rank-tab {
    padding: 8px 20px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 30px;
    font-size: 15px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
    backdrop-filter: blur(5px);

    &:hover {
      background: rgba(255, 255, 255, 0.25);
      transform: translateY(-2px);
    }

    &.active {
      background: linear-gradient(135deg, #ff9a3c, #ff6b6b);
      box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
    }
  }
}

.play-controls {
  display: flex;
  align-items: center;
  gap: 20px;

  .play-all-btn {
    padding: 12px 30px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 30px;
    box-shadow: 0 5px 15px rgba(255, 107, 107, 0.4);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 20px rgba(255, 107, 107, 0.5);
    }
  }

  .song-count {
    font-size: 15px;
    opacity: 0.9;
  }
}

.music-table-container {
  padding: 20px 30px 40px;
}

.music-table {
  border-radius: 12px;
  overflow: hidden;

  :deep(.el-table__row) {
    transition: background-color 0.3s ease;

    &:hover {
      background-color: rgba(255, 154, 60, 0.05) !important;

      .play-icon {
        opacity: 1;
        transform: scale(1);
      }
    }

    &.playing-row {
      background: rgba(64, 158, 255, 0.08) !important;

      .music-name {
        color: #409eff;
        font-weight: 700;
      }
    }
  }
}

.rank-number {
  font-size: 18px;
  font-weight: 700;
  color: #999;

  &.top3 {
    color: #ff6b6b;
    font-size: 22px;
  }
}

.music-cell {
  display: flex;
  align-items: center;
  gap: 20px;
}

.cover-container {
  position: relative;
  width: 70px;
  height: 70px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;

  .music-cover {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .play-icon {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 24px;
    opacity: 0;
    transition: opacity 0.3s ease;
    cursor: pointer;

    .el-icon {
      transition: transform 0.3s ease;
    }

    &:hover .el-icon {
      transform: scale(1.2);
    }
  }

  .playing-indicator {
    position: absolute;
    bottom: 5px;
    right: 5px;
    background: rgba(0, 0, 0, 0.7);
    padding: 3px 8px;
    border-radius: 12px;
    color: #fff;
    font-size: 11px;

    .sound-wave {
      display: flex;
      align-items: center;
      gap: 3px;
      height: 16px;

      span {
        display: inline-block;
        width: 3px;
        background: #fff;
        border-radius: 2px;
        animation: soundWave 1.2s infinite ease-in-out;

        &:nth-child(1) { height: 6px; animation-delay: 0s; }
        &:nth-child(2) { height: 9px; animation-delay: 0.2s; }
        &:nth-child(3) { height: 12px; animation-delay: 0.4s; }
      }
    }
  }
}

.music-meta {
  flex: 1;
  min-width: 0;

  .music-name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .singer-name {
    font-size: 13px;
    color: #666;
    margin-bottom: 8px;
  }

  .music-tags {
    display: flex;
    gap: 6px;

    .tag {
      background: rgba(64, 158, 255, 0.1);
      color: #409eff;
      border: none;
      border-radius: 4px;
      padding: 0 8px;
      height: 24px;
      line-height: 24px;
    }
  }
}

.duration {
  font-size: 14px;
  color: #666;
}

.play-count {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #666;
}

.music-actions {
  display: flex;
  gap: 8px;

  .action-btn {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(240, 240, 240, 0.8);
    border: none;
    transition: all 0.3s ease;

    &:hover {
      background: #409eff;
      color: white;
      transform: translateY(-3px);
      box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);
    }
  }
}

@keyframes soundWave {
  0%, 100% { transform: scaleY(0.8); }
  50% { transform: scaleY(1.5); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .rank-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .cover-container {
    width: 120px;
    height: 120px;
    align-self: center;
  }

  .rank-switcher {
    flex-wrap: wrap;
  }

  .music-cell {
    flex-wrap: wrap;
  }

  .music-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>