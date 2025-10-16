import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/user/NewsList.vue'),
    meta: { requiresAuth: false, title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false, title: '注册' }
  },
  {
    path: '/news/:id',
    name: 'NewsDetail',
    component: () => import('@/views/user/NewsDetail.vue'),
    meta: { requiresAuth: false, title: '新闻详情' }
  },
  {
    path: '/user/center',
    name: 'UserCenter',
    component: () => import('@/views/user/UserCenter.vue'),
    meta: { requiresAuth: true, title: '个人中心' }
  },
  {
    path: '/category/:id',
    name: 'CategoryNews',
    component: () => import('@/views/user/CategoryNews.vue'),
    meta: { requiresAuth: false, title: '分类新闻' }
  },
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理后台' }
      },
      {
        path: 'news',
        name: 'NewsManagement',
        component: () => import('@/views/admin/NewsManagement.vue'),
        meta: { title: '新闻管理' }
      },
      {
        path: 'news/create',
        name: 'NewsCreate',
        component: () => import('@/views/admin/NewsEdit.vue'),
        meta: { title: '创建新闻' }
      },
      {
        path: 'news/edit/:id',
        name: 'NewsEdit',
        component: () => import('@/views/admin/NewsEdit.vue'),
        meta: { title: '编辑新闻' }
      },
      {
        path: 'categories',
        name: 'CategoryManage',
        component: () => import('@/views/admin/CategoryManage.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'crawler',
        name: 'CrawlerManage',
        component: () => import('@/views/admin/CrawlerManage.vue'),
        meta: { title: '采集管理' }
      },
      {
        path: 'rules',
        name: 'ClassificationRules',
        component: () => import('@/views/admin/ClassificationRules.vue'),
        meta: { title: '分类规则' }
      },
      {
        path: 'audit-log',
        name: 'AuditLog',
        component: () => import('@/views/admin/AuditLog.vue'),
        meta: { title: '审计日志' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 新闻管理系统`;
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('news_management_token');
    
    if (!token) {
      // 未登录，跳转到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
      return;
    }
    
    // TODO: 检查token有效性和用户角色
    // 这里可以调用API验证token或从store获取用户信息
    
    if (to.meta.requiresAdmin) {
      // TODO: 检查是否为管理员
      // 暂时假设token存在就是管理员
      next();
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router;

