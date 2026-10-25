<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <h3>公告管理</h3>
        <el-button type="primary" @click="openDialog()"
          ><el-icon><Plus /></el-icon> 发布公告</el-button
        >
      </div>
    </template>
    <el-table v-loading="loading" :data="notices">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.noticeType === 1 ? '' : row.noticeType === 2 ? 'success' : 'warning'">
            {{ row.noticeType === 1 ? '系统公告' : row.noticeType === 2 ? '更新公告' : '活动公告' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{
            row.status === 1 ? '已发布' : '草稿'
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.noticeType">
            <el-option label="系统公告" :value="1" />
            <el-option label="更新公告" :value="2" />
            <el-option label="活动公告" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="发布"
            inactive-text="草稿"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoticeList, addNotice, updateNotice, deleteNotice } from '@/api/notice'

const notices = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, title: '', content: '', noticeType: 1, status: 1 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getNoticeList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: -1,
    })
    notices.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const openDialog = (row = null) => {
  if (row)
    Object.assign(form, {
      id: row.id,
      title: row.title,
      content: row.content,
      noticeType: row.noticeType,
      status: row.status,
    })
  else Object.assign(form, { id: null, title: '', content: '', noticeType: 1, status: 1 })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  if (form.id) await updateNotice(form)
  else await addNotice(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除公告「${row.title}」？`, '警告', { type: 'warning' })
    .then(async () => {
      await deleteNotice(row.id)
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
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
