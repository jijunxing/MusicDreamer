<template>
  <div class="search-page">
    <!-- 结果分类标签 -->
    <el-tabs v-model="activeTab" class="search-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="歌曲" name="music">
        <div v-loading="loading">
          <div v-if="musicResults.length" class="result-container">
            <div
                v-for="music in musicResults"
                :key="music.musicId"
                class="music-item"
                @click="playMusic(music)"
            >
              <el-image
                  :src="music.imageUrl || defaultCover"
                  class="cover"
                  fit="cover"
              />
              <div class="music-info">
                <div class="title">{{ music.musicName }}</div>
                <div class="singer">{{ music.singerName }}</div>
                <div class="meta">
                  <span><el-icon><Headset /></el-icon> {{ music.listenNumb || 0 }}次</span>
                  <span>{{ formatTime(music.timelength) }}</span>
                </div>
              </div>
              <el-button type="primary" circle @click.stop="playMusic(music)">
                <el-icon><VideoPlay /></el-icon>
              </el-button>
            </div>
          </div>
          <el-empty v-else description="未找到相关歌曲" />
        </div>

        <!-- 歌曲分页 -->
        <el-pagination
            v-if="musicPagination.total > 0"
            v-model:current-page="musicPagination.pageNum"
            v-model:page-size="musicPagination.pageSize"
            :total="musicPagination.total"
            layout="prev, pager, next, jumper"
            @current-change="handlePageChange('music')"
            class="pagination"
        />
      </el-tab-pane>

      <el-tab-pane label="歌手" name="singer">
        <div v-loading="loading">
          <div v-if="singerResults.length" class="singer-grid">
            <div
                v-for="singer in singerResults"
                :key="singer.id"
                class="singer-card"
                @click="viewSinger(singer)"
            >
              <el-avatar :src="singer.avatar || defaultAvatar" :size="100" />
              <div class="singer-name">{{ singer.username }}</div>
              <div class="song-count">{{ singer.songCount }} 首单曲</div>
            </div>
          </div>
          <el-empty v-else description="未找到相关歌手" />
        </div>

        <!-- 歌手分页 -->
        <el-pagination
            v-if="singerPagination.total > 0"
            v-model:current-page="singerPagination.pageNum"
            v-model:page-size="singerPagination.pageSize"
            :total="singerPagination.total"
            layout="prev, pager, next, jumper"
            @current-change="handlePageChange('singer')"
            class="pagination"
        />
      </el-tab-pane>

      <el-tab-pane label="歌单" name="songlist">
        <div v-loading="loading">
          <div v-if="songlistResults.length" class="songlist-grid">
            <div
                v-for="songlist in songlistResults"
                :key="songlist.id"
                class="songlist-card"
                @click="viewSongList(songlist)"
            >
              <el-image
                  :src="songlist.imageUrl || defaultPlaylistCover"
                  class="songlist-cover"
                  fit="cover"
              />
              <div class="songlist-info">
                <div class="songlist-name">{{ songlist.name }}</div>
                <div class="songlist-creator">创建者: {{ songlist.userName }}</div>
                <div class="songlist-meta">
                  <span>{{ songlist.musicCount || 0 }} 首歌曲</span>
                  <span>{{ formatDate(songlist.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="未找到相关歌单" />
        </div>

        <!-- 歌单分页 -->
        <el-pagination
            v-if="songlistPagination.total > 0"
            v-model:current-page="songlistPagination.pageNum"
            v-model:page-size="songlistPagination.pageSize"
            :total="songlistPagination.total"
            layout="prev, pager, next, jumper"
            @current-change="handlePageChange('songlist')"
            class="pagination"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, VideoPlay, Headset } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import {player} from "@/utils/player";

const router = useRouter()

// 默认图片
const defaultCover = ref('/src/assets/imgs/default_music_cover.png')
const defaultAvatar = ref('/src/assets/imgs/default_singer_avatar.png')
const defaultPlaylistCover = ref('/src/assets/imgs/default_playlist_cover.png')

// 搜索参数
const keyword = ref('')
const activeTab = ref('music')
const loading = ref(false)

// 搜索结果
const musicResults = ref([])
const singerResults = ref([])
const songlistResults = ref([])

// 分页配置（歌曲和歌手独立）
const musicPagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const singerPagination = ref({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

const songlistPagination = ref({
  pageNum: 1,
  pageSize: 12,
  total: 0
})
// 防抖定时器
let searchTimer = null

// 执行搜索
const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    // 重置分页到第一页
    musicPagination.value.pageNum = 1
    singerPagination.value.pageNum = 1

    // 根据当前标签执行搜索
    if (keyword.value.trim()) {
      if (activeTab.value === 'music') {
        searchMusic()
      } else if (activeTab.value === 'singer') {
        searchSingers()
      } else if (activeTab.value === 'songlist') { // 歌单搜索
        searchSongLists()
      }
    }
  }, 300)
}

// 重置搜索
const resetSearch = () => {
  musicResults.value = []
  singerResults.value = []
  songlistResults.value = []
}

// 切换标签处理
const handleTabChange = () => {
  if (keyword.value.trim()) {
    handleSearch()
  }
}

// 搜索歌曲
const searchMusic = async () => {
  loading.value = true
  try {
    const res = await request.get('/music/selectPage', {
      params: {
        musicName: keyword.value,
        singerName: keyword.value,
        pageNum: musicPagination.value.pageNum,
        pageSize: musicPagination.value.pageSize
      }
    })

    if (res.code === '200') {
      musicResults.value = res.data.list || []
      musicPagination.value.total = res.data.total || 0
    } else {
      ElMessage.error('歌曲搜索失败: ' + res.msg)
    }
  } catch (err) {
    console.error('歌曲搜索错误:', err)
    ElMessage.error('搜索请求失败')
  } finally {
    loading.value = false
  }
}

// 搜索歌手
const searchSingers = async () => {
  loading.value = true
  try {
    // 第一步：获取歌手列表
    const singerRes = await request.get('/user/selectPage', {
      params: {
        username: keyword.value,
        role: 'SINGER',
        pageNum: singerPagination.value.pageNum,
        pageSize: singerPagination.value.pageSize
      }
    })

    if (singerRes.code !== '200') {
      ElMessage.error('歌手搜索失败: ' + singerRes.msg)
      return
    }

    const singerList = singerRes.data.list || []
    singerPagination.value.total = singerRes.data.total || 0

    // 第二步：并行获取每位歌手的单曲数量
    const singersWithSongCount = await Promise.all(
        singerList.map(async (singer) => {
          try {
            const musicRes = await request.get('/music/selectAll', {
              params: { singerName: singer.username }
            })
            const songCount = musicRes.code === '200' ? musicRes.data.length : 0
            return { ...singer, songCount }
          } catch (err) {
            console.error('获取歌手歌曲失败:', err)
            return { ...singer, songCount: 0 }
          }
        })
    )

    singerResults.value = singersWithSongCount
  } catch (err) {
    console.error('歌手搜索错误:', err)
    ElMessage.error('搜索请求失败')
  } finally {
    loading.value = false
  }
}

// 搜索歌单
const searchSongLists = async () => {
  loading.value = true
  try {
    const res = await request.get('/songlist/selectPage', {
      params: {
        name: keyword.value,
        pageNum: songlistPagination.value.pageNum,
        pageSize: songlistPagination.value.pageSize
      }
    })

    if (res.code === '200') {
      // 处理歌单数据，添加歌曲数量
      const processedSonglists = res.data.list.map(songlist => {
        return {
          ...songlist,
          musicCount: songlist.musics ? songlist.musics.length : 0
        }
      })

      songlistResults.value = processedSonglists
      songlistPagination.value.total = res.data.total || 0
    } else {
      ElMessage.error('歌单搜索失败: ' + res.msg)
    }
  } catch (err) {
    console.error('歌单搜索错误:', err)
    ElMessage.error('歌单搜索请求失败')
  } finally {
    loading.value = false
  }
}

// 分页处理
const handlePageChange = (type) => {
  if (keyword.value.trim()) {
    if (type === 'music') {
      searchMusic()
    } else {
      searchSingers()
    }
  }
}

// 播放音乐
const playMusic = (music) => {
  player.play(music)
}

// 查看歌手详情
const viewSinger = (singer) => {
  router.push({ name: 'SingerDetail', params: { id: singer.id } })
}

const viewSongList = (songlist) => {
  router.push({ name: 'SongListDetail', params: { id: songlist.id } })
}

// 格式化时间 (秒 -> mm:ss)
const formatTime = (seconds) => {
  if (!seconds) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 初始化时如果有URL参数
onMounted(() => {
  const route = useRouter()
  if (route.currentRoute.value.query.keyword) {
    keyword.value = decodeURIComponent(route.currentRoute.value.query.keyword)
    handleSearch()
  }
})
</script>

<style scoped lang="scss">
.search-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}

.search-tabs {
  margin-top: 60px;
  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }

  :deep(.el-tabs__nav-wrap)::after {
    height: 1px;
  }
}

.result-container {
  display: grid;
  gap: 15px;
}

.music-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.03);
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .cover {
    width: 80px;
    height: 80px;
    border-radius: 6px;
    margin-right: 15px;
  }

  .music-info {
    flex: 1;

    .title {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 5px;
    }

    .singer {
      font-size: 14px;
      color: #aaa;
      margin-bottom: 8px;
    }

    .meta {
      display: flex;
      gap: 15px;
      font-size: 13px;
      color: #888;

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
}

.singer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 25px;
}

.singer-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.03);
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  }

  .singer-name {
    font-size: 16px;
    font-weight: 600;
    margin-top: 12px;
    margin-bottom: 8px;
  }

  .song-count {
    font-size: 14px;
    color: #409eff;
    background: rgba(64, 158, 255, 0.1);
    padding: 4px 12px;
    border-radius: 12px;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-header {
    flex-direction: column;

    .el-input, .el-button {
      width: 100%;
    }
  }

  .singer-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 15px;
  }

  .music-item {
    .cover {
      width: 60px;
      height: 60px;
    }

    .music-info .title {
      font-size: 16px;
    }
  }
}
</style>