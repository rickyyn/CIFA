<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, 
  MoreHorizontal, 
  Pencil, 
  Trash2, 
  ShieldAlert, 
  UserPlus, 
  Camera,
  Loader2
} from 'lucide-vue-next'
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { 
  DropdownMenu, 
  DropdownMenuContent, 
  DropdownMenuItem, 
  DropdownMenuTrigger 
} from '@/components/ui/dropdown-menu'
import { 
  Dialog, 
  DialogContent, 
  DialogDescription, 
  DialogFooter, 
  DialogHeader, 
  DialogTitle 
} from '@/components/ui/dialog'
import { 
  Select, 
  SelectContent, 
  SelectItem, 
  SelectTrigger, 
  SelectValue 
} from '@/components/ui/select'
import { Separator } from '@/components/ui/separator'
import { useToast } from '@/components/ui/toast/use-toast'

const router = useRouter()
const { toast } = useToast()

interface Student {
  id: number
  name: string
  period: string
  course: string
  registration: string
  contact: string
  avatar: string
  status: 'Ativo' | 'Inativo' | 'Bloqueado' | 'Visitante'
  semester: number
}

// Estados Principais
const allStudents = ref<Student[]>([])
const searchQuery = ref('')
const isLoadingData = ref(true)

// Estados dos Modais
const isEditModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const studentToEdit = ref<Student | null>(null)
const studentToDelete = ref<Student | null>(null)

// Configuração de Abas Formais
type TabType = 'todos' | 1 | 2 | 3 | 4 | 5 | 6 | 'inativos'
const activeTab = ref<TabType>('todos')

const tabs: { id: TabType, label: string }[] = [
  { id: 'todos', label: 'Todos' },
  { id: 1, label: '1º Sem' },
  { id: 2, label: '2º Sem' },
  { id: 3, label: '3º Sem' },
  { id: 4, label: '4º Sem' },
  { id: 5, label: '5º Sem' },
  { id: 6, label: '6º Sem' },
  { id: 'inativos', label: 'Inativos' }
]

// ==========================================
// SINCRONIZAÇÃO DE DADOS (API + LOCAL)
// ==========================================
const fetchAPIStudents = async (): Promise<Student[]> => {
  try {
    const response = await fetch('https://jsonplaceholder.typicode.com/users')
    const data = await response.json()
    return data.map((user: any) => ({
      id: user.id,
      name: user.name,
      period: user.id % 2 === 0 ? 'Noturno' : 'Matutino',
      course: user.id % 3 === 0 ? 'DSM' : (user.id % 2 === 0 ? 'ADS' : 'COMEX'),
      registration: `1460282113${user.id.toString().padStart(3, '0')}`,
      contact: user.email.toLowerCase(),
      avatar: `https://api.dicebear.com/8.x/avataaars/svg?seed=${encodeURIComponent(user.name)}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`,
      status: user.id % 4 === 0 ? 'Inativo' : 'Ativo',
      semester: (user.id % 6) + 1
    }))
  } catch (error) {
    return []
  }
}

const loadStudents = async () => {
  isLoadingData.value = true
  const apiStudents = await fetchAPIStudents()
  
  const storedData = localStorage.getItem('cifa_students')
  const persistedStudents: Student[] = storedData ? JSON.parse(storedData) : []
  
  const deletedIds: number[] = JSON.parse(localStorage.getItem('cifa_deleted_ids') || '[]')
  
  // Filtra IDs deletados da API
  const validApiStudents = apiStudents.filter(s => !deletedIds.includes(s.id))
  
  const merged = [...validApiStudents]
  
  // Mescla com locais garantindo avatares e semestres
  persistedStudents.forEach(ps => {
    if (!deletedIds.includes(ps.id)) {
      const index = merged.findIndex(s => s.id === ps.id)
      const formattedStudent = {
        ...ps,
        semester: ps.semester || 1,
        avatar: ps.avatar || `https://api.dicebear.com/8.x/avataaars/svg?seed=${encodeURIComponent(ps.name)}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`
      }
      if (index !== -1) merged[index] = formattedStudent
      else merged.push(formattedStudent)
    }
  })
  
  allStudents.value = merged.sort((a, b) => a.name.localeCompare(b.name))
  isLoadingData.value = false
}

onMounted(() => {
  loadStudents()
})

