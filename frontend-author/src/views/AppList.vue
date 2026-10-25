<template>
  <div>
    <div class="toolbar">
      <h3>我的应用</h3>
      <el-button type="primary" @click="$router.push('/app/add')"
        ><el-icon><Plus /></el-icon> 上传新应用</el-button
      >
    </div>
    <el-card>
      <el-table v-loading="loading" :data="apps">
        <el-table-column prop="appName" label="应用名称" />
        <el-table-column prop="version" label="当前版本" width="100" />
        <el-table-column prop="downloadCount" label="下载量" width="100" />
        <el-table-column prop="rating" label="评分" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.auditStatus)">{{ statusText(row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/app/edit/${row.id}`)">编辑</el-button>
            <el-button size="small" type="primary" @click="$router.push(`/version/${row.id}`)"
              >版本管理</el-button
            >
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyApps, deleteApp } from '@/api/app'

const apps = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyApps({ pageNum: pageNum.value, pageSize: pageSize.value })
    apps.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除应用「${row.appName}」？`, '警告', { type: 'warning' })
    .then(async () => {
      await deleteApp(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
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
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
