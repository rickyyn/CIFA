import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import Dashboard from '@/views/Dashboard.vue'
import Cadastro from '@/views/Cadastro.vue'
import Administracao from '@/views/Administracao.vue'
import Relatorios from '@/views/Relatorios.vue'
import Contato from '@/views/Contato.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard
      },
      {
        path: 'cadastro',
        name: 'Cadastro',
        component: Cadastro
      },
      {
        path: 'administracao',
        name: 'Administracao',
        component: Administracao
      },
      {
        path: 'relatorios',
        name: 'Relatorios',
        component: Relatorios
      },
      {
        path: 'contato',
        name: 'Contato',
        component: Contato
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('cifa_auth_token') === 'true'
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router