<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="brand">
        <span class="brand-mark"><el-icon :size="16" color="#fff"><ShoppingCart /></el-icon></span>
      </div>
      <h2 class="title">欢迎回来</h2>
      <p class="subtitle">登录后可下载应用、收藏与发表评价</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
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
      <div class="footer-text">
        还没有账号？<el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
      </div>
    </el-card>
    <p class="copyright">轻量应用商店 · 让每一款好应用都被发现</p>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    router.push('/home')
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
.footer-text {
  text-align: center;
  margin-top: 12px;
  font-size: 13px;
  color: var(--app-text-regular, #606266);
}
</style>
