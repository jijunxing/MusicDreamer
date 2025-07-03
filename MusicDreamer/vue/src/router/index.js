import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' }, // 添加根路径重定向
    { path: '/login', name: 'Login', component: () => import('@/views/Login.vue')},
    { path: '/register', name: 'Register', component: () => import('@/views/Register.vue')},
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/Manager.vue'),
      redirect: '/admin/home/',
      children: [
        { path: 'home', name: 'ManagerHome', component: () => import('@/views/manager/ManagerHome.vue')},
        { path: 'user', name: 'UserInfo', component: () => import('@/views/manager/UserInfo.vue')},
        { path: 'singer', name: 'SingerInfo', component: () => import('@/views/manager/SingerInfo.vue')},
        { path: 'admin', name: 'AdminInfo', component: () => import('@/views/manager/AdminInfo.vue')},
        { path: 'music', name: 'MusicInfo', component: () => import('@/views/manager/MusicInfo.vue'),},
        { path: 'logs', name: 'LogInfo', component: () => import('@/views/manager/LogInfo.vue')},
        { path: 'notice', name: 'NoticeInfo', component: () => import('@/views/manager/NoticeInfo.vue')},
        { path: 'songlist', name: 'SongListInfo', component: () => import('@/views/manager/SongListInfo.vue')},
      ]
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('@/views/User.vue'),
      children: [
        { path: 'home', name: 'Home', component: () => import('@/views/user/ManagerHome.vue')},
        { path: 'personal', name: 'PersonalInfo', component: () => import('@/views/user/personalInfo.vue')},
      ]
    },
    {
      path: '/lyrics',
      name: 'LyricsPage',
      component: () => import('@/components/LyricsPage.vue'),
      props: route => ({ songId: route.query.id })
    }
  ]
})

export default router