const filteredStudents = computed(() => {
  return allStudents.value.filter(student => {
    const matchesSearch = student.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                          student.registration.includes(searchQuery.value)
    
    let matchesTab = true
    if (activeTab.value === 'inativos') matchesTab = student.status === 'Inativo'
    else if (typeof activeTab.value === 'number') matchesTab = student.semester === activeTab.value

    return matchesSearch && matchesTab
  })
})

const getCount = (tabId: TabType) => {
  if (tabId === 'todos') return allStudents.value.length
  if (tabId === 'inativos') return allStudents.value.filter(s => s.status === 'Inativo').length
  return allStudents.value.filter(s => s.semester === tabId).length
}

// ==========================================
// AÇÕES ADMINISTRATIVAS
// ==========================================
const openEdit = (student: Student) => {
  studentToEdit.value = JSON.parse(JSON.stringify(student))
  isEditModalOpen.value = true
}

const saveEdit = () => {
  if (!studentToEdit.value) return
  const storedData = localStorage.getItem('cifa_students')
  let persistedStudents: Student[] = storedData ? JSON.parse(storedData) : []
  
  const index = persistedStudents.findIndex(s => s.id === studentToEdit.value!.id)
  if (index !== -1) persistedStudents[index] = studentToEdit.value
  else persistedStudents.push(studentToEdit.value)

  localStorage.setItem('cifa_students', JSON.stringify(persistedStudents))
  loadStudents()
  isEditModalOpen.value = false
  toast({ title: "Dados Atualizados", description: "Alterações salvas no banco de dados." })
}

const openDelete = (student: Student) => {
  studentToDelete.value = student
  isDeleteModalOpen.value = true
}

const confirmDelete = () => {
  if (!studentToDelete.value) return
  const targetId = studentToDelete.value.id
  
  // 1. Remove do LocalStorage
  const storedData = localStorage.getItem('cifa_students')
  let persistedStudents: Student[] = storedData ? JSON.parse(storedData) : []
  persistedStudents = persistedStudents.filter(s => s.id !== targetId)
  localStorage.setItem('cifa_students', JSON.stringify(persistedStudents))

  // 2. Marca como deletado (para sumir da API também)
  const deletedIds: number[] = JSON.parse(localStorage.getItem('cifa_deleted_ids') || '[]')
  if (!deletedIds.includes(targetId)) {
    deletedIds.push(targetId)
    localStorage.setItem('cifa_deleted_ids', JSON.stringify(deletedIds))
  }

  loadStudents()
  isDeleteModalOpen.value = false
  toast({ title: "Registro Removido", description: "O perfil foi excluído permanentemente.", variant: "destructive" })
}

const getStatusStyle = (status: string) => {
  switch (status) {
    case 'Ativo': return 'bg-emerald-100 text-emerald-700'
    case 'Inativo': return 'bg-slate-200 text-slate-700'
    case 'Bloqueado': return 'bg-red-100 text-red-700'
    case 'Visitante': return 'bg-indigo-100 text-indigo-700'
    default: return 'bg-slate-100 text-slate-700'
  }
}
</script>

<template>
  <div class="flex flex-col h-full space-y-4 font-poppins shrink-0">
    
    <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
      <div>
        <h2 class="text-2xl font-bold text-slate-900 tracking-tight">Gestão de Acesso</h2>
        <p class="text-slate-500 text-[10px] uppercase font-bold tracking-[0.2em]">Administração de Usuários e Sincronização</p>
      </div>
      <Button @click="router.push('/admin/cadastro')" class="bg-[#1A1A3A] hover:bg-[#0F0F24] text-white rounded-xl px-6 h-10 shadow-md gap-2 w-full sm:w-auto shrink-0 transition-all active:scale-95">
        <UserPlus class="w-4 h-4" /> Novo Cadastro
      </Button>
    </div>

    <div class="flex flex-col sm:flex-row items-center gap-4 py-2 shrink-0">
      <div class="relative w-full sm:w-96">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
        <input v-model="searchQuery" placeholder="Buscar por nome ou matrícula..." class="pl-10 h-10 border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#0A102E] bg-white w-full transition-all text-sm outline-none shadow-sm" />
      </div>
    </div>

    <div class="w-full flex shrink-0 border-b border-slate-200 mt-2 mb-2 bg-slate-50/50 rounded-t-xl overflow-hidden">
      <button
        v-for="tab in tabs" 
        :key="tab.id"
        @click="activeTab = tab.id"
        class="flex-1 min-w-0 pb-3 px-1 pt-3 text-[10px] sm:text-xs font-semibold transition-all duration-300 relative flex justify-center items-center gap-1.5 group overflow-hidden"
        :class="activeTab === tab.id ? 'text-[#0A102E] bg-white' : 'text-slate-500 hover:text-slate-800 hover:bg-white/50'"
      >
        <span class="truncate">{{ tab.label }}</span>
        <span class="px-1.5 py-0.5 rounded-full text-[9px] font-bold transition-colors shrink-0" :class="activeTab === tab.id ? 'bg-[#0A102E] text-white' : 'bg-slate-200 text-slate-500'">
          {{ getCount(tab.id) }}
        </span>
        <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 w-full h-[3px] bg-[#0A102E] rounded-t-full"></div>
      </button>
    </div>

    <div class="flex-1 bg-white border border-slate-200 rounded-[2.5rem] shadow-sm overflow-hidden flex flex-col min-h-0 relative">
      
      <div v-if="isLoadingData" class="absolute inset-0 flex flex-col items-center justify-center bg-white/80 backdrop-blur-sm z-10">
        <Loader2 class="w-10 h-10 animate-spin text-[#0A102E] mb-4" />
        <span class="text-sm font-bold text-slate-500 uppercase tracking-widest">Sincronizando Gestão...</span>
      </div>

      <div class="flex-1 overflow-x-auto overflow-y-auto custom-scrollbar">
        <Table class="min-w-[900px] w-full">
          <TableHeader class="bg-slate-50/80 sticky top-0 z-10 backdrop-blur-sm">
            <TableRow class="border-b-slate-200">
              <TableHead class="text-slate-600 font-bold px-6 h-12">Estudante</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Matrícula</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Curso</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Período / Sem</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Status</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12 text-center w-24">Ações</TableHead>
            </TableRow>
          </TableHeader>
          
          <TableBody v-auto-animate>
            <template v-if="filteredStudents.length > 0">
              <TableRow v-for="student in filteredStudents" :key="student.id" class="hover:bg-slate-50/80 transition-colors border-b-slate-100">
                <TableCell class="py-4 px-6 whitespace-nowrap">
                  <div class="flex items-center gap-3">
                    <img :src="student.avatar" class="w-10 h-10 rounded-full object-cover bg-slate-100 border border-slate-200" />
                    <div class="flex flex-col">
                      <span class="font-bold text-slate-800 text-sm tracking-tight leading-tight">{{ student.name }}</span>
                      <span class="text-[10px] text-slate-500 font-medium">{{ student.contact }}</span>
                    </div>
                  </div>
                </TableCell>
                <TableCell class="py-4 px-6 font-mono text-xs font-semibold text-slate-700">{{ student.registration }}</TableCell>
                <TableCell class="py-4 px-6 text-sm font-semibold text-slate-600">{{ student.course }}</TableCell>
                <TableCell class="py-4 px-6 text-sm text-slate-600">
                  {{ student.period }} 
                  <span class="text-[10px] font-bold bg-slate-100 px-1.5 py-0.5 rounded ml-1">{{ student.semester }}º</span>
                </TableCell>
                <TableCell class="py-4 px-6">
                  <Badge :class="getStatusStyle(student.status)" class="border-none font-bold text-[10px] uppercase shadow-none px-3">{{ student.status }}</Badge>
                </TableCell>
                <TableCell class="py-4 px-6 text-center">
                  <DropdownMenu>
                    <DropdownMenuTrigger as-child>
                      <Button variant="ghost" class="h-8 w-8 p-0 rounded-full hover:bg-slate-200">
                        <MoreHorizontal class="w-4 h-4 text-slate-600" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" class="w-48 rounded-xl border-none shadow-xl font-poppins">
                      <DropdownMenuItem @click="openEdit(student)" class="cursor-pointer gap-2 font-medium">
                        <Pencil class="w-4 h-4 text-indigo-600" /> Editar Dados
                      </DropdownMenuItem>
                      <Separator class="my-1" />
                      <DropdownMenuItem @click="openDelete(student)" class="cursor-pointer gap-2 font-bold text-red-600 focus:text-red-700 focus:bg-red-50">
                        <Trash2 class="w-4 h-4" /> Excluir Perfil
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </TableCell>
              </TableRow>
            </template>
            <template v-else-if="!isLoadingData">
              <TableRow><TableCell colspan="6" class="h-64 text-center text-slate-400 italic font-medium">Nenhum registro para esta categoria.</TableCell></TableRow>
            </template>
          </TableBody>
        </Table>
      </div>
    </div>

    <Dialog v-model:open="isEditModalOpen">
      <DialogContent class="rounded-[2.5rem] w-[95vw] sm:max-w-[650px] border-none shadow-2xl p-6 sm:p-8 max-h-[90vh] overflow-y-auto custom-scrollbar font-poppins">
        <DialogHeader>
          <DialogTitle class="text-2xl font-bold text-slate-900">Editar Estudante</DialogTitle>
          <DialogDescription>Atualize os dados acadêmicos e de contato.</DialogDescription>
        </DialogHeader>
        <div v-if="studentToEdit" class="flex flex-col gap-6 py-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div class="flex flex-col space-y-1.5 md:col-span-2">
              <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Nome Completo</label>
              <Input v-model="studentToEdit.name" class="h-11 rounded-xl bg-slate-50 border-slate-200" />
            </div>
            
            <div class="flex flex-col space-y-1.5">
              <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Matrícula</label>
              <Input v-model="studentToEdit.registration" class="h-11 rounded-xl bg-slate-50 border-slate-200 font-mono" />
            </div>
            
            <div class="flex flex-col space-y-1.5">
              <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Semestre Atual</label>
              <Select 
                :model-value="String(studentToEdit.semester)" 
                @update:model-value="v => studentToEdit!.semester = Number(v)"
              >
                <SelectTrigger class="h-11 rounded-xl bg-slate-50 border-slate-200">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem v-for="n in 6" :key="n" :value="String(n)">{{ n }}º Semestre</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            <div class="flex flex-col space-y-1.5">
              <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Status</label>
              <Select v-model="studentToEdit.status">
                <SelectTrigger class="h-11 rounded-xl bg-slate-50 border-slate-200">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Ativo">Ativo</SelectItem>
                  <SelectItem value="Inativo">Inativo</SelectItem>
                  <SelectItem value="Bloqueado">Bloqueado</SelectItem>
                  <SelectItem value="Visitante">Visitante</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            <div class="flex flex-col space-y-1.5">
              <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Curso</label>
              <Select v-model="studentToEdit.course">
                <SelectTrigger class="h-11 rounded-xl bg-slate-50 border-slate-200">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="DSM">DSM</SelectItem>
                  <SelectItem value="ADS">ADS</SelectItem>
                  <SelectItem value="COMEX">COMEX</SelectItem>
                  <SelectItem value="GEEM">GEEM</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            <div class="flex flex-col space-y-1.5 md:col-span-2">
              <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Contato</label>
              <Input v-model="studentToEdit.contact" class="h-11 rounded-xl bg-slate-50 border-slate-200" />
            </div>
          </div>
        </div>
        <DialogFooter class="border-t pt-4 flex flex-col sm:flex-row gap-3">
          <Button variant="ghost" @click="isEditModalOpen = false" class="rounded-xl font-bold text-slate-500 w-full sm:w-auto">Cancelar</Button>
          <Button @click="saveEdit" class="bg-[#1A1A3A] hover:bg-[#0F0F24] rounded-xl px-10 text-white font-bold shadow-md w-full sm:w-auto transition-all active:scale-95">Salvar Alterações</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <Dialog v-model:open="isDeleteModalOpen">
      <DialogContent class="rounded-[2.5rem] w-[95vw] sm:max-w-[400px] border-none shadow-2xl text-center p-6 sm:p-8 font-poppins">
        <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-red-100 mb-6"><Trash2 class="h-8 w-8 text-red-600" /></div>
        <DialogHeader class="mb-6">
          <DialogTitle class="text-xl font-bold text-slate-900 text-center mb-2">Confirmar Exclusão?</DialogTitle>
          <DialogDescription class="text-center text-sm leading-relaxed">Deseja remover <strong>{{ studentToDelete?.name }}</strong>? Esta ação é definitiva e removerá o acesso.</DialogDescription>
        </DialogHeader>
        <DialogFooter class="flex flex-row justify-center gap-3 w-full">
          <Button variant="outline" @click="isDeleteModalOpen = false" class="rounded-xl flex-1 border-slate-200 font-bold">Não</Button>
          <Button @click="confirmDelete" class="bg-red-600 hover:bg-red-700 rounded-xl flex-1 text-white font-bold shadow-md border-none transition-all active:scale-95">Sim, Excluir</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

  </div>
</template>

<style scoped>
.font-poppins { font-family: 'Poppins', sans-serif; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 20px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background-color: #94a3b8; }
</style>