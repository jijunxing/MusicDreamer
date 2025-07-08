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
            <el-icon><User /></el-icon>
            <span>基本信息</span>
          </el-menu-item>
          <el-menu-item index="password">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </el-menu-item>
          <el-menu-item index="favorites">
            <el-icon><StarFilled /></el-icon>
            <span>我的喜欢</span>
          </el-menu-item>
          <el-menu-item index="playlists">
            <el-icon><Folder /></el-icon>
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
              <el-input v-model="userData.username" disabled />
            </el-form-item>

            <div class="form-row">
              <el-form-item label="电话" class="half-width">
                <el-input v-model="userData.phone" placeholder="请输入电话号码" />
              </el-form-item>

              <el-form-item label="邮箱" class="half-width">
                <el-input v-model="userData.email" placeholder="请输入邮箱" />
              </el-form-item>
            </div>

            <el-form-item label="性别">
              <el-radio-group v-model="userData.sex">
                <el-radio label="male">男</el-radio>
                <el-radio label="female">女</el-radio>
                <el-radio label="unknown">保密</el-radio>
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
                  placeholder="8-20位，包含字母和数字"
                  show-password
                  @input="calculatePasswordStrength"
              />
              <password-strength-indicator :strength="passwordStrength" />
            </el-form-item>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
              />
              <div v-if="showMismatchError" class="error-tip">
                <el-icon><Warning /></el-icon> 两次输入的密码不一致
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

        <!-- 我的喜欢 (占位) -->
        <div v-if="activeTab === 'favorites'" class="placeholder-section">
          <div class="placeholder-content">
            <el-icon><StarFilled /></el-icon>
            <h3>我的喜欢</h3>
            <p>正在努力开发中，敬请期待...</p>
          </div>
        </div>

        <!-- 我的歌单 (占位) -->
        <div v-if="activeTab === 'playlists'" class="placeholder-section">
          <div class="placeholder-content">
            <el-icon><Folder /></el-icon>
            <h3>我的歌单</h3>
            <p>正在努力开发中，敬请期待...</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { User, Lock, StarFilled, Folder, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import UploadFile from '@/components/UploadFile.vue'

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

// 密码强度
const passwordStrength = ref(0)
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
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '长度在8到20个字符', trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/,
      message: '需包含字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' }
  ]
}

// 计算密码强度
const calculatePasswordStrength = () => {
  const pwd = passwordForm.newPassword
  let strength = 0

  if (pwd.length >= 8) strength += 1
  if (/[A-Z]/.test(pwd)) strength += 1
  if (/[a-z]/.test(pwd)) strength += 1
  if (/[0-9]/.test(pwd)) strength += 1
  if (/[^A-Za-z0-9]/.test(pwd)) strength += 1

  passwordStrength.value = strength
}

// 切换标签页
const handleTabChange = (tab) => {
  activeTab.value = tab
}

// 处理头像更新
const handleAvatarUpdate = (newAvatar) => {
  userData.avatar = newAvatar
  updateUserInfo()
}

// 获取用户信息
const fetchUserData = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('currentUser'))
    const userId = user.id
    if (!userId) {
      ElMessage.error('未获取到用户ID')
      return
    }

    const response = await request.get(`/user/selectById/${userId}`)
    Object.assign(userData, response.data)
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
    const { id, username, createTime, ...updateData } = userData

    await request.put('/user/update', updateData)
    ElMessage.success('个人信息更新成功')
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
    await request.post('/user/updatePassword', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    ElMessage.success('密码修改成功')

    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordStrength.value = 0
  } catch (error) {
    const errorMsg = error.response?.data?.message || '修改密码失败'
    ElMessage.error(errorMsg)
    console.error('修改密码失败:', error)
  } finally {
    changingPassword.value = false
  }
}

// 密码强度指示器组件
const PasswordStrengthIndicator = {
  props: ['strength'],
  template: `
    <div class="password-strength">
      <div
        v-for="n in 5"
        :key="n"
        class="strength-bar"
        :class="{
          'weak': n <= strength && strength <= 2,
          'medium': n <= strength && strength === 3,
          'strong': n <= strength && strength >= 4,
          'active': n <= strength
        }"
      ></div>
      <div class="strength-text">
        {{ strengthText }}
      </div>
    </div>
  `,
  computed: {
    strengthText() {
      if (this.strength === 0) return '未输入'
      if (this.strength <= 2) return '弱'
      if (this.strength === 3) return '中'
      return '强'
    }
  }
}

onMounted(() => {
  fetchUserData()
})
</script>

<style scoped>
.user-center-container {
  max-width: 1200px;
  margin: 60px auto;
  padding: 20px;
}

.user-header {
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
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

/* 占位区域样式 */
.placeholder-section {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.placeholder-content {
  text-align: center;
  color: #7f8c8d;
}

.placeholder-content .el-icon {
  font-size: 64px;
  color: #ecf0f1;
  margin-bottom: 20px;
}

.placeholder-content h3 {
  font-size: 24px;
  margin-bottom: 10px;
  color: #34495e;
}

.placeholder-content p {
  font-size: 16px;
  margin: 0;
}

/* 响应式调整 */
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
}
</style>