import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/code-review',
    children: [
      {
        path: '/code-review',
        name: 'CodeReview',
        component: () => import('@/views/code-review/Index.vue')
      },
      {
        path: '/chat',
        name: 'Chat',
        component: () => import('@/views/chat/Index.vue')
      },
      {
        path: '/approval',
        name: 'Approval',
        component: () => import('@/views/approval/Index.vue')
      },
      {
        path: '/knowledge',
        name: 'Knowledge',
        component: () => import('@/views/knowledge/Index.vue')
      },
      {
        path: '/prompt',
        name: 'Prompt',
        component: () => import('@/views/prompt/Index.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
