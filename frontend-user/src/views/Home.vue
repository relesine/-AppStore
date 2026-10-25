<template>
  <div class="home container">
    <!-- 品牌搜索横幅 -->
    <div class="hero">
      <h2 class="hero-title">发现优质应用</h2>
      <p class="hero-sub">海量精品应用，安全下载、一键安装</p>
      <el-input
        v-model="heroKeyword"
        size="large"
        class="hero-search"
        placeholder="搜索应用名称、功能描述，回车直达"
        clearable
        @keyup.enter="goSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>
    <!-- 轮播公告 -->
    <el-carousel v-if="notices.length" height="200px" class="banner" :interval="5000">
      <el-carousel-item v-for="notice in notices" :key="notice.id">
        <div class="banner-item" @click="openNotice(notice)">
          <h3>{{ notice.title }}</h3>
          <p>{{ notice.content?.substring(0, 80) }}...</p>
        </div>
      </el-carousel-item>
    </el-carousel>
    <el-card v-else class="banner-empty" shadow="never">
      <el-empty description="暂无公告" :image-size="60" />
    </el-card>

    <!-- 分类导航 -->
    <div class="section">
      <h3 class="section-title">应用分类</h3>
      <div v-if="categories.length" class="category-grid">
        <div
          v-for="(cat, $index) in categories"
          :key="cat.id"
          class="category-item card-hover"
          @click="goCategory(cat.id)"
        >
          <span class="cat-tile" :style="{ background: catTint($index).bg }">
            <el-icon :size="22" :color="catTint($index).fg"><Box /></el-icon>
          </span>
          <span class="cat-name">{{ cat.categoryName }}</span>
        </div>
      </div>
      <el-empty v-else description="暂无分类" :image-size="60" />
    </div>

    <!-- 热门应用 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">热门下载</h3>
        <el-link type="primary" @click="$router.push('/category')">查看更多</el-link>
      </div>
      <div v-if="hotApps.length" class="app-grid">
        <div
          v-for="app in hotApps"
          :key="app.id"
          class="app-card card-hover"
          @click="goDetail(app.id)"
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
      </div>
      <el-empty v-else description="暂无热门应用" :image-size="60" />
    </div>

    <!-- 最新应用 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">最新上架</h3>
      </div>
      <div v-if="newApps.length" class="app-grid">
        <div
          v-for="app in newApps"
          :key="app.id"
          class="app-card card-hover"
          @click="goDetail(app.id)"
        >
          <div class="app-icon">
            <img v-if="app.appIcon" :src="app.appIcon" alt="" />
            <span v-else>{{ app.appName?.charAt(0) }}</span>
          </div>
          <div class="app-info">
            <div class="app-name">{{ app.appName }}</div>
            <div class="app-desc">{{ app.shortDesc }}</div>
            <div class="app-meta">
              <span>版本 {{ app.version }}</span>
              <span>{{ formatSize(app.size) }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无最新应用" :image-size="60" />
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="noticeVisible" :title="currentNotice?.title" width="640px">
      <div class="notice-content">{{ currentNotice?.content }}</div>
      <template #footer>
        <span class="notice-time">发布时间：{{ currentNotice?.publishTime }}</span>
        <el-button type="primary" @click="noticeVisible = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { StarFilled } from '@element-plus/icons-vue'
import { getHotApps, getNewApps } from '@/api/app'
import { getCategories } from '@/api/category'
import { getNoticeList } from '@/api/notice'

const router = useRouter()
const hotApps = ref([])
const newApps = ref([])
const categories = ref([])
const notices = ref([])
const heroKeyword = ref('')

const goSearch = () => {
  router.push({ path: '/search', query: { keyword: heroKeyword.value || '' } })
}
const noticeVisible = ref(false)
const currentNotice = ref(null)

// 分类磁贴低饱和色板（真实应用市场同款克制配色，循环取色）
const CAT_TINTS = [
  { bg: '#ecf1fc', fg: '#2e6be6' },
  { bg: '#ecf9ef', fg: '#4f9e5f' },
  { bg: '#fff4e8', fg: '#e08e3c' },
  { bg: '#f3f0ff', fg: '#7b61c9' },
  { bg: '#e8f7fa', fg: '#3a97ad' },
]
const catTint = (i) => CAT_TINTS[i % CAT_TINTS.length]

const loadData = async () => {
  const [hotRes, newRes, catRes, noticeRes] = await Promise.all([
    getHotApps(8),
    getNewApps(8),
    getCategories(),
    getNoticeList({ pageNum: 1, pageSize: 5 }),
  ])
  hotApps.value = hotRes.data || []
  newApps.value = newRes.data || []
  categories.value = catRes.data?.filter((c) => c.parentId === 0) || []
  notices.value = noticeRes.data.records || []
}

const goDetail = (id) => router.push(`/app/${id}`)
const goCategory = (id) => router.push({ path: '/category', query: { categoryId: id } })
const openNotice = (notice) => {
  currentNotice.value = notice
  noticeVisible.value = true
}

const formatDownload = (num) => (num >= 10000 ? (num / 10000).toFixed(1) + '万' : num)
const formatSize = (bytes) =>
  bytes >= 1048576 ? (bytes / 1048576).toFixed(1) + 'MB' : (bytes / 1024).toFixed(0) + 'KB'

onMounted(loadData)
</script>

<style scoped>
.banner {
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 30px;
}
.banner-empty {
  border-radius: 8px;
  margin-bottom: 30px;
}
.banner-item {
  height: 100%;
  background: var(--app-bg-muted, #f0f7ff);
  color: var(--app-text-regular, #606266);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 60px;
  border-left: 3px solid var(--app-primary, #409eff);
  cursor: pointer;
}
.banner-item h3 {
  font-size: 17px;
  font-weight: 600;
  color: var(--app-text-main, #303133);
  margin-bottom: 8px;
}
.banner-item p {
  font-size: 13px;
  color: var(--app-text-regular, #606266);
}
.section {
  margin-bottom: 30px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
}
.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.category-item {
  background: #fff;
  border-radius: var(--app-radius-md, 6px);
  padding: 18px 16px 14px;
  text-align: center;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.cat-tile {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.cat-name {
  font-size: 14px;
  color: var(--app-text-regular, #606266);
}
.app-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.app-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  gap: 12px;
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
  flex-shrink: 0;
  overflow: hidden;
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
  font-size: 15px;
  margin-bottom: 4px;
}
.app-desc {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}
.app-meta {
  font-size: 12px;
  color: #606266;
  display: flex;
  gap: 12px;
}
.rating {
  color: #e6a23c;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.notice-content {
  line-height: 1.9;
  color: #303133;
  white-space: pre-wrap;
}
.notice-time {
  color: #909399;
  font-size: 13px;
  margin-right: 16px;
}
/* 品牌搜索横幅：同色系极浅渐变，层次克制 */
.hero {
  background: linear-gradient(135deg, #e9f1fd 0%, #f7faff 75%);
  border-radius: 10px;
  padding: 44px 40px 40px;
  margin-bottom: 24px;
  text-align: center;
}
.hero-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--app-text-main, #1d2633);
}
.hero-sub {
  font-size: 14px;
  color: var(--app-text-regular, #6b7787);
  margin: 8px 0 22px;
}
.hero-search {
  max-width: 520px;
  width: 100%;
}
.hero-search .el-input__wrapper {
  border-radius: 8px;
  background: #fff;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .category-grid,
  .app-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 576px) {
  .category-grid,
  .app-grid {
    grid-template-columns: 1fr;
  }
  .banner-item {
    padding: 0 24px;
  }
}
</style>
