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
  Camera
} from 'lucide-vue-next'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
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
}

const allStudents = ref<Student[]>([])
const searchQuery = ref('')

const isEditModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const studentToEdit = ref<Student | null>(null)
const studentToDelete = ref<Student | null>(null)

const defaultStudents: Student[] = [
  { id: 1, name: 'Leonardo Mendonça', period: 'Noturno', course: 'DSM', registration: '1460282113001', contact: 'leonardo.mendonca@fatec.sp.gov.br', avatar: 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?q=80&w=200&fit=crop', status: 'Ativo' },
  { id: 2, name: 'Ana Clara Souza', period: 'Matutino', course: 'ADS', registration: '1460282113042', contact: 'ana.clara@fatec.sp.gov.br', avatar: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?q=80&w=200&fit=crop', status: 'Visitante' },
  { id: 3, name: 'Felipe Mendes', period: 'Noturno', course: 'COMEX', registration: '1460282113088', contact: 'felipe.m@fatec.sp.gov.br', avatar: 'https://images.unsplash.com/photo-1474511320723-9a56873867b5?q=80&w=200&fit=crop', status: 'Inativo' },
  { id: 4, name: 'Beatriz Lima', period: 'Vespertino', course: 'GEEM', registration: '1460282113105', contact: 'bia.lima@fatec.sp.gov.br', avatar: 'https://images.unsplash.com/photo-1540573133985-87b6da6d54a9?q=80&w=200&fit=crop', status: 'Bloqueado' }
]

const loadStudents = () => {
  const storedData = localStorage.getItem('cifa_students')
  const persistedStudents: Student[] = storedData ? JSON.parse(storedData) : []
  const deletedIds: number[] = JSON.parse(localStorage.getItem('cifa_deleted_ids') || '[]')

  let baseStudents = defaultStudents.filter(s => !deletedIds.includes(s.id))

  const merged = [...baseStudents]
  persistedStudents.forEach(ps => {
    const index = merged.findIndex(s => s.id === ps.id)
    if (index !== -1) merged[index] = ps
    else merged.push(ps)
  })

  allStudents.value = merged.sort((a, b) => a.name.localeCompare(b.name))
}

onMounted(() => {
  loadStudents()
})

const filteredStudents = computed(() => {
  return allStudents.value.filter(student => 
    student.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    student.registration.includes(searchQuery.value)
  )
})

const openEdit = (student: Student) => {
  studentToEdit.value = JSON.parse(JSON.stringify(student))
  isEditModalOpen.value = true
}

const saveEdit = () => {
  if (!studentToEdit.value) return

  const storedData = localStorage.getItem('cifa_students')
  let persistedStudents: Student[] = storedData ? JSON.parse(storedData) : []
  
  const index = persistedStudents.findIndex(s => s.id === studentToEdit.value!.id)
  if (index !== -1) {
    persistedStudents[index] = studentToEdit.value
  } else {
    persistedStudents.push(studentToEdit.value)
  }

  localStorage.setItem('cifa_students', JSON.stringify(persistedStudents))
  loadStudents()
  
  isEditModalOpen.value = false
  toast({ title: "Dados Atualizados", description: "As alterações, incluindo a nova foto, foram salvas." })
}

const openDelete = (student: Student) => {
  studentToDelete.value = student
  isDeleteModalOpen.value = true
}

const confirmDelete = () => {
  if (!studentToDelete.value) return

  const targetId = studentToDelete.value.id
  const storedData = localStorage.getItem('cifa_students')
  let persistedStudents: Student[] = storedData ? JSON.parse(storedData) : []
  persistedStudents = persistedStudents.filter(s => s.id !== targetId)
  localStorage.setItem('cifa_students', JSON.stringify(persistedStudents))

  const deletedIds: number[] = JSON.parse(localStorage.getItem('cifa_deleted_ids') || '[]')
  if (!deletedIds.includes(targetId)) {
    deletedIds.push(targetId)
    localStorage.setItem('cifa_deleted_ids', JSON.stringify(deletedIds))
  }

  loadStudents()
  isDeleteModalOpen.value = false
  toast({ title: "Registro Removido", description: "O perfil foi excluído do sistema.", variant: "destructive" })
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
  <div class="flex flex-col h-full space-y-4 font-nunito animate-in fade-in duration-500">
    
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-slate-900 tracking-tight">Gestão de Acesso</h2>
        <p class="text-slate-500 text-[10px] uppercase font-bold tracking-[0.2em]">Administração de Usuários</p>
      </div>
      <Button @click="router.push('/admin/cadastro')" class="bg-[#1A1A3A] hover:bg-[#0F0F24] text-white rounded-xl px-6 h-10 shadow-md gap-2">
        <UserPlus class="w-4 h-4" />
        Novo Cadastro
      </Button>
    </div>

    <div class="flex flex-col sm:flex-row items-center gap-4 py-2">
      <div class="relative w-full sm:w-96">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
        <input 
          v-model="searchQuery"
          placeholder="Buscar por nome ou matrícula..." 
          class="pl-10 h-10 border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#0A102E] bg-white w-full transition-all text-sm outline-none shadow-sm"
        />
      </div>
    </div>

    <div class="flex-1 bg-white border border-slate-200 rounded-[2.5rem] shadow-sm overflow-hidden flex flex-col min-h-0">
      <div class="flex-1 overflow-auto custom-scrollbar">
        <Table>
          <TableHeader class="bg-slate-50/80 sticky top-0 z-10 backdrop-blur-sm">
            <TableRow class="border-b-slate-200">
              <TableHead class="text-slate-600 font-bold px-6 h-12">Estudante</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Matrícula</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Curso</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Período</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Status</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12 text-center w-24">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <template v-if="filteredStudents.length > 0">
              <TableRow v-for="student in filteredStudents" :key="student.id" class="hover:bg-slate-50/80 transition-colors border-b-slate-100">
                <TableCell class="py-4 px-6">
                  <div class="flex items-center gap-3">
                    <img :src="student.avatar" class="w-9 h-9 rounded-full object-cover bg-slate-100" />
                    <div class="flex flex-col">
                      <span class="font-bold text-slate-800 text-sm tracking-tight leading-tight">{{ student.name }}</span>
                      <span class="text-[10px] text-slate-500 font-medium">{{ student.contact }}</span>
                    </div>
                  </div>
                </TableCell>
                <TableCell class="py-4 px-6 font-mono text-xs font-semibold text-slate-700">{{ student.registration }}</TableCell>
                <TableCell class="py-4 px-6 text-sm font-semibold text-slate-600">{{ student.course }}</TableCell>
                <TableCell class="py-4 px-6 text-sm text-slate-600">{{ student.period }}</TableCell>
                <TableCell class="py-4 px-6">
                  <Badge :class="getStatusStyle(student.status)" class="border-none font-bold text-[10px] uppercase shadow-none">
                    {{ student.status }}
                  </Badge>
                </TableCell>
                <TableCell class="py-4 px-6 text-center">
                  <DropdownMenu>
                    <DropdownMenuTrigger as-child>
                      <Button variant="ghost" class="h-8 w-8 p-0 rounded-full hover:bg-slate-200">
                        <MoreHorizontal class="w-4 h-4 text-slate-600" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" class="w-44 rounded-xl border-none shadow-xl">
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
            <template v-else>
              <TableRow>
                <TableCell colspan="6" class="h-64 text-center text-slate-400 italic">Nenhum registro encontrado.</TableCell>
              </TableRow>
            </template>
          </TableBody>
        </Table>
      </div>
    </div>

    <Dialog v-model:open="isEditModalOpen">
      <DialogContent class="rounded-[2.5rem] sm:max-w-[650px] border-none shadow-2xl p-8 max-h-[90vh] overflow-y-auto custom-scrollbar">
        <DialogHeader>
          <DialogTitle class="text-2xl font-bold text-slate-900">Editar Perfil</DialogTitle>
          <DialogDescription>Modifique as informações e a imagem do estudante.</DialogDescription>
        </DialogHeader>
        
        <div v-if="studentToEdit" class="flex flex-col gap-6 py-4">
          <div class="flex justify-center mb-2">
            <div class="relative group">
              <img 
                :src="studentToEdit.avatar" 
                class="w-32 h-32 rounded-full object-cover bg-slate-100 shadow-md border-4 border-white" 
                onerror="this.src='https://via.placeholder.com/150?text=Avatar'"
              />
              <div class="absolute inset-0 flex items-center justify-center bg-black/20 rounded-full opacity-0 group-hover:opacity-100 transition-opacity">
                <Camera class="text-white w-6 h-6" />
              </div>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div class="flex flex-col space-y-1.5 md:col-span-2">
              <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">URL da Imagem de Perfil</label>
              <Input v-model="studentToEdit.avatar" placeholder="https://..." class="h-11 rounded-xl bg-slate-50 border-slate-200 text-xs" />
            </div>

            <div class="flex flex-col space-y-1.5 md:col-span-2">
              <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Nome Completo</label>
              <Input v-model="studentToEdit.name" class="h-11 rounded-xl bg-slate-50 border-slate-200" />
            </div>
            
            <div class="flex flex-col space-y-1.5">
              <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Matrícula</label>
              <Input v-model="studentToEdit.registration" class="h-11 rounded-xl bg-slate-50 border-slate-200 font-mono" />
            </div>

            <div class="flex flex-col space-y-1.5">
              <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Status</label>
              <Select v-model="studentToEdit.status">
                <SelectTrigger class="h-11 rounded-xl bg-slate-50 border-slate-200"><SelectValue /></SelectTrigger>
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
                <SelectTrigger class="h-11 rounded-xl bg-slate-50 border-slate-200"><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="DSM">DSM</SelectItem>
                  <SelectItem value="ADS">ADS</SelectItem>
                  <SelectItem value="COMEX">COMEX</SelectItem>
                  <SelectItem value="GEEM">GEEM</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div class="flex flex-col space-y-1.5">
              <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Período</label>
              <Select v-model="studentToEdit.period">
                <SelectTrigger class="h-11 rounded-xl bg-slate-50 border-slate-200"><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="Matutino">Matutino</SelectItem>
                  <SelectItem value="Vespertino">Vespertino</SelectItem>
                  <SelectItem value="Noturno">Noturno</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div class="flex flex-col space-y-1.5 md:col-span-2">
              <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Contato</label>
              <Input v-model="studentToEdit.contact" class="h-11 rounded-xl bg-slate-50 border-slate-200" />
            </div>
          </div>
        </div>
        
        <DialogFooter class="border-t pt-4">
          <Button variant="ghost" @click="isEditModalOpen = false" class="rounded-xl font-bold text-slate-500">Cancelar</Button>
          <Button @click="saveEdit" class="bg-[#1A1A3A] hover:bg-[#0F0F24] rounded-xl px-10 text-white font-bold shadow-md active:scale-95 transition-all">Atualizar Cadastro</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <Dialog v-model:open="isDeleteModalOpen">
      <DialogContent class="rounded-[2.5rem] sm:max-w-[400px] border-none shadow-2xl text-center p-8">
        <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-red-100 mb-6">
          <Trash2 class="h-8 w-8 text-red-600" />
        </div>
        <DialogHeader class="mb-6">
          <DialogTitle class="text-xl font-bold text-slate-900 text-center mb-2">Confirmar Exclusão?</DialogTitle>
          <DialogDescription class="text-center text-sm leading-relaxed">
            Deseja mesmo excluir <strong>{{ studentToDelete?.name }}</strong>? Esta ação é irreversível.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter class="flex flex-row justify-center gap-3 w-full">
          <Button variant="outline" @click="isDeleteModalOpen = false" class="rounded-xl flex-1 border-slate-200 font-bold">Não</Button>
          <Button @click="confirmDelete" class="bg-red-600 hover:bg-red-700 rounded-xl flex-1 text-white font-bold shadow-md border-none">Sim, Excluir</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

  </div>
</template>

<style scoped>
.font-nunito {
  font-family: 'Nunito', sans-serif;
}

.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 20px; }
</style>