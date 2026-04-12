<template>
  <div class="login-container">
    <!-- 左侧区域：原背景 + 动画角色 -->
    <div class="left-panel" :class="{ 'fallback-bg': !hasBgImage }">
      <div class="left-top">
        <img v-if="!logoError" src="@/assets/images/logo.png" alt="Travel AI" class="brand-logo" @error="handleLogoError" />
        <el-icon v-if="logoError" size="40" color="#ffffff" class="logo-fallback"><Location /></el-icon>
        <span class="brand-name">Travel AI推荐</span>
      </div>

      <div class="characters-area">
        <AnimatedCharacters
          :is-typing="isTyping"
          :show-password="showPassword"
          :password-length="loginForm.password.length"
          :is-password-guard-mode="isPasswordGuardMode"
        />
      </div>

      <div class="left-decor-text">
        <h2>发现世界的美</h2>
        <p>基于AI智能算法，为您推荐最适合的旅游目的地</p>
      </div>
    </div>

    <!-- 右侧区域：淡蓝色背景 + 旅游元素 + 玻璃态表单 -->
    <div class="right-panel">
      <!-- 旅游元素 -->
      <div class="travel-elements">
        <el-icon class="travel-icon element-plane"><Promotion /></el-icon>
        <el-icon class="travel-icon element-map"><MapLocation /></el-icon>
        <el-icon class="travel-icon element-cloud"><LocationInformation /></el-icon>
      </div>

      <div class="form-wrapper">
        <div class="form-header">
          <h1 class="form-title">进入系统</h1>
          <p class="form-subtitle">体验智能路线规划与个性化AI助理</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
          size="large"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <div class="field-label">账号 / 用户名</div>
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              class="custom-input"
              @focus="isTyping = true"
              @blur="isTyping = false"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <div class="field-label">密码</div>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              class="custom-input"
              @keyup.enter="handleLogin"
              @focus="onPasswordFocus"
              @blur="onPasswordBlur"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
              <template #suffix>
                <el-icon class="eye-toggle" @click="showPassword = !showPassword">
                  <View v-if="showPassword"/>
                  <Hide v-else/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item class="submit-item">
            <el-button
              type="primary"
              :loading="loading"
              class="submit-btn"
              @click="handleLogin"
            >
              {{ loading ? '加载中...' : '开始探索' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-options">
          <span>还没有账户？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { FormInstance } from 'element-plus'
import { Location, User, Lock, View, Hide, Promotion, MapLocation, LocationInformation } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useRoute, useRouter } from 'vue-router'
import AnimatedCharacters from '@/components/user/AnimatedCharacters.vue'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

// 登录表单
const loginForm = ref({
  username: '',
  password: ''
})

// 表单校验规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
  ]
}

const loading = ref(false)
const loginFormRef = ref<FormInstance>()
const logoError = ref(false)
const hasBgImage = ref(false)

// 动画角色相关状态
const isTyping = ref(false)
const showPassword = ref(false)
const passwordFocused = ref(false)
const isPasswordGuardMode = computed(() => passwordFocused.value)

const onPasswordFocus = () => {
  passwordFocused.value = true
}

const onPasswordBlur = () => {
  passwordFocused.value = false
}

// 处理logo加载错误
const handleLogoError = () => {
  logoError.value = true
}

// 检查背景图片是否存在
const checkBgImage = () => {
  const img = new Image()
  img.onload = () => {
    hasBgImage.value = true
  }
  img.onerror = () => {
    hasBgImage.value = false
  }
  img.src = '/src/assets/images/login-bg.jpg'
}

// 页面加载时检查背景图片
onMounted(() => {
  checkBgImage()
})

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    await userStore.login(loginForm.value.username, loginForm.value.password)

    // 如果有重定向地址，则跳转到重定向地址
    const redirect = route.query.redirect as string
    router.replace(redirect || '/')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  overflow: hidden;
}

@media (max-width: 1024px) {
  .login-container {
    grid-template-columns: 1fr;
  }
}

/* ==== 左侧区域 ==== */
.left-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
  background: url('@/assets/images/login-bg.jpg') no-repeat center center;
  background-size: cover;
  overflow: hidden;
}

.left-panel::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.2) 0%, rgba(0,0,0,0.6) 100%);
  z-index: 1;
}

.left-panel.fallback-bg {
  background: linear-gradient(145deg, #0f172a 0%, #1e3a8a 50%, #1e40af 100%);
}

@media (max-width: 1024px) {
  .left-panel {
    display: none;
  }
}

.left-top {
  position: relative;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 40px;
  height: 40px;
  border-radius: 8px;
}

.brand-name {
  color: #ffffff;
  font-size: 22px;
  font-weight: bold;
  letter-spacing: 1px;
}

.characters-area {
  position: relative;
  z-index: 20;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 500px;
  transform: scale(0.9);
}

.left-decor-text {
  position: relative;
  z-index: 20;
  color: #fff;
  text-align: center;
  margin-bottom: 40px;
}
.left-decor-text h2 {
  font-size: 28px;
  margin-bottom: 12px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}
.left-decor-text p {
  font-size: 16px;
  opacity: 0.85;
}

/* ==== 右侧区域 ==== */
.right-panel {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  /* 淡蓝色背景 */
  background: linear-gradient(135deg, #e0f7fa 0%, #bbdefb 100%);
  overflow: hidden;
}

/* 旅游元素装饰 */
.travel-elements {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.travel-icon {
  position: absolute;
  color: rgba(255, 255, 255, 0.6);
  font-size: 80px;
  animation: float 6s ease-in-out infinite;
}

.element-plane {
  top: 15%;
  right: 15%;
  transform: rotate(45deg);
  animation-delay: 0s;
}

.element-map {
  bottom: 20%;
  left: 10%;
  font-size: 120px;
  opacity: 0.4;
  animation-delay: 2s;
}

.element-cloud {
  top: 40%;
  right: 5%;
  font-size: 60px;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(45deg); }
  50% { transform: translateY(-20px) rotate(45deg); }
}

.form-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 440px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 16px 40px rgba(0, 120, 212, 0.1);
  backdrop-filter: blur(16px);
  padding: 40px;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-title {
  font-size: 26px;
  font-weight: bold;
  color: #0b1220;
  margin-bottom: 8px;
}

.form-subtitle {
  font-size: 14px;
  color: #64748b;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 8px;
}

.custom-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 0 0 1px #e2e8f0;
  border-radius: 12px;
  height: 48px;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #0ea5e9;
  background: #ffffff;
}

.eye-toggle {
  cursor: pointer;
  color: #64748b;
  transition: color 0.2s;
}
.eye-toggle:hover {
  color: #0ea5e9;
}

.submit-item {
  margin-top: 30px;
  margin-bottom: 20px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #0284c7 0%, #00a8ff 100%);
  border: none;
  box-shadow: 0 8px 16px rgba(2, 132, 199, 0.2);
  transition: transform 0.2s, box-shadow 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(2, 132, 199, 0.3);
}

.login-options {
  text-align: center;
  font-size: 14px;
  color: #64748b;
}

.register-link {
  color: #0ea5e9;
  font-weight: 600;
  text-decoration: none;
  margin-left: 8px;
  transition: color 0.2s;
}

.register-link:hover {
  color: #0284c7;
}
</style>