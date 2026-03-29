<!-- 个人中心页面 -->
<template>
  <div class="profile-container">
    <!-- 顶部个人信息卡片 -->
    <el-card class="profile-header-card" shadow="hover">
      <div class="profile-header">
        <div class="avatar-container">
          <el-avatar
            :size="100"
            :src="userInfo?.avatarUrl"
          />
          <el-upload
            class="avatar-uploader"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleAvatarChange"
            :before-upload="beforeAvatarUpload"
          >
            <el-button
              type="primary"
              link
              class="change-avatar-btn"
            >
              <el-icon><Camera /></el-icon>
              更换头像
            </el-button>
          </el-upload>
        </div>
        
        <div class="user-base-info">
          <h2 class="username">{{ userInfo?.username || '未登录' }}</h2>
          <div class="user-meta">
            <el-tag :type="userInfo?.role === 1 ? 'danger' : 'info'" size="small">
              {{ userInfo?.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
            <el-tag :type="userInfo?.status === 1 ? 'success' : 'danger'" size="small">
              {{ userInfo?.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </div>
          <p class="user-desc" v-if="userInfo?.email">{{ userInfo?.email }}</p>
        </div>
      </div>
    </el-card>

    <!-- 标签页 -->
    <el-card class="profile-tabs-card" shadow="hover">
      <el-tabs type="border-card">
        <!-- 个人信息标签页 -->
        <el-tab-pane label="个人信息">
          <div class="tab-header">
            <h3>个人详细信息</h3>
            <el-button
              type="primary"
              @click="handleEdit"
            >
              <el-icon><Edit /></el-icon>
              编辑资料
            </el-button>
          </div>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">
              {{ userInfo?.username }}
            </el-descriptions-item>
            <el-descriptions-item label="真实姓名">
              {{ userInfo?.realName || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ userInfo?.phone || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ userInfo?.email || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="角色" :span="2">
              <el-tag :type="userInfo?.role === 1 ? 'danger' : 'info'">
                {{ userInfo?.role === 1 ? '管理员' : '普通用户' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="账号状态" :span="2">
              <el-tag :type="userInfo?.status === 1 ? 'success' : 'danger'">
                {{ userInfo?.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        
        <!-- 修改密码标签页 -->
        <el-tab-pane label="修改密码">
          <div class="tab-header">
            <h3>安全设置</h3>
          </div>
          
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="password-form"
            status-icon
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码"
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请确认新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="passwordLoading"
                @click="handleChangePassword"
              >
                <el-icon><Check /></el-icon>
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 我的收藏标签页 -->
        <el-tab-pane label="我的收藏">
          <MyFavorites />
        </el-tab-pane>
        
        <!-- 用户画像标签页 -->
        <el-tab-pane label="用户画像">
          <div class="tab-header">
            <h3>我的用户画像</h3>
            <el-tooltip content="基于知识图谱分析您的行为偏好" placement="top">
              <el-button type="info" plain size="small">
                <el-icon><InfoFilled /></el-icon>
                什么是用户画像?
              </el-button>
            </el-tooltip>
          </div>
          <UserProfileComponent />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 编辑个人信息对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑个人信息"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="editForm.realName"
            placeholder="请输入真实姓名"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="editForm.phone"
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="editForm.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>
        
        <!-- 将按钮放到表单内部的底部 -->
        <el-form-item class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="editLoading"
            @click="handleSaveEdit"
          >
            确定
          </el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { FormInstance, UploadProps, FormItemRule } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUser } from '@/api/user'
import { fileRequest } from '@/api/file_request'
import type { UpdateUserParams } from '@/types/user'
import { Edit, Camera, Lock, Check, InfoFilled } from '@element-plus/icons-vue'
import MyFavorites from './MyFavorites.vue'
import UserProfileComponent from '@/components/user/UserProfileComponent.vue'

const userStore = useUserStore()

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 修改密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 修改密码表单校验规则
const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

const passwordLoading = ref(false)
const passwordFormRef = ref<FormInstance>()

// 处理修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true

    await userStore.changePassword({
      id: userInfo.value?.id as number,
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })

    ElMessage.success('密码修改成功')
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

// 编辑表单
const editForm = ref({
  realName: '',
  phone: '',
  email: ''
})

// 编辑表单校验规则
const editRules = {
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
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
} satisfies Record<string, FormItemRule[]>

const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()

// 处理编辑
const handleEdit = () => {
  editForm.value = {
    realName: userInfo.value?.realName || '',
    phone: userInfo.value?.phone || '',
    email: userInfo.value?.email || ''
  }
  editDialogVisible.value = true
}

// 处理保存编辑
const handleSaveEdit = async () => {
  if (!editFormRef.value) return

  try {
    await editFormRef.value.validate()
    editLoading.value = true

    await updateUser({
      id: userInfo.value?.id as number,
      realName: editForm.value.realName,
      phone: editForm.value.phone,
      email: editForm.value.email,
      role: userInfo.value?.role as number,
      status: userInfo.value?.status as number
    })

    // 更新本地用户信息
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      const data = JSON.parse(storedUserInfo)
      data.userInfo = {
        ...data.userInfo,
        realName: editForm.value.realName,
        phone: editForm.value.phone,
        email: editForm.value.email
      }
      localStorage.setItem('userInfo', JSON.stringify(data))
      userStore.initUserInfo()
    }

    ElMessage.success('个人信息修改成功')
    editDialogVisible.value = false
  } catch (error) {
    console.error('修改个人信息失败:', error)
  } finally {
    editLoading.value = false
  }
}

// 处理头像上传前的验证
const beforeAvatarUpload: UploadProps['beforeUpload'] = (file: any) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 处理头像文件改变
const handleAvatarChange: UploadProps['onChange'] = async (uploadFile: any) => {
  if (!uploadFile.raw) return
  
  try {
    // 上传文件到文件服务
    const { bucket, objectKey } = await fileRequest.upload('avatars', uploadFile.raw)
    
    // 更新用户头像
    const updateParams: UpdateUserParams = {
      id: userInfo.value?.id as number,
      avatarBucket: bucket,
      avatarObjectKey: objectKey
    }
    await updateUser(updateParams)
    
    // 更新本地用户信息
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      const data = JSON.parse(storedUserInfo)
      data.userInfo = {
        ...data.userInfo,
        avatarBucket: bucket,
        avatarObjectKey: objectKey,
        avatarUrl: fileRequest.getFileUrl(bucket, objectKey)
      }
      localStorage.setItem('userInfo', JSON.stringify(data))
      userStore.initUserInfo()
    }
    
    ElMessage.success('头像更新成功')
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
  min-height: calc(100vh - 140px);
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-header-card {
  transition: all 0.3s;
}

.profile-header-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.profile-header {
  display: flex;
  gap: 30px;
  align-items: center;
  padding: 20px;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.change-avatar-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.user-base-info {
  flex: 1;
}

.username {
  margin: 0 0 10px 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.user-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.user-desc {
  color: #606266;
  margin: 5px 0;
}

.profile-tabs-card {
  transition: all 0.3s;
  margin-bottom: 20px;
}

.profile-tabs-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.tab-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.password-form {
  max-width: 500px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  padding: 0 20px;
}

:deep(.el-tabs__nav) {
  border-radius: 4px 4px 0 0;
}

:deep(.el-tabs--border-card > .el-tabs__header) {
  background-color: #f5f7fa;
}

:deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item.is-active) {
  background-color: #fff;
  border-bottom-color: #fff;
  color: #409eff;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}

:deep(.el-button--primary) {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

:deep(.el-form--label-top .el-form-item__label) {
  margin-bottom: 8px;
}

:deep(.el-form-item__content) {
  flex-wrap: nowrap;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 20px;
  margin-bottom: 0;
}
</style> 