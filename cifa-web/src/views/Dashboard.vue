<script setup lang="ts">
import { ref } from 'vue'
import { Button } from '@/components/ui/button'
import {
  LayoutDashboard,
  Users,
  UserPlus,
  FileText,
  Settings,
  LogOut,
  UserCheck,
  UserX,
  User
} from 'lucide-vue-next'

// Dados simulados para o gráfico de barras
const barChartData = [
  { day: 'Segunda', value: 25 },
  { day: 'Terça', value: 45 },
  { day: 'Quarta', value: 27 },
  { day: 'Quinta', value: 38 },
  { day: 'Sexta', value: 45 },
  { day: 'Sábado', value: 12 },
  { day: 'Domingo', value: 10 },
]

// Encontra o valor máximo para calcular a altura percentual das barras
const maxBarValue = Math.max(...barChartData.map(d => d.value))

// Navegação simulada
const activeMenu = ref('Dashboard')
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
          <button 
            @click="activeMenu = 'Dashboard'"
            :class="['flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors', 
                     activeMenu === 'Dashboard' ? 'bg-white text-[#0A102E]' : 'text-slate-300 hover:bg-white/10 hover:text-white']"
          >
            <LayoutDashboard class="w-5 h-5" />
            Dashboard
          </button>
          
          <button 
            @click="activeMenu = 'Estudantes'"
            :class="['flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors', 
                     activeMenu === 'Estudantes' ? 'bg-white text-[#0A102E]' : 'text-slate-300 hover:bg-white/10 hover:text-white']"
          >
            <Users class="w-5 h-5" />
            Estudantes
          </button>

          <button 
            @click="activeMenu = 'Cadastro'"
            :class="['flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors', 
                     activeMenu === 'Cadastro' ? 'bg-white text-[#0A102E]' : 'text-slate-300 hover:bg-white/10 hover:text-white']"
          >
            <UserPlus class="w-5 h-5" />
            Cadastro
          </button>

          <button 
            @click="activeMenu = 'Relatórios'"
            :class="['flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors', 
                     activeMenu === 'Relatórios' ? 'bg-white text-[#0A102E]' : 'text-slate-300 hover:bg-white/10 hover:text-white']"
          >
            <FileText class="w-5 h-5" />
            Relatórios
          </button>
        </nav>

        <div class="w-full h-px bg-slate-700 my-6"></div>

        <button 
          @click="activeMenu = 'Administração'"
          :class="['flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors', 
                   activeMenu === 'Administração' ? 'bg-white text-[#0A102E]' : 'text-slate-300 hover:bg-white/10 hover:text-white']"
        >
          <Settings class="w-5 h-5" />
          Administração
        </button>
      </div>

      <div class="p-6 pt-0 mt-auto">
        <button class="flex items-center gap-3 px-4 py-3 rounded-lg font-medium text-slate-300 hover:bg-white/10 hover:text-white w-full transition-colors">
          <LogOut class="w-5 h-5" />
          Sair
        </button>
      </div>
    </aside>

    <main class="flex-1 ml-64 p-10 flex flex-col">
      
      <header class="flex justify-between items-start mb-8">
        <div>
          <h2 class="text-4xl font-bold text-slate-900 mb-1">Olá, Vinicius!</h2>
          <p class="text-slate-500 font-medium text-lg">23 de março de 2026</p>
        </div>
        <Button variant="default" class="bg-[#1A2342] hover:bg-[#0A102E] text-white rounded-full px-8 py-5 shadow-sm">
          Sair
        </Button>
      </header>

      <div class="text-slate-400 text-sm font-medium mb-6">
        Dashboard >
      </div>

      <div class="grid grid-cols-3 gap-6 mb-8">
        <div class="bg-[#0A102E] text-white rounded-2xl p-6 flex flex-col justify-between h-40 shadow-sm relative overflow-hidden">
          <User class="w-10 h-10 mb-4 text-slate-300" />
          <div>
            <h3 class="font-semibold text-lg">Total de acessos</h3>
            <p class="text-3xl font-bold mt-1">1.248</p>
          </div>
        </div>
        
        <div class="bg-[#0A102E] text-white rounded-2xl p-6 flex flex-col justify-between h-40 shadow-sm relative overflow-hidden">
          <UserCheck class="w-10 h-10 mb-4 text-slate-300" />
          <div>
            <h3 class="font-semibold text-lg">Acessos autorizados</h3>
            <p class="text-3xl font-bold mt-1">1.190</p>
          </div>
        </div>

        <div class="bg-[#FF3B3B] text-white rounded-2xl p-6 flex flex-col justify-between h-40 shadow-sm relative overflow-hidden">
          <UserX class="w-10 h-10 mb-4 text-white/90" />
          <div>
            <h3 class="font-semibold text-lg">Acessos negados</h3>
            <p class="text-3xl font-bold mt-1">58</p>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-3 gap-6 flex-1 min-h-[400px]">
        
        <div class="col-span-2 bg-[#0A102E] rounded-2xl p-8 flex flex-col shadow-sm">
          <div class="flex items-center gap-3 text-white mb-8">
            <FileText class="w-6 h-6" />
            <h3 class="font-bold text-xl tracking-wide">Acessos da semana</h3>
          </div>
          
          <div class="flex-1 flex items-end justify-between px-4 pb-2 mt-4 relative">
            <div 
              v-for="(item, index) in barChartData" 
              :key="index"
              class="flex flex-col items-center gap-3 w-full group cursor-pointer"
            >
              <span class="text-slate-300 text-sm font-semibold opacity-0 group-hover:opacity-100 transition-opacity">
                {{ item.value }}
              </span>
              <div 
                class="w-12 bg-white rounded-t-md transition-all duration-500 ease-out hover:bg-slate-200"
                :style="{ height: `${(item.value / maxBarValue) * 100}%`, minHeight: '10%' }"
              ></div>
              <span class="text-slate-400 text-xs font-medium">{{ item.day }}</span>
            </div>
          </div>
        </div>

        <div class="col-span-1 bg-[#0A102E] rounded-2xl p-8 flex flex-col items-center justify-center shadow-sm relative">
          
          <div class="relative w-48 h-48 mb-6">
            <svg viewBox="0 0 100 100" class="w-full h-full transform -rotate-90">
              <circle cx="50" cy="50" r="35" fill="none" stroke="#00C4FF" stroke-width="30" stroke-dasharray="220" stroke-dashoffset="0" class="transition-all hover:stroke-width-32 cursor-pointer"></circle>
              <circle cx="50" cy="50" r="35" fill="none" stroke="#FF4D6D" stroke-width="30" stroke-dasharray="220" stroke-dashoffset="55" class="transition-all hover:stroke-width-32 cursor-pointer"></circle>
              <circle cx="50" cy="50" r="35" fill="none" stroke="#FF9100" stroke-width="30" stroke-dasharray="220" stroke-dashoffset="110" class="transition-all hover:stroke-width-32 cursor-pointer"></circle>
              <circle cx="50" cy="50" r="35" fill="none" stroke="#FFEA00" stroke-width="30" stroke-dasharray="220" stroke-dashoffset="150" class="transition-all hover:stroke-width-32 cursor-pointer"></circle>
              <circle cx="50" cy="50" r="35" fill="none" stroke="#00E676" stroke-width="30" stroke-dasharray="220" stroke-dashoffset="185" class="transition-all hover:stroke-width-32 cursor-pointer"></circle>
              
              <circle cx="50" cy="50" r="20" fill="#0A102E"></circle>
            </svg>
          </div>

          <div class="w-full grid grid-cols-2 gap-x-4 gap-y-2 text-xs font-medium text-slate-300 absolute bottom-6 px-6">
            <div class="flex items-center gap-2"><div class="w-2 h-2 rounded-full bg-[#FF4D6D]"></div> ADS</div>
            <div class="flex items-center gap-2"><div class="w-2 h-2 rounded-full bg-[#FF9100]"></div> COMEX</div>
            <div class="flex items-center gap-2"><div class="w-2 h-2 rounded-full bg-[#FFEA00]"></div> DSM</div>
            <div class="flex items-center gap-2"><div class="w-2 h-2 rounded-full bg-[#00E676]"></div> GEEM</div>
            <div class="flex items-center gap-2"><div class="w-2 h-2 rounded-full bg-[#00C4FF]"></div> OUTROS</div>
          </div>
        </div>

      </div>

      <footer class="mt-8 text-center text-sm text-slate-400 font-medium">
        CIFA &copy; 2026
      </footer>

    </main>
  </div>
</template>

<style>
body {
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}
</style>