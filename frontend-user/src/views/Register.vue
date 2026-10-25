<template>
  <div class="register-page">
    <el-card class="register-card">
      <h2 class="title">用户注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名（字母开头，3-20位）"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码（6-20位）"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="昵称（选填）"
            size="large"
            :prefix-icon="UserFilled"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="邮箱（选填）"
            size="large"
            :prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="手机号（选填）"
            size="large"
            :prefix-icon="Iphone"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleRegister"
            >注册</el-button
          >
        </el-form-item>
      </el-form>
      <div class="footer-text">
        已有账号？<el-link type="primary" @click="$router.push('/login')">去登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, UserFilled, Message, Iphone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: '',
  userType: 1,
})

// 校验规则：与服务端保持一致
const validateUsername = (rule, value, callback) => {
  if (!/^[a-zA-Z][a-zA-Z0-9_]{2,19}$/.test(value)) {
    callback(new Error('用户名需以字母开头，3-20位，仅限字母数字下划线'))
  } else callback()
}
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6-20位之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
}

const handleRegister = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const { confirmPassword, ...submitData } = form
    await register(submitData)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-bg-page, #f5f7fa);
}
.register-card {
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
