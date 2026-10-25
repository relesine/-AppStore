<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6"
        ><el-card class="stat-card"
          ><div class="stat-icon blue">
            <el-icon :size="20"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="num">{{ stats.userCount || 0 }}</div>
            <div class="label">用户总数</div>
            <div class="sub">今日新增 {{ stats.todayNewUsers || 0 }}</div>
          </div></el-card
        ></el-col
      >
      <el-col :span="6"
        ><el-card class="stat-card"
          ><div class="stat-icon green">
            <el-icon :size="20"><Files /></el-icon>
          </div>
          <div class="stat-info">
            <div class="num">{{ stats.appCount || 0 }}</div>
            <div class="label">应用总数</div>
            <div class="sub">待审核 {{ stats.pendingAuditCount || 0 }}</div>
          </div></el-card
        ></el-col
      >
      <el-col :span="6"
        ><el-card class="stat-card"
          ><div class="stat-icon orange">
            <el-icon :size="20"><Download /></el-icon>
          </div>
          <div class="stat-info">
            <div class="num">{{ stats.downloadCount || 0 }}</div>
            <div class="label">下载总数</div>
            <div class="sub">今日 {{ stats.todayDownloadCount || 0 }}</div>
          </div></el-card
        ></el-col
      >
      <el-col :span="6"
        ><el-card class="stat-card"
          ><div class="stat-icon purple">
            <el-icon :size="20"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-info">
            <div class="num">{{ stats.commentCount || 0 }}</div>
            <div class="label">评论总数</div>
            <div class="sub">公告 {{ stats.noticeCount || 0 }}</div>
          </div></el-card
        ></el-col
      >
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header><h3>系统概览</h3></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="平台名称">轻量应用商店</el-descriptions-item>
            <el-descriptions-item label="系统版本">v1.2.0</el-descriptions-item>
            <el-descriptions-item label="技术栈"
              >Spring Boot 2.7 + Vue 3 + MySQL 8.0 + Redis</el-descriptions-item
            >
            <el-descriptions-item label="运行状态"
              ><el-tag type="success">正常运行</el-tag></el-descriptions-item
            >
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><h3>快捷操作</h3></template>
          <el-space wrap>
            <el-button type="primary" @click="$router.push('/audit')">应用审核</el-button>
            <el-button type="success" @click="$router.push('/users')">用户管理</el-button>
            <el-button type="warning" @click="$router.push('/categories')">分类管理</el-button>
            <el-button type="danger" @click="$router.push('/notices')">公告管理</el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboard } from '@/api/statistics'

const stats = ref({})
onMounted(async () => {
  const res = await getDashboard()
  stats.value = res.data
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--app-radius-md, 6px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-bg-muted, #f0f7ff);
  color: var(--app-primary, #409eff);
  flex-shrink: 0;
}
.stat-info .num {
  font-size: 22px;
  font-weight: 600;
  line-height: 1.3;
}
.stat-info .label {
  color: #606266;
  font-size: 14px;
}
.stat-info .sub {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}
</style>
