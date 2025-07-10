<template>
  <div class="user-center-container">
    <!-- 用户信息头部 -->
    <div class="user-header">
      <div class="avatar-container">
        <upload-file
            type="image"
            v-model:modelValue="userData.avatar"
            @update:modelValue="handleAvatarUpdate"
        />
        <div class="user-info">
          <h2>{{ userData.username }}</h2>
          <p class="user-role">{{ roleMap[userData.role] }}</p>
          <p class="join-date">加入日期：{{ userData.createTime }}</p>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 左侧导航 -->
      <div class="sidebar">
        <el-menu :default-active="activeTab" @select="handleTabChange">
          <el-menu-item index="basic">
            <el-icon>
              <User/>
            </el-icon>
            <span>基本信息</span>
          </el-menu-item>
          <el-menu-item index="password">
            <el-icon>
              <Lock/>
            </el-icon>
            <span>修改密码</span>
          </el-menu-item>
          <el-menu-item index="favorites">
            <Icon icon="mdi:heart-outline" style="margin-left: 3px; font-size: 1.2rem; margin-right: 8px"></Icon>
            <span>我的喜欢</span>
          </el-menu-item>
          <el-menu-item index="playlists">
            <el-icon>
              <Folder/>
            </el-icon>
            <span>我的歌单</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容 -->
      <div class="content-panel">
        <!-- 基本信息 -->
        <div v-if="activeTab === 'basic'" class="info-section">
          <h3 class="section-title">个人信息</h3>
          <el-form
              :model="userData"
              label-width="100px"
              label-position="top"
          >
            <el-form-item label="用户名">
              <el-input v-model="userData.username" disabled/>
            </el-form-item>

            <div class="form-row">
              <el-form-item label="电话" class="half-width">
                <el-input v-model="userData.phone" placeholder="请输入电话号码"/>
              </el-form-item>

              <el-form-item label="邮箱" class="half-width">
                <el-input v-model="userData.email" placeholder="请输入邮箱"/>
              </el-form-item>
            </div>

            <el-form-item label="性别">
              <el-radio-group v-model="userData.sex">
                <el-radio label="male">男</el-radio>
                <el-radio label="female">女</el-radio>
                <el-radio label="secret">保密</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="个人简介">
              <el-input
                  v-model="userData.about"
                  type="textarea"
                  :rows="4"
                  placeholder="介绍一下你自己..."
                  maxlength="200"
                  show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-button
                  type="primary"
                  @click="updateUserInfo"
                  :loading="updating"
              >
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 修改密码 -->
        <div v-if="activeTab === 'password'" class="password-section">
          <h3 class="section-title">修改密码</h3>
          <el-form
              :model="passwordForm"
              :rules="passwordRules"
              ref="passwordFormRef"
              label-width="120px"
              label-position="top"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入当前密码"
                  show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码（8-12位）"
                  show-password
              />
              <!-- 移除密码强度指示器 -->
            </el-form-item>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
              />
              <div v-if="showMismatchError" class="error-tip">
                <el-icon>
                  <Warning/>
                </el-icon>
                两次输入的密码不一致
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                  type="primary"
                  @click="changePassword"
                  :loading="changingPassword"
              >
                确认修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 我的喜欢 -->
        <div v-if="activeTab === 'favorites'" class="favorites-section">
          <!-- 头部横幅 -->

          <div class="banner-content">
            <div class="play-controls">
              <el-button type="primary" @click="playAllFavorites">
                <el-icon>
                  <VideoPlay/>
                </el-icon>
                播放全部
              </el-button>
            </div>
          </div>

          <!-- 空状态提示 -->
          <div v-if="filteredFavorites.length === 0" class="empty-tip">
            <el-icon>
              <Star/>
            </el-icon>
            <p>您还没有喜欢的歌曲</p>
            <el-button @click="$router.push('/user/songs')">去发现音乐</el-button>
          </div>

          <div v-else class="music-table-container">
            <el-table :data="filteredFavorites" v-loading="loading" class="song-table">
              <el-table-column type="index" width="60"/>
              <el-table-column label="歌曲信息" min-width="300">
                <template #default="{ row }">
                  <div class="song-info">
                    <el-image
                        :src="row.imageUrl || defaultCover"
                        class="song-cover"
                        :preview-src-list="[row.imageUrl || defaultCover]"
                        :preview-teleported="true"
                        append-to-body
                        style="width: 50px; height: 50px; border-radius: 10%; cursor: pointer"
                        fit="cover"
                    />
                    <div class="song-details">
                      <div class="song-title">{{ row.musicName }}</div>
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
                  <span class="play-count-song">
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
                            class="like-icon liked-icon"/>
                    </transition>
                    <Icon v-if="!isMusicLiked(row.musicId)"
                          icon="mdi:heart-outline"
                          class="like-icon"/>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center">
                <template #default="{ row }">
                  <el-button
                      circle
                      size="small"
                      @click="playMusic(row)"
                      title="播放"
                  >
                    <el-icon>
                      <VideoPlay/>
                    </el-icon>
                  </el-button>
                  <el-button
                      circle
                      size="small"
                      @click="addToQueue(row)"
                      title="添加到播放队列"
                  >
                    <el-icon>
                      <Plus/>
                    </el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 我的歌单 -->
        <div v-if="activeTab === 'playlists'" class="playlists-section">
          <div class="playlists-header">
            <h3 class="section-title">我的歌单</h3>
            <el-button type="primary" @click="openCreateSonglistDialog" class="create-btn">
              <el-icon><Plus /></el-icon>创建歌单
            </el-button>
          </div>

          <!-- 歌单搜索栏 -->

          <div v-if="songlists.length === 0" class="empty-playlists">
            <el-icon><Folder /></el-icon>
            <p>您还没有创建任何歌单</p>
            <el-button type="primary" @click="openCreateSonglistDialog">
              创建第一个歌单
            </el-button>
          </div>

          <div v-else>
            <!-- 歌单表格 -->
            <div class="card" style="margin-bottom: 20px">
              <el-table :data="songlists" style="width: 100%">
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column prop="name" label="歌单名" min-width="150" />
                <el-table-column prop="imageUrl" label="封面" width="100">
                  <template v-slot="scope">
                    <el-image
                        v-if="scope.row.imageUrl"
                        :src="scope.row.imageUrl"
                        :preview-src-list="[scope.row.imageUrl]"
                        :preview-teleported="true"
                        append-to-body
                        style="width: 50px; height: 50px; border-radius: 10%; cursor: pointer"
                        fit="cover"
                    />
                  </template>
                </el-table-column>
                <el-table-column prop="about" label="简介" min-width="200" />
                <el-table-column prop="musicCount" label="音乐数" width="80" />
                <el-table-column prop="createTime" label="创建时间" min-width="160" />
                <el-table-column label="操作" min-width="200">
                  <template #default="scope">
                    <el-button
                        type="primary"
                        size="small"
                        @click="openManageDialog(scope.row)"
                    >查看</el-button>
                    <el-button
                        type="success"
                        size="small"
                        :icon="VideoPlay"
                        circle
                        @click="playSonglist(scope.row)"
                        title="播放全部"
                    ></el-button>
                    <el-button
                        type="primary"
                        size="small"
                        :icon="Edit"
                        circle
                        @click="openEditSonglistDialog(scope.row)"
                        title="编辑"
                    />
                    <el-button
                        type="danger"
                        size="small"
                        :icon="Delete"
                        circle
                        @click="deleteSonglist(scope.row.id)"
                        title="删除"
                    />
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 分页 -->
            <div class="card" v-if="total > 0">
              <el-pagination
                  background
                  layout="prev, pager, next"
                  @current-change="handlePageChange"
                  :page-size="pageSize"
                  v-model:current-page="currentPage"
                  :total="total"
              />
            </div>
          </div>
        </div>

        <!-- 创建歌单对话框 -->
        <el-dialog
            v-model="createSonglistDialogVisible"
            :title="editingSonglist ? '编辑歌单' : '创建新歌单'"
            width="500px"
        >
          <el-form
              :model="songlistForm"
              :rules="songlistRules"
              ref="songlistFormRef"
              label-width="80px"
          >
            <el-form-item label="歌单名" prop="name">
              <el-input
                  v-model="songlistForm.name"
                  placeholder="请输入歌单名称"
              />
            </el-form-item>

            <el-form-item label="封面">
              <upload-file
                  type="image"
                  v-model:modelValue="songlistForm.imageUrl"
              />
            </el-form-item>

            <el-form-item label="简介">
              <el-input
                  v-model="songlistForm.about"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入歌单简介"
              />
            </el-form-item>
          </el-form>

          <template #footer>
            <el-button @click="createSonglistDialogVisible = false">取消</el-button>
            <el-button
                type="primary"
                @click="submitSonglistForm"
                :loading="submittingSonglist"
            >
              {{ editingSonglist ? '保存' : '创建' }}
            </el-button>
          </template>
        </el-dialog>

        <!-- 歌曲管理对话框 -->
        <el-dialog
            v-model="manageDialogVisible"
            :title="`管理歌曲 - ${currentSonglist?.name || ''}`"
            width="80%"
        >
          <div style="margin-bottom: 10px;">
            <el-button type="primary" @click="openAddMusicDialog">添加歌曲</el-button>
          </div>

          <el-table :data="currentMusics" style="width: 100%">
            <el-table-column prop="musicName" label="歌曲名" min-width="200" />
            <el-table-column prop="singerName" label="歌手" min-width="150" />
            <el-table-column prop="listenNumb" label="播放量" align="center" width="120" />
            <el-table-column label="状态" align="center" width="100">
              <template #default="scope">
                <el-tag type="success" v-if="scope.row.activation === 1">激活</el-tag>
                <el-tag type="info" v-if="scope.row.activation === 0">冻结</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="180">
              <template #default="scope">
                <el-button
                    type="success"
                    size="small"
                    :icon="VideoPlay"
                    circle
                    @click="playMusic(scope.row)"
                    title="播放"
                ></el-button>
                <el-button
                    type="warning"
                    size="small"
                    :icon="Plus"
                    circle
                    @click="addToQueue(scope.row)"
                    title="添加到队列"
                ></el-button>
                <el-button
                    type="danger"
                    size="small"
                    :icon="Delete"
                    circle
                    @click="removeMusic(scope.row.musicId)"
                    title="移除"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>

          <template #footer>
            <el-button @click="manageDialogVisible = false">关闭</el-button>
          </template>
        </el-dialog>

        <!-- 添加歌曲对话框 -->
        <el-dialog v-model="addMusicDialogVisible" title="添加歌曲到歌单" width="70%">
          <div style="display: flex; gap: 10px; margin-bottom: 10px;">
            <el-input
                v-model="musicSearchKeyword"
                placeholder="请输入歌曲名或歌手"
                clearable
                style="flex: 1"
            />
            <el-button type="primary" @click="loadSelectableMusics">搜索</el-button>
          </div>

          <el-table
              :data="selectableMusics"
              style="width: 100%"
              height="400"
              @selection-change="handleSelectChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="musicName" label="歌曲名" min-width="200" />
            <el-table-column prop="singerName" label="歌手" min-width="150" />
            <el-table-column prop="listenNumb" label="播放量" align="center" width="120" />
          </el-table>

          <template #footer>
            <el-button @click="addMusicDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="addSelectedMusicsToSonglist">添加选中歌曲</el-button>
          </template>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, reactive, computed, onMounted, watch} from 'vue'
import {useRouter} from 'vue-router'
import {User, Lock, StarFilled, Folder, Warning, Plus, VideoPlay, Headset, More, Delete, Edit} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import request from '@/utils/request'
import UploadFile from '@/components/UploadFile.vue'
import {player} from "@/utils/player";
import {initUserLikes, isMusicLiked, toggleLike, userLikesMap} from '@/utils/likeUtils';
import {Icon} from "@iconify/vue";

const router = useRouter()

const playMusic = (music) => {
  player.play(music)
}
const addToQueue = (music) => {
  player.addToQueue(music)
}
// 角色映射
const roleMap = {
  'USER': '普通用户',
  'SINGER': '歌手',
  'ADMIN': '管理员'
}

// 当前激活的标签页
const activeTab = ref('basic')

// 用户数据
const userData = reactive({
  id: null,
  username: '',
  avatar: '',
  role: 'user',
  phone: '',
  email: '',
  about: '',
  createTime: '',
  sex: 'unknown'
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})


const showMismatchError = computed(() =>
    passwordForm.newPassword !== passwordForm.confirmPassword &&
    passwordForm.confirmPassword !== ''
)

// 加载状态
const updating = ref(false)
const changingPassword = ref(false)

// 密码规则
const passwordRules = {
  oldPassword: [
    {required: true, message: '请输入原密码', trigger: 'blur'}
  ],
  newPassword: [
    {required: true, message: '请输入新密码', trigger: 'blur'},
    {min: 8, max: 20, message: '长度在8到20个字符', trigger: 'blur'},
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/,
      message: '需包含字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {required: true, message: '请确认新密码', trigger: 'blur'}
  ]
}

// 默认图片
const defaultPlaylistCover = ref('/src/assets/imgs/default_playlist_cover.png')

// 处理头像更新
const handleAvatarUpdate = (newAvatar) => {
  userData.avatar = newAvatar
  updateUserInfo()
}

// 获取用户信息
const fetchUserData = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('currentUser') || '{}')
    Object.assign(userData, user)
  } catch (error) {
    ElMessage.error('获取用户信息失败')
    console.error('获取用户信息失败:', error)
  }
}

// 更新用户信息
const updateUserInfo = async () => {
  updating.value = true
  try {
    // 过滤掉不需要更新的字段
    const {id, username, createTime, ...updateData} = userData
    await request.put('/user/update', updateData)
    ElMessage.success('个人信息更新成功')
    localStorage.removeItem('currentUser')
    localStorage.setItem('currentUser',JSON.stringify(userData))
  } catch (error) {
    ElMessage.error('更新失败，请重试')
    console.error('更新用户信息失败:', error)
  } finally {
    updating.value = false
  }
}

// 修改密码
const changePassword = async () => {
  changingPassword.value = true
  try {
    // 验证表单
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      ElMessage.error('两次输入的新密码不一致')
      return
    }

    // 调用修改密码接口
    await request.post('/user/changePwd', {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
    })

    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''

    ElMessage.success('密码修改成功')

  } catch (error) {
    const errorMsg = error.response?.data?.message || '修改密码失败'
    ElMessage.error(errorMsg)
    console.error('修改密码失败:', error)
  } finally {
    changingPassword.value = false
  }
}

//我的喜欢
// 全量歌曲列表 & 加载状态
const allSongs = ref([]);
const loading = ref(false);

// 获取所有歌曲
const fetchAllSongs = async () => {
  loading.value = true;
  try {
    const res = await request.get('/music/selectAll');
    if (res.code === '200') allSongs.value = res.data || [];
  } finally {
    loading.value = false;
  }
};

// 筛选喜欢的歌曲（计算属性）
const filteredFavorites = computed(() => {
  return allSongs.value.filter(song =>
      isMusicLiked(song.musicId) && song.activation !== 0
  );
});

// 初始化用户喜欢数据
const initUserFavorites = async () => {
  await initUserLikes(); // 加载用户喜欢的 musicId 到 userLikesMap
};

// 播放全部喜欢的歌曲
const playAllFavorites = () => {
  if (filteredFavorites.value.length === 0) return;
  player.playList(filteredFavorites.value);
};

// 监听标签切换
const handleTabChange = (tab) => {
  activeTab.value = tab;
  if (tab === 'favorites') {
    fetchAllSongs();
    initUserFavorites();
  } else if (tab === 'playlists') { // 新增逻辑
    fetchSonglists(); // 触发歌单数据加载
  }
};

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 格式化播放量
const formatPlayCount = (count) => {
  if (!count) return '0'
  return count > 10000
      ? `${(count / 10000).toFixed(1)}万`
      : count
}

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

// 监听全局点赞状态变化（实时更新列表）
watch(userLikesMap, () => {
  // 计算属性 filteredFavorites 会自动更新
}, {deep: true});


// 歌单相关状态
const songlists = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// 表单对话框状态
const createSonglistDialogVisible = ref(false)
const songlistForm = reactive({
  id: null,
  name: '',
  imageUrl: '',
  about: '',
  user: null
})
const editingSonglist = ref(false)
const submittingSonglist = ref(false)

// 歌单管理状态
const manageDialogVisible = ref(false)
const addMusicDialogVisible = ref(false)
const currentSonglist = ref(null)
const currentMusics = ref([])
const selectableMusics = ref([])
const musicSearchKeyword = ref('')
const selectedMusicIds = ref([])

// 歌单表单规则
const songlistRules = {
  name: [
    { required: true, message: '请输入歌单名称', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在2到30个字符', trigger: 'blur' }
  ]
}

// 获取用户歌单
const fetchSonglists = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('currentUser') || '{}')
    const userId = user.id
    if (!userId) {
      ElMessage.error('未获取到用户ID')
      return
    }

    const response = await request.get('/songlist/selectPage', {
      params: {
        user: userId,
        name: searchKeyword.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })

    if (response.code === '200') {
      songlists.value = response.data.list.map(songlist => ({
        ...songlist,
        musicCount: songlist.musics?.length || 0
      }))
      total.value = response.data.total
    } else {
      ElMessage.error('获取歌单失败: ' + response.msg)
    }
  } catch (error) {
    ElMessage.error('获取歌单失败')
    console.error('获取歌单失败:', error)
  }
}


// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchSonglists()
}

// 打开创建歌单对话框
const openCreateSonglistDialog = () => {
  editingSonglist.value = false
  songlistForm.id = null
  songlistForm.name = ''
  songlistForm.imageUrl = ''
  songlistForm.about = ''
  createSonglistDialogVisible.value = true
}

// 打开编辑歌单对话框
const openEditSonglistDialog = (songlist) => {
  editingSonglist.value = true
  songlistForm.id = songlist.id
  songlistForm.name = songlist.name
  songlistForm.imageUrl = songlist.imageUrl
  songlistForm.about = songlist.about
  createSonglistDialogVisible.value = true
}

// 提交歌单表单（创建/编辑）
const submitSonglistForm = async () => {
  submittingSonglist.value = true
  try {
    const user = JSON.parse(localStorage.getItem('currentUser') || '{}')
    const userId = user.id
    if (!userId) {
      ElMessage.error('未获取到用户ID')
      return
    }

    const formData = {
      ...songlistForm,
      user: userId
    }

    const url = editingSonglist.value ? '/songlist/update' : '/songlist/add'
    const method = editingSonglist.value ? 'put' : 'post'

    const response = await request[method](url, formData)
    if (response.code === '200') {
      ElMessage.success(editingSonglist.value ? '歌单更新成功' : '歌单创建成功')
      createSonglistDialogVisible.value = false
      fetchSonglists()
    } else {
      ElMessage.error((editingSonglist.value ? '更新' : '创建') + '歌单失败: ' + response.msg)
    }
  } catch (error) {
    ElMessage.error((editingSonglist.value ? '更新' : '创建') + '歌单失败')
    console.error('操作失败:', error)
  } finally {
    submittingSonglist.value = false
  }
}

// 删除歌单
const deleteSonglist = async (id) => {
  try {
    await ElMessageBox.confirm('删除后数据无法恢复,您确认要删除吗？', '确认删除', {
      type: 'warning'
    })

    const response = await request.delete('/songlist/delete/' + id)
    if (response.code === '200') {
      ElMessage.success('删除成功')
      fetchSonglists()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    // 用户取消了删除
  }
}

// 查看歌单详情
const viewSonglistDetail = (songlist) => {
  router.push({ name: 'SongListDetail', params: { id: songlist.id } })
}

// 播放歌单全部歌曲
const playSonglist = (songlist) => {
  if (!songlist.musics || songlist.musics.length === 0) {
    ElMessage.warning('歌单中没有歌曲')
    return
  }
  player.playList(songlist.musics)
  ElMessage.success('开始播放歌单全部歌曲')
}

// 打开管理对话框
const openManageDialog = (songlist) => {
  currentSonglist.value = songlist
  currentMusics.value = songlist.musics || []
  manageDialogVisible.value = true
}

// 打开添加歌曲对话框
const openAddMusicDialog = async () => {
  try {
    musicSearchKeyword.value = ''
    await loadSelectableMusics()
    addMusicDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载歌曲失败')
  }
}

// 加载可添加的歌曲
const loadSelectableMusics = async () => {
  try {
    const response = await request.get('/songlist/selectForSonglist', {
      params: {
        keyword: musicSearchKeyword.value,
        excludeSonglistId: currentSonglist.value?.id
      }
    })

    if (response.code === '200') {
      selectableMusics.value = response.data || []
    } else {
      ElMessage.error(response.msg || '加载歌曲失败')
    }
  } catch (error) {
    ElMessage.error('加载歌曲失败')
    console.error('加载歌曲失败:', error)
  }
}

// 处理歌曲选择
const handleSelectChange = (selection) => {
  selectedMusicIds.value = selection.map(item => item.musicId)
}

// 添加选中歌曲到歌单
const addSelectedMusicsToSonglist = async () => {
  if (!selectedMusicIds.value.length) {
    ElMessage.warning('请选择要添加的歌曲')
    return
  }

  try {
    const response = await request.post('/songlist/addMusics', {
      listId: currentSonglist.value.id,
      musicIds: selectedMusicIds.value
    })

    if (response.code === '200') {
      ElMessage.success('添加成功')
      // 刷新当前歌单的歌曲列表
      const res = await request.get(`/songlist/selectById/${currentSonglist.value.id}`)
      if (res.code === '200') {
        currentMusics.value = res.data.musics || []
      }
      addMusicDialogVisible.value = false
    } else {
      ElMessage.error(response.msg || '添加失败')
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

// 移除歌曲
const removeMusic = async (musicId) => {
  try {
    await request.delete('/songlist/removeMusic', {
      params: {
        listId: currentSonglist.value.id,
        musicId: musicId
      }
    })

    ElMessage.success('移除成功')
    // 刷新当前歌单的歌曲列表
    const res = await request.get(`/songlist/selectById/${currentSonglist.value.id}`)
    if (res.code === '200') {
      currentMusics.value = res.data.musics || []
    }
  } catch (error) {
    ElMessage.error('移除失败')
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

onMounted(() => {
  fetchUserData();
  if (activeTab.value === 'favorites') {
    fetchAllSongs();
    initUserFavorites();
  } else if (activeTab.value === 'playlists') { // 新增逻辑
    fetchSonglists(); // 初始化时加载歌单
  }
})

watch(() => player.likedSongs, (newLikes) => {
  userLikes.value = new Set(newLikes);
});
</script>

<style scoped lang="scss">
.user-center-container {
  max-width: 1200px;
  margin: 60px auto;
  padding: 20px;
}

.user-header {
  background: linear-gradient(135deg, #2c73d2, #0081cf);
  border-radius: 12px;
  padding: 30px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 30px;
}

.user-info h2 {
  font-size: 28px;
  margin: 0 0 8px 0;
}

.user-role {
  background: rgba(255, 255, 255, 0.2);
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 14px;
  margin-bottom: 10px;
}

.join-date {
  opacity: 0.8;
  font-size: 14px;
  margin: 0;
}

.main-content {
  display: flex;
  gap: 24px;
  margin-top: 24px;
}

.sidebar {
  width: 240px;
  flex-shrink: 0;
}

.sidebar .el-menu {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.content-panel {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section-title {
  margin-top: 0;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
  color: #2c3e50;
  font-weight: 600;
}

.form-row {
  display: flex;
  gap: 20px;
}

.half-width {
  flex: 1;
}

.error-tip {
  color: #e74c3c;
  font-size: 13px;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 密码强度指示器样式 */
.password-strength {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
}

.strength-bar {
  height: 6px;
  flex: 1;
  background: #e0e0e0;
  border-radius: 3px;
  transition: all 0.3s ease;
}

.strength-bar.active {
  background: #e74c3c;
}

.strength-bar.weak.active {
  background: #e74c3c;
}

.strength-bar.medium.active {
  background: #f39c12;
}

.strength-bar.strong.active {
  background: #2ecc71;
}

.strength-text {
  font-size: 13px;
  color: #7f8c8d;
  width: 50px;
}

/* 我的喜欢 */
.favorites-banner {
  padding: 30px;
  border-radius: 12px 12px 0 0;
  color: white;

  .banner-content {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.empty-tip {
  text-align: center;
  padding: 50px 0;

  .el-icon {
    font-size: 60px;
    color: #c4c4c4;
  }

  p {
    margin: 15px 0;
    color: #888;
  }
}

.song-table {
  background: transparent;
  border-radius: 12px;
  overflow: hidden;

  :deep(.el-table__row) {
    background: rgba(255, 255, 255, 0.03);
    transition: background 0.3s;

    &:hover {
      background: rgba(255, 154, 60, 0.1) !important;
    }
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

.play-count-song {
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

// 响应式适配
@media (max-width: 768px) {
  .favorites-banner .banner-content {
    flex-direction: column;
    gap: 15px;
  }
}

/* 歌单区域样式 */
.playlists-section {
  min-height: 400px;
}

.playlists-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.empty-playlists {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  text-align: center;
  color: #7f8c8d;

  .el-icon {
    font-size: 80px;
    color: #ecf0f1;
    margin-bottom: 20px;
  }

  p {
    font-size: 18px;
    margin-bottom: 20px;
  }
}

.songlists-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.songlist-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);

    .play-overlay {
      opacity: 1;
    }

    .songlist-cover {
      transform: scale(1.05);
    }
  }
}

.cover-container {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.songlist-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.play-count {
  position: absolute;
  bottom: 8px;
  left: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 12px;
  border-radius: 12px;
}

.songlist-info {
  padding: 15px;

  .songlist-name {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 5px;
    color: #333;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    height: 24px; /* 确保单行显示 */
  }

  .song-count {
    font-size: 14px;
    color: #666;
    margin-bottom: 3px;
  }

  .create-time {
    font-size: 12px;
    color: #999;
  }
}

.management-actions {
  margin-top: 10px;
  padding: 0 15px 15px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .songlists-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
  }

  .cover-container {
    height: 140px;
  }

  .songlist-name {
    font-size: 14px;
    height: 22px;
  }
}


/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .avatar-container {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }

  .songlists-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
  }

  .cover-container {
    height: 140px;
  }

  .el-dialog {
    width: 95% !important;
  }

  .songlist-name {
    font-size: 14px;
    height: 22px;
  }
}
</style>
