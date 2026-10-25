<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <h3>分类管理</h3>
        <el-button type="primary" @click="openDialog()"
          ><el-icon><Plus /></el-icon> 新增分类</el-button
        >
      </div>
    </template>
    <el-table
      v-loading="loading"
      :data="categories"
      row-key="id"
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="categoryName" label="分类名称" />
      <el-table-column label="层级" width="100">
        <template #default="{ row }">
          <el-tag :type="row.parentId === 0 ? 'primary' : 'info'">{{
            row.parentId === 0 ? '一级' : '二级'
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{
            row.status === 1 ? '启用' : '禁用'
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button
            v-if="row.parentId === 0"
            size="small"
            type="primary"
            @click="openDialog(null, row.id)"
            >添加子分类</el-button
          >
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑分类' : form.parentId ? '添加子分类' : '新增分类'"
      width="400px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.categoryName" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { getCategories, addCategory, updateCategory, deleteCategory } from '@/api/category'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, categoryName: '', parentId: 0, sortOrder: 0, status: 1 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCategories()
    const all = res.data || []
    // 构建树形结构
    const tree = all
      .filter((c) => c.parentId === 0)
      .map((parent) => ({
        ...parent,
        children: all.filter((c) => c.parentId === parent.id),
      }))
    categories.value = tree
  } finally {
    loading.value = false
  }
}

const openDialog = (row = null, parentId = 0) => {
  if (row) {
    Object.assign(form, {
      id: row.id,
      categoryName: row.categoryName,
      parentId: row.parentId,
      sortOrder: row.sortOrder,
      status: row.status,
    })
  } else {
    Object.assign(form, { id: null, categoryName: '', parentId, sortOrder: 0, status: 1 })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.categoryName) {
    ElMessage.warning('请输入分类名称')
    return
  }
  if (form.id) await updateCategory(form)
  else await addCategory(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除分类「${row.categoryName}」？`, '警告', { type: 'warning' })
    .then(async () => {
      await deleteCategory(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
