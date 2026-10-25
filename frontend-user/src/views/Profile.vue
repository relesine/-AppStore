<template>
  <div class="profile-page container">
    <!-- 用户信息卡片 -->
    <el-card v-loading="infoLoading" class="profile-card">
      <div class="profile-header">
        <el-avatar :size="80" :src="userInfo.avatar || ''">{{
          userInfo.nickname?.charAt(0) || 'U'
        }}</el-avatar>
        <div class="profile-info">
          <h2>{{ userInfo.nickname }}</h2>
          <p>用户名：{{ userInfo.username }}</p>
          <p>邮箱：{{ userInfo.email || '未设置' }}</p>
          <p>手机：{{ userInfo.phone || '未设置' }}</p>
          <el-tag :type="userTypeTag">{{ userTypeText }}</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 资料编辑与密码修改 -->
    <el-card class="menu-card">
      <template #header>
        <div class="card-header">
          <h3>账号设置</h3>
        </div>
      </template>
      <el-tabs v-model="activeTab">
        <!-- 编辑资料 -->
        <el-tab-pane label="编辑资料" name="profile">
          <el-form
            ref="editFormRef"
            :model="editForm"
            :rules="editRules"
            label-width="90px"
            style="max-width: 520px"
          >
            <el-form-item label="昵称" prop="nickname">
              <el-input
                v-model="editForm.nickname"
                maxlength="20"
                show-word-limit
                placeholder="请输入昵称"
              />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="editForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="editForm.phone" maxlength="11" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存修改</el-button>
              <el-button @click="resetEdit">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="pwdFormRef"
            :model="pwdForm"
            :rules="pwdRules"
            label-width="90px"
            style="max-width: 520px"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="pwdForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="pwdForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码（至少6位）"
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="pwdForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" :loading="pwdSaving" @click="submitPassword"
                >确认修改</el-button
              >
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 快捷入口 -->
    <el-card class="menu-card">
      <template #header><h3>快捷入口</h3></template>
      <div class="quick-links">
        <el-button @click="$router.push('/downloads')"
          ><el-icon><Download /></el-icon> 下载记录</el-button
        >
        <el-button @click="$router.push('/favorites')"
          ><el-icon><Star /></el-icon> 我的收藏</el-button
        >
        <el-button type="danger" plain @click="handleLogout"
          ><el-icon><SwitchButton /></el-icon> 退出登录</el-button
        >
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getUserInfo, updateProfile, changePassword } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const infoLoading = ref(false)
const saving = ref(false)
const pwdSaving = ref(false)
const activeTab = ref('profile')
const editFormRef = ref()
const pwdFormRef = ref()
const userInfo = ref({ ...(userStore.userInfo || {}) })

// 编辑资料表单
const editForm = reactive({ nickname: '', email: '', phone: '' })
const editRules = {
  nickname: [
    { required: true, message: '昵称不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
}

// 修改密码表单
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '新密码长度在 6 到 20 位之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

const userTypeText = computed(() => {
  const map = { 1: '普通用户', 2: '应用作者', 3: '管理员' }
  return map[userInfo.value.userType] || '普通用户'
})
const userTypeTag = computed(() => {
  const map = { 1: '', 2: 'warning', 3: 'danger' }
  return map[userInfo.value.userType] || ''
})

const loadUserInfo = async () => {
  infoLoading.value = true
  try {
    const res = await getUserInfo()
    userInfo.value = res.data || {}
    userStore.userInfo = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    resetEdit()
  } catch (e) {
    userInfo.value = { ...(userStore.userInfo || {}) }
  } finally {
    infoLoading.value = false
  }
}

const resetEdit = () => {
  editForm.nickname = userInfo.value.nickname || ''
  editForm.email = userInfo.value.email || ''
  editForm.phone = userInfo.value.phone || ''
}

const saveProfile = async () => {
  await editFormRef.value.validate()
  saving.value = true
  try {
    const res = await updateProfile({ ...editForm })
    userInfo.value = res.data || userInfo.value
    userStore.userInfo = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    ElMessage.success('资料修改成功')
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    saving.value = false
  }
}

const submitPassword = async () => {
  await pwdFormRef.value.validate()
  pwdSaving.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    pwdSaving.value = false
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    .then(() => {
      userStore.logout()
      router.push('/home')
    })
    .catch(() => {})
}

onMounted(loadUserInfo)
</script>

<style scoped>
.profile-card {
  margin-bottom: 20px;
}
.profile-header {
  display: flex;
  gap: 24px;
  align-items: center;
}
.profile-info h2 {
  margin-bottom: 8px;
}
.profile-info p {
  margin-bottom: 4px;
  color: #606266;
}
.menu-card {
  margin-bottom: 20px;
}
.card-header h3 {
  margin: 0;
}
.quick-links {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
