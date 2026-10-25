<template>
  <div class="page container">
    <el-card>
      <template #header><h3>我的收藏</h3></template>
      <div
        v-for="app in apps"
        :key="app.id"
        class="app-item"
        @click="$router.push(`/app/${app.id}`)"
      >
        <div class="app-icon">
          <img v-if="app.appIcon" :src="app.appIcon" alt="" />
          <span v-else>{{ app.appName?.charAt(0) }}</span>
        </div>
        <div class="app-info">
          <div class="app-name">{{ app.appName }}</div>
          <div class="app-desc">{{ app.shortDesc }}</div>
          <div class="app-meta">
            <span class="rating"
              ><el-icon><StarFilled /></el-icon> {{ app.rating || '暂无' }}</span
            >
            <span>{{ formatDownload(app.downloadCount) }}次下载</span>
          </div>
        </div>
        <el-button type="danger" size="small" @click.stop="removeFav(app.id)">取消收藏</el-button>
      </div>
      <el-empty v-if="apps.length === 0" description="暂无收藏" />
      <el-pagination
        v-if="total > 0"
        class="pagination"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="handlePage"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { StarFilled } from '@element-plus/icons-vue'
import { getMyFavorites, cancelFavorite } from '@/api/favorite'

const apps = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const loadData = async () => {
  const res = await getMyFavorites({ pageNum: pageNum.value, pageSize: pageSize.value })
  apps.value = res.data.records || []
  total.value = res.data.total || 0
}

const removeFav = async (id) => {
  await cancelFavorite(id)
  ElMessage.success('已取消收藏')
  loadData()
}

const handlePage = (page) => {
  pageNum.value = page
  loadData()
}

const formatDownload = (num) => (num >= 10000 ? (num / 10000).toFixed(1) + '万' : num)

onMounted(loadData)
</script>

<style scoped>
.app-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f5f7fa;
  cursor: pointer;
}
.app-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: var(--app-bg-muted, #f0f7ff);
  color: var(--app-primary, #409eff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  overflow: hidden;
  flex-shrink: 0;
}
.app-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.app-info {
  flex: 1;
  min-width: 0;
}
.app-name {
  font-weight: bold;
  margin-bottom: 4px;
}
.app-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}
.app-meta {
  font-size: 12px;
  color: #606266;
  display: flex;
  gap: 16px;
}
.rating {
  color: #e6a23c;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.pagination {
  margin-top: 20px;
  text-align: center;
}
</style>
