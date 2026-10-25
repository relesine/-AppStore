<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <h3>用户管理</h3>
        <div class="search-bar">
          <el-input
            v-model="keyword"
            placeholder="搜索用户名/昵称"
            style="width: 200px"
            clearable
            @clear="loadData"
            @keyup.enter="loadData"
          />
          <el-select
            v-model="userType"
            placeholder="用户类型"
            clearable
            style="width: 120px"
            @change="loadData"
          >
            <el-option label="普通用户" :value="1" />
            <el-option label="作者" :value="2" />
            <el-option label="管理员" :value="3" />
          </el-select>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </div>
      </div>
    </template>
    <el-table v-loading="loading" :data="users">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.userType === 3 ? 'danger' : row.userType === 2 ? 'warning' : ''">
            {{ row.userType === 3 ? '管理员' : row.userType === 2 ? '作者' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="(val) => toggleStatus(row, val)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="pagination"
      background
      layout="prev, pager, next, total"
      :total="total"
      :page-size="pageSize"
      :current-page="pageNum"
      @current-change="handlePage"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, deleteUser, updateStatus } from '@/api/user'

const users = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const userType = ref(null)
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
      userType: userType.value,
    })
    users.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row, val) => {
  await updateStatus({ id: row.id, status: val ? 1 : 0 })
  ElMessage.success('状态已更新')
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除用户「${row.username}」？`, '警告', { type: 'warning' })
    .then(async () => {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

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
.search-bar {
  display: flex;
  gap: 10px;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
