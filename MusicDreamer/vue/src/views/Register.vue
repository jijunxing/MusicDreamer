<template>
  <div class="register-container">
    <!-- 装饰性背景元素 -->
    <div class="decorative-shapes">
      <div class="shape-circle"></div>
      <div class="shape-wave"></div>
      <div class="shape-triangle"></div>
    </div>

    <div class="register-box">
      <!-- Logo和标题区域 -->
      <div class="header">
        <div class="logo-wrapper">
          <img src="@/assets/imgs/logo.svg" alt="Logo" class="logo">
        </div>
        <h2 class="title">MusicDreamer</h2>
        <p class="subtitle">开启音乐之旅</p>
      </div>

      <!-- 注册表单 -->
      <div class="form-container">
        <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="0"
        >
          <el-form-item prop="username" class="input-with-glow">
            <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                size="large"
                clearable
            />
          </el-form-item>

          <el-form-item prop="password" class="input-with-glow">
            <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                size="large"
                show-password
                clearable
            />
          </el-form-item>

          <el-form-item prop="confirmPassword" class="input-with-glow">
            <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请确认密码"
                :prefix-icon="Lock"
                size="large"
                show-password
                clearable
            />
          </el-form-item>

          <!-- 注册按钮 -->
          <el-form-item>
            <el-button
                type="primary"
                class="register-btn"
                size="large"
                :loading="loading"
                @click="register"
            >
              {{ loading ? '注册中...' : '注册' }}
              <span class="hover-effect"></span>
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 登录链接 -->
        <div class="login-link">
          <span>已有账号？</span>
          <router-link to="/login" class="link">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import request from '@/utils/request'
import router from '@/router'
import { ElMessage } from 'element-plus'

const formRef = ref()
const loading = ref(false)

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认密码'))
  } else if (value !== form.password) {
    callback(new Error('输入密码不一致!'))
  } else {
    callback()
  }
}

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  role: 'USER'
})

const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名应在 2 到 20 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在 6 到 20 个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' },
  ]
})

const register = () => {
  formRef.value.validate((valid) => {
    if (!valid) return

    loading.value = true
    request.post('/register', form).then(res => {
      if (res.code === '200') {
        ElMessage.success('注册成功')
        router.push('/login')
      } else {
        ElMessage.error(res.msg)
      }
    }).finally(() => {
      loading.value = false
    })
  }).catch(error => {
    console.error(error)
  })
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #3a8ffe 0%, #a96bf8 100%);
  position: relative;
  overflow: hidden;
  z-index: 1002;
}

/* 装饰性形状 */
.decorative-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.shape-circle {
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  top: 15%;
  right: 10%;
}

.shape-wave {
  position: absolute;
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, transparent 40%, rgba(255, 255, 255, 0.1) 70%);
  bottom: -50px;
  left: -50px;
  border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  animation: waveRotate 25s linear infinite;
}

.shape-triangle {
  position: absolute;
  width: 0;
  height: 0;
  border-left: 200px solid transparent;
  border-right: 200px solid transparent;
  border-bottom: 350px solid rgba(255, 255, 255, 0.05);
  transform: rotate(45deg);
  top: 5%;
  left: -120px;
}

@keyframes waveRotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.register-box {
  width: 420px;
  padding: 50px 40px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 24px;
  box-shadow:
      0 15px 35px rgba(0, 0, 0, 0.15),
      0 0 0 1px rgba(255, 255, 255, 0.3) inset;
  backdrop-filter: blur(12px);
  position: relative;
  z-index: 2;
  transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.1);
}

.register-box:hover {
  transform: translateY(-5px);
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-wrapper {
  display: inline-block;
  padding: 15px;
  border-radius: 50%;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.logo-wrapper:hover {
  transform: scale(1.08) rotate(5deg);
}

.logo {
  width: 70px;
  height: 70px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.title {
  font-size: 32px;
  color: #303133;
  margin: 0 0 8px;
  background: linear-gradient(135deg, #FFA745 0%, #FE5F5F 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 700;
  letter-spacing: 1px;
}

.subtitle {
  font-size: 16px;
  color: #8b8fa6;
  margin: 0;
  letter-spacing: 2px;
}

.form-container {
  padding: 0 10px;
}

:deep(.input-with-glow .el-input__wrapper) {
  border-radius: 12px !important;
  padding: 1px 15px !important;
  background: rgba(255, 255, 255, 0.85) !important;
  box-shadow:
      0 4px 10px rgba(0, 0, 0, 0.05),
      0 0 0 1px rgba(160, 183, 255, 0.25) inset !important;
  transition: all 0.3s ease, box-shadow 0.3s ease;
}

:deep(.input-with-glow .el-input__wrapper:hover) {
  box-shadow:
      0 4px 15px rgba(0, 0, 0, 0.08),
      0 0 0 1px #a0b7ff inset !important;
}

:deep(.input-with-glow .el-input__wrapper.is-focus) {
  box-shadow:
      0 6px 16px rgba(130, 155, 255, 0.25),
      0 0 0 2px rgba(153, 179, 255, 0.6) inset !important;
}

:deep(.el-input__prefix) {
  margin-right: 10px;
}

.register-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  border-radius: 12px !important;
  background: linear-gradient(135deg, #FFA745 0%, #FE5F5F 100%) !important;
  border: none !important;
  margin-top: 20px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  z-index: 1;
  box-shadow: 0 8px 15px rgba(254, 95, 95, 0.3);
}

.register-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(254, 95, 95, 0.4);
}

.register-btn:active {
  transform: translateY(0px);
  box-shadow: 0 5px 12px rgba(254, 95, 95, 0.25);
}

.hover-effect {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.25) 0%, transparent 100%);
  border-radius: 12px;
  transform: translateX(-100%);
  transition: transform 0.6s ease;
  z-index: -1;
}

.register-btn:hover .hover-effect {
  transform: translateX(100%);
}

.login-link {
  text-align: center;
  font-size: 14px;
  color: #7a7c8c;
  margin-top: 30px;
  font-weight: 500;
}

.link {
  color: #FF8450;
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
  font-weight: 600;
  margin-left: 6px;
}

.link:hover {
  color: #ff5f8e;
}

.link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #FF8450 0%, #ff5f8e 100%);
  transition: width 0.3s ease;
}

.link:hover::after {
  width: 100%;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .register-box {
    width: 90%;
    padding: 40px 25px;
  }

  .decorative-shapes {
    display: none;
  }

  .logo {
    width: 60px;
    height: 60px;
  }

  .title {
    font-size: 28px;
  }

  .subtitle {
    font-size: 14px;
  }

  :deep(.el-input__wrapper) {
    padding: 1px 12px !important;
  }
}
</style>