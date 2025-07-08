<template>
  <div class="songlist-detail-page">
    <!-- 歌单头部信息区 -->
    <div class="songlist-header">
      <el-image
          :src="songlist.imageUrl || defaultPlaylistCover"
          class="songlist-cover"
          fit="cover"
      />
      <div class="songlist-info">
        <h1 class="songlist-name">{{ songlist.name }}</h1>

        <div class="creator-info">
          创建人：
          <el-avatar :src="creatorAvatar" :size="40" class="creator-avatar" />
          <span class="creator-name">{{ songlist.userName }}</span>
        </div>

        <div class="songlist-meta">
          <span class="meta-item">
            <el-icon><Headset /></el-icon> {{ songlist.musicCount || 0 }} 首歌曲
          </span>
          <span class="meta-item">
            <el-icon><Calendar /></el-icon> {{ formatDate(songlist.createTime) }}
          </span>
        </div>

        <div class="songlist-actions">
          <el-button type="primary" round @click="playAllSongs" :disabled="songs.length === 0">
            <el-icon><VideoPlay /></el-icon> 播放全部
          </el-button>
          <el-button type="info" round>
            <el-icon><Star /></el-icon> 收藏
          </el-button>
        </div>

        <div class="songlist-about" v-if="songlist.about">
          <h2>歌单简介</h2>
          <p>{{ songlist.about }}</p>
        </div>
      </div>
    </div>

    <!-- 歌曲列表区域 -->
    <div class="songlist-songs">
      <h2 class="section-title">歌曲列表</h2>

      <div class="songs-container">
        <div
            v-for="(song, index) in songs"
            :key="song.id"
            class="song-item"
            :class="{ 'playing': player.current && player.current.id === song.id }"
        >
          <div class="song-index">{{ index + 1 }}</div>

          <div class="song-info" @click="playSong(song)">
            <div class="song-title">{{ song.musicName }}</div>
            <div class="song-artist">{{ song.singerName }}</div>
          </div>

          <div class="song-duration">{{ formatDuration(song.timelength) }}</div>

          <div class="song-actions">
            <el-button circle size="small" @click.stop="playSong(song)" title="播放">
              <el-icon><VideoPlay /></el-icon>
            </el-button>
            <el-button circle size="small" @click.stop="addToPlaylist(song)" title="添加到歌单">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { player } from '@/utils/player'
import request from '@/utils/request'
import { Headset, VideoPlay, Star, Plus, Calendar } from '@element-plus/icons-vue'
import {ElMessage} from "element-plus";

const route = useRoute()
const songlistId = ref(route.params.id)

const songlist = ref({
  id: null,
  name: '',
  imageUrl: '',
  about: '',
  userName: '',
  user: null,
  createTime: '',
  musics: [],
  musicCount: 0
})

const songs = ref([])
const loading = ref(false)
const creatorAvatar = ref(null)

// 默认图片
const defaultPlaylistCover = ref('/src/assets/imgs/default_playlist_cover.png')
const defaultAvatar = ref('/src/assets/imgs/default_singer_avatar.png')

// 获取歌单详情
const fetchSonglistDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/songlist/selectById/${songlistId.value}`)
    if (res.code === '200') {
      songlist.value = res.data

      // 处理歌曲数据
      if (res.data.musics && res.data.musics.length > 0) {
        songs.value = res.data.musics
        songlist.value.musicCount = songs.value.length
      }
    } else {
      ElMessage.error('获取歌单详情失败: ' + res.msg)
    }
  } catch (err) {
    console.error(err)
    ElMessage.error('请求错误: ' + err.message)
  } finally {
    loading.value = false
    getcreatorAvatar()
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
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

// 播放全部歌曲
const playAllSongs = () => {
  if (songs.value.length === 0) return
  player.playList(songs.value)
}

// 添加到播放列表
const addToPlaylist = (song) => {
  player.addToQueue(song)
  ElMessage.success('已添加到播放列表')
}

// 计算创建者头像
const getcreatorAvatar = () => {
  request.get('user/selectById/'+songlist.value.user).then(res=>{
    creatorAvatar.value = res.data.avatar
  })
}

onMounted(() => {
  fetchSonglistDetail()
})
</script>

<style scoped lang="scss">
.songlist-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 15px;
}

.songlist-header {
  display: flex;
  gap: 40px;
  margin-top: 50px;
  padding: 30px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.songlist-cover {
  width: 280px;
  height: 280px;
  border-radius: 12px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.25);
  flex-shrink: 0;
}

.songlist-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.songlist-name {
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 15px;
  color: #39393A;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  .creator-name {
    font-size: 18px;
    color: #aaa;
  }
}

.songlist-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 25px;

  .meta-item {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 24px;
    font-size: 14px;
    font-weight: 500;
    color: #aaa;
  }
}

.songlist-actions {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;

  .el-button {
    padding: 12px 25px;
    font-weight: 600;

    &:first-child {
      background: linear-gradient(135deg, #1db954, #1ed760);
      border: none;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 5px 15px rgba(29, 185, 84, 0.3);
      }
    }
  }
}

.songlist-about {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;

  h2 {
    font-size: 20px;
    margin-bottom: 15px;
    color: #1db954;
    border-bottom: 1px solid rgba(29, 185, 84, 0.3);
    padding-bottom: 8px;
  }

  p {
    line-height: 1.7;
    color: #39393A;
    font-size: 14px;
  }
}

.songlist-songs {
  margin-top: 40px;
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
  border-bottom: 2px solid rgba(29, 185, 84, 0.3);
  color: #39393A;
}

.songs-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.song-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.03);
  transition: all 0.3s ease;

  &:hover {
    background: rgba(29, 185, 84, 0.1);

    .song-actions {
      opacity: 1;
    }
  }

  &.playing {
    .song-title {
      color: #1db954;
    }
  }
}

.song-index {
  width: 40px;
  text-align: center;
  font-size: 16px;
  color: #39393A;
}

.song-info {
  flex: 1;
  cursor: pointer;

  .song-title {
    font-size: 16px;
    font-weight: 500;
    color: #39393A;
    margin-bottom: 5px;
  }

  .song-artist {
    font-size: 13px;
    color: #888;
  }
}

.song-duration {
  width: 60px;
  text-align: center;
  font-size: 14px;
  color: #888;
}

.song-actions {
  display: flex;
  gap: 10px;
  opacity: 0;
  transition: opacity 0.3s ease;

  .el-button {
    background: rgba(255, 255, 255, 0.1);
    color: #39393A;

    &:hover {
      background: rgba(29, 185, 84, 0.2);
      color: #fff;
    }
  }
}

/* 响应式设计 */
@media (max-width: 992px) {
  .songlist-header {
    gap: 30px;
  }

  .songlist-cover {
    width: 220px;
    height: 220px;
  }
}

@media (max-width: 768px) {
  .songlist-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 20px;
  }

  .songlist-cover {
    width: 200px;
    height: 200px;
    margin-bottom: 20px;
  }

  .creator-info {
    justify-content: center;
  }

  .songlist-meta {
    justify-content: center;
    flex-wrap: wrap;
  }

  .songlist-actions {
    justify-content: center;
  }

  .section-title {
    font-size: 22px;
    text-align: center;
  }

  .song-item {
    padding: 10px;
  }

  .song-actions {
    opacity: 1;
  }
}
</style>