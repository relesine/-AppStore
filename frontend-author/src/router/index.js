import { createRouter, createWebHistory } from 'vue-router'
import { useAuthorStore } from '@/store/author'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'apps', name: 'Apps', component: () => import('@/views/AppList.vue') },
      { path: 'app/add', name: 'AppAdd', component: () => import('@/views/AppEdit.vue') },
      { path: 'app/edit/:id', name: 'AppEdit', component: () => import('@/views/AppEdit.vue') },
      {
        path: 'version/:appId',
        name: 'VersionManage',
        component: () => import('@/views/VersionManage.vue'),
      },
      { path: 'notices', name: 'Notices', component: () => import('@/views/Notices.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue') },
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const store = useAuthorStore()
  if (to.path !== '/login' && !store.isLogin) next('/login')
  else next()
})

export default router
