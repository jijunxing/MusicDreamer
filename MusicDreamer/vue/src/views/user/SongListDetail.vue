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
          <el-button type="info" round :class="{'favorited': isFavorited}" @click.stop="toggleFavoriteForSonglist">
            <transition name="bounce">
              <Icon v-if="isFavorited" icon="mdi:star" color="#ffc107" class="star-icon"/>
              <Icon v-else icon="mdi:star-outline" color="black" class="star-icon"/>
            </transition>
            {{ isFavorited ? '已收藏' : '未收藏' }}
            <!-- Iconify图标（收藏状态时显示） -->
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

      <el-table :data="songs" :row-class-name="tableRowClassName" v-loading="loading" class="songs-table"
                header-row-class-name="song-table-header">
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
                <div class="song-artist">{{ row.singerName }}</div>
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
        <el-table-column label="喜欢" width="80" align="center">
          <template #default="{ row }">
            <div class="like-btn" @click.stop="toggleLikeForMusic(row)">
              <transition name="bounce">
                <Icon v-if="isMusicLiked(row.musicId)"
                      icon="mdi:heart"
                      color="#ff4081"
                      class="like-icon liked-icon" />
              </transition>
              <Icon v-if="!isMusicLiked(row.musicId)"
                    icon="mdi:heart-outline"
                    class="like-icon" />
            </div>
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
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { player } from '@/utils/player'
import request from '@/utils/request'
import { Headset, VideoPlay, Star, Plus, Calendar } from '@element-plus/icons-vue'
import {ElMessage} from "element-plus";
import { Icon } from "@iconify/vue"
import {
  initUserLikes,
  toggleLike,
  isMusicLiked
} from '@/utils/likeUtils'
import {
  initUserFavorites,
  toggleFavorite as toggleSonglistFavorite,
  isSonglistFavorited
} from '@/utils/favoriteUtils'
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
const tableRowClassName = ({ row }) => {
  return player.current && player.current.id === row.id ? 'playing-row' : '';
};
// 默认图片
const defaultPlaylistCover = ref('/src/assets/imgs/default_playlist_cover.png')
const defaultAvatar = ref('/src/assets/imgs/default_singer_avatar.png')
const isFavorited = ref(false) // 当前歌单的收藏状态
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

// 格式化播放量
const formatPlayCount = (count) => {
  if (!count) return '0'
  return count > 10000
      ? `${(count / 10000).toFixed(1)}万`
      : count
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

const toggleLikeForSong = async (song) => {
  if (!song.musicId) return

  try {
    const success = await toggleLike(song.musicId)
    if (success) {
      ElMessage.success(`已喜欢 "${song.musicName}"`)
    } else {
      ElMessage.info(`已取消喜欢 "${song.musicName}"`)
    }
  } catch (error) {
    if (error.message === '用户未登录') {
      ElMessage.warning('请先登录')
      router.push('/login')
    } else {
      ElMessage.error('操作失败，请重试')
    }
  }
}

const toggleFavoriteForSonglist = async () => {
  if (!songlistId.value) return

  try {
    const success = await toggleSonglistFavorite(songlistId.value)
    isFavorited.value = success // 更新收藏状态

    if (success) {
      ElMessage.success(`已收藏歌单 "${songlist.value.name}"`)
    } else {
      ElMessage.info(`已取消收藏歌单 "${songlist.value.name}"`)
    }
  } catch (error) {
    if (error.message === '用户未登录') {
      ElMessage.warning('请先登录')
      router.push('/login')
    } else {
      ElMessage.error('操作失败，请重试')
    }
  }
}

onMounted(async () => {
  await initUserLikes()
  await fetchSonglistDetail()
  await initUserFavorites()
  isFavorited.value = isSonglistFavorited(songlistId.value)
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

.songs-table {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);

  :deep(.el-table__row) {
    background: rgba(255, 255, 255, 0.03);
    transition: background-color 0.3s ease;

    &:hover {
      background: rgba(29, 185, 84, 0.1) !important;
    }

    &.playing-row {
      .song-title {
        color: #1db954 !important;
        font-weight: 600;
      }
    }
  }

  :deep(.song-table-header th) {
    background: rgba(29, 185, 84, 0.1);
    color: #1db954;
    font-weight: 600;
    font-size: 15px;
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

.like-btn {
  cursor: pointer;
  padding: 4px;
  border-radius: 50%;
  width: 25px;
  height: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  margin: 0 auto;

  &:hover {
    transform: scale(1.1);
    background: rgba(240, 240, 240, 0.8);
    box-shadow: 0 3px 12px rgba(0, 0, 0, 0.2);
  }
}

.like-icon {
  font-size: 1.4rem;
  position: absolute;
}

.liked-icon {
  color: #ff4081 !important;
}

// 点赞动画
.bounce-enter-active {
  animation: bounce-in 0.5s;
}

@keyframes bounce-in {
  0% { transform: scale(0.5); opacity: 0; }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); opacity: 1; }
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

  .song-actions .like-btn {
    opacity: 1;
  }
}

/* 收藏按钮样式 */
.songlist-actions .el-button.favorited {
  background: rgba(255, 193, 7, 0.1);
  border: 1px solid rgba(255, 193, 7, 0.3);
  color: #ffc107;

  &:hover {
    background: rgba(255, 193, 7, 0.15);
    border-color: rgba(255, 193, 7, 0.5);
  }
}

.star-icon {
  position: relative;
  top: 1px;
  margin-left: 0px;
  font-size: 1.2rem;
}

/* 收藏动画 */
.bounce-enter-active {
  animation: bounce-in 0.5s;
}

@keyframes bounce-in {
  0% { transform: scale(0.5); opacity: 0; }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); opacity: 1; }
}

</style>