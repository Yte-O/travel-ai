<!-- 登录页面 -->
<template>
  <div class="login-container" :data-has-bg="hasBgImage">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
    </div>
    
    <div class="login-content">
      <!-- 左侧介绍区域 -->
      <div class="intro-section">
        <div class="intro-content">
          <div class="logo">
            <img src="@/assets/images/logo.png" alt="Travel AI 推荐" class="logo-img" @error="handleLogoError" />
            <el-icon v-if="logoError" size="48" color="#ffd700" class="logo-fallback"><Location /></el-icon>
            <h1>Travel AI 推荐</h1>
          </div>
          <h2>发现世界的美好</h2>
          <p>基于AI智能算法，为您推荐最适合的旅游目的地</p>
          <div class="features">
            <div class="feature-item">
              <el-icon size="20" color="#ffd700"><Star /></el-icon>
              <span>个性化推荐</span>
            </div>
            <div class="feature-item">
              <el-icon size="20" color="#ffd700"><Location /></el-icon>
              <span>智能路线规划</span>
            </div>
            <div class="feature-item">
              <el-icon size="20" color="#ffd700"><ChatDotRound /></el-icon>
              <span>AI智能助手</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录表单 -->
      <div class="login-form-section">
        <div class="login-box">
          <div class="form-header">
            <h3>欢迎回来</h3>
            <p>登录您的账户，开启Travel AI 之旅</p>
          </div>
          
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-width="0"
            size="large"
            class="login-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                class="custom-input"
                @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="login-button"
                @click="handleLogin"
              >
                <el-icon><Right /></el-icon>
                开始探索
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="login-options">
            <span>还没有账户？</span>
            <router-link to="/register" class="register-link">
              立即注册
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { FormInstance } from 'element-plus'
import { Location, Star, ChatDotRound, User, Lock, Right } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useRoute, useRouter } from 'vue-router'

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
  height: 100vh;
  display: flex;
  background: url('@/assets/images/login-bg.jpg') no-repeat center center;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
}

/* 备用CSS背景 - 当图片不存在时显示 */
.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.3) 0%, transparent 50%),
    linear-gradient(45deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  background-size: cover;
  background-position: center;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

/* 当没有背景图片时显示CSS背景 */
.login-container:not([data-has-bg])::before {
  opacity: 1;
}

/* 轻微遮罩层 - 确保文字可读性 */
.login-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.1) 0%, rgba(0, 0, 0, 0.05) 100%);
  z-index: 0;
  opacity: 0.5;
  transition: opacity 0.3s ease;
}

/* 当有背景图片时，减少遮罩透明度 */
.login-container[data-has-bg]::after {
  opacity: 0.2;
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.floating-shapes {
  position: relative;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 80px;
  height: 80px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 120px;
  height: 120px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 60px;
  height: 60px;
  top: 80%;
  left: 20%;
  animation-delay: 4s;
}

.shape-4 {
  width: 100px;
  height: 100px;
  top: 10%;
  right: 30%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.login-content {
  display: flex;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  z-index: 2;
  position: relative;
}

/* 左侧介绍区域 */
.intro-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  padding: 40px;
}

.intro-content {
  text-align: center;
  max-width: 400px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
}

.logo-img {
  width: 60px;
  height: 60px;
  margin-right: 15px;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 当logo图片加载失败时的备用样式 */
.logo-img:not([src]), .logo-img[src=""] {
  display: none;
}

.logo-fallback {
  margin-right: 15px;
}

.logo h1 {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.intro-content h2 {
  font-size: 24px;
  margin-bottom: 20px;
  font-weight: 300;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

.intro-content p {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 40px;
  opacity: 0.9;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 25px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.feature-item .el-icon {
  /* 图标样式已在组件中设置 */
}

.feature-item span {
  font-size: 14px;
  font-weight: 500;
}

/* 右侧登录表单 */
.login-form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-header h3 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
}

.form-header p {
  color: #666;
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e1e5e9;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #667eea;
  background: white;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background: white;
}

.login-button {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.login-options {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.register-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #764ba2;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-content {
    flex-direction: column;
  }
  
  .intro-section {
    padding: 20px;
    min-height: 40vh;
  }
  
  .login-form-section {
    padding: 20px;
  }
  
  .login-box {
    padding: 30px 20px;
  }
  
  .logo h1 {
    font-size: 24px;
  }
  
  .intro-content h2 {
    font-size: 20px;
  }
}
</style> 