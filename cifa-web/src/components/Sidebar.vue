<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { 
  LayoutDashboard, 
  Users, 
  UserPlus, 
  FileText, 
  Settings, 
  LogOut,
  PanelLeftClose,
  PanelLeft
} from 'lucide-vue-next'

const props = defineProps<{ 
  isOpen: boolean,
  isCollapsed: boolean
}>()

const emit = defineEmits(['logout', 'close', 'toggle-collapse'])

const route = useRoute()
const router = useRouter()

const menuItems = [
  { name: 'Dashboard', path: '/admin/dashboard', icon: LayoutDashboard },
  { name: 'Estudantes', path: '/admin/estudantes', icon: Users },
  { name: 'Cadastro', path: '/admin/cadastro', icon: UserPlus },
  { name: 'Relatórios', path: '/admin/relatorios', icon: FileText },
]

const isActive = (path: string) => route.path === path

const navigate = (path: string) => {
  router.push(path)
  emit('close')
}
</script>

<template>
  <aside 
    :class="[
      'fixed left-0 top-0 h-[100dvh] bg-[#0A102E] text-white flex flex-col justify-between z-50 shadow-2xl transition-all duration-300 ease-in-out lg:translate-x-0',
      isOpen ? 'translate-x-0' : '-translate-x-full',
      isCollapsed ? 'w-20' : 'w-64'
    ]"
  >
    <div 
      class="flex flex-col flex-1 overflow-y-auto overflow-x-hidden custom-scrollbar transition-all duration-300"
      :class="isCollapsed ? 'p-4' : 'p-6'"
    >
      
      <div 
        class="flex items-center mb-10 select-none shrink-0 transition-all duration-300 h-10"
        :class="isCollapsed ? 'justify-center' : 'gap-3'"
      >
        <h1 class="text-3xl font-bold tracking-wider transition-all duration-300">
          {{ isCollapsed ? 'C' : 'CIFA' }}
        </h1>
        <div 
          v-if="!isCollapsed" 
          class="text-[0.6rem] leading-tight text-slate-300 font-medium uppercase tracking-tighter whitespace-nowrap animate-in fade-in duration-500"
        >
          <p>Controle Inteligente</p>
          <p>de Fluxo Acadêmico</p>
        </div>
      </div>

      <nav class="flex flex-col gap-2 shrink-0">
        <button 
          v-for="item in menuItems" 
          :key="item.path"
          @click="navigate(item.path)"
          class="flex items-center rounded-lg font-medium transition-all duration-200 text-left group relative"
          :class="[
            isActive(item.path) ? 'bg-white text-[#0A102E] shadow-lg' : 'text-slate-300 hover:bg-white/10 hover:text-white',
            isCollapsed ? 'justify-center px-0 py-3' : 'px-4 py-3'
          ]"
          :title="isCollapsed ? item.name : ''"
        >
          <component :is="item.icon" class="w-5 h-5 shrink-0" />
          <span 
            v-if="!isCollapsed" 
            class="ml-3 truncate animate-in fade-in duration-300"
          >
            {{ item.name }}
          </span>
        </button>
      </nav>

      <div class="w-full h-px bg-slate-700/50 my-6 shrink-0"></div>

      <button 
        @click="navigate('/admin/administracao')"
        class="flex items-center rounded-lg font-medium transition-all duration-200 text-left shrink-0 group relative"
        :class="[
          isActive('/admin/administracao') ? 'bg-white text-[#0A102E] shadow-lg' : 'text-slate-300 hover:bg-white/10 hover:text-white',
          isCollapsed ? 'justify-center px-0 py-3' : 'px-4 py-3'
        ]"
        :title="isCollapsed ? 'Gestão' : ''"
      >
        <Settings class="w-5 h-5 shrink-0" />
        <span 
          v-if="!isCollapsed" 
          class="ml-3 truncate animate-in fade-in duration-300"
        >
          Gestão
        </span>
      </button>
    </div>

    <div 
      class="flex flex-col gap-2 pt-4 mt-auto shrink-0 border-t border-slate-800/50 bg-[#0A102E] transition-all duration-300"
      :class="isCollapsed ? 'p-4' : 'p-6'"
    >
      <button 
        @click="emit('logout')"
        class="flex items-center rounded-lg font-medium text-slate-400 hover:bg-red-500/10 hover:text-red-400 w-full transition-all duration-200 group text-left"
        :class="isCollapsed ? 'justify-center px-0 py-3' : 'px-4 py-3'"
        :title="isCollapsed ? 'Sair do Sistema' : ''"
      >
        <LogOut class="w-5 h-5 shrink-0 transition-transform group-hover:-translate-x-1" />
        <span 
          v-if="!isCollapsed" 
          class="ml-3 truncate animate-in fade-in duration-300"
        >
          Sair do Sistema
        </span>
      </button>

      <button 
        @click="emit('toggle-collapse')"
        class="hidden lg:flex items-center rounded-lg font-medium text-slate-400 hover:bg-white/10 hover:text-white w-full transition-all duration-200 group text-left"
        :class="isCollapsed ? 'justify-center px-0 py-3' : 'px-4 py-3'"
        :title="isCollapsed ? 'Expandir Menu' : 'Recolher Menu'"
      >
        <PanelLeft v-if="isCollapsed" class="w-5 h-5 shrink-0 transition-transform group-hover:translate-x-1" />
        <PanelLeftClose v-else class="w-5 h-5 shrink-0 transition-transform group-hover:-translate-x-1" />
        <span 
          v-if="!isCollapsed" 
          class="ml-3 truncate animate-in fade-in duration-300"
        >
          Recolher Menu
        </span>
      </button>
    </div>
  </aside>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #334155; border-radius: 10px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background-color: #475569; }
</style>