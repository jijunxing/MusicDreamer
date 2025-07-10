<template>
  <div class="user-home">
    <div class="carousel">
      <!-- 轮播图区域 -->
      <el-carousel
          ref="carouselRef"
          :interval="4000"
          type="card"
          height="200px"
          :autoplay="true"
      >
        <!-- 轮播项 -->
        <el-carousel-item
            v-for="item in carouselList"
            :key="item.id"
            class="carousel-item"
            @click="navigateToRelated(item)"
        >
          <!-- 图片展示 -->
          <div class="image-container">
            <img
                :src="item.imageUrl"
                :alt="item.title"
                class="carousel-image"
            />
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 侧边公告栏 -->
    <div class="announcement-sidebar" v-if="showAnnouncement">
      <div class="sidebar-header">
        <el-icon>
          <Bell/>
        </el-icon>
        <span>最新公告</span>
        <el-button
            class="close-btn"
            type="text"
            @click="showAnnouncement = false"
        >
          <el-icon>
            <Close/>
          </el-icon>
        </el-button>
      </div>

      <!-- 公告内容 -->
      <div v-if="latestNotice" class="notice-content" @click="goToNoticePage">
        <div class="notice-title">{{ latestNotice.title }}</div>
        <div class="notice-meta">
          <span>{{ formatDate(latestNotice.createTime) }}</span>
          <span>{{ latestNotice.userName }}</span>
        </div>
        <div class="notice-excerpt">{{ truncateText(latestNotice.msg, 60) }}</div>
        <div class="view-detail">点击查看详情
          <el-icon>
            <ArrowRight/>
          </el-icon>
        </div>
      </div>
    </div>

    <!-- 歌曲推荐栏目 -->
    <div class="recommend-section">
      <h2 class="section-title">
        推荐歌曲
        <span class="view-more" @click="navigateToSongs" style="margin-left: 10px">>>查看更多</span></h2>
      <div class="music-list">
        <div v-for="music in musicList" :key="music.musicId" class="music-item"
             @mouseenter="hoverStates[music.musicId] = true"
             @mouseleave="hoverStates[music.musicId] = false">
          <div class="music-main">
            <div class="cover-container-song" @click="playMusic(music)">
              <img :src="music.imageUrl" alt="歌曲封面" class="music-cover"/>
              <div class="play-overlay">
                <el-icon :size="36" color="#fff">
                  <VideoPlay/>
                </el-icon>
              </div>
              <div class="play-count">
                <el-icon>
                  <Headset/>
                </el-icon>
                {{ music.listenNumb || 0 }}
              </div>
            </div>
            <div class="music-info">
              <div class="music-name">{{ music.musicName }}</div>
              <div class="singer-name">{{ music.singerName }}</div>
              <div class="music-stats">
                <span class="tags">
                  <el-tag v-for="tag in music.tags" :key="tag.id" size="small" class="tag">
                    {{ tag.name }}
                  </el-tag>
                </span>
              </div>
            </div>
          </div>
          <transition name="fade">
            <el-button
                v-show="hoverStates[music.musicId]"
                class="add-queue-btn"
                type="link"
                size="small"
                @click.stop="addToQueue(music)"
            >
              <el-icon>
                <Plus/>
              </el-icon>
            </el-button>
          </transition>

          <div class="like-btn" @click.stop="toggleLikeForMusic(music)">
            <transition name="bounce">
              <Icon v-if="isMusicLiked(music.musicId)"
                    icon="mdi:heart"
                    color="#ff4081"
                    class="like-icon liked-icon"/>
            </transition>
            <Icon v-if="!isMusicLiked(music.musicId)"
                  icon="mdi:heart-outline"
                  class="like-icon"/>
          </div>
        </div>
      </div>
    </div>

    <!-- 歌单推荐栏目 -->
    <div class="recommend-section">
      <h2 class="section-title">
        推荐歌单
        <span class="view-more" @click="navigateToSongLists" style="margin-left: 10px">>>查看更多</span></h2>
      <div class="songlist-grid">
        <!-- 歌单项 -->
        <div v-for="songlist in songList" :key="songlist.id" class="songlist-item">
          <div class="cover-container-songList" @click="viewSongList(songlist)">
            <img :src="songlist.imageUrl" alt="歌单封面" class="songlist-cover"/>
            <div class="overlay">
              <div class="play-count">
                <el-icon>
                  <Headset/>
                </el-icon>
                {{ songlist.playCount || 0 }}
              </div>
              <div class="music-count">
                <el-icon>
                  <List/>
                </el-icon>
                {{ songlist.musicCount || 0 }}首
              </div>
            </div>
          </div>
          <!-- 优化后的歌单信息布局 -->
          <div class="songlist-info">
            <div class="songlist-name">{{ songlist.name }}</div>
            <div class="creator-info">
              <span class="creator">创建者: {{ songlist.userName }}</span>
            </div>
          </div>
          <!-- 收藏按钮 -->
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
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, reactive} from 'vue'
import request from '@/utils/request'
import {useRouter} from 'vue-router'
import {ElMessage} from "element-plus";
import {player} from "@/utils/player";
import {
  userLikesMap,
  initUserLikes,
  toggleLike,
  isMusicLiked
} from '@/utils/likeUtils'
import {
  initUserFavorites,
  toggleFavorite as toggleSonglistFavorite,
  isSonglistFavorited
} from '@/utils/favoriteUtils'
import {Icon} from "@iconify/vue";
import {Bell, Close, ArrowRight} from '@element-plus/icons-vue'

const router = useRouter()
const carouselList = ref([])
const musicList = ref([])
const songList = ref([])
const carouselData = reactive({
  type: '',
  id: null,
  music: {},
  singer: {},
  songList: {}
})
const hoverStates = ref({});
// 获取轮播图数据
const fetchCarouselData = async () => {
  request.get('/carousel/selectAll').then((res) => {
    if (res.code === "200") {
      carouselList.value = res.data || []
    } else {
      ElMessage.error('获取轮播图数据失败:' + res.msg)
    }
  })
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 导航到相关内容
const navigateToRelated = (item) => {
  if (item.relatedType === 'SONG') {
    request.get('music/selectById/' + item.relatedId).then(async res => {
      carouselData.music = res.data
      await player.play(carouselData.music)
      await router.push('/lyrics')
    })
  } else if (item.relatedType === 'SINGER') {
    router.push(`/user/singers/${item.relatedId}`)
  } else if (item.relatedType === 'SONGLIST') {
    router.push(`/user/songLists/${item.relatedId}`)
  } else
    ElMessage.warning('相关内容不存在')
}

// 获取推荐歌曲
const fetchMusicList = async () => {
  const response = await request.get('/music/selectTop?limit=12')

  if (response.code === "200") {
    musicList.value = response.data.map(music => ({
      ...music,
      tags: music.tags || []
    }))
  } else {
    ElMessage.error('获取推荐歌曲失败: ' + response.msg)
  }
}

// 获取推荐歌单
const fetchSongList = async () => {
  const response = await request.get('/songlist/selectPage', {
    params: {
      pageNum: 1,
      pageSize: 6,
    }
  })

  if (response.code === '200') {
    songList.value = response.data.list.map(list => {
      // 计算歌单总播放量
      const totalPlayCount = list.musics?.reduce((sum, music) =>
          sum + (music.listenNumb || 0), 0) || 0

      return {
        ...list,
        playCount: totalPlayCount || 0,
        userName: list.userName || '未知用户',
      }
    })
  } else {
    ElMessage.error('获取推荐歌单失败: ' + response.msg)
  }
}

// 播放歌曲
const playMusic = (music) => {
  if (music.activation === 0) {
    ElMessage.warning('该歌曲已被冻结，无法播放！')
    return
  }
  player.play(music)
}

// 添加到播放队列
const addToQueue = (music) => {
  if (music.activation === 0) {
    ElMessage.warning('该歌曲已被冻结，无法添加！')
    return
  }
  player.addToQueue(music)
  ElMessage.success(`"${music.musicName}" 已添加到播放队列`)
}

// 查看歌单详情
const viewSongList = (songlist) => {
  router.push({name: 'SongListDetail', params: {id: songlist.id}})
}

const navigateToSongs = () => {
  router.push('/user/songs') // 替换为实际路由名
}

const navigateToSongLists = () => {
  router.push('/user/songLists') // 替换为实际路由名
}

onMounted(async () => {
  await initUserLikes()
  await initUserFavorites()
  await fetchCarouselData()
  await fetchMusicList()
  await fetchSongList()
  await fetchLatestNotice();
})

// 添加点赞切换方法
const toggleLikeForMusic = async (music) => {
  if (!music.musicId) return

  try {
    const success = await toggleLike(music.musicId)
    if (success) {
      ElMessage.success(`已喜欢 "${music.musicName}"`)
    } else {
      ElMessage.info(`已取消喜欢 "${music.musicName}"`)
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

//歌单收藏切换
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

const showAnnouncement = ref(true);
const latestNotice = ref(null);

// 获取最新公告
const fetchLatestNotice = async () => {
  try {
    const response = await request.get('/notice/selectAll', {
      params: {
        pageNum: 1,
        pageSize: 1,
        sortField: 'create_time',
        sortOrder: 'desc'
      }
    });

    if (response.code === "200" && response.data.length > 0) {
      latestNotice.value = response.data[0];
    }
  } catch (error) {
    console.error('获取公告失败:', error);
  }
};

// 文字截断方法
const truncateText = (text, maxLength) => {
  if (!text) return '';
  return text.length > maxLength
      ? text.substring(0, maxLength) + '...'
      : text;
};

// 跳转到公告页面
const goToNoticePage = () => {
  router.push('/user/notices');
};

</script>

<style scoped>
.user-home {
  width: 100%;
  max-width: 1200px;
  margin: 50px auto;
  padding: 20px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(0.8);
}

.carousel-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  color: white;
}

.carousel-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 10px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.carousel-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  font-size: 14px;
  opacity: 0.9;
}

.recommend-section {
  margin-top: 40px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}


.section-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 25px;
  color: #333;
  display: flex;
  align-items: center;
}

.section-title::after {
  content: "";
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, #409eff, transparent);
  margin-left: 15px;
}

.view-more {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #409eff;
  font-size: 14px;
  transition: color 0.3s ease;
}

.view-more:hover {
  color: #66b1ff;
}

.view-more .el-icon {
  margin-left: 5px;
  font-size: 12px;
}

/* 歌曲推荐样式 */
.music-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.music-item {
  position: relative;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.music-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.music-main {
  display: flex;
  cursor: pointer;
  padding: 12px;
}

.cover-container-song {
  position: relative;
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.cover-container-song:hover img {
  transform: scale(1.05);
}

.music-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.cover-container-song:hover .play-overlay {
  opacity: 1;
}

.play-count {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  display: flex;
  align-items: center;
}

.play-count .el-icon {
  margin-right: 3px;
  font-size: 14px;
}

.music-item:hover {
  opacity: 1;
}


.music-info {
  flex: 1;
  padding: 0 15px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.music-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.singer-name {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.music-stats {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.music-stats .el-icon {
  margin-right: 4px;
}

.tags {
  margin-left: 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.tag {
  margin: 0 3px;
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
  border: none;
}

.add-queue-btn {
  position: absolute;
  top: 5px;
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
  transition: all 0.3s ease;
  opacity: 0; /* 默认隐藏 */
  visibility: hidden; /* 确保隐藏时不占用空间 */
}

.music-item:hover .add-queue-btn {
  opacity: 1; /* 悬停时显示 */
  visibility: visible; /* 确保显示 */
}

/* 淡入淡出动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter, .fade-leave-to {
  opacity: 0;
}

.like-btn {
  position: absolute;
  top: 40px;
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
  transition: transform 0.3s ease;
}

.like-icon {
  font-size: 1.4rem;
}

.liked-icon {
  color: #ff4081 !important;
}

.like-btn:hover {
  transform: scale(1.1);
  background: #fff;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.2);
}

/* 点赞动画 */
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

.songlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 25px;
}

.songlist-item {
  position: relative;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
}

.songlist-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.songlist-cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

.cover-container-songList {
  position: relative;
  flex-shrink: 0;
}

.overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  padding: 10px;
  display: flex;
  justify-content: space-between;
  color: #fff;
  font-size: 12px;
}

.play-count, .music-count {
  display: flex;
  align-items: center;
  gap: 4px;
}

.play-count .el-icon, .music-count .el-icon {
  font-size: 14px;
}

.songlist-info {
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.songlist-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 42px; /* 保持两行高度 */
}

.creator-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.creator {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.play-total .el-icon {
  color: #409eff;
}

/* 新增收藏按钮样式 */
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

/* 侧边公告栏 */
.announcement-sidebar {
  position: fixed;
  top: 100px;
  left: 20px;
  width: 240px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  z-index: 998;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
}

.sidebar-header {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: white;
  font-weight: 600;
  font-size: 16px;

  .el-icon {
    margin-right: 8px;
    font-size: 18px;
  }
}

.close-btn {
  margin-left: auto;
  color: rgba(255, 255, 255, 0.8);

  &:hover {
    color: white;
  }
}

.notice-content {
  padding: 16px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f5f7fa;

    .view-detail {
      color: #409eff;
    }
  }
}

.notice-title {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 8px;
  color: #303133;
  line-height: 1.4;
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}

.notice-excerpt {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

.view-detail {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #909399;
  transition: color 0.3s;

  .el-icon {
    margin-left: 4px;
    font-size: 12px;
  }
}

.no-notice {
  padding: 30px 20px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .announcement-sidebar {
    right: 10px;
    width: 260px;
  }
}
</style>