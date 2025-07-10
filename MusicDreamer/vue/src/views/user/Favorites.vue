<template>
  <div class="my-favorites-page">
    <!-- 标题区域 -->
    <div class="page-header">
      <h1 class="page-title">我的收藏</h1>
      <el-button v-if="filteredSonglists.length > 0"
                 type="primary"
                 @click="navigateToDiscover">
        发现更多歌单
      </el-button>
    </div>

    <!-- 收藏歌单列表 -->
    <div v-if="filteredSonglists.length > 0" class="songlist-grid">
      <div v-for="songlist in filteredSonglists"
           :key="songlist.id"
           class="songlist-card"
           @click="viewSongListDetail(songlist)">
        <!-- 歌单封面 -->
        <div class="card-cover">
          <el-image
              :src="songlist.imageUrl || defaultPlaylistCover"
              class="songlist-cover"
              fit="cover"
          />
          <div class="hover-overlay">
            <el-icon :size="40" color="#fff"><VideoPlay /></el-icon>
          </div>

          <!-- 收藏按钮 -->
          <div class="favorite-btn" @click.stop="toggleFavoriteForSonglist(songlist)">
            <Icon v-if="isSonglistFavorited(songlist.id)"
                  icon="mdi:star"
                  color="#ffc107"
                  class="favorite-icon favorited-icon"/>
          </div>
        </div>

        <!-- 歌单信息 -->
        <div class="card-info">
          <h3 class="songlist-name">{{ songlist.name }}</h3>
          <div class="songlist-meta">
            <span class="creator">
              <el-icon><User /></el-icon> {{ songlist.userName || '未知用户' }}
            </span>
            <span class="song-count">
              <el-icon><Headset /></el-icon> {{ songlist.musicCount || 0 }} 首
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 无收藏提示 -->
    <div v-else class="no-favorites">
      <el-empty description="您还没有收藏任何歌单">
        <el-button type="primary" @click="navigateToDiscover">去发现歌单</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { VideoPlay, User, Headset } from '@element-plus/icons-vue'
import { Icon } from '@iconify/vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import {
  userFavoritesMap,
  initUserFavorites,
  toggleFavorite,
  isSonglistFavorited
} from '@/utils/favoriteUtils'

const router = useRouter()
const defaultPlaylistCover = ref('/src/assets/imgs/default_playlist_cover.png')

// 所有歌单列表
const allSonglists = ref([])
// 加载状态
const loading = ref(false)

// 计算属性：过滤出用户收藏的歌单
const filteredSonglists = computed(() => {
  const favoriteIds = Object.keys(userFavoritesMap.value)
  return allSonglists.value.filter(
      songlist => favoriteIds.includes(songlist.id.toString())
  )
})

// 获取所有歌单
const fetchAllSonglists = async () => {
  loading.value = true
  try {
    const res = await request.get('/songlist/selectAll')

    if (res.code === '200') {
      // 处理歌单数据，添加歌曲数量
      allSonglists.value = res.data.map(songlist => ({
        ...songlist,
        musicCount: songlist.musics ? songlist.musics.length : 0
      }))
    } else {
      ElMessage.error('获取歌单列表失败: ' + res.msg)
      allSonglists.value = []
    }
  } catch (error) {
    console.error('获取歌单列表失败:', error)
    ElMessage.error('获取歌单列表失败，请重试')
    allSonglists.value = []
  } finally {
    loading.value = false
  }
}

// 查看歌单详情
const viewSongListDetail = (songlist) => {
  router.push(`/user/songLists/${songlist.id}`)
}

// 切换歌单收藏状态
const toggleFavoriteForSonglist = async (songlist) => {
  try {
    const success = await toggleFavorite(songlist.id)
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

// 跳转到发现歌单页面
const navigateToDiscover = () => {
  router.push('/user/songLists')
}

// 初始化
onMounted(async () => {
  await initUserFavorites()
  await fetchAllSonglists()
})
</script>

<style scoped lang="scss">
/* 样式保持不变 */
.my-favorites-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: #333;
  }
}

.songlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 25px;
}

.songlist-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);

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
  color: #333;
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
  color: #888;

  .creator, .song-count {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.no-favorites {
  text-align: center;
  padding: 50px 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
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
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;

  &:hover {
    transform: scale(1.1);
  }
}

.favorite-icon {
  font-size: 1.4rem;
}

.favorited-icon {
  color: #ffc107 !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;

    .el-button {
      align-self: flex-end;
    }
  }

  .songlist-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 15px;
  }
}
</style>