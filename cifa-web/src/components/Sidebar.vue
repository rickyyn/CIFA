<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { 
  LayoutDashboard, 
  Users, 
  UserPlus, 
  FileText, 
  Settings, 
  LogOut 
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

// Define os eventos que este componente pode disparar
const emit = defineEmits(['logout'])

// Lista de navegação para facilitar a manutenção
const menuItems = [
  { name: 'Dashboard', path: '/admin/dashboard', icon: LayoutDashboard },
  { name: 'Estudantes', path: '/admin/estudantes', icon: Users },
  { name: 'Cadastro', path: '/admin/cadastro', icon: UserPlus },
  { name: 'Relatórios', path: '/admin/relatorios', icon: FileText },
]

// Função para verificar se a rota está ativa
const isActive = (path: string) => route.path === path
</script>

<template>
  <aside class="w-64 bg-[#0A102E] text-white flex flex-col justify-between fixed h-full z-10 shadow-2xl">
    <div class="p-6 flex flex-col h-full">
      <div class="flex items-center gap-3 mb-10 select-none">
        <h1 class="text-3xl font-bold tracking-wider">CIFA</h1>
        <div class="text-[0.6rem] leading-tight text-slate-300 font-medium uppercase tracking-tighter">
          <p>Controle Inteligente</p>
          <p>de Fluxo Acadêmico</p>
        </div>
      </div>

      <nav class="flex flex-col gap-2">
        <button 
          v-for="item in menuItems" 
          :key="item.path"
          @click="router.push(item.path)"
          :class="[
            'flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-all duration-200',
            isActive(item.path) 
              ? 'bg-white text-[#0A102E] shadow-lg' 
              : 'text-slate-300 hover:bg-white/10 hover:text-white'
          ]"
        >
          <component :is="item.icon" class="w-5 h-5" />
          {{ item.name }}
        </button>
      </nav>

      <div class="w-full h-px bg-slate-700/50 my-6"></div>

      <button 
        @click="router.push('/admin/administracao')"
        :class="[
          'flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-all duration-200',
          isActive('/admin/administracao') 
            ? 'bg-white text-[#0A102E]' 
            : 'text-slate-300 hover:bg-white/10 hover:text-white'
        ]"
      >
        <Settings class="w-5 h-5" />
        Gestão
      </button>
    </div>

    <div class="p-6 pt-0 mt-auto">
      <button 
        @click="emit('logout')"
        class="flex items-center gap-3 px-4 py-3 rounded-lg font-medium text-slate-400 hover:bg-red-500/10 hover:text-red-400 w-full transition-all duration-200 group"
      >
        <LogOut class="w-5 h-5 transition-transform group-hover:-translate-x-1" />
        Sair do Sistema
      </button>
    </div>
  </aside>
</template>