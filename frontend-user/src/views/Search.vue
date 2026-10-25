<template>
  <div class="search-page container">
    <el-card class="search-card">
      <el-input
        v-model="keyword"
        size="large"
        placeholder="输入应用名称搜索"
        clearable
        @keyup.enter="doSearch"
      >
        <template #prefix
          ><el-icon><Search /></el-icon
        ></template>
        <template #append>
          <el-button @click="doSearch">搜索</el-button>
        </template>
      </el-input>
    </el-card>

    <div v-if="searched" class="results">
      <h3>搜索结果（共 {{ total }} 个）</h3>
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
          </div>
        </div>
      </div>
      <el-empty v-if="apps.length === 0" description="没有找到相关应用" />
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
    </div>
    <div v-else class="hot-tags">
      <h3>热门搜索</h3>
      <div class="tags">
        <el-tag v-for="tag in hotTags" :key="tag" class="hot-tag" @click="searchTag(tag)">{{
          tag
        }}</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { StarFilled } from '@element-plus/icons-vue'
import { searchApps } from '@/api/app'

const route = useRoute()
const keyword = ref('')
const apps = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searched = ref(false)
const hotTags = ['清理', '游戏', '办公', '学习', '安全', '图片']

const doSearch = async () => {
  if (!keyword.value.trim()) return
  searched.value = true
  pageNum.value = 1
  const res = await searchApps({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: keyword.value,
  })
  apps.value = res.data.records || []
  total.value = res.data.total || 0
}

const handlePage = (page) => {
  pageNum.value = page
  doSearch()
}

const searchTag = (tag) => {
  keyword.value = tag
  doSearch()
}

const formatDownload = (num) => (num >= 10000 ? (num / 10000).toFixed(1) + '万' : num)

onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword
    doSearch()
  }
})
</script>

<style scoped>
.search-card {
  margin-bottom: 20px;
}
.results h3 {
  margin-bottom: 16px;
}
.app-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
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
.hot-tags {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}
.hot-tags h3 {
  margin-bottom: 16px;
}
.hot-tag {
  margin-right: 12px;
  margin-bottom: 12px;
  cursor: pointer;
}
</style>
