<template>
  <div>
    <div class="toolbar">
      <h3>版本管理 - {{ appName }}</h3>
      <el-button type="primary" @click="dialogVisible = true"
        ><el-icon><Plus /></el-icon> 发布新版本</el-button
      >
    </div>
    <el-card>
      <el-table :data="versions">
        <el-table-column prop="versionNo" label="版本号" width="120" />
        <el-table-column prop="versionCode" label="版本编码" width="100" />
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="updateLog" label="更新日志" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'warning'">
              {{ row.status === 1 ? '已通过' : row.status === 2 ? '已拒绝' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170" />
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="发布新版本" width="500px">
      <el-form :model="versionForm" label-width="80px">
        <el-form-item label="版本号" required>
          <el-input v-model="versionForm.versionNo" placeholder="如 1.0.1" />
        </el-form-item>
        <el-form-item label="安装包" required>
          <el-upload :show-file-list="false" :before-upload="beforeApkUpload">
            <el-button>选择APK文件</el-button>
          </el-upload>
          <span v-if="versionForm.fileUrl" style="margin-left: 10px; color: #67c23a">已上传</span>
        </el-form-item>
        <el-form-item label="更新日志">
          <el-input
            v-model="versionForm.updateLog"
            type="textarea"
            :rows="3"
            placeholder="描述本次更新内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitVersion">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVersions, addVersion } from '@/api/version'
import { getAppDetail } from '@/api/app'
import { uploadApk } from '@/api/file'

const route = useRoute()
const versions = ref([])
const appName = ref('')
const dialogVisible = ref(false)
const submitting = ref(false)
const versionForm = reactive({
  appId: route.params.appId,
  versionNo: '',
  fileUrl: '',
  fileSize: 0,
  updateLog: '',
})

const loadData = async () => {
  const [vRes, aRes] = await Promise.all([
    getVersions(route.params.appId),
    getAppDetail(route.params.appId),
  ])
  versions.value = vRes.data || []
  appName.value = aRes.data?.appName || ''
}

const beforeApkUpload = async (file) => {
  const res = await uploadApk(file)
  versionForm.fileUrl = res.data
  versionForm.fileSize = file.size
  ElMessage.success('文件上传成功')
  return false
}

const submitVersion = async () => {
  if (!versionForm.versionNo || !versionForm.fileUrl) {
    ElMessage.warning('请填写版本号并上传安装包')
    return
  }
  submitting.value = true
  try {
    await addVersion(versionForm)
    ElMessage.success('版本提交成功，等待审核')
    dialogVisible.value = false
    versionForm.versionNo = ''
    versionForm.fileUrl = ''
    versionForm.updateLog = ''
    loadData()
  } finally {
    submitting.value = false
  }
}

const formatSize = (bytes) =>
  bytes >= 1048576 ? (bytes / 1048576).toFixed(1) + 'MB' : (bytes / 1024).toFixed(0) + 'KB'

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
