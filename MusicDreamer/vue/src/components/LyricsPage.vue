<template>
  <div class="lyrics-page" :style="backgroundStyle">
    <!-- 返回按钮 -->
    <div class="back-button" @click="goBack">
      <Icon icon="mdi:arrow-left" />
    </div>

    <div class="lyrics-container">
      <!-- 左侧：旋转封面和歌曲信息 -->
      <div class="cover-section">
        <div class="rotating-cover" :class="{ rotating: isPlaying }">
          <img :src="currentSong.imageUrl || defaultCover" alt="专辑封面" class="cover-image" />
        </div>
        <div class="song-info">
          <h1 class="song-title">{{ currentSong.musicName || '未知歌曲' }}</h1>
          <div class="song-artist">{{ currentSong.singerName || '未知歌手' }}</div>
          <div class="play-controls">
            <button @click="togglePlay" class="play-btn">
              <Icon :icon="isPlaying ? 'mdi:pause' : 'mdi:play'" />
            </button>
            <div class="time-display">
              {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：歌词显示区域 -->
      <div class="lyrics-section" ref="lyricsContainer">
        <div v-if="parsedLyrics.length === 0" class="no-lyrics">
          <Icon icon="mdi:music-off" />
          <p>暂无歌词</p>
        </div>
        <div class="lyrics-display" :style="{ transform: `translateY(${scrollOffset}px)` }">
          <div
              v-for="(line, index) in parsedLyrics"
              :key="index"
              class="lyric-line"
              :class="{ active: currentLyricIndex === index }"
              @click="seekToTime(line.time)"
          >
            {{ line.text }}
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { player } from '@/utils/player'
import defaultCover from '@/assets/default-cover.jpg'

const router = useRouter()
const lyricsContainer = ref(null)
const scrollOffset = ref(0)
const currentLyricIndex = ref(0)
const currentTime = computed(() => player.currentTime || 0)
const duration = ref(0)
const lineHeight = ref(60)

const currentSong = computed(() => player.current || {
  musicName: '未知歌曲',
  singerName: '未知歌手',
  imageUrl: defaultCover,
  lyricUrl: ''
})

const isPlaying = computed(() => player.isPlaying)

const backgroundStyle = computed(() => ({
  background: `linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)`,
  backgroundImage: currentSong.value.imageUrl
      ? `url(${currentSong.value.imageUrl})`
      : 'none',
  backgroundSize: 'cover',
  backgroundBlendMode: 'overlay'
}))

// 修正：直接解析lyricUrl中的歌词内容
const parseLyric = (lrcText) => {
  if (!lrcText) return [];

  // 处理歌词中的换行符和空格
  const lines = lrcText
      .replace(/\\n/g, '\n')   // 转换转义字符为实际换行符
      .split('\n')             // 按行分割
      .map(line => line.trim()) // 去除每行首尾空格
      .filter(line => line);    // 过滤空行

  const lyrics = [];

  lines.forEach(line => {
    // 匹配时间标签 [mm:ss.xx]
    const timeMatch = line.match(/\[(\d{2}):(\d{2})\.(\d{2,3})\]/);
    if (timeMatch) {
      const minutes = parseInt(timeMatch[1], 10);
      const seconds = parseInt(timeMatch[2], 10);
      const milliseconds = parseInt(timeMatch[3], 10) / 1000;
      const timeInSeconds = minutes * 60 + seconds + milliseconds;

      // 获取歌词文本（移除时间标签）
      const text = line.replace(timeMatch[0], '').trim();

      if (text) {
        lyrics.push({time: timeInSeconds, text});
      }
    }
  });

  return lyrics.sort((a, b) => a.time - b.time);
}

// 解析歌词数据 - 直接使用lyricUrl内容
const parsedLyrics = computed(() => {
  return parseLyric(currentSong.value.lyricUrl);
});

// 同步测量行高
const measureLineHeight = () => {
  nextTick(() => {
    const lines = lyricsContainer.value?.querySelectorAll('.lyric-line');
    if (lines && lines.length > 0) {
      // 不再需要固定行高，使用实际DOM测量
      lineHeight.value = lines[0].offsetHeight;
    } else {
      lineHeight.value = 60;
    }
  });
};

// 滚动到当前歌词位置
function scrollToLyric(index) {
  if (!lyricsContainer.value || !parsedLyrics.value.length) return;

  const container = lyricsContainer.value;
  const containerHeight = container.clientHeight;
  const lyricLines = container.querySelectorAll('.lyric-line');

  if (index >= lyricLines.length) return;

  // 获取当前歌词行的位置信息
  const currentLine = lyricLines[index];
  const currentLineHeight = currentLine.offsetHeight;
  const currentLineTop = currentLine.offsetTop;

  // ✅ 精准居中公式（修复坐标系问题）
  const targetOffset = currentLineTop - (containerHeight / 2) + (currentLineHeight / 2);

  // ✅ 动态边界保护（支持短歌词）
  const maxScroll = container.scrollHeight - containerHeight;
  const finalOffset = Math.max(0, Math.min(maxScroll, targetOffset));

  // ✅ 应用平滑滚动
  container.scrollTo({
    top: finalOffset,
    behavior: 'smooth'
  });
}

// 点击歌词跳转到对应时间
function seekToTime(time) {
  if (player.audio) {
    player.audio.currentTime = time
    if (!player.isPlaying) {
      player.play()
    }
  }
}

// 播放/暂停控制
function togglePlay() {
  player.toggle()
}

// 格式化时间显示
function formatTime(seconds) {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 返回播放器页面
function goBack() {
  router.go(-1)
}

// 组件挂载时初始化
onMounted(() => {
  player.listeners.add(updateLyricsState)  // 注册歌词更新函数
})

// 组件卸载时
onUnmounted(() => {
  player.listeners.delete(updateLyricsState) // 卸载时移除
})

const updateLyricsState = () => {
  if (!player.audio) return
  duration.value = player.audio.duration
  // 歌词滚动逻辑（原有代码）
  const time = player.audio.currentTime
  if (!parsedLyrics.value.length) return
  let low = 0, high = parsedLyrics.value.length - 1
  let index = -1

  while (low <= high) {
    const mid = Math.floor((low + high) / 2)
    const midTime = parsedLyrics.value[mid].time

    if (midTime <= time) {
      index = mid
      low = mid + 1
    } else {
      high = mid - 1
    }
  }
  if (index !== -1 && currentLyricIndex.value !== index) {
    currentLyricIndex.value = index
    scrollToLyric(index)
  }
}

watch(parsedLyrics, (newVal) => {
  if (newVal.length) {
    nextTick(measureLineHeight); // ✅ DOM更新后重新测量
  }
}, { immediate: true });
</script>

<style scoped>
.lyrics-page {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  color: #e6e6e6;
  font-family: 'Segoe UI', 'PingFang SC', sans-serif;
  z-index: 1000;
  overflow: hidden;
  transition: background 0.5s ease;
}

.back-button {
  position: absolute;
  top: 20px;
  left: 20px;
  font-size: 1.8rem;
  color: white;
  cursor: pointer;
  z-index: 1001;
  transition: transform 0.2s;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-button:hover {
  transform: scale(1.1);
  background: rgba(79, 195, 247, 0.3);
}

.lyrics-container {
  display: flex;
  height: 100vh;
  padding: 60px 20px 20px;
  box-sizing: border-box;
  backdrop-filter: blur(10px);
}

/* 左侧封面区域 */
.cover-section {
  flex: 0 0 35%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.rotating-cover {
  width: 280px;
  height: 280px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  margin-bottom: 30px;
  position: relative;
  transition: transform 0.3s ease;
  border: 8px solid rgba(255, 255, 255, 0.1);
}

.rotating-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.rotating {
  animation: rotation 20s linear infinite;
}

@keyframes rotation {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.song-info {
  text-align: center;
  width: 100%;
  max-width: 300px;
}

.song-title {
  font-size: 2.2rem;
  font-weight: 700;
  margin-bottom: 10px;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.7);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-artist {
  font-size: 1.4rem;
  color: #a0a0b0;
  margin-bottom: 25px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.play-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 20px;
}

.play-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(79, 195, 247, 0.7);
  border: none;
  color: white;
  font-size: 1.8rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.play-btn:hover {
  background: rgba(79, 195, 247, 1);
  transform: scale(1.05);
}

.time-display {
  margin-top: 15px;
  font-size: 1.1rem;
  color: #ccc;
}

/* 右侧歌词区域 */
.lyrics-section {
  flex: 1;
  overflow-y: auto; /* 改为可滚动容器 */
  padding: 40px 60px;
  position: relative;
  scroll-behavior: smooth; /* 添加平滑滚动效果 */
  /* 隐藏滚动条 */
  scrollbar-width: none; /* Firefox */
}
.lyrics-section::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Edge */
}

.lyrics-display {
  position: relative;
  left: 0;
  width: 100%;
  padding: 20px 0;
  transition: none; /* 移除transform过渡 */
}

.lyric-line {
  font-size: 1.8rem;
  line-height: 1.6;
  text-align: center;
  transition: all 0.4s ease;
  opacity: 0.4;
  transform: scale(0.95);
  padding: 15px 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  cursor: pointer;
  min-height: 60px; /* 设置最小高度防止抖动 */
  box-sizing: border-box;
}

.lyric-line:hover {
  opacity: 0.7;
}

.lyric-line.active {
  font-size: 2.3rem;
  font-weight: 600;
  color: #4fc3f7;
  text-shadow: 0 0 15px rgba(79, 195, 247, 0.8);
  opacity: 1;
  transform: scale(1.05);
  //background: linear-gradient(to right, transparent, rgba(79, 195, 247, 0.15), transparent);
}

/* 响应式设计 */
@media (max-width: 900px) {
  .lyrics-container {
    flex-direction: column;
    padding-top: 80px;
  }

  .cover-section {
    flex: none;
    margin-bottom: 30px;
  }

  .rotating-cover {
    width: 200px;
    height: 200px;
  }

  .lyrics-section {
    padding: 0 20px;
    flex: 1;
  }

  .lyric-line {
    font-size: 1.5rem;
    padding: 12px 0;
  }

  .lyric-line.active {
    font-size: 1.8rem;
  }
}

@media (max-width: 480px) {
  .rotating-cover {
    width: 150px;
    height: 150px;
  }

  .song-title {
    font-size: 1.8rem;
  }

  .lyric-line {
    font-size: 1.3rem;
  }

  .lyric-line.active {
    font-size: 1.6rem;
  }
}

.lyrics-display {
  transition: transform 0.5s cubic-bezier(0.23, 1, 0.32, 1);
  will-change: transform; /* 提升滚动性能 */
}

/* 无歌词提示 */
.no-lyrics {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #a0a0b0;
  font-size: 1.5rem;
  text-align: center;
}
</style>