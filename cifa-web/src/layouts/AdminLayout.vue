<script setup lang="ts">
import { useRouter, useRoute, RouterView } from 'vue-router'
import Sidebar from '@/components/Sidebar.vue'
import Toaster from '@/components/ui/toast/Toaster.vue'

const router = useRouter()
const route = useRoute()

const handleLogout = () => {
  localStorage.removeItem('cifa_auth_token')
  router.push('/login')
}
</script>

<template>
  <div class="flex min-h-screen bg-slate-50 font-nunito overflow-hidden">
    
    <Sidebar @logout="handleLogout" />

    <main class="flex-1 ml-64 p-6 flex flex-col h-screen overflow-hidden">
      
      <header 
        v-if="route.path === '/admin/dashboard'" 
        class="flex justify-between items-start mb-4"
      >
        <div class="animate-in fade-in slide-in-from-left duration-500">
          <h2 class="text-2xl font-bold text-slate-900 tracking-tight leading-none mb-1.5">Olá, Vinicius!</h2>
          <p class="text-slate-500 font-bold text-[10px] uppercase tracking-[0.2em]">14 de abril de 2026</p>
        </div>
      </header>
      
      <section class="flex-1 min-h-0 flex flex-col relative">
        <RouterView v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </RouterView>
      </section>
      
    </main>

    <Toaster />
  </div>
</template>

<style>
/* Importação da fonte Nunito via Google Fonts caso não esteja no CSS global */
@import url('https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700;800&display=swap');

.font-nunito {
  font-family: 'Nunito', sans-serif;
}

html, body {
  margin: 0;
  padding: 0;
  overflow: hidden;
  height: 100%;
  -webkit-font-smoothing: antialiased;
}

/* Transição suave entre telas */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Custom Scrollbar Global para manter estética Liquid */
::-webkit-scrollbar {
  width: 5px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}
</style>