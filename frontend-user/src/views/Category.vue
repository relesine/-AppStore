<template>
  <div class="category-page container">
    <div class="layout">
      <el-aside width="200px" class="sidebar">
        <div class="sidebar-title">全部分类</div>
        <div
          v-for="cat in parentCategories"
          :key="cat.id"
          class="cat-item"
          :class="{ active: selectedParent === cat.id }"
          @click="selectParent(cat.id)"
        >
          {{ cat.categoryName }}
        </div>
      </el-aside>
      <el-main class="content">
        <div v-if="subCategories.length" class="sub-cats">
          <el-tag
            v-for="sub in subCategories"
            :key="sub.id"
            :type="selectedSub === sub.id ? 'primary' : 'info'"
            class="sub-tag"
            @click="selectSub(sub.id)"
          >
            {{ sub.categoryName }}
          </el-tag>
        </div>
        <div class="app-list">
          <div
            v-for="app in apps"
            :key="app.id"
            class="app-item card-hover"
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
                <span>{{ formatSize(app.size) }}</span>
              </div>
            </div>
            <el-button type="primary" size="small" @click.stop="download(app)">下载</el-button>
          </div>
        </div>
        <el-pagination
          v-if="total > 0"
          class="pagination"
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="pageNum"
          @current-change="handlePageChange"
        />
        <el-empty v-if="apps.length === 0" description="该分类下暂无应用" />
      </el-main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { StarFilled } from '@element-plus/icons-vue'
import { getAppList, downloadApp } from '@/api/app'
import { getCategories, getSubCategories } from '@/api/category'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const parentCategories = ref([])
const subCategories = ref([])
const apps = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const selectedParent = ref(null)
const selectedSub = ref(null)

const loadCategories = async () => {
  const res = await getCategories()
  parentCategories.value = res.data?.filter((c) => c.parentId === 0) || []
  if (parentCategories.value.length) {
    const catId = route.query.categoryId
      ? Number(route.query.categoryId)
      : parentCategories.value[0].id
    selectParent(catId)
  }
}

const selectParent = async (id) => {
  selectedParent.value = id
  selectedSub.value = null
  const res = await getSubCategories(id)
  subCategories.value = res.data || []
  pageNum.value = 1
  loadApps()
}

const selectSub = (id) => {
  selectedSub.value = selectedSub.value === id ? null : id
  pageNum.value = 1
  loadApps()
}

const loadApps = async () => {
  const res = await getAppList({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    categoryId: selectedParent.value,
    subCategoryId: selectedSub.value,
  })
  apps.value = res.data.records || []
  total.value = res.data.total || 0
}

const handlePageChange = (page) => {
  pageNum.value = page
  loadApps()
}

const download = async (app) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const res = await downloadApp(app.id)
  ElMessage.success('开始下载：' + app.appName)
  window.open(res.data, '_blank')
}

const formatDownload = (num) => (num >= 10000 ? (num / 10000).toFixed(1) + '万' : num)
const formatSize = (bytes) =>
  bytes >= 1048576 ? (bytes / 1048576).toFixed(1) + 'MB' : (bytes / 1024).toFixed(0) + 'KB'

onMounted(loadCategories)
</script>

<style scoped>
.layout {
  display: flex;
  gap: 20px;
}
.sidebar {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  height: fit-content;
}
.sidebar-title {
  font-weight: bold;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
.cat-item {
  padding: 10px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 4px;
}
.cat-item:hover {
  background: #f5f7fa;
}
.cat-item.active {
  background: #409eff;
  color: #fff;
}
.content {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  min-height: 500px;
}
.sub-cats {
  margin-bottom: 20px;
}
.sub-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}
.app-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #fafafa;
}
.app-icon {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  background: var(--app-bg-muted, #f0f7ff);
  color: var(--app-primary, #409eff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
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
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 4px;
}
.app-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}
.app-meta {
  font-size: 12px;
  color: #606266;
  display: flex;
  flex-wrap: wrap;
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
