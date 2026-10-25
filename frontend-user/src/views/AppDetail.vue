<template>
  <div v-loading="loading" class="detail-page container">
    <el-result v-if="loadError" icon="error" title="应用不存在或已下架">
      <template #extra>
        <el-button type="primary" @click="$router.push('/home')">返回首页</el-button>
      </template>
    </el-result>

    <template v-else>
      <div v-if="app" class="app-header">
        <div class="app-icon">
          <img v-if="app.appIcon" :src="app.appIcon" alt="" />
          <span v-else>{{ app.appName?.charAt(0) }}</span>
        </div>
        <div class="app-main">
          <h2>{{ app.appName }}</h2>
          <p class="desc">{{ app.shortDesc }}</p>
          <div class="meta">
            <span class="rating"
              ><el-icon><StarFilled /></el-icon> {{ app.rating }}分</span
            >
            <span>{{ formatDownload(app.downloadCount) }}次下载</span>
            <span>版本 {{ app.version }}</span>
            <span>{{ formatSize(app.size) }}</span>
            <span>作者：{{ app.authorName }}</span>
          </div>
        </div>
        <div class="actions">
          <el-button type="primary" size="large" :loading="downloading" @click="handleDownload">
            <el-icon><Download /></el-icon> 下载应用
          </el-button>
          <el-button :type="isFav ? 'warning' : 'default'" size="large" @click="toggleFavorite">
            <el-icon><Star /></el-icon> {{ isFav ? '已收藏' : '收藏' }}
          </el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="应用介绍" name="desc">
          <div class="content-box">
            <p class="description-text">{{ app?.description }}</p>
          </div>
        </el-tab-pane>
        <el-tab-pane label="版本记录" name="version">
          <div class="content-box">
            <el-timeline v-if="versions.length">
              <el-timeline-item
                v-for="v in versions"
                :key="v.id"
                :timestamp="v.publishTime || v.createTime"
              >
                <h4>版本 {{ v.versionNo }}</h4>
                <p class="update-log">{{ v.updateLog || '暂无更新说明' }}</p>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无版本记录" :image-size="60" />
          </div>
        </el-tab-pane>
        <el-tab-pane :label="`用户评价 (${comments.length})`" name="comment">
          <div class="content-box">
            <div v-if="userStore.isLogin" class="comment-form">
              <div class="comment-form-title">发表评价</div>
              <el-rate v-model="newComment.rating" />
              <el-input
                v-model="newComment.content"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                placeholder="写下你的评价（请文明用语）..."
              />
              <el-button
                type="primary"
                style="margin-top: 10px"
                :loading="commenting"
                @click="submitComment"
                >发表评论</el-button
              >
            </div>
            <div v-else class="login-tip">
              <el-link type="primary" @click="$router.push('/login')">登录</el-link> 后发表评论
            </div>
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <el-avatar :src="c.avatar || ''">{{ c.nickname?.charAt(0) || 'U' }}</el-avatar>
              <div class="comment-content">
                <div class="comment-header">
                  <span class="username">{{ c.nickname || c.username }}</span>
                  <el-rate :model-value="c.rating" disabled size="small" />
                  <span class="time">{{ c.createTime }}</span>
                </div>
                <p>{{ c.content }}</p>
              </div>
            </div>
            <el-empty
              v-if="comments.length === 0"
              description="暂无评论，快来抢沙发"
              :image-size="60"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { StarFilled } from '@element-plus/icons-vue'
import { getAppDetail, downloadApp } from '@/api/app'
import { getComments, addComment } from '@/api/comment'
import { addFavorite, cancelFavorite, checkFavorite } from '@/api/favorite'
import { getVersionsByAppId } from '@/api/version'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const loadError = ref(false)
const app = ref(null)
const comments = ref([])
const versions = ref([])
const activeTab = ref('desc')
const isFav = ref(false)
const downloading = ref(false)
const commenting = ref(false)
const newComment = reactive({ content: '', rating: 5 })

const loadData = async () => {
  loading.value = true
  loadError.value = false
  try {
    const id = route.params.id
    const [appRes, commentRes, versionRes] = await Promise.all([
      getAppDetail(id),
      getComments(id),
      getVersionsByAppId(id, 1),
    ])
    app.value = appRes.data
    comments.value = commentRes.data || []
    // 仅展示已通过审核的版本（status=1）
    versions.value = versionRes.data || []
    if (userStore.isLogin) {
      const favRes = await checkFavorite(id)
      isFav.value = favRes.data
    }
  } catch (e) {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

const handleDownload = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  downloading.value = true
  try {
    const res = await downloadApp(app.value.id)
    ElMessage.success('开始下载')
    if (res.data) window.open(res.data, '_blank')
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    downloading.value = false
  }
}

const toggleFavorite = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (isFav.value) {
    await cancelFavorite(app.value.id)
    isFav.value = false
    ElMessage.success('已取消收藏')
  } else {
    await addFavorite(app.value.id)
    isFav.value = true
    ElMessage.success('收藏成功')
  }
}

const submitComment = async () => {
  if (!newComment.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  commenting.value = true
  try {
    await addComment({
      appId: app.value.id,
      content: newComment.content,
      rating: newComment.rating,
    })
    ElMessage.success('评论成功')
    newComment.content = ''
    newComment.rating = 5
    const res = await getComments(app.value.id)
    comments.value = res.data || []
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    commenting.value = false
  }
}

const formatDownload = (num) => (num >= 10000 ? (num / 10000).toFixed(1) + '万' : num)
const formatSize = (bytes) =>
  bytes >= 1048576 ? (bytes / 1048576).toFixed(1) + 'MB' : (bytes / 1024).toFixed(0) + 'KB'

onMounted(loadData)
</script>

<style scoped>
.app-header {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.app-icon {
  width: 96px;
  height: 96px;
  border-radius: 20px;
  background: var(--app-bg-muted, #f0f7ff);
  color: var(--app-primary, #409eff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: bold;
  flex-shrink: 0;
  overflow: hidden;
}
.app-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.app-main {
  flex: 1;
  min-width: 0;
}
.app-main h2 {
  margin-bottom: 8px;
}
.desc {
  color: #606266;
  margin-bottom: 12px;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}
.rating {
  color: #e6a23c;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.detail-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 0 20px;
}
.content-box {
  padding: 20px 0;
}
.description-text {
  line-height: 1.9;
  color: #303133;
  white-space: pre-wrap;
}
.update-log {
  color: #606266;
}
.comment-form {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}
.comment-form-title {
  margin-bottom: 10px;
  font-weight: bold;
}
.login-tip {
  margin-bottom: 20px;
}
.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f5f7fa;
}
.comment-content {
  flex: 1;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}
.username {
  font-weight: bold;
}
.time {
  font-size: 12px;
  color: #c0c4cc;
}
</style>
