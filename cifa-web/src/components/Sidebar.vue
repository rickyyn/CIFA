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

// Recebe o estado de abertura (mobile) do AdminLayout
const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits(['logout', 'close'])

const route = useRoute()
const router = useRouter()

const menuItems = [
  { name: 'Dashboard', path: '/admin/dashboard', icon: LayoutDashboard },
  { name: 'Estudantes', path: '/admin/estudantes', icon: Users },
  { name: 'Cadastro', path: '/admin/cadastro', icon: UserPlus },
  { name: 'Relatórios', path: '/admin/relatorios', icon: FileText },
]

const isActive = (path: string) => route.path === path

// Função de navegação responsiva (muda a rota e fecha a sidebar no mobile)
const navigate = (path: string) => {
  router.push(path)
  emit('close')
}
</script>

<template>
  <aside 
    :class="[
      'fixed left-0 top-0 h-screen w-64 bg-[#0A102E] text-white flex flex-col justify-between z-50 shadow-2xl transition-transform duration-300 ease-in-out lg:translate-x-0',
      isOpen ? 'translate-x-0' : '-translate-x-full'
    ]"
  >
    <div class="p-6 flex flex-col h-full overflow-y-auto custom-scrollbar">
      
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
          @click="navigate(item.path)"
          :class="[
            'flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-all duration-200 text-left',
            isActive(item.path) 
              ? 'bg-white text-[#0A102E] shadow-lg scale-[1.02]' 
              : 'text-slate-300 hover:bg-white/10 hover:text-white'
          ]"
        >
          <component :is="item.icon" class="w-5 h-5 shrink-0" />
          <span class="truncate">{{ item.name }}</span>
        </button>
      </nav>

      <div class="w-full h-px bg-slate-700/50 my-6 shrink-0"></div>

      <button 
        @click="navigate('/admin/administracao')"
        :class="[
          'flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-all duration-200 text-left shrink-0',
          isActive('/admin/administracao') 
            ? 'bg-white text-[#0A102E] shadow-lg scale-[1.02]' 
            : 'text-slate-300 hover:bg-white/10 hover:text-white'
        ]"
      >
        <Settings class="w-5 h-5 shrink-0" />
        Gestão
      </button>
    </div>

    <div class="p-6 pt-0 mt-auto shrink-0 border-t border-slate-800/50">
      <button 
        @click="emit('logout')"
        class="flex items-center gap-3 px-4 py-4 rounded-lg font-medium text-slate-400 hover:bg-red-500/10 hover:text-red-400 w-full transition-all duration-200 group mt-4 text-left"
      >
        <LogOut class="w-5 h-5 shrink-0 transition-transform group-hover:-translate-x-1" />
        Sair do Sistema
      </button>
    </div>
  </aside>
</template>

<style scoped>
/* Pequeno ajuste para a barra de scroll no tema escuro da sidebar */
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #334155; border-radius: 10px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background-color: #475569; }
</style>