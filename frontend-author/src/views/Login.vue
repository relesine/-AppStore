<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="brand">
        <span class="brand-mark"><el-icon :size="16" color="#fff"><Box /></el-icon></span>
      </div>
      <h2 class="title">作者中心登录</h2>
      <p class="subtitle">登录后可上传应用、管理版本与查看数据</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="作者账号"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
            >登录</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>
    <p class="copyright">轻量应用商店 · 作者中心</p>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useAuthorStore } from '@/store/author'

const router = useRouter()
const store = useAuthorStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    if (res.data.userType !== 2 && res.data.userType !== 3) {
      ElMessage.error('该账号不是作者账号')
      return
    }
    store.setLogin(res.data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-bg-page, #f5f7fa);
}
.login-card {
  width: 400px;
  padding: 36px 36px 28px;
}
.brand {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}
.brand-mark {
  width: 40px;
  height: 40px;
  border-radius: 9px;
  background: var(--app-primary, #2e6be6);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(46, 107, 230, 0.22);
}
.title {
  text-align: center;
  margin-bottom: 6px;
  color: var(--app-text-main, #1d2633);
  font-size: 18px;
  font-weight: 600;
}
.subtitle {
  text-align: center;
  font-size: 13px;
  color: var(--app-text-secondary, #909399);
  margin-bottom: 24px;
}
.copyright {
  position: fixed;
  bottom: 18px;
  left: 0;
  right: 0;
  text-align: center;
  color: #8a919c;
  font-size: 12px;
}
</style>
