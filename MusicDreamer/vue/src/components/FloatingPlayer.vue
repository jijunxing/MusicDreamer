<template>
  <div id="MusicControl" ref="playerRef">
    <div class="control-bar">
      <div class="player-content">
        <!-- 歌曲信息 -->
        <!-- 歌曲信息 + 封面 -->
        <div class="info-and-cover">
          <div class="song-info">
            <span class="song-title">{{ currentMusic.musicName || '歌名'}}</span>
            <span class="separator">———</span>
            <span class="song-artist">{{ currentMusic.singerName || '歌手' }}</span>
          </div>
          <div class="cover-wrapper" :class="{ rotating: isMusicPlaying }">
            <img :src="currentMusic.imageUrl ||  'https://www.bing.com/th/id/OIP.bdx_XoTmIk_1rlUlI6I41gHaHa?w=209&h=211&c=8&rs=1&qlt=90&o=6&cb=ircwebpc1&dpr=1.5&pid=3.1&rm=2'" alt="封面" class="cover-image" />
          </div>
        </div>

        <!-- 进度条 -->
        <div class="progress-area">
          <div class="progress-container" @click="seekAudio">
            <div class="progress-bar" :style="{ width: progress + '%' }"></div>
            <div id="progress-fill" :style="{ left: progress + '%' }"></div>
          </div>
          <div class="time-display">
            {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
          </div>
        </div>

        <!-- 播放控制按钮 -->
        <div class="main-controls">
          <button class="playlist-btn" @click="togglePlaylist" title="播放列表">
            <Icon icon="mdi:playlist-music" />
          </button>
          <button class="prev-btn" @click="handlePrev" title="上一首">
            <Icon icon="mdi:skip-previous" />
          </button>
          <button class="play-btn" @click="toggleMusic" title="播放/暂停">
            <Icon :icon="isMusicPlaying ? 'mdi:pause-circle' : 'mdi:play-circle'" />
          </button>
          <button class="next-btn" @click="handleNext" title="下一首">
            <Icon icon="mdi:skip-next" />
          </button>

          <!-- 音量控制 -->
          <div class="volume-control" @mouseenter="showVolume = true" @mouseleave="showVolume = false">
            <button class="volume-btn" :title="isMuted ? '取消静音' : '静音'">
              <Icon :icon="isMuted ? 'mdi:volume-off' : 'mdi:volume-high'" />
            </button>
            <transition name="slide-down">
              <div class="volume-slider" v-show="showVolume">
                <input type="range" min="0" max="1" step="0.01" v-model.number="volume" @input="handleVolumeChange"
                       class="vertical-slider" />
              </div>
            </transition>
          </div>
        </div>
      </div>

      <!-- 播放列表 -->
      <transition name="slide">
        <div class="playlist" v-show="showPlaylist">
          <div v-for="(song, index) in player.queue" :key="index"
               :class="{ 'playing': currentIndex === index }" @click="playSong(index)">
            <div class="song-number">{{ index + 1 }}</div>
            <div class="song-details">
              <div class="song-title">{{ song.musicName }}</div>
              <div class="song-artist">{{ song.singerName || '未知艺术家' }}</div>
            </div>
            <div class="song-duration">{{ Math.floor(song.timelength/60)}}:{{song.timelength%60}}</div>
          </div>
        </div>
      </transition>
    </div>

    <audio ref="bgMusic" :src="player.current?.musicUrl" @ended="handleNext" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { player } from "@/utils/player"
import { Icon } from '@iconify/vue'
const showPlaylist = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(0.8)
const isMuted = ref(false)
const showVolume = ref(false)

const bgMusic = ref(null)
const playerRef = ref(null)
const currentMusic = computed(() =>
    player.current || { name: '无歌曲', artist: '' }
)
// 状态同步函数
const syncState = () => {
  // 更新播放时间（从audio元素获取实时时间）
  if (player.audio) {
    currentTime.value = player.audio.currentTime
    const audioDuration = player.audio.duration;
    if (!audioDuration || isNaN(audioDuration) || !isFinite(audioDuration)) {
      // audio返回无效时，使用后端时长（单位需统一，假设是秒）
      duration.value = currentMusic.value.timelength || 0;
    } else {
      duration.value = audioDuration;
    }

  }
}

// 注册状态更新监听器
onMounted(() => {
  // 设置音频元素并初始化事件
  player.setAudioElement(bgMusic.value)

  // 注册状态更新监听器
  player.listeners.add(syncState)

  // 初始同步状态
  syncState()

  // 添加音频事件监听（进度更新）
  if (player.audio) {
    player.audio.addEventListener('timeupdate', () => {
      currentTime.value = player.audio.currentTime
    })

    player.audio.addEventListener('loadedmetadata', () => {
      const audioDuration = player.audio.duration;

      if (!audioDuration || isNaN(audioDuration) || !isFinite(audioDuration)) {
        // audio返回无效时，使用后端时长（单位需统一，假设是秒）
        duration.value = currentMusic.value.timelength || 0;
      } else {
        duration.value = audioDuration;
      }
    })
  }

  document.addEventListener('click', onClickOutside)
})

// 组件卸载时移除监听器
onUnmounted(() => {
  player.listeners.delete(syncState)
  document.removeEventListener('click', onClickOutside)
})

const onClickOutside = (e) => {
  if (!playerRef.value.contains(e.target)) {
    showPlaylist.value = false
  }
}

// 计算属性
const isMusicPlaying = computed(() =>
    player.isPlaying && player.audio && !!player.current
);
const currentIndex = computed(() => {
  if (!player.current || player.queue.length === 0) return -1
  return player.queue.findIndex(item => item.musicId === player.current.musicId)
})
const musicList = computed(() => player.queue)
const progress = computed(() => {
  if (duration.value <= 0) return 0
  return (currentTime.value / duration.value) * 100 || 0
})

// 播放控制方法
const toggleMusic = () => player.toggle();


const handlePrev = () => player.prev()
const handleNext = () => player.next()

const playSong = (index) => {
  if (index >= 0 && index < player.queue.length) {
    player.play(player.queue[index])
  }
}

// 进度条控制
const seekAudio = (e) => {
  if (!player.audio || duration.value <= 0) return

  const rect = e.currentTarget.getBoundingClientRect()
  const pos = (e.clientX - rect.left) / rect.width
  player.audio.currentTime = pos * duration.value
}

// 音量控制
const handleVolumeChange = () => {
  if (!player.audio) return

  player.audio.volume = volume.value
  isMuted.value = volume.value === 0
}

const togglePlaylist = () => {
  showPlaylist.value = !showPlaylist.value
}

// 时间格式化
const formatTime = (seconds) => {
  if (isNaN(seconds)) return "0:00"

  const minutes = Math.floor(seconds / 60)
  seconds = Math.floor(seconds % 60)
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}
</script>

<style scoped>
:root {
  --bg-color: #111; /* 主背景黑 */
  --text-color: #eee;
  --secondary-color: #999;
  --primary-color: #1db954; /* Spotify绿风格 */
  --hover-color: #222;
  --highlight-color: #2a2a2a;
}

#MusicControl {
  position: fixed;
  left: 0;
  bottom: 0;
  width: 100%;
  background: var(--bg-color);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.5);
  z-index: 999;
  color: var(--text-color);
  font-family: "Segoe UI", sans-serif;
}

