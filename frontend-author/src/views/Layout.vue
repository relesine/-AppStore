<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon :size="22" color="#409eff"><Box /></el-icon>
        <span>作者中心</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
      >
        <el-menu-item index="/dashboard"
          ><el-icon><DataAnalysis /></el-icon><span>数据概览</span></el-menu-item
        >
        <el-menu-item index="/apps"
          ><el-icon><Files /></el-icon><span>我的应用</span></el-menu-item
        >
        <el-menu-item index="/notices"
          ><el-icon><Bell /></el-icon><span>平台公告</span></el-menu-item
        >
        <el-menu-item index="/profile"
          ><el-icon><User /></el-icon><span>个人资料</span></el-menu-item
        >
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">轻量应用商店 - 作者后台</span>
        <el-dropdown @command="handleCommand">
          <span class="user"
            >{{ authorStore.userInfo?.nickname }}<el-icon><ArrowDown /></el-icon
          ></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthorStore } from '@/store/author'

const router = useRouter()
const authorStore = useAuthorStore()

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定退出？', '提示', { type: 'warning' })
      .then(() => {
        authorStore.logout()
        router.push('/login')
      })
      .catch(() => {})
  } else router.push('/' + cmd)
}
</script>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background: #fff;
  border-right: 1px solid var(--app-border, #e4e7ed);
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--app-text-main, #303133);
  font-size: 17px;
  font-weight: 600;
  border-bottom: 1px solid var(--app-border-light, #ebeef5);
}
.header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--app-border, #e4e7ed);
}
.title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text-main, #303133);
}
.user {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.main {
  background: var(--app-bg-page, #f5f7fa);
  padding: 20px;
}
</style>
