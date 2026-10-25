<template>
  <!-- 管理后台 - 版本管理页面 -->
  <!-- 功能：按应用ID查询版本列表、审核版本（通过/拒绝）、删除版本 -->
  <el-card>
    <template #header>
      <div class="card-header">
        <h3>版本管理</h3>
        <!-- 显示当前查询的应用名称 -->
        <span v-if="appName" class="app-name">当前应用：{{ appName }}</span>
      </div>
    </template>

    <!-- 搜索栏：输入应用ID查询版本 -->
    <div class="search-bar">
      <el-input-number v-model="appId" :min="1" placeholder="应用ID" controls-position="right" />
      <el-button type="primary" :loading="loading" @click="loadData">
        <el-icon><Search /></el-icon> 查询版本
      </el-button>
    </div>

    <!-- 版本列表表格 -->
    <el-table v-loading="loading" :data="versions" style="margin-top: 16px" stripe>
      <el-table-column prop="id" label="版本ID" width="80" />
      <el-table-column prop="versionNo" label="版本号" width="120" />
      <el-table-column prop="versionCode" label="版本编码" width="100" />
      <el-table-column label="文件大小" width="120">
        <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column prop="updateLog" label="更新日志" show-overflow-tooltip />
      <!-- 状态列：1=已通过(绿) 2=已拒绝(红) 0=待审核(黄) -->
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="170" />
      <el-table-column prop="publishTime" label="发布时间" width="170">
        <template #default="{ row }">{{ row.publishTime || '-' }}</template>
      </el-table-column>
      <!-- 操作列：审核（仅待审核状态显示）+ 删除 -->
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <!-- 只有待审核(status=0)的版本才显示审核按钮 -->
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="handleAudit(row, 2)">通过</el-button>
            <el-button size="small" type="danger" @click="handleAudit(row, 3)">拒绝</el-button>
          </template>
          <el-button size="small" type="info" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态提示 -->
    <el-empty
      v-if="versions.length === 0 && !loading"
      description="暂无版本数据，请输入应用ID查询"
    />
  </el-card>
</template>

<script setup>
/**
 * 管理后台 - 版本管理页面
 */
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVersions, deleteVersion } from '@/api/version'
import { auditVersion as auditVersionApi } from '@/api/audit'
import { getAppDetail } from '@/api/audit'

// 响应式变量
const appId = ref(1) // 当前查询的应用ID
const appName = ref('') // 当前应用名称（增强功能）
const versions = ref([]) // 版本列表数据
const loading = ref(false) // 加载状态

/**
 * 加载版本列表数据
 * 同时查询应用详情以显示应用名称
 */
const loadData = async () => {
  if (!appId.value) {
    ElMessage.warning('请输入应用ID')
    return
  }
  loading.value = true
  try {
    // 并行请求版本列表和应用详情
    const [vRes, aRes] = await Promise.all([
      getVersions(appId.value),
      getAppDetail(appId.value).catch(() => ({ data: null })),
    ])
    versions.value = vRes.data || []
    appName.value = aRes.data?.appName || ''
  } catch (e) {
    // 错误已在请求拦截器中提示
  } finally {
    loading.value = false
  }
}

/**
 * 版本审核处理函数（原 auditVersion，重命名避免冲突）
 * @param {Object} row - 当前行的版本数据
 * @param {Number} result - 审核结果：2=通过，3=拒绝
 */
const handleAudit = (row, result) => {
  const action = result === 2 ? '通过' : '拒绝'
  // 弹出输入框，让管理员填写审核意见
  ElMessageBox.prompt(`请输入${action}意见`, '版本审核', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入审核意见（可选）',
  })
    .then(async ({ value }) => {
      try {
        // 调用后端审核接口（使用重命名后的 API 函数）
        await auditVersionApi({
          versionId: row.id,
          auditResult: result,
          auditRemark: value || '',
        })
        ElMessage.success(`已${action}`)
        // 审核成功后刷新列表
        loadData()
      } catch (e) {
        // 错误已在请求拦截器中提示
      }
    })
    .catch(() => {
      // 用户取消，不做处理
    })
}

/**
 * 删除版本（功能增强）
 * @param {Object} row - 当前行的版本数据
 */
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除版本「${row.versionNo}」吗？删除后不可恢复！`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
  })
    .then(async () => {
      try {
        await deleteVersion(row.id)
        ElMessage.success('删除成功')
        loadData()
      } catch (e) {
        // 错误已在请求拦截器中提示
      }
    })
    .catch(() => {})
}

/**
 * 状态标签颜色映射
 * @param {Number} status - 版本状态：0=待审核，1=已通过，2=已拒绝
 */
const statusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

/**
 * 状态文本映射
 */
const statusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

/**
 * 文件大小格式化：字节 → MB/KB
 */
const formatSize = (bytes) => {
  if (!bytes) return '-'
  return bytes >= 1048576 ? (bytes / 1048576).toFixed(1) + 'MB' : (bytes / 1024).toFixed(0) + 'KB'
}
</script>

<style scoped>
/* 搜索栏样式 */
.search-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}
/* 卡片头部：标题和应用名称左右分布 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header h3 {
  margin: 0;
}
.app-name {
  color: #409eff;
  font-size: 14px;
  font-weight: normal;
}
</style>
