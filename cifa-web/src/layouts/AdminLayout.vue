<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Button } from '@/components/ui/button'
import { LayoutDashboard, Users, UserPlus, FileText, Settings, LogOut } from 'lucide-vue-next'

const router = useRouter()
const activeMenu = ref('Dashboard')

const handleLogout = () => {
  // Limpa o token simulado
  localStorage.removeItem('cifa_auth_token')
  // Força o redirecionamento para o login
  router.push('/login')
}
</script>

<template>
  <div class="flex min-h-screen bg-slate-50 font-sans">
    
    <aside class="w-64 bg-[#0A102E] text-white flex flex-col justify-between fixed h-full z-10">
      <div class="p-6 flex flex-col h-full">
        <div class="flex items-center gap-3 mb-10">
          <h1 class="text-3xl font-bold tracking-wider">CIFA</h1>
          <div class="text-[0.6rem] leading-tight text-slate-300 font-medium">
            <p>Controle Inteligente</p>
            <p>de Fluxo Acadêmico</p>
          </div>
        </div>

        <nav class="flex flex-col gap-2">
          <button @click="activeMenu = 'Dashboard'; router.push('/admin/dashboard')" :class="['flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors', activeMenu === 'Dashboard' ? 'bg-white text-[#0A102E]' : 'text-slate-300 hover:bg-white/10 hover:text-white']"><LayoutDashboard class="w-5 h-5" /> Dashboard</button>
          <button @click="activeMenu = 'Estudantes'" :class="['flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors', activeMenu === 'Estudantes' ? 'bg-white text-[#0A102E]' : 'text-slate-300 hover:bg-white/10 hover:text-white']"><Users class="w-5 h-5" /> Estudantes</button>
          <button @click="activeMenu = 'Cadastro'" :class="['flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors', activeMenu === 'Cadastro' ? 'bg-white text-[#0A102E]' : 'text-slate-300 hover:bg-white/10 hover:text-white']"><UserPlus class="w-5 h-5" /> Cadastro</button>
          <button @click="activeMenu = 'Relatórios'" :class="['flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors', activeMenu === 'Relatórios' ? 'bg-white text-[#0A102E]' : 'text-slate-300 hover:bg-white/10 hover:text-white']"><FileText class="w-5 h-5" /> Relatórios</button>
        </nav>

        <div class="w-full h-px bg-slate-700 my-6"></div>

        <button @click="activeMenu = 'Administração'" :class="['flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors', activeMenu === 'Administração' ? 'bg-white text-[#0A102E]' : 'text-slate-300 hover:bg-white/10 hover:text-white']"><Settings class="w-5 h-5" /> Administração</button>
      </div>

      <div class="p-6 pt-0 mt-auto">
        <button @click="handleLogout" class="flex items-center gap-3 px-4 py-3 rounded-lg font-medium text-slate-300 hover:bg-white/10 hover:text-white w-full transition-colors"><LogOut class="w-5 h-5" /> Sair</button>
      </div>
    </aside>

    <main class="flex-1 ml-64 p-10 flex flex-col">
      <header class="flex justify-between items-start mb-8">
        <div>
          <h2 class="text-4xl font-bold text-slate-900 mb-1">Olá, Vinicius!</h2>
          <p class="text-slate-500 font-medium text-lg">23 de março de 2026</p>
        </div>
        <Button @click="handleLogout" variant="default" class="bg-[#1A2342] hover:bg-[#0A102E] text-white rounded-full px-8 py-5 shadow-sm">
          Sair
        </Button>
      </header>
      
      <slot />
      
      <footer class="mt-8 text-center text-sm text-slate-400 font-medium">CIFA &copy; 2026</footer>
    </main>
  </div>
</template>