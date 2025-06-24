import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/Manager.vue'),
      redirect: '/admin/home/',
      children: [
        { path: 'home', name: 'Home', component: () => import('@/views/manager/Home.vue')},
        { path: 'user', name: 'User', component: () => import('@/views/manager/UserInfo.vue')},
        { path: 'singer', name: 'singer', component: () => import('@/views/manager/SingerInfo.vue')},
        { path: 'admin', name: 'admin', component: () => import('@/views/manager/AdminInfo.vue')},
      ]
    },
    // {
    //   path: '/user',
    //   name: 'user',
    //   component: () => import('@/views/UserInfo.vue'),
    //   redirect: '/user/home',
    //   children: [
    //     { path: 'home', name: 'Home', component: () => import('@/views/user/Home.vue')},
    //   ]
    // },
    { path: '/login', name: 'Login', component: () => import('@/views/Login.vue')},
    { path: '/register', name: 'Register', component: () => import('@/views/Register.vue')},
  ]
})

export default router
