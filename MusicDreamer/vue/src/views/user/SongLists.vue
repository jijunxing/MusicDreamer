<template>
  <div class="songlist-page">
    <!-- 顶部筛选区域 -->
    <div class="songlist-header">
      <h1 class="page-title">发现歌单</h1>
      <div class="filter-area">
        <el-input
            v-model="searchKeyword"
            placeholder="搜索歌单..."
            clearable
            class="search-input"
            @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 歌单卡片网格 -->
    <div class="songlist-grid" v-loading="loading">
      <div
          v-for="songlist in filteredSonglists"
          :key="songlist.id"
          class="songlist-card"
          @click="viewSongListDetail(songlist)"
      >
        <div class="card-cover">
          <el-image
              :src="songlist.imageUrl || defaultPlaylistCover"
              class="songlist-cover"
              fit="cover"
          />
          <div class="hover-overlay">
            <el-icon :size="40" color="#fff"><VideoPlay /></el-icon>
          </div>
          <div class="favorite-btn" @click.stop="toggleFavoriteForSonglist(songlist)">
            <transition name="bounce">
              <Icon v-if="isSonglistFavorited(songlist.id)"
                    icon="mdi:star"
                    color="#ffc107"
                    class="favorite-icon favorited-icon"/>
            </transition>
            <Icon v-if="!isSonglistFavorited(songlist.id)"
                  icon="mdi:star-outline"
                  class="favorite-icon"/>
          </div>
        </div>
        <div class="card-info">
          <h3 class="songlist-name">{{ songlist.name }}</h3>
          <div class="songlist-meta">
            <span class="creator">
              <el-icon><User /></el-icon> {{ songlist.userName }}
            </span>
            <span class="song-count">
              <el-icon><Headset /></el-icon> {{ songlist.musicCount || 0 }} 首
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 无数据提示 -->
    <div v-if="!loading && filteredSonglists.length === 0" class="no-data">
      <el-empty description="未找到匹配的歌单" />
    </div>

    <!-- 分页控件 -->
    <div class="pagination" v-if="pagination.total > pagination.pageSize">
      <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[24, 48, 96]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, VideoPlay, Headset, User } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import {
  initUserFavorites,
  toggleFavorite as toggleSonglistFavorite,
  isSonglistFavorited
} from '@/utils/favoriteUtils'
import {Icon} from "@iconify/vue";

const router = useRouter()

// 默认图片
const defaultPlaylistCover = ref('/src/assets/imgs/default_playlist_cover.png')

// 搜索关键字
const searchKeyword = ref('')

// 歌单数据
const songlists = ref([])

// 分页参数
const pagination = ref({
  pageNum: 1,
  pageSize: 36,
  total: 0
})

// 加载状态
const loading = ref(false)

// 过滤后的歌单列表
const filteredSonglists = computed(() => {
  let result = songlists.value

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(songlist =>
        songlist.name.toLowerCase().includes(keyword) ||
        (songlist.userName && songlist.userName.toLowerCase().includes(keyword))
    )
  }

  return result
})

// 获取歌单数据
const fetchSonglists = async () => {
  loading.value = true
  try {
    const res = await request.get('/songlist/selectPage', {
      params: {
        name: searchKeyword.value || undefined,
        pageNum: pagination.value.pageNum,
        pageSize: pagination.value.pageSize
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

      songlists.value = processedSonglists
      pagination.value.total = res.data.total || 0
    } else {
      ElMessage.error('获取歌单列表失败: ' + res.msg)
    }
  } catch (err) {
    console.error(err)
    ElMessage.error('请求错误: ' + err.message)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.value.pageNum = 1
  fetchSonglists()
}

// 分页大小变化
const handleSizeChange = (newSize) => {
  pagination.value.pageSize = newSize
  fetchSonglists()
}

// 页码变化
const handlePageChange = (newPage) => {
  pagination.value.pageNum = newPage
  fetchSonglists()
}

// 查看歌单详情
const viewSongListDetail = (songlist) => {
  router.push(`/user/songLists/${songlist.id}`)
}

const toggleFavoriteForSonglist = async (songlist) => {
  if (!songlist.id) return

  try {
    const success = await toggleSonglistFavorite(songlist.id)
    if (success) {
      ElMessage.success(`已收藏歌单 "${songlist.name}"`)
    } else {
      ElMessage.info(`已取消收藏歌单 "${songlist.name}"`)
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


onMounted(() => {
  fetchSonglists()
  initUserFavorites()
})
</script>

<style scoped lang="scss">
.songlist-page {
  max-width: 1200px;
  margin: 60px auto;
  padding: 20px;
}

.songlist-header {
  margin-bottom: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #1db954, #1ed760);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 20px;
}

.filter-area {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
}

.search-input {
  width: 300px;

  :deep(.el-input__inner) {
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.1);
    border: none;
    transition: all 0.3s ease;

    &:focus {
      background: rgba(255, 255, 255, 0.15);
      box-shadow: 0 0 15px rgba(29, 185, 84, 0.2);
    }
  }
}

.songlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 25px;
  margin-bottom: 30px;
}

.songlist-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
    background: rgba(255, 255, 255, 0.08);

    .hover-overlay {
      opacity: 1;
    }

    .songlist-cover {
      transform: scale(1.05);
    }
  }
}

.card-cover {
  position: relative;
  height: 180px;
  overflow: hidden;

  .songlist-cover {
    width: 100%;
    height: 100%;
    transition: transform 0.5s ease;
  }
}

.hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.card-info {
  padding: 15px;
}

.songlist-name {
  font-size: 16px;
  font-weight: 600;
  color: #39393A;
  margin-bottom: 10px;
  height: 44px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.songlist-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #aaa;

  .creator, .song-count {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.no-data {
  padding: 40px 0;
  text-align: center;
  color: #888;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-area {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }

  .songlist-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 15px;
  }

  .card-cover {
    height: 150px;
  }

  .songlist-name {
    font-size: 14px;
    height: 40px;
  }
}

.favorite-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
  cursor: pointer;
  padding: 6px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.favorite-icon {
  font-size: 1.4rem;
}

.favorited-icon {
  color: #ffc107 !important;
}

.favorite-btn:hover {
  transform: scale(1.1);
  background: #fff;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.2);
}

/* 收藏动画 */
.bounce-enter-active {
  animation: bounce-in 0.5s;
}

@keyframes bounce-in {
  0% {
    transform: scale(0.5);
    opacity: 0;
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>