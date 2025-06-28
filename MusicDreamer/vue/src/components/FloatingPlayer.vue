<!-- src/components/FloatingPlayer.vue -->
<template>
  <div v-if="player.current" class="floating-player">
    <div class="info">
      🎵 正在播放：{{ player.current.musicName }}
    </div>
    <div class="controls">
      <el-button :icon="player.isPlaying ? VideoPause : VideoPlay" @click="player.toggle()">
        {{ player.isPlaying ? '暂停' : '播放' }}
      </el-button>
      <el-button type="info" @click="player.next">下一首</el-button>
    </div>
  </div>
</template>

<script setup>
import { player } from '@/utils/player.js'
import { VideoPlay, VideoPause } from '@element-plus/icons-vue'
</script>

<style scoped>
.floating-player {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
  background: white;
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 15px 20px;
  box-shadow: 0 0 10px rgba(0,0,0,0.1);
}
.info {
  font-weight: bold;
  margin-bottom: 10px;
}
.controls {
  display: flex;
  gap: 10px;
}
</style>
