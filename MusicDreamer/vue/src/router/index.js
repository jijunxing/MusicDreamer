import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' }, // 添加根路径重定向
    { path: '/login', name: 'Login', component: () => import('@/views/Login.vue')},
    { path: '/register', name: 'Register', component: () => import('@/views/Register.vue')},
    { path: '/lyrics', name: 'LyricsPage', component: () => import('@/views/LyricsPage.vue')},
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
        { path: 'carousel', name: 'carouselInfo', component: () => import('@/views/manager/CarouselInfo.vue')},
        { path: 'notice', name: 'NoticeInfo', component: () => import('@/views/manager/NoticeInfo.vue')},
        { path: 'songlist', name: 'SongListInfo', component: () => import('@/views/manager/SongListInfo.vue')},
      ]
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('@/views/User.vue'),
      redirect: 'user/home',
      children: [
        { path: 'home', name: 'Home', component: () => import('@/views/user/UserHome.vue')},
        { path: 'singers', name: 'Singers', component: () => import('@/views/user/Singers.vue')},
        {
          path: 'singers/:id',
          name: 'SingerDetail',
          component: ()=>import('@/views/user/SingerDetail.vue'),
          props: (route) => ({
            singerId: parseInt(route.params.id), // 参数重命名+类型转换
            visible: true // 可传递其他固定参数
          })
        },
        { path: 'songs', name: 'Songs', component: () => import('@/views/user/Songs.vue')},
        { path: 'songLists', name: 'SongLists', component: () => import('@/views/user/SongLists.vue')},
        {
          path: 'songLists/:id',
          name: 'SongListDetail',
          component: ()=>import('@/views/user/SongListDetail.vue'),
          props: (route) => ({
            singerId: parseInt(route.params.id),
            visible: true
          })
        },
        { path: 'creatorCenter', name: 'CreatorCenter', component: () => import('@/views/user/CreatorCenter.vue')},
        { path: 'userCenter', name: 'UserCenter', component: () => import('@/views/user/UserCenter.vue')},
        { path: 'favorites', name: 'Favorites', component: () => import('@/views/user/Favorites.vue')},
        {
          path: '/search', name: 'SearchPage', component: () =>import('@/views/user/SearchPage.vue'),
          props: (route) => ({ keyword: route.query.keyword || '' }) // 自动解析 keyword 参数
        },
        {
          path: '/user/notices',
          name: 'NoticeList',
          component: () => import('@/views/user/Notices.vue'),
          meta: { title: '系统公告' }
        }
      ]
    },
  ]
})

export default router
