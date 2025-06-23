import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Manager',
      component: () => import('@/views/Manager.vue'),
      redirect: '/home',
      children: [
        { path: 'home', name: 'Home', component: () => import('@/views/manager/Home.vue')},
        { path: 'user', name: 'User', component: () => import('@/views/manager/User.vue')},
      ]
    },
    // {
    //   path: '/',
    //   name: 'User',
    //   component: () => import('@/views/User.vue'),
    //   redirect: '/home',
    //   children: [
    //     { path: 'home', name: 'Home', component: () => import('@/views/manager/Home.vue')},
    //     { path: 'user', name: 'User', component: () => import('@/views/manager/User.vue')},
    //   ]
    // },
    { path: '/login', name: 'Login', component: () => import('@/views/Login.vue')},
    { path: '/register', name: 'Register', component: () => import('@/views/Register.vue')},
  ]
})

export default router
