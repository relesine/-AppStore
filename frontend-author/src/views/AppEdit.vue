<template>
  <el-card>
    <template #header
      ><h3>{{ isEdit ? '编辑应用' : '上传新应用' }}</h3></template
    >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      style="max-width: 700px"
    >
      <el-form-item label="应用名称" prop="appName">
        <el-input v-model="form.appName" placeholder="请输入应用名称" />
      </el-form-item>
      <el-form-item label="一级分类" prop="categoryId">
        <el-select
          v-model="form.categoryId"
          placeholder="请选择"
          style="width: 100%"
          @change="onCategoryChange"
        >
          <el-option v-for="c in parentCats" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="二级分类" prop="subCategoryId">
        <el-select v-model="form.subCategoryId" placeholder="请选择" style="width: 100%">
          <el-option v-for="c in subCats" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="应用图标">
        <el-upload :show-file-list="false" :before-upload="beforeIconUpload" accept="image/*">
          <el-button>上传图标</el-button>
        </el-upload>
        <span v-if="form.appIcon" style="margin-left: 10px; color: #67c23a">已上传</span>
      </el-form-item>
      <el-form-item label="简短描述" prop="shortDesc">
        <el-input
          v-model="form.shortDesc"
          placeholder="一句话描述应用"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="详细描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="5"
          placeholder="详细介绍应用功能"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSubmit">提交审核</el-button>
        <el-button @click="$router.back()">返回</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addApp, updateApp, getAppDetail } from '@/api/app'
import { getCategories, getSubCategories } from '@/api/category'
import { uploadIcon } from '@/api/file'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const isEdit = computed(() => !!route.params.id)
const parentCats = ref([])
const subCats = ref([])
const form = reactive({
  appName: '',
  categoryId: null,
  subCategoryId: null,
  appIcon: '',
  shortDesc: '',
  description: '',
})
const rules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  shortDesc: [{ required: true, message: '请输入简短描述', trigger: 'blur' }],
}

const loadCategories = async () => {
  const res = await getCategories()
  parentCats.value = res.data?.filter((c) => c.parentId === 0) || []
}

const onCategoryChange = async (id) => {
  form.subCategoryId = null
  const res = await getSubCategories(id)
  subCats.value = res.data || []
}

const beforeIconUpload = async (file) => {
  const res = await uploadIcon(file)
  form.appIcon = res.data
  ElMessage.success('图标上传成功')
  return false
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    if (isEdit.value) {
      form.id = Number(route.params.id)
      await updateApp(form)
    } else {
      await addApp(form)
    }
    ElMessage.success('提交成功，等待审核')
    router.push('/apps')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadCategories()
  if (isEdit.value) {
    const res = await getAppDetail(route.params.id)
    Object.assign(form, res.data)
    if (form.categoryId) await onCategoryChange(form.categoryId)
  }
})
</script>
