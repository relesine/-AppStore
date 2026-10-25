<template>
  <div class="layout">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/home')">
          <span class="logo-mark"><el-icon :size="16" color="#fff"><ShoppingCart /></el-icon></span>
          <span class="logo-text">轻量应用商店</span>
        </div>
        <el-menu mode="horizontal" :default-active="$route.path" router class="nav-menu">
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/category">分类</el-menu-item>
          <el-menu-item index="/search">搜索</el-menu-item>
        </el-menu>
        <div class="header-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索应用"
            style="width: 200px"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix
              ><el-icon><Search /></el-icon
            ></template>
          </el-input>
          <el-dropdown v-if="userStore.isLogin" @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32">{{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}</el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="downloads">下载记录</el-dropdown-item>
                <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button v-else type="primary" @click="$router.push('/login')">登录</el-button>
        </div>
      </div>
    </el-header>
    <el-main class="main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
    <el-footer class="footer">
      <p>轻量应用商店 © 2026 学习级项目</p>
    </el-footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
      .then(() => {
        userStore.logout()
        router.push('/home')
      })
      .catch(() => {})
  } else {
    router.push('/' + command)
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.header {
  background: #fff;
  border-bottom: 1px solid var(--app-border, #e4e7ed);
  padding: 0;
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 20;
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 100%;
}
.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 40px;
  gap: 8px;
}
.logo-mark {
  width: 30px;
  height: 30px;
  border-radius: 7px;
  background: var(--app-primary, #2e6be6);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--app-text-main, #303133);
  margin-left: 8px;
}
.nav-menu {
  flex: 1;
  border-bottom: none;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
}
.username {
  font-size: 14px;
  color: #606266;
}
.main {
  flex: 1;
  padding: 20px 0;
  background: #f5f7fa;
}
.footer {
  text-align: center;
  color: #909399;
  font-size: 13px;
  background: #fff;
  padding: 20px 0;
}
</style>
