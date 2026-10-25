import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/store/admin'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'users', name: 'Users', component: () => import('@/views/UserManage.vue') },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/CategoryManage.vue'),
      },
      { path: 'audit', name: 'Audit', component: () => import('@/views/AppAudit.vue') },
      { path: 'versions', name: 'Versions', component: () => import('@/views/VersionManage.vue') },
      { path: 'notices', name: 'Notices', component: () => import('@/views/NoticeManage.vue') },
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const store = useAdminStore()
  if (to.path !== '/login' && !store.isLogin) next('/login')
  else next()
})

export default router
