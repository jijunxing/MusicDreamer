<template>
  <div class="singer-detail-page">
    <!-- 歌手头部信息区 -->
    <div class="singer-header">
      <el-image
          :src="singer.avatar || defaultAvatar"
          class="singer-avatar"
          fit="cover"
      />
      <div class="singer-info">
        <h1 class="singer-name">{{ singer.username }}</h1>
        <div class="singer-meta">
          <span class="meta-item">
            <el-icon><Headset /></el-icon> {{ singer.songCount }} 首单曲
          </span>
          <span class="meta-item">
            <el-icon><User /></el-icon> {{ singer.fansCount || 0 }} 粉丝
          </span>
        </div>
        <div class="singer-about">
          <h2>歌手简介</h2>
          <p>{{ singer.about || '暂无歌手简介' }}</p>
        </div>
      </div>
    </div>

    <!-- 单曲列表区域 -->
    <div class="singer-songs">
      <h2 class="section-title">{{ singer.username }}的单曲</h2>
      <el-button
          type="primary"
          @click="playAllSongs"
          :disabled="songs.length === 0"
          class="play-all-btn"
      >
        <el-icon><VideoPlay /></el-icon> 全部播放
      </el-button>
      <el-table :data="songs" v-loading="loading" class="song-table">
        <el-table-column type="index" width="60" />
        <el-table-column label="歌曲信息" min-width="300">
          <template #default="{ row }">
            <div class="song-info">
              <el-image
                  :src="row.imageUrl || defaultCover"
                  class="song-cover"
                  :preview-src-list="[row.imageUrl || defaultCover]"
              />
              <div class="song-details">
                <div class="song-title">{{ row.musicName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="100" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.timelength) }}
          </template>
        </el-table-column>
        <el-table-column label="播放量" width="120" align="center">
          <template #default="{ row }">
            <span class="play-count">
              {{ formatPlayCount(row.listenNumb) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
                circle
                size="small"
                @click="playSong(row)"
                title="播放"
            >
              <el-icon><VideoPlay /></el-icon>
            </el-button>
            <el-button
                circle
                size="small"
                @click="addToPlaylist(row)"
                title="添加到歌单"
            >
              <el-icon><Plus /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { player } from '@/utils/player'
import request from '@/utils/request'

const route = useRoute()
const singerId = ref(route.params.id)

const singer = ref({
  id: null,
  username: '',
  avatar: '',
  about: '',
  songCount: 0,
  fansCount: 0
})

const songs = ref([])
const loading = ref(false)

// 默认图片
const defaultAvatar = ref('/src/assets/imgs/default_singer_avatar.png')
const defaultCover = ref('/src/assets/imgs/default_music_cover.png')

// 获取歌手详情
const fetchSingerName = async () => {
  const res = await request.get(`/user/selectById/${singerId.value}`);
  if (res.code === '200') {
    singer.value = res.data; // 关键字段映射
  }
};

// 根据歌手名称获取歌曲
const fetchSongs = async () => {
  const res = await request.get('/music/selectAll', {
    params: { singerName: singer.value.username } // 使用 singerName 作为参数
  });
  if (res.code === '200') {
    songs.value = res.data;
    singer.value.songCount = songs.value.length
  }
};

// 格式化播放量
const formatPlayCount = (count) => {
  if (!count) return '0'
  return count > 10000
      ? `${(count / 10000).toFixed(1)}万`
      : count
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 播放歌曲
const playSong = (song) => {
  player.play(song)
}

const playAllSongs = () => {
  console.log(songs.value)
  if (songs.value.length === 0) return;
  player.playList(songs.value)
}
// 添加到播放列表
const addToPlaylist = (song) => {
  player.addToQueue(song)
}

onMounted(async () => {
  await fetchSingerName();
  if (singer.value) {
    await fetchSongs();
  }
});
</script>

<style scoped lang="scss">
.singer-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 15px; /* 减少页面整体内边距 */
}

.singer-header {
  display: flex;
  gap: 30px;
  margin-top: 50px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.singer-avatar {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  border: 3px solid rgba(255, 154, 60, 0.2);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.25);
  flex-shrink: 0;
}

.singer-info {
  flex: 1;
}

.singer-name {
  font-size: 36px; /* 减小字体大小 */
  font-weight: 800;
  margin-bottom: 15px; /* 减少底部间距 */
  background: linear-gradient(135deg, #ff9a3c, #ff6b6b);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.singer-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;

  .meta-item {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 24px;
    font-size: 14px;
    font-weight: 500;

    &:first-child {
      background: rgba(64, 158, 255, 0.15);
      color: #409eff;
    }

    &:last-child {
      background: rgba(255, 107, 107, 0.15);
      color: #ff6b6b;
    }
  }
}

.singer-about {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;

  h2 {
    font-size: 20px; /* 减小标题字体大小 */
    margin-bottom: 15px; /* 减少底部间距 */
    color: #ff9a3c;
    border-bottom: 1px solid rgba(255, 154, 60, 0.3);
    padding-bottom: 8px; /* 减少底部内边距 */
  }

  p {
    line-height: 1.7;
    color: #39393A;
    font-size: 14px; /* 减小正文字体大小 */
  }
}

.singer-songs {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid rgba(255, 154, 60, 0.3);
}

.play-all-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  padding: 10px 20px;
  border-radius: 30px;
  background: linear-gradient(135deg, #ff9a3c, #ff6b6b);
  border: none;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(255, 107, 107, 0.3);
  }

  &:active {
    transform: translateY(0);
  }

  .el-icon {
    font-size: 18px;
  }
}
.song-table {
  background: transparent;
  border-radius: 12px;
  overflow: hidden;

  :deep(.el-table__row) {
    background: rgba(255, 255, 255, 0.03);
    transition: background 0.3s;

    &:hover {
      background: rgba(255, 154, 60, 0.1) !important;
    }
  }
}

.song-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.song-cover {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.song-details {
  flex: 1;
}

.song-title {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 5px;
}

.play-count {
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .singer-header {
    gap: 30px;
  }

  .singer-avatar {
    width: 180px;
    height: 180px;
  }
}

@media (max-width: 768px) {
  .singer-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 20px;
  }

  .singer-meta {
    justify-content: center;
    flex-wrap: wrap;
  }

  .singer-avatar {
    width: 150px;
    height: 150px;
  }

  .section-title {
    font-size: 22px;
    text-align: center;
  }
}
</style>