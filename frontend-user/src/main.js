import { createApp } from 'vue'
import { createPinia } from 'pinia'
// Element Plus 样式统一引入（组件已按需自动注册）
import 'element-plus/dist/index.css'
import {
  Box,
  Download,
  Search,
  ShoppingCart,
  Star,
  StarFilled,
  SwitchButton,
} from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './assets/main.css'

const app = createApp(App)

// 仅全局注册实际使用的图标，避免打包全部图标导致体积过大
const icons = { Box, Download, Search, ShoppingCart, Star, StarFilled, SwitchButton }
for (const [key, component] of Object.entries(icons)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.mount('#app')