.control-bar {
  display: flex;
  flex-direction: column;
  padding: 10px 20px;
}

.player-content {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.title {
  font-weight: bold;
  font-size: 1.1rem;
  color: var(--text-color);
}

.artist {
  font-size: 0.85rem;
  color: var(--secondary-color);
  margin-top: 2px;
}

.progress-area {
  flex: 1;
  max-width: 1000px;
}

.progress-container {
  position: relative;
  height: 8px;
  background: #444;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
}

.progress-bar {
  height: 100%;
  background: var(--primary-color);
  border-radius: 4px;
  transition: width 0.3s ease;
}

#progress-fill {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 14px;
  height: 14px;
  background: var(--primary-color);
  border-radius: 50%;
  z-index: 1;
}

.time-display {
  font-size: 0.75rem;
  color: var(--secondary-color);
  text-align: right;
  margin-top: 4px;
}

.main-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.2rem;
  color: var(--text-color);
  transition: transform 0.15s ease;
}

button:hover {
  transform: scale(1.1);
  color: var(--primary-color);
}

.play-btn {
  font-size: 1.8rem;
}

.volume-control {
  position: relative;
}

.volume-btn {
  font-size: 1.2rem;
}

.volume-slider {
  position: absolute;
  bottom: 36px;
  left: 50%;
  transform: translateX(-50%);
  background: #222;
  padding: 6px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
}

.vertical-slider {
  height: 90px;
  width: 4px;
  background: var(--primary-color);
  -webkit-appearance: slider-vertical;
  border-radius: 3px;
}

.playlist {
  position: absolute;
  bottom: 64px; /* 调整为显示在播放器上方，64px为播放器高度 */
  right: 20px;
  width: 300px; /* 控制宽度，适当变窄 */
  max-height: 360px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.3);
  z-index: 1000;
}

.playlist > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  margin-bottom: 6px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.playlist > div:hover {
  background: var(--hover-color);
}

.playlist > div.playing {
  background: skyblue;
  color: #fff;
}

.song-number {
  font-weight: bold;
  width: 24px;
  text-align: center;
  color: var(--primary-color);
}

.song-details {
  flex: 1;
  margin-left: 10px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.song-title {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-artist {
  font-size: 0.75rem;
  color: var(--secondary-color);
}

.song-duration {
  font-size: 0.75rem;
  color: var(--secondary-color);
  white-space: nowrap;
}

.slide-down-enter-active,
.slide-down-leave-active,
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to,
.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.info-and-cover {
  display: flex;
  align-items: center;
  gap: 16px;
}

.song-info {
  width: 300px; /* 固定宽度，可以根据实际需求调整 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: bold;
  color: var(--text-color);
}

.song-title, .song-artist {
  display: inline-block;
  max-width: 45%; /* 给歌名和歌手各自限定最大宽度 */
  vertical-align: middle;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.separator {
  margin: 0 5px;
  color: #aaa;
}

.title-line {
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-color);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* 封面图样式 */
.cover-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  transition: transform 0.5s linear;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

/* 旋转动画 */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.rotating {
  animation: spin 10s linear infinite;
}
</style>
