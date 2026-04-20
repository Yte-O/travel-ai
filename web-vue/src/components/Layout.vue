<template>
  <div class="admin-workbench">
    <div class="ambient ambient-left"></div>
    <div class="ambient ambient-right"></div>

    <header class="workbench-header glass-card">
      <div class="brand-block">
        <img src="@/assets/images/logo.png" alt="logo" />
        <div>
          <strong>Travel AI OPS</strong>
          <p>智能运营工作台</p>
        </div>
      </div>

      <nav class="workbench-nav" v-if="isAdmin">
        <button
          v-for="item in navItems"
          :key="item.path"
          class="nav-chip"
          :class="{ active: activeMenu === item.path }"
          @click="router.push(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="header-tools">
        <AlgoHealthCheck class="health-check" />
        <el-dropdown @command="handleCommand">
          <span class="user-box">
            <el-avatar :size="34" :src="userInfo?.avatarUrl" />
            <span>{{ userInfo?.username }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <section class="workspace-grid">
      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="route-fade" mode="out-in">
            <component :is="Component" class="section-enter" />
          </transition>
        </router-view>
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Monitor, User, Files, Star, Clock } from '@element-plus/icons-vue'
import AlgoHealthCheck from './AlgoHealthCheck.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 计算当前激活的菜单项
const activeMenu = computed(() => route.path)

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 是否是管理员
const isAdmin = computed(() => userStore.isAdmin())

const navItems = [
  { path: '/admin', label: '控制台', icon: Monitor },
  { path: '/admin/kgm', label: '知识图谱', icon: Star },
  { path: '/admin/users', label: '用户治理', icon: User },
  { path: '/admin/categories', label: '分类中心', icon: Files },
  { path: '/admin/items', label: '景点资产', icon: Files },
  { path: '/admin/user-actions', label: '行为日志', icon: Clock }
]

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'logout':
      userStore.logout()
      break
  }
}
</script>

<style scoped>
.admin-workbench {
  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  padding: 14px;
  background: #E0FFFF; /* 淡青色背景 */
}

.ambient {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(70px);
  opacity: 0.65;
}

.ambient-left {
  width: 320px;
  height: 320px;
  top: -120px;
  left: -80px;
  background: #ffe7b3;
}

.ambient-right {
  width: 420px;
  height: 420px;
  right: -150px;
  bottom: -180px;
  background: #c8f6eb;
}

.workbench-header {
  position: sticky;
  top: 10px;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: space-between;
  border-radius: 22px;
  padding: 12px;
  border: 1px solid rgba(31, 43, 31, 0.08);
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-block img {
  width: 34px;
  height: 34px;
}

.brand-block strong {
  font-size: 15px;
  letter-spacing: 0.6px;
}

.brand-block p {
  margin: 3px 0 0;
  font-size: 11px;
  color: #607260;
}

.workbench-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  justify-content: center;
  flex-wrap: wrap;
}

.nav-chip {
  border: 1px solid rgba(23, 35, 23, 0.12);
  background: rgba(255, 255, 255, 0.62);
  color: #364936;
  min-height: 36px;
  padding: 0 12px;
  border-radius: 999px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.nav-chip:hover,
.nav-chip.active {
  background: #104a40;
  border-color: transparent;
  color: #fff;
}

.header-tools {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 3px 8px 3px 3px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.7);
}

.workspace-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  align-items: start;
}

.admin-content {
  padding: 0;
  min-width: 0;
  align-self: start;
}

.route-fade-enter-active,
.route-fade-leave-active {
  transition: all 0.25s ease;
}

.route-fade-enter-from,
.route-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 980px) {
  .admin-workbench {
    padding: 10px;
  }

  .workbench-header {
    flex-wrap: wrap;
    top: 4px;
  }

  .workspace-grid {
    grid-template-columns: 1fr;
  }

  .context-panel {
    position: relative;
  }
}
</style> 