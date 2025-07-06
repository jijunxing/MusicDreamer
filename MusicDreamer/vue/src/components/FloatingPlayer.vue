<template>
  <div id="MusicControl" ref="playerRef">
    <div class="control-bar">
      <div class="player-content">
        <!-- 歌曲信息 -->
        <!-- 歌曲信息 + 封面 -->
        <div class="info-and-cover" @click="goToLyricsPage">
          <div class="song-info-vertical">
            <div class="song-title-vertical">{{ currentMusic.musicName || '歌名'}}</div>
            <div class="song-artist-vertical">{{ currentMusic.singerName || '歌手' }}</div>
          </div>
          <div class="cover-wrapper" :class="{ rotating: isMusicPlaying }">
            <img :src="coverUrl" @error="handleCoverError" alt="封面" class="cover-image" />
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
          <button class="mode-btn" @click="togglePlayMode" :title="playModeTitle">
            <Icon :icon="playModeIcon" />
          </button>
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
          <button class="favorite-btn" @click="toggleFavorite" :title="isFavorite ? '取消喜欢' : '喜欢'">
            <Icon :icon="isFavorite ? 'mdi:heart' : 'mdi:heart-outline'" :color="isFavorite ? '#ff4081' : ''" />
          </button>
          <!-- 音量控制 -->
          <div class="volume-control" @mouseenter="showVolume = true" @mouseleave="showVolume = false">
            <button class="volume-btn" :title="isMuted ? '取消静音' : '静音'" @click="toggleMute">
              <Icon :icon="isMuted ? 'mdi:volume-off' : 'mdi:volume-high'" />
            </button>
            <transition name="slide-down">
              <div class="volume-slider" v-show="showVolume">
                <input type="range" min="0" max="1" step="0.01" v-model.number="volume" @input="handleVolumeChange"
                       class="vertical-slider" :style="{'--volume-percent': volume * 100 + '%'}" />
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
              <div class="song-title-list">{{ song.musicName }}</div>
              <div class="song-artist-list">{{ song.singerName || '未知艺术家' }}</div>
            </div>
            <div class="song-duration">{{ Math.floor(song.timelength/60)}}:{{song.timelength%60}}</div>
            <div class="delete-btn" @click.stop="removeSong(index)" title="删除">
              <Icon icon="mdi:delete" />
            </div>
          </div>
        </div>
      </transition>
    </div>

    <audio ref="bgMusic" crossorigin="anonymous" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { player } from "@/utils/player"
import { Icon } from '@iconify/vue'
import defaultCover from "@/assets/imgs/default_cover.svg";
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

const handleLoadedMetadata = () => {
  if (player.audio) {
    const audioDuration = player.audio.duration;
    if (audioDuration > 0 && !isNaN(audioDuration) && isFinite(audioDuration)) {
      duration.value = audioDuration;
    } else {
      // 从音乐信息中获取时长（假设单位为秒）
      duration.value = currentMusic.value.timelength || 0;
    }
  }
}

// 状态同步函数
const syncState = () => {
  if (!player.audio) return;

  const newTime = player.audio.currentTime;
  if (Math.abs(newTime - currentTime.value) > 0.05) { // 避免无效更新
    currentTime.value = newTime;
  }

  const audioDuration = player.audio.duration;
  if (audioDuration > 0 && !isNaN(audioDuration) && isFinite(audioDuration)) {
    duration.value = audioDuration;
  } else if (currentMusic.value.timelength) {
    duration.value = currentMusic.value.timelength;
  }
};


// 注册状态更新监听器
onMounted(() => {
  player.setAudioElement(bgMusic.value)

  // 注册状态更新监听器
  player.listeners.add(syncState)

  // 初始同步状态
  syncState()

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
  // 检查音频是否就绪
  const isAudioReady = player.audio &&
      player.audio.readyState >= 2 &&
      duration.value > 0;

  if (!isAudioReady) {
    console.warn("音频未就绪，无法跳转");
    return;
  }

  // 获取事件位置（支持鼠标和触摸事件）
  let clientX;
  if (e.type === 'touchstart' || e.type === 'touchmove') {
    clientX = e.touches[0].clientX;
  } else {
    clientX = e.clientX;
  }

  // 计算跳转位置
  const rect = e.currentTarget.getBoundingClientRect();
  const pos = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width));
  const targetTime = pos * duration.value;

  console.log(`尝试跳转至 ${targetTime.toFixed(2)}s (${(pos * 100).toFixed(1)}%)`);

  // 使用防抖技术，避免短时间内多次跳转
  if (seekAudio.lastSeek && Date.now() - seekAudio.lastSeek < 100) {
    console.log("防抖：忽略频繁跳转请求");
    return;
  }
  seekAudio.lastSeek = Date.now();

  try {
    // 执行跳转
    player.audio.currentTime = targetTime;

    // 触发跳转成功事件
    if (typeof player.onSeekSuccess === 'function') {
      player.onSeekSuccess(targetTime);
    }

    console.log(`跳转成功：${targetTime.toFixed(2)}s`);
  } catch (err) {
    console.error(`跳转失败 (${targetTime.toFixed(2)}s):`, err);

    // // 触发跳转失败事件
    // if (typeof player.onSeekError === 'function') {
    //   player.onSeekError(targetTime, err);
    // }
  }
};

// 初始化防抖时间戳
seekAudio.lastSeek = 0;

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

watch(() => player.current, () => {
  if (player.current && player.audio) {
    // 当播放歌曲切换时，重置进度
    currentTime.value = 0;
    // 重新加载元数据
    handleLoadedMetadata();
  }
})

// 创建响应式封面URL
const coverUrl = ref('')

// 监听currentMusic变化，更新封面URL
watch(() => player.current, () => {
  if (player.current) {
    coverUrl.value = player.current.imageUrl || defaultCover
  } else {
    coverUrl.value = defaultCover
  }
}, { immediate: true })

const handleCoverError = () => {
  coverUrl.value = defaultCover
}

const previousVolume = ref(0.5);

const toggleMute = () => {
  if (!player.audio) return;

  if (!isMuted.value) {
    // 记录当前音量
    previousVolume.value = volume.value;
    volume.value = 0;
    player.audio.volume = 0;
  } else {
    // 恢复音量
    volume.value = previousVolume.value || 0.5;
    player.audio.volume = volume.value;
  }

  isMuted.value = volume.value === 0;
};

// 播放模式切换
const togglePlayMode = () => player.togglePlayMode();

// 播放模式图标和标题
const playModeIcon = computed(() => {
  switch (player.playMode) {
    case 'loop': return 'mdi:repeat-once'
    case 'list-loop': return 'mdi:repeat'
    case 'random': return 'mdi:shuffle'
    case 'order':
    default:
      return 'mdi:playlist-play'
  }
})

const playModeTitle = computed(() => {
  switch (player.playMode) {
    case 'loop': return '单曲循环'
    case 'list-loop': return '列表循环'
    case 'random': return '随机播放'
    case 'order':
    default:
      return '顺序播放'
  }
})

// 爱心按钮功能（暂时只实现UI）
const isFavorite = ref(false)
const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value
  // 后续可在此处添加喜欢功能的实现
}

const removeSong = async (index) => {
  // 如果删除的是当前正在播放的歌曲
  if (currentIndex.value === index) {
    // 停止播放
    player.audio.pause();
    player.isPlaying = false;
    player.current = null;
  }
  // 从播放列表中删除该歌曲
  player.queue.splice(index, 1);
};

const router = useRouter()
// 跳转到歌词页
const goToLyricsPage = () => {
  if (!player.current) return
  router.push({
    name: 'LyricsPage',
  })
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
  --volume-percent: 0%;
}

#MusicControl {
  position: fixed;
  left: 0;
  bottom: 0;
  width: 100%;
  background: var(--bg-color);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.5);
  z-index: 1001;
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
  background: skyblue;
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
  padding: 10px; /* 增加内边距 */
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
  z-index: 1001; /* 确保覆盖层级 */
}

.vertical-slider {
  writing-mode: vertical-lr;
  direction: rtl;
  height: 90px;
  width: 10px; /* 增加宽度提高可见性 */
  -webkit-appearance: none;
  appearance: none;
  background: #444; /* 默认轨道背景色 */
  border-radius: 4px;
  outline: none;
  cursor: pointer;
}

/* 使用伪元素创建可视线性轨道 */
.vertical-slider::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  height: var(--volume-percent, 0%);
  width: 100%;
  background: #00ffff;
  border-radius: 4px 4px 0 0;
  pointer-events: none; /* 避免干扰滑块操作 */
}


/* Chrome / Safari / Edge */
.vertical-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  height: 16px; /* 增大滑块 */
  width: 16px;
  background: white;
  border: 1px solid var(--primary-color);
  border-radius: 50%;
  cursor: pointer;
  position: relative; /* 确保z-index生效 */
  z-index: 2; /* 位于轨道上方 */
}

/* Firefox */
.vertical-slider::-moz-range-thumb {
  height: 16px;
  width: 16px;
  background: white;
  border: 1px solid var(--primary-color);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  z-index: 2;
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

.song-title-list {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-artist-list {
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

/* 新增的垂直布局样式 */
.song-info-vertical {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 150px; /* 确保足够空间展示完整信息 */
}

.song-title-vertical {
  font-size: 1.3rem; /* 增大歌名字体 */
  font-weight: 600; /* 加粗歌名 */
  line-height: 1.3;
  color: var(--text-color);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px; /* 限制宽度避免溢出 */
}

.song-artist-vertical {
  font-size: 0.95rem; /* 歌手字体略小 */
  color: var(--secondary-color);
  line-height: 1.4;
  margin-top: 4px; /* 与歌名保持间距 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 调整整体布局 */
.info-and-cover {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 280px; /* 确保有足够空间展示信息 */
  cursor: pointer;
  transition: transform 0.3s;
}
.info-and-cover:hover {
  transform: scale(1.02);
}

.song-title, .song-artist {
  display: inline-block;
  max-width: 45%; /* 给歌名和歌手各自限定最大宽度 */
  vertical-align: middle;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.song-title:hover,
.song-artist:hover {
  color: #4fc3f7;
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
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  transition: box-shadow 0.3s;
}
.cover:hover {
  box-shadow: 0 4px 12px rgba(79, 195, 247, 0.4);
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
  animation: spin 16s linear infinite;
}

.mode-btn, .favorite-btn {
  font-size: 1.2rem;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-color);
  transition: transform 0.15s ease, color 0.3s ease;
}

.mode-btn:hover, .favorite-btn:hover {
  transform: scale(1.1);
  color: var(--primary-color);
}

.favorite-btn .iconify[data-icon="mdi:heart"] {
  color: #ff4081; /* 爱心填充时的颜色 */
}

/* 调整控制按钮间距 */
.main-controls {
  display: flex;
  align-items: center;
  gap: 14px; /* 稍微增加间距 */
}
</style>
