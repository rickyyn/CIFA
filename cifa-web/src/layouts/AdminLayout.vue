<script setup lang="ts">
<<<<<<< HEAD
import { ref, onMounted } from 'vue'
=======
import { ref } from 'vue'
>>>>>>> e6819bfbf6a12330714de153f143f8b90643f011
import { useRouter, useRoute, RouterView } from 'vue-router'
import Sidebar from '@/components/Sidebar.vue'
import Toaster from '@/components/ui/toast/Toaster.vue'
import { Menu } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'

const router = useRouter()
const route = useRoute()

// Controlo do menu mobile (abrir/fechar)
const isMobileMenuOpen = ref(false)

<<<<<<< HEAD
// Controlo da Sidebar minimizada (Desktop)
const isSidebarCollapsed = ref(false)

// Nome Personalizado do Admin
const adminName = ref('Administrador')

onMounted(() => {
  const storedName = localStorage.getItem('cifa_admin_name')
  if (storedName) {
    adminName.value = storedName
  }
})

=======
// NOVO: Controlo da Sidebar minimizada (Desktop)
const isSidebarCollapsed = ref(false)

>>>>>>> e6819bfbf6a12330714de153f143f8b90643f011
const handleLogout = () => {
  localStorage.removeItem('cifa_auth_token')
  router.push('/login')
}

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
}

const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}
</script>

<template>
  <div class="flex min-h-[100dvh] bg-slate-50 font-poppins overflow-hidden relative text-slate-900">
    
    <div 
      v-if="isMobileMenuOpen" 
      @click="closeMobileMenu"
      class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-40 lg:hidden transition-opacity duration-300"
    ></div>

    <Sidebar 
      :is-open="isMobileMenuOpen" 
      :is-collapsed="isSidebarCollapsed"
      @close="closeMobileMenu" 
      @logout="handleLogout" 
      @toggle-collapse="toggleSidebar"
    />

    <main 
      class="flex-1 flex flex-col h-[100dvh] overflow-hidden w-full transition-all duration-300 bg-slate-50"
      :class="[
        isSidebarCollapsed ? 'lg:ml-20' : 'lg:ml-64'
      ]"
    >
      
      <div class="lg:hidden flex items-center justify-between p-4 bg-white border-b border-slate-200 shrink-0 shadow-sm z-30">
        <div class="flex items-center gap-3 select-none">
          <h1 class="text-3xl font-bold tracking-wider text-[#0A102E]">CIFA</h1>
          <div class="text-[0.6rem] leading-tight text-slate-500 font-medium uppercase tracking-tighter">
            <p>Controle Inteligente</p>
            <p>de Fluxo Acadêmico</p>
          </div>
        </div>
        <Button variant="ghost" size="icon" @click="isMobileMenuOpen = true" class="rounded-xl bg-slate-50 hover:bg-slate-100">
          <Menu class="w-6 h-6 text-[#0A102E]" />
        </Button>
      </div>

      <section class="flex-1 flex flex-col relative p-4 md:p-6 overflow-y-auto custom-scrollbar">
        
        <header 
          v-if="route.path === '/admin/dashboard'" 
          class="flex justify-between items-start mb-4 shrink-0"
        >
          <div v-auto-animate>
<<<<<<< HEAD
            <h2 class="text-xl md:text-2xl font-bold text-slate-900 tracking-tight leading-none mb-1.5">
              Olá, {{ adminName }}!
            </h2>
=======
            <h2 class="text-xl md:text-2xl font-bold text-slate-900 tracking-tight leading-none mb-1.5">Olá, Vinícius!</h2>
>>>>>>> e6819bfbf6a12330714de153f143f8b90643f011
            <p class="text-slate-500 font-bold text-[10px] uppercase tracking-[0.2em]">Visão Geral Diária</p>
          </div>
        </header>

        <RouterView />
      </section>
      
    </main>

    <Toaster />
  </div>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;800;900&display=swap');

.font-poppins { font-family: 'Poppins', sans-serif; }
html, body { margin: 0; padding: 0; overflow: hidden; height: 100%; -webkit-font-smoothing: antialiased; }

.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 20px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background-color: #94a3b8; }
</style>