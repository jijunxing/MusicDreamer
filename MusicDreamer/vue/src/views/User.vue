<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <header class="main-header">
      <div class="header-left">
        <div class="logo-container">
          <el-icon :size="32" color="#ff9a3c"><Headset /></el-icon>
          <h1 class="logo-text">MusicDreamer</h1>
        </div>

        <!-- 主导航菜单 -->
        <el-menu
            router
            mode="horizontal"
            background-color="transparent"
            text-color="#e0e0e0"
            active-text-color="#ff9a3c"
            :default-active="$route.path"
            :default-openeds="['/home', '2']"
        >
          <el-menu-item index="/user/home">首页</el-menu-item>
          <el-menu-item index="/user/singers">歌手</el-menu-item>
          <el-menu-item index="/user/songs">歌曲</el-menu-item>
          <el-menu-item index="/user/SongLists">歌单</el-menu-item>
          <el-menu-item index="/user/creatorCenter" class="creator-center">
              <el-icon><EditPen /></el-icon>
              <span>创作者中心</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="header-right">
        <!-- 搜索框 -->
        <div class="search-container">
          <el-input
              v-model="searchText"
              placeholder="搜索歌曲、歌手..."
              clearable
              @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 用户区域 -->
        <div class="user-area">
          <el-dropdown v-if="isLoggedIn" trigger="click">
            <div class="user-info">
              <el-avatar :src="avatarUrl" @error="handleAvatarError">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="user-name">{{ data.user.name }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToProfile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="goToFavorites">
                  <el-icon><Star /></el-icon>我的收藏
                </el-dropdown-item>
                <el-dropdown-item @click="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-button
              v-else
              type="primary"
              class="login-btn"
              @click="login"
          >
            <el-icon><User /></el-icon>登录
          </el-button>
        </div>
      </div>
    </header>
    <div class="content">
      <router-view @updateUser="updateUser"/>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, reactive} from 'vue'
import { useRouter } from 'vue-router'

const data = reactive({
  user : JSON.parse(localStorage.getItem('currentUser') || '{}')
})
const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('currentUser') || '{}')
}
// 路由实例
const router = useRouter()

// 响应式数据
const searchText = ref('')
const isLoggedIn = ref(false)

// 用户信息
// 默认头像URL
const defaultAvatar = '/src/assets/imgs/default_avatar.png'

// 计算头像URL
const avatarUrl = computed(() =>
    data.user.avatar || defaultAvatar
)

// 初始化检查登录状态
onMounted(() => {
  checkLoginStatus()
})

// 检查登录状态
function checkLoginStatus() {
  if(data.user.id != null)
    isLoggedIn.value = true
}

// 头像加载失败处理
function handleAvatarError() {
  data.user.avatar = defaultAvatar
}

// 搜索处理
function handleSearch() {
  if (searchText.value.trim()) {
    router.push(`/search?keyword=${encodeURIComponent(searchText.value)}`)
  }
}

// 登录跳转
function login() {
  router.push('/login')
}

// 个人中心
function goToProfile() {
  router.push('/user/profile')
}

// 我的收藏
function goToFavorites() {
  router.push('/user/favorites')
}

// 退出登录
function logout() {
  localStorage.removeItem('currentUser')
  isLoggedIn.value = false
  router.push('/')
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 70px;
  background: linear-gradient(135deg, rgba(14, 22, 33, 0.98), rgba(22, 34, 49, 0.95));
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-text {
  font-size: 26px;
  font-weight: bold;
  background: linear-gradient(to right, #ff9a3c, #ffd166);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 0 10px rgba(255, 154, 60, 0.5);
}

.el-menu {
  border: none !important;
  height: 100%;
}

.el-menu--horizontal > .el-menu-item {
  height: 70px;
  line-height: 70px;
  padding: 0 20px;
  transition: all 0.3s ease;
}

.el-menu--horizontal > .el-menu-item:hover {
  background-color: rgba(255, 154, 60, 0.1) !important;
}

.el-menu--horizontal > .el-menu-item a {
  color: inherit;
  text-decoration: none;
  display: block;
  width: 100%;
  height: 100%;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 25px;
}

.search-container {
  width: 220px;
}

.search-container :deep(.el-input__inner) {
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 30px;
  color: #fff;
  transition: all 0.3s ease;
}

.search-container :deep(.el-input__inner:focus) {
  width: 260px;
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 15px rgba(255, 154, 60, 0.2);
}

.search-container :deep(.el-icon) {
  color: rgba(255, 255, 255, 0.7);
}

.user-area {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 30px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.08);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.login-btn {
  background: linear-gradient(135deg, #ff9a3c, #ff7700);
  color: white;
  border: none;
  border-radius: 30px;
  padding: 8px 22px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 119, 0, 0.25);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 7px 20px rgba(255, 119, 0, 0.35);
}
</style>