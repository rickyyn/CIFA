import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import Dashboard from '@/views/Dashboard.vue'
import Cadastro from '@/views/Cadastro.vue'
import Estudantes from '@/views/Estudantes.vue'
import Relatorios from '@/views/Relatorios.vue'
import Administracao from '@/views/Administracao.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: Dashboard
        },
        {
          path: 'cadastro',
          name: 'cadastro',
          component: Cadastro
        },
        {
          path: 'estudantes',
          name: 'estudantes',
          component: Estudantes 
        },
        {
          path: 'relatorios',
          name: 'relatorios',
          component: Relatorios 
        },
        {
          path: 'administracao',
          name: 'administracao',
          component: Administracao
        }
      ]
    }
  ]
})


router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('cifa_auth_token') === 'true'

  if (to.path.startsWith('/admin') && !isAuthenticated) {
    next('/login')
  } 
  else if (to.path === '/login' && isAuthenticated) {
    next('/admin/dashboard')
  } 
  else {
    next()
  }
})

export default router