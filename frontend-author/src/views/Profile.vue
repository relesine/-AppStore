<template>
  <!-- 个人资料页面：包含信息展示、编辑资料、修改密码三大功能 -->
  <div class="profile-page">
    <!-- 第一部分：用户信息卡片（头像 + 基本信息） -->
    <el-card v-loading="infoLoading" class="info-card">
      <div class="profile-header">
        <!-- 头像区域：有头像显示图片，无头像显示昵称首字 -->
        <el-avatar :size="90" class="avatar">
          <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="头像" />
          <span v-else>{{ userInfo.nickname?.charAt(0) || '作' }}</span>
        </el-avatar>
        <div class="profile-info">
          <h2 class="nickname">{{ userInfo.nickname || '未设置昵称' }}</h2>
          <p class="username">用户名：{{ userInfo.username }}</p>
          <el-tag type="warning" size="large">应用作者</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 第二部分：个人资料详情（查看/编辑切换） -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h3>个人资料</h3>
          <!-- 编辑/保存按钮切换 -->
          <el-button v-if="!isEditing" type="primary" @click="startEdit">
            <el-icon><Edit /></el-icon> 编辑资料
          </el-button>
          <div v-else>
            <el-button type="success" :loading="saving" @click="saveProfile">保存</el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </div>
        </div>
      </template>

      <!-- 查看模式：使用描述列表展示 -->
      <el-descriptions v-if="!isEditing" :column="1" border>
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{
          userInfo.nickname || '未设置'
        }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userInfo.email || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userInfo.phone || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag type="warning">应用作者</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ userInfo.userId || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 编辑模式：使用表单编辑 -->
      <el-form
        v-else
        ref="formRef"
        :model="editForm"
        :rules="rules"
        label-width="100px"
        style="max-width: 500px"
      >
        <el-form-item label="头像">
          <el-upload
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            accept="image/*"
            class="avatar-uploader"
          >
            <el-avatar :size="60">
              <img v-if="editForm.avatar" :src="editForm.avatar" />
              <el-icon v-else><Plus /></el-icon>
            </el-avatar>
          </el-upload>
          <span class="upload-tip">点击上传头像（支持 JPG/PNG）</span>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="editForm.nickname"
            placeholder="请输入昵称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 第三部分：修改密码（功能增强） -->
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <h3>修改密码</h3>
          <el-button type="warning" @click="passwordVisible = true">
            <el-icon><Lock /></el-icon> 修改密码
          </el-button>
        </div>
      </template>
      <el-alert type="info" :closable="false" show-icon title="定期修改密码可以提高账户安全性" />
    </el-card>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordVisible" title="修改密码" width="450px" @closed="resetPasswordForm">
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码（至少6位）"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSaving" @click="submitPassword"
          >确认修改</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 作者中心 - 个人资料页面
 * 功能：
 * 1. 展示当前登录作者的个人信息
 * 2. 支持编辑昵称、邮箱、手机号、头像
 * 3. 支持修改登录密码（功能增强）
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthorStore } from '@/store/author'
import { getUserInfo, updateProfile, changePassword } from '@/api/auth'
import { uploadIcon } from '@/api/file'

// 使用 Pinia 状态管理
const store = useAuthorStore()

const infoLoading = ref(false) // 信息加载状态
const isEditing = ref(false) // 是否处于编辑模式
const saving = ref(false) // 保存中状态
const formRef = ref() // 表单引用，用于校验
const userInfo = ref({
  // 用户信息（从 store 初始化，页面加载时从后端刷新）
  ...(store.userInfo || {}),
})

// 编辑表单数据
const editForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
})

// 表单校验规则
const rules = {
  nickname: [
    { required: true, message: '昵称不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
}

const passwordVisible = ref(false)
const passwordSaving = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

/**
 * 页面加载时从后端获取最新用户信息
 * 因为 store 中存的是登录时的信息，可能不是最新的
 */
const loadUserInfo = async () => {
  infoLoading.value = true
  try {
    const res = await getUserInfo()
    userInfo.value = res.data || {}
    // 同步更新 store 和 localStorage，保持全局状态一致
    store.userInfo = res.data
    localStorage.setItem('author_info', JSON.stringify(res.data))
  } catch (e) {
    // 接口失败时使用 store 中的缓存信息
    userInfo.value = { ...(store.userInfo || {}) }
  } finally {
    infoLoading.value = false
  }
}

/**
 * 进入编辑模式：将当前用户信息复制到编辑表单
 */
const startEdit = () => {
  editForm.nickname = userInfo.value.nickname || ''
  editForm.email = userInfo.value.email || ''
  editForm.phone = userInfo.value.phone || ''
  editForm.avatar = userInfo.value.avatar || ''
  isEditing.value = true
}

/**
 * 取消编辑：重置表单，回到查看模式
 */
const cancelEdit = () => {
  isEditing.value = false
}

/**
 * 头像上传前的处理：调用后端上传接口，返回图片 URL
 * 返回 false 阻止 el-upload 默认上传行为，改用我们自己的接口
 */
const beforeAvatarUpload = async (file) => {
  // 校验文件大小（不超过 2MB）
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('头像大小不能超过 2MB')
    return false
  }
  try {
    const res = await uploadIcon(file)
    editForm.avatar = res.data
    ElMessage.success('头像上传成功')
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
  return false
}

/**
 * 保存个人资料：调用后端更新接口，成功后刷新本地信息
 */
const saveProfile = async () => {
  // 先校验表单
  await formRef.value.validate()
  saving.value = true
  try {
    const res = await updateProfile({
      nickname: editForm.nickname,
      email: editForm.email,
      phone: editForm.phone,
      avatar: editForm.avatar,
    })
    // 更新成功后刷新本地用户信息
    userInfo.value = res.data || userInfo.value
    // 同步更新 store 和 localStorage
    store.userInfo = res.data
    localStorage.setItem('author_info', JSON.stringify(res.data))
    ElMessage.success('资料修改成功')
    isEditing.value = false
  } catch (e) {
    // 错误信息已在 request 拦截器中统一提示
  } finally {
    saving.value = false
  }
}

/**
 * 重置密码表单
 */
const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

/**
 * 提交修改密码
 * 调用后端 /auth/password 接口，校验原密码后更新为新密码
 */
const submitPassword = async () => {
  await passwordFormRef.value.validate()
  passwordSaving.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordVisible.value = false
    // 修改密码后强制退出登录，重新验证新密码
    store.logout()
    location.href = '/login'
  } catch (e) {
    // 错误信息已在 request 拦截器中统一提示
  } finally {
    passwordSaving.value = false
  }
}

onMounted(loadUserInfo)
</script>

<style scoped>
/* 页面整体样式 */
.profile-page {
  max-width: 800px;
}
.info-card {
  margin-bottom: 20px;
}
/* 头像和信息横向排列 */
.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
}
.avatar {
  border: 3px solid #409eff;
  flex-shrink: 0;
}
.profile-info .nickname {
  margin: 0 0 8px 0;
  font-size: 24px;
}
.profile-info .username {
  margin: 0 0 8px 0;
  color: #909399;
}
.detail-card {
  margin-bottom: 20px;
}
.password-card {
  margin-bottom: 20px;
}
/* 卡片头部：标题和按钮左右分布 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header h3 {
  margin: 0;
}
/* 头像上传区域 */
.avatar-uploader {
  display: inline-block;
  vertical-align: middle;
}
.upload-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}
</style>
