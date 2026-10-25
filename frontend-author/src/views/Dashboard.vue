<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="8"
        ><el-card class="stat-card"
          ><div class="stat-icon blue">
            <el-icon :size="20"><Files /></el-icon>
          </div>
          <div class="stat-info">
            <div class="num">{{ stats.myAppCount || 0 }}</div>
            <div class="label">我的应用</div>
          </div></el-card
        ></el-col
      >
      <el-col :xs="24" :sm="8"
        ><el-card class="stat-card"
          ><div class="stat-icon orange">
            <el-icon :size="20"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="num">{{ stats.pendingAuditCount || 0 }}</div>
            <div class="label">待审核</div>
          </div></el-card
        ></el-col
      >
      <el-col :xs="24" :sm="8"
        ><el-card class="stat-card"
          ><div class="stat-icon green">
            <el-icon :size="20"><Download /></el-icon>
          </div>
          <div class="stat-info">
            <div class="num">{{ stats.myDownloadCount ?? 0 }}</div>
            <div class="label">总下载量</div>
          </div></el-card
        ></el-col
      >
    </el-row>
    <el-card style="margin-top: 20px">
      <template #header><h3>快捷操作</h3></template>
      <el-space wrap>
        <el-button type="primary" @click="$router.push('/app/add')"
          ><el-icon><Plus /></el-icon> 上传新应用</el-button
        >
        <el-button @click="$router.push('/apps')">管理我的应用</el-button>
        <el-button @click="$router.push('/notices')">查看平台公告</el-button>
      </el-space>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAuthorStats } from '@/api/statistics'

const stats = ref({})
onMounted(async () => {
  try {
    const res = await getAuthorStats()
    stats.value = res.data || {}
  } catch (e) {
    // 错误已在拦截器提示
  }
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
  color: #909399;
  font-size: 14px;
}
</style>
