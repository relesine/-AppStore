import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'category', name: 'Category', component: () => import('@/views/Category.vue') },
      { path: 'app/:id', name: 'AppDetail', component: () => import('@/views/AppDetail.vue') },
      { path: 'search', name: 'Search', component: () => import('@/views/Search.vue') },
      {
        path: 'downloads',
        name: 'MyDownloads',
        component: () => import('@/views/MyDownloads.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'favorites',
        name: 'MyFavorites',
        component: () => import('@/views/MyFavorites.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { requiresAuth: true },
      },
    ],
  },
  // 404兜底路由
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 全局前置守卫：登录态校验 + 页面标题
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  document.title = to.meta.title ? `${to.meta.title} - 轻量应用商店` : '轻量应用商店'
  if (to.meta.requiresAuth && !userStore.isLogin) {
    next('/login')
  } else {
    next()
  }
})

export default router
