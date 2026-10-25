<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <h3>应用审核</h3>
        <el-radio-group v-model="auditStatus" @change="loadData">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="0">待审核</el-radio-button>
          <el-radio-button :label="2">已通过</el-radio-button>
          <el-radio-button :label="3">已拒绝</el-radio-button>
        </el-radio-group>
      </div>
    </template>
    <el-table v-loading="loading" :data="apps">
      <el-table-column prop="appName" label="应用名称" width="150" />
      <el-table-column prop="authorName" label="作者" width="120" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="shortDesc" label="简介" show-overflow-tooltip />
      <el-table-column prop="version" label="版本" width="80" />
      <el-table-column label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.auditStatus)">{{ statusText(row.auditStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="170" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="audit(row, 2)">通过</el-button>
          <el-button size="small" type="danger" @click="audit(row, 3)">拒绝</el-button>
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="pagination"
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="pageNum"
      @current-change="handlePage"
    />

    <el-dialog v-model="detailVisible" title="应用详情" width="600px">
      <el-descriptions v-if="currentApp" :column="1" border>
        <el-descriptions-item label="应用名称">{{ currentApp.appName }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ currentApp.authorName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentApp.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ currentApp.version }}</el-descriptions-item>
        <el-descriptions-item label="详细描述">{{ currentApp.description }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAuditList, auditApp, getAppDetail } from '@/api/audit'

const apps = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const auditStatus = ref(null)
const loading = ref(false)
const detailVisible = ref(false)
const currentApp = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAuditList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      auditStatus: auditStatus.value,
    })
    apps.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const audit = (row, result) => {
  const action = result === 2 ? '通过' : '拒绝'
  ElMessageBox.prompt(`请输入${action}意见`, '审核确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入审核意见（可选）',
  })
    .then(async ({ value }) => {
      await auditApp({ appId: row.id, auditResult: result, auditRemark: value || '' })
      ElMessage.success(`已${action}`)
      loadData()
    })
    .catch(() => {})
}

const viewDetail = async (row) => {
  const res = await getAppDetail(row.id)
  currentApp.value = res.data
  detailVisible.value = true
}

const statusType = (s) => ({ 0: 'warning', 1: 'info', 2: 'success', 3: 'danger' })[s] || 'info'
const statusText = (s) => ({ 0: '待审核', 1: '审核中', 2: '已通过', 3: '已拒绝' })[s] || '未知'
const handlePage = (p) => {
  pageNum.value = p
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
