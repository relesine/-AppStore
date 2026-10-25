<template>
  <el-card>
    <template #header><h3>平台公告</h3></template>
    <el-table :data="notices">
      <el-table-column prop="title" label="标题" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.noticeType === 1 ? '' : row.noticeType === 2 ? 'success' : 'warning'">
            {{ row.noticeType === 1 ? '系统公告' : row.noticeType === 2 ? '更新公告' : '活动公告' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="viewDetail(row)">查看</el-button>
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

    <el-dialog v-model="detailVisible" :title="current?.title" width="600px">
      <!-- 安全加固：改为纯文本渲染（white-space: pre-wrap 保留换行），避免公告内容注入HTML脚本(XSS) -->
      <div class="notice-content">{{ current?.content }}</div>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNoticeList, getNoticeDetail } from '@/api/notice'

const notices = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const detailVisible = ref(false)
const current = ref(null)

const loadData = async () => {
  const res = await getNoticeList({ pageNum: pageNum.value, pageSize: pageSize.value })
  notices.value = res.data.records || []
  total.value = res.data.total || 0
}

const viewDetail = async (row) => {
  const res = await getNoticeDetail(row.id)
  current.value = res.data
  detailVisible.value = true
}

const handlePage = (p) => {
  pageNum.value = p
  loadData()
}
onMounted(loadData)
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  text-align: right;
}
.notice-content {
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
