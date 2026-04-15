<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Search, Filter, Download, FileText, FileSpreadsheet } from 'lucide-vue-next'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Separator } from '@/components/ui/separator'

const route = useRoute()

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

// Estados Reactivos
const allStudents = ref<Student[]>([])
const searchQuery = ref('')
const selectedStudent = ref<Student | null>(null)
const isDialogOpen = ref(false)

// Filtros expansivos
const filters = ref({
  period: 'todos',
  course: 'todos',
  status: 'todos'
})

// Banco de dados simulado nativo
const defaultStudents: Student[] = [
  { id: 1, name: 'Leonardo Mendonça', period: 'Noturno', course: 'DSM', registration: '1460282113001', contact: 'leonardo.mendonca@fatec.sp.gov.br', avatar: 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?q=80&w=200&auto=format&fit=crop', status: 'Ativo' },
  { id: 2, name: 'Ana Clara Souza', period: 'Matutino', course: 'ADS', registration: '1460282113042', contact: 'ana.clara.souza.longemailtest@fatec.sp.gov.br', avatar: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?q=80&w=200&auto=format&fit=crop', status: 'Visitante' },
  { id: 3, name: 'Felipe Mendes', period: 'Noturno', course: 'COMEX', registration: '1460282113088', contact: 'felipe.mendes@fatec.sp.gov.br', avatar: 'https://images.unsplash.com/photo-1474511320723-9a56873867b5?q=80&w=200&auto=format&fit=crop', status: 'Inativo' },
  { id: 4, name: 'Beatriz Lima', period: 'Vespertino', course: 'GEEM', registration: '1460282113105', contact: '(13) 97766-3311', avatar: 'https://images.unsplash.com/photo-1540573133985-87b6da6d54a9?q=80&w=200&auto=format&fit=crop', status: 'Bloqueado' },
  { id: 5, name: 'Carlos Eduardo Santos', period: 'Noturno', course: 'DSM', registration: '1460282113019', contact: 'carlos.edu.santos.silva.fatec.pg@gmail.com', avatar: 'https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?q=80&w=200&auto=format&fit=crop', status: 'Ativo' },
]

// Carrega dados do LocalStorage e mescla com os padrões
const loadStudents = () => {
  const storedData = localStorage.getItem('cifa_students')
  const persistedStudents = storedData ? JSON.parse(storedData) : []
  
  // Combina os estudantes mockados com os novos cadastros
  allStudents.value = [...defaultStudents, ...persistedStudents]
}

onMounted(() => {
  loadStudents()

  // Interceptador de Rota (Abre o aluno se vier da tela de Relatórios)
  const queryId = route.query.id
  if (queryId) {
    const target = allStudents.value.find(s => s.id === Number(queryId))
    if (target) {
      selectedStudent.value = target
      return
    }
  }
  
  // Seleção Padrão
  if (allStudents.value.length > 0) {
    selectedStudent.value = allStudents.value[0]
  }
})

// Lógica de Filtragem Cruzada Completa
const filteredStudents = computed(() => {
  return allStudents.value.filter(student => {
    const matchesSearch = student.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesPeriod = filters.value.period === 'todos' || student.period === filters.value.period
    const matchesCourse = filters.value.course === 'todos' || student.course === filters.value.course
    const matchesStatus = filters.value.status === 'todos' || student.status === filters.value.status
    return matchesSearch && matchesPeriod && matchesCourse && matchesStatus
  })
})

const hasActiveFilters = computed(() => filters.value.period !== 'todos' || filters.value.course !== 'todos' || filters.value.status !== 'todos')

const clearFilters = () => {
  filters.value.period = 'todos'
  filters.value.course = 'todos'
  filters.value.status = 'todos'
}

// Estilização Dinâmica de Status
const getStatusStyle = (status: string) => {
  switch (status) {
    case 'Ativo': return 'text-emerald-600'
    case 'Inativo': return 'text-slate-500'
    case 'Bloqueado': return 'text-red-600'
    case 'Visitante': return 'text-indigo-600'
    default: return 'text-slate-900'
  }
}

// FUNÇÕES DE EXPORTAÇÃO
const exportToCSV = () => {
  if (filteredStudents.value.length === 0) return
  const headers = ['Nome', 'Status', 'Periodo', 'Curso', 'Matricula', 'Contato']
  const rows = filteredStudents.value.map(s => [`"${s.name}"`, s.status, s.period, `"${s.course}"`, s.registration, `"${s.contact}"`])
  const csvContent = [headers.join(';'), ...rows.map(r => r.join(';'))].join('\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.setAttribute('href', url)
  link.setAttribute('download', `relatorio-estudantes-cifa.csv`)
  link.click()
}

const exportToPDF = () => {
  if (filteredStudents.value.length === 0) return
  const printWindow = window.open('', '_blank')
  if (!printWindow) return

  let htmlStr = `
    <html>
      <head>
        <title>Relatório de Estudantes - CIFA</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 20px; color: #333; }
          h1 { font-size: 24px; color: #0A102E; margin-bottom: 5px; }
          p { color: #666; margin-bottom: 20px; font-size: 14px; }
          table { width: 100%; border-collapse: collapse; margin-top: 20px; }
          th, td { border: 1px solid #ddd; padding: 10px; text-align: left; font-size: 12px; }
          th { background-color: #f4f4f5; color: #0A102E; }
        </style>
      </head>
      <body>
        <h1>Relatório de Estudantes</h1>
        <p>Sistema CIFA - Emissão: ${new Date().toLocaleDateString('pt-BR')} ${new Date().toLocaleTimeString('pt-BR')}</p>
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>Status</th>
              <th>Período</th>
              <th>Curso</th>
              <th>Matrícula</th>
              <th>Contato</th>
            </tr>
          </thead>
          <tbody>
  `
  filteredStudents.value.forEach(s => {
    htmlStr += `<tr><td>${s.name}</td><td>${s.status}</td><td>${s.period}</td><td>${s.course}</td><td>${s.registration}</td><td>${s.contact}</td></tr>`
  })

  htmlStr += `</tbody></table></body></html>`
  
  printWindow.document.write(htmlStr)
  printWindow.document.close()
  printWindow.onload = () => { printWindow.focus(); printWindow.print(); }
}
</script>

<template>
  <div class="flex flex-1 flex-col min-h-0 gap-4 font-nunito">
    
    <div class="flex flex-col sm:flex-row justify-between items-center gap-4 py-1">
      <div class="flex items-center gap-3 w-full sm:w-auto">
        
        <div class="relative w-full sm:w-80">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input 
            v-model="searchQuery"
            placeholder="Pesquisar por nome..." 
            class="pl-10 h-10 border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#0A102E] bg-white w-full transition-all text-sm outline-none shadow-sm"
          />
        </div>
        
        <Dialog v-model:open="isDialogOpen">
          <DialogTrigger as-child>
            <Button variant="outline" class="h-10 px-6 border-slate-200 rounded-xl bg-white text-slate-700 hover:bg-slate-50 gap-2 shadow-sm relative">
              <Filter class="w-4 h-4" />
              Filtros
              <span v-if="hasActiveFilters" class="absolute -top-1 -right-1 flex h-3 w-3">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-3 w-3 bg-indigo-600 border border-white"></span>
              </span>
            </Button>
          </DialogTrigger>
          <DialogContent class="rounded-[2rem] sm:max-w-[425px] border-none shadow-2xl">
            <DialogHeader>
              <DialogTitle class="text-xl font-bold">Refinar Estudantes</DialogTitle>
              <DialogDescription>Selecione os critérios para filtrar a listagem.</DialogDescription>
            </DialogHeader>
            <div class="grid gap-5 py-4">
              <div class="space-y-2">
                <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest">Status</label>
                <Select v-model="filters.status">
                  <SelectTrigger class="rounded-xl h-11"><SelectValue placeholder="Selecione o status" /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="todos">Todos os Status</SelectItem>
                    <SelectItem value="Ativo">Ativo</SelectItem>
                    <SelectItem value="Inativo">Inativo</SelectItem>
                    <SelectItem value="Bloqueado">Bloqueado</SelectItem>
                    <SelectItem value="Visitante">Visitante</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div class="space-y-2">
                <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest">Período</label>
                <Select v-model="filters.period">
                  <SelectTrigger class="rounded-xl h-11"><SelectValue placeholder="Selecione o período" /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="todos">Todos os Períodos</SelectItem>
                    <SelectItem value="Matutino">Matutino</SelectItem>
                    <SelectItem value="Vespertino">Vespertino</SelectItem>
                    <SelectItem value="Noturno">Noturno</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div class="space-y-2">
                <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest">Curso</label>
                <Select v-model="filters.course">
                  <SelectTrigger class="rounded-xl h-11"><SelectValue placeholder="Selecione o curso" /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="todos">Todos os Cursos</SelectItem>
                    <SelectItem value="DSM">DSM</SelectItem>
                    <SelectItem value="ADS">ADS</SelectItem>
                    <SelectItem value="COMEX">COMEX</SelectItem>
                    <SelectItem value="GEEM">GEEM</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
            <Separator />
            <DialogFooter class="flex sm:justify-between gap-2 pt-2">
              <Button variant="ghost" @click="clearFilters" class="text-slate-500 hover:text-red-500 rounded-xl">Limpar Filtros</Button>
              <Button @click="isDialogOpen = false" class="bg-[#1A1A3A] hover:bg-[#0F0F24] rounded-xl px-8 text-white">Aplicar</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <DropdownMenu>
        <DropdownMenuTrigger as-child>
          <Button :disabled="filteredStudents.length === 0" variant="outline" class="h-10 px-6 border-slate-200 rounded-xl bg-white text-slate-700 hover:bg-slate-50 gap-2 shadow-sm disabled:opacity-50">
            <Download class="w-4 h-4" /> Exportar
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" class="w-40 rounded-xl border-none shadow-xl">
          <DropdownMenuItem @click="exportToCSV" class="cursor-pointer gap-2 font-medium text-slate-700">
            <FileSpreadsheet class="w-4 h-4 text-emerald-600" /> CSV
          </DropdownMenuItem>
          <DropdownMenuItem @click="exportToPDF" class="cursor-pointer gap-2 font-medium text-slate-700">
            <FileText class="w-4 h-4 text-red-600" /> PDF
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>

    <div class="flex flex-1 gap-6 min-h-0 pb-4">
      
      <div class="w-[360px] flex flex-col">
        <h3 class="text-[0.65rem] font-bold uppercase tracking-[0.2em] text-slate-400 mb-3 border-b border-slate-100 pb-2 ml-1">Estudantes ({{ filteredStudents.length }})</h3>
        
        <div class="flex-1 overflow-y-auto pr-2 space-y-2 custom-scrollbar">
          <div 
            v-for="student in filteredStudents" 
            :key="student.id"
            class="flex items-center justify-between p-2.5 rounded-xl transition-all border border-transparent cursor-pointer"
            :class="selectedStudent?.id === student.id ? 'bg-slate-100 border-slate-200 shadow-sm' : 'hover:bg-slate-50'"
            @click="selectedStudent = student"
          >
            <div class="flex items-center gap-3">
              <img :src="student.avatar" class="w-10 h-10 rounded-full object-cover bg-slate-200" />
              <div class="flex flex-col">
                <span class="font-bold text-slate-700 text-sm tracking-tight leading-tight">{{ student.name }}</span>
                <span class="text-[10px] font-bold uppercase tracking-wider mt-0.5" :class="getStatusStyle(student.status)">{{ student.status }}</span>
              </div>
            </div>
            <Button class="bg-[#1A1A3A] hover:bg-[#0F0F24] text-white rounded-lg px-4 h-8 text-xs font-bold transition-all active:scale-95">
              Acessar
            </Button>
          </div>
          <div v-if="filteredStudents.length === 0" class="text-center py-10 text-slate-400 text-xs italic">Nenhum resultado encontrado.</div>
        </div>
      </div>

      <div class="flex-1 bg-[#EBEBEB] rounded-[2.5rem] p-8 flex flex-col shadow-inner overflow-y-auto custom-scrollbar">
        <template v-if="selectedStudent">
          <div class="flex items-center gap-6 mb-10 animate-in fade-in slide-in-from-bottom-2 duration-300">
            <img :src="selectedStudent.avatar" class="w-24 h-24 rounded-full object-cover bg-slate-200 shadow-lg border-4 border-white/50" />
            <div class="flex flex-col">
              <h2 class="text-3xl font-extrabold text-slate-900 tracking-tighter leading-tight">{{ selectedStudent.name }}</h2>
              <span class="text-sm font-bold uppercase tracking-widest mt-1" :class="getStatusStyle(selectedStudent.status)">
                Status: {{ selectedStudent.status }}
              </span>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-x-10 gap-y-10 max-w-2xl animate-in fade-in slide-in-from-bottom-4 duration-500">
            
            <div class="flex flex-col border-l-2 border-slate-300 pl-5 py-1">
              <span class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest mb-1">Período Letivo</span>
              <span class="text-lg font-bold text-slate-800">{{ selectedStudent.period }}</span>
            </div>

            <div class="flex flex-col border-l-2 border-slate-300 pl-5 py-1">
              <span class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest mb-1">Matrícula (RA)</span>
              <span class="text-lg font-bold text-slate-800 font-mono">{{ selectedStudent.registration }}</span>
            </div>
            
            <div class="flex flex-col border-l-2 border-slate-300 pl-5 py-1 col-span-2">
              <span class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest mb-1">Curso / Formação</span>
              <span class="text-lg font-bold text-slate-800">{{ selectedStudent.course }}</span>
            </div>
            
            <div class="flex flex-col border-l-2 border-slate-300 pl-5 py-1 col-span-2">
              <span class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest mb-1">Informações de Contato</span>
              <span class="text-lg font-bold text-slate-800 break-all leading-tight">{{ selectedStudent.contact }}</span>
            </div>
          </div>
        </template>
        
        <div v-else class="flex-1 flex items-center justify-center text-slate-400 text-sm italic">
          Selecione um estudante para visualizar a ficha técnica.
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.font-nunito {
  font-family: 'Nunito', sans-serif;
}

.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 20px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background-color: #94a3b8; }
</style>