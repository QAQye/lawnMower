import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes : [
    { path: '/', redirect: '/main' },   // ✅ 加这个：解决 "/" 无匹配
    { path: '/login', component: () => import('../view/login/Login.vue') },
    { path: '/register', component: () => import('../view/register/Register.vue') },
    { path: '/main',name: 'Main',component: () => import('../view/main/Main.vue'),
      //给需要保护的页面打上标记,最开始的时候重定向到自己的子页面main中
      meta: { requiresAuth: true } ,redirect: '/main/index',
      // 这里写他的子路由的信息
      children: [
        {
          // 注意：子路由的 path 前面不要加斜杠 "/"
          path: 'index', 
          name: 'MainIndex',
          component: () => import('../view/main/MainIndex.vue'),
          // 子路由通常也继承父级的权限，但最好显式声明一下或者在守卫里做父级检查
          meta: { requiresAuth: true ,title:"首页"},
          
        },
        {
          // 注意：子路由的 path 前面不要加斜杠 "/"
          path: 'remote', 
          name: 'remotelawn',
          component: () => import('../view/main/Remote.vue'),
          // 子路由通常也继承父级的权限，但最好显式声明一下或者在守卫里做父级检查
          meta: { requiresAuth: true,title:"远程驾驶" } 
        },
        {
          // 注意：子路由的 path 前面不要加斜杠 "/"
          path: 'ai', 
          name: 'ai-chat',
          component: () => import('../view/main/Ai.vue'),
          // 子路由通常也继承父级的权限，但最好显式声明一下或者在守卫里做父级检查
          meta: { requiresAuth: true,title:"AI问答" } 
        },
        {
          // 注意：子路由的 path 前面不要加斜杠 "/"
          path: 'auto', 
          name: 'autolawn',
          component: () => import('../view/main/Auto.vue'),
          // 子路由通常也继承父级的权限，但最好显式声明一下或者在守卫里做父级检查
          meta: { requiresAuth: true,title:"自动除草"  } 
        },
        {
          // 注意：子路由的 path 前面不要加斜杠 "/"
          path: 'mowerlist', 
          name: 'mowerlist',
          component: () => import('../view/main/MowerList.vue'),
          // 子路由通常也继承父级的权限，但最好显式声明一下或者在守卫里做父级检查
          meta: { requiresAuth: true,title:"除草机列表"  } 
        },
        {
          // 注意：子路由的 path 前面不要加斜杠 "/"
          path: 'tasklist', 
          name: 'tasklist',
          component: () => import('../view/main/TaskList.vue'),
          // 子路由通常也继承父级的权限，但最好显式声明一下或者在守卫里做父级检查
          meta: { requiresAuth: true,title:"除草机列表"  } 
        },
        {
          // 注意：子路由的 path 前面不要加斜杠 "/"
          path: 'detection', 
          name: 'detection',
          component: () => import('../view/main/Detection.vue'),
          // 子路由通常也继承父级的权限，但最好显式声明一下或者在守卫里做父级检查
          meta: { requiresAuth: true,title:"功能介绍-目标检测"  } 
        },

      ] 
    }
    ],
   
})
// 添加“路由守卫”
// to: 要去哪里, from: 从哪里来, next: 放行函数
router.beforeEach((to, from, next) => {
  // 1. 获取本地存储的 Token（刚才在 Login.vue 里存进去的那个）
  const token = localStorage.getItem('token')

  // 2. 判断：如果要去的页面是“需要登录”的（meta.requiresAuth 为 true）
  if (to.meta.requiresAuth) {
    // 3. 再判断：如果没有 Token
    if (!token) {
      // ➡️ 强制通过 next 跳转回登录页
      next('/login')
    } else {
      // ➡️ 有 Token，放行
      next()
    }
  } else {
    // 4. 如果去的页面不需要登录（比如 login 或 register），直接放行
    next()
  }
})

export default router
