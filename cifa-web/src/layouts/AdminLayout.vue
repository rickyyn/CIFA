<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute, RouterView } from 'vue-router'
import Sidebar from '@/components/Sidebar.vue'
import Toaster from '@/components/ui/toast/Toaster.vue'
import { Menu } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'

const router = useRouter()
const route = useRoute()

// Controle do menu mobile
const isMobileMenuOpen = ref(false)

const handleLogout = () => {
  localStorage.removeItem('cifa_auth_token')
  router.push('/login')
}

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
}
</script>

<template>
  <div class="flex min-h-screen bg-slate-50 font-poppins overflow-hidden relative">
    
    <div 
      v-if="isMobileMenuOpen" 
      @click="closeMobileMenu"
      class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-40 lg:hidden transition-opacity duration-300"
    ></div>

    <Sidebar 
      :is-open="isMobileMenuOpen" 
      @close="closeMobileMenu" 
      @logout="handleLogout" 
    />

    <main class="flex-1 flex flex-col h-screen overflow-hidden w-full lg:ml-64 transition-all duration-300 bg-slate-50">
      
      <div class="lg:hidden flex items-center justify-between p-4 bg-white border-b border-slate-200 shrink-0 shadow-sm z-30">
        <div class="flex items-center gap-3 select-none">
          <h1 class="text-3xl font-bold tracking-wider text-[#0A102E]">CIFA</h1>
          <div class="text-[0.6rem] leading-tight text-slate-500 font-medium uppercase tracking-tighter">
            <p>Controle Inteligente</p>
            <p>de Fluxo Acadêmico</p>
          </div>
        </div>
        <Button 
          variant="ghost" 
          size="icon" 
          @click="isMobileMenuOpen = true" 
          class="rounded-xl bg-slate-50 hover:bg-slate-100 transition-colors"
        >
          <Menu class="w-6 h-6 text-[#0A102E]" />
        </Button>
      </div>

      <section 
        class="flex-1 min-h-0 flex flex-col relative p-4 md:p-6 overflow-y-auto custom-scrollbar"
      >
        
        <header 
          v-if="route.path === '/admin/dashboard'" 
          class="flex justify-between items-start mb-4 shrink-0"
        >
          <div>
            <h2 class="text-xl md:text-2xl font-bold text-slate-900 tracking-tight leading-none mb-1.5">Olá, Vinícius!</h2>
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

.font-poppins { 
  font-family: 'Poppins', sans-serif; 
}

html, body { 
  margin: 0; 
  padding: 0; 
  /* Mantém o body fixo para evitar o 'bounce' indesejado no mobile */
  overflow: hidden; 
  height: 100%; 
  -webkit-font-smoothing: antialiased; 
}

/* Scrollbar Líquida personalizada para o container de conteúdo */
.custom-scrollbar::-webkit-scrollbar { 
  width: 6px; 
}

.custom-scrollbar::-webkit-scrollbar-track { 
  background: transparent; 
}

.custom-scrollbar::-webkit-scrollbar-thumb { 
  background-color: #cbd5e1; 
  border-radius: 20px; 
}

.custom-scrollbar:hover::-webkit-scrollbar-thumb { 
  background-color: #94a3b8; 
}

button, input, select { 
  font-family: inherit; 
}
</style>