<!-- 注册页面 -->
<template>
  <div class="register-container" :data-has-bg="hasBgImage">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
    </div>
    
    <div class="register-content">
      <!-- 左侧介绍区域 -->
      <div class="intro-section">
        <div class="intro-content">
          <div class="logo">
            <img src="@/assets/images/logo.png" alt="Travel AI 推荐" class="logo-img" @error="handleLogoError" />
            <el-icon v-if="logoError" size="48" color="#ffd700" class="logo-fallback"><Location /></el-icon>
            <h1>加入我们</h1>
          </div>
          <h2>开启您的旅行故事</h2>
          <p>注册账户，享受个性化的旅游推荐服务</p>
          <div class="benefits">
            <div class="benefit-item">
              <el-icon size="16" color="#ffd700"><User /></el-icon>
              <span>个性化体验</span>
            </div>
            <div class="benefit-item">
              <el-icon size="16" color="#ffd700"><Collection /></el-icon>
              <span>收藏心仪景点</span>
            </div>
            <div class="benefit-item">
              <el-icon size="16" color="#ffd700"><ChatLineRound /></el-icon>
              <span>智能对话助手</span>
            </div>
            <div class="benefit-item">
              <el-icon size="16" color="#ffd700"><DataAnalysis /></el-icon>
              <span>智能推荐算法</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧注册表单 -->
      <div class="register-form-section">
        <div class="register-box">
          <div class="form-header">
            <h3>创建账户</h3>
            <p>填写信息，开始您的Travel AI 之旅</p>
          </div>
          
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-width="0"
            size="large"
            class="register-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
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
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                show-password
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="realName">
              <el-input
                v-model="registerForm.realName"
                placeholder="请输入真实姓名"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><UserFilled /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><Phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="register-button"
                @click="handleRegister"
              >
                <el-icon><Plus /></el-icon>
                创建账户
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="register-options">
            <span>已有账户？</span>
            <router-link to="/login" class="login-link">
              立即登录
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
import { Location, User, Collection, ChatLineRound, DataAnalysis, Lock, UserFilled, Phone, Message, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

// 注册表单
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: ''
})

// 表单校验规则
const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.value.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '真实姓名长度应在2-20个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email' as const, message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const loading = ref(false)
const registerFormRef = ref<FormInstance>()
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

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    loading.value = true

    const { confirmPassword, ...registerData } = registerForm.value
    await userStore.register(registerData)

    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  background: url('@/assets/images/login-bg.jpg') no-repeat center center;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
}

/* 备用CSS背景 - 当图片不存在时显示 */
.register-container::before {
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
.register-container:not([data-has-bg])::before {
  opacity: 1;
}

/* 轻微遮罩层 - 确保文字可读性 */
.register-container::after {
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
.register-container[data-has-bg]::after {
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

.register-content {
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

.benefits {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.benefit-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 12px;
}

.benefit-item .el-icon {
  /* 图标样式已在组件中设置 */
}

.benefit-item span {
  font-weight: 500;
}

/* 右侧注册表单 */
.register-form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.register-box {
  width: 100%;
  max-width: 450px;
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

.register-form {
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

.register-button {
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

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.register-options {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.login-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #764ba2;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-content {
    flex-direction: column;
  }
  
  .intro-section {
    padding: 20px;
    min-height: 40vh;
  }
  
  .register-form-section {
    padding: 20px;
  }
  
  .register-box {
    padding: 30px 20px;
  }
  
  .logo h1 {
    font-size: 24px;
  }
  
  .intro-content h2 {
    font-size: 20px;
  }
  
  .benefits {
    grid-template-columns: 1fr;
  }
}
</style> 