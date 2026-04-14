import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import Dashboard from '@/views/Dashboard.vue'

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
        }
      ]
    }
  ]
})

// Proteção Global de Rotas (Navigation Guard)
router.beforeEach((to, from, next) => {
  // Simulando a verificação de um Token JWT no LocalStorage
  const isAuthenticated = localStorage.getItem('cifa_auth_token') === 'true'

  // Se tentar acessar o painel administrativo sem estar logado
  if (to.path.startsWith('/admin') && !isAuthenticated) {
    next('/login')
  } 
  // Se tentar acessar o login já estando logado
  else if (to.path === '/login' && isAuthenticated) {
    next('/admin/dashboard')
  } 
  // Caso contrário, permite a navegação normal
  else {
    next()
  }
})

export default router