<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { 
  Search, 
  Filter, 
  Download, 
  FileText, 
  FileSpreadsheet,
  GraduationCap,
  CalendarDays,
  Contact2,
  Hash,
  ChevronRight,
  Users2,
  Loader2 
} from 'lucide-vue-next'
import {
  Dialog,
  DialogContent,
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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Badge } from '@/components/ui/badge'

const route = useRoute()
const router = useRouter()

const API_BASE = 'https://reply-imprint-skier.ngrok-free.dev'

// ID alterado para suportar as chaves criptografadas da sua API
interface Student {
  id: string | number
  name: string
  period: string
  course: string
  registration: string
  contact: string
  avatar: string
  status: 'Ativo' | 'Inativo' | 'Bloqueado' | 'Visitante'
  semester: number
}

const allStudents = ref<Student[]>([])
const searchQuery = ref('')
const selectedStudent = ref<Student | null>(null)
const isDetailModalOpen = ref(false)
const isFilterDialogOpen = ref(false)
const isLoadingData = ref(true) 

type TabType = 'todos' | 1 | 2 | 3 | 4 | 5 | 6 | 'inativos'
const activeTab = ref<TabType>('todos')

const tabs: { id: TabType, label: string }[] = [
  { id: 'todos', label: 'Todos' },
  { id: 1, label: '1º Semestre' },
  { id: 2, label: '2º Semestre' },
  { id: 3, label: '3º Semestre' },
  { id: 4, label: '4º Semestre' },
  { id: 5, label: '5º Semestre' },
  { id: 6, label: '6º Semestre' },
  { id: 'inativos', label: 'Inativos' }
]

const filters = ref({
  period: 'todos',
  course: 'todos',
  status: 'todos'
})

// ==========================================
// ADAPTADOR CIFA PRO (Lê a sua API nativamente)
// ==========================================
const fetchAPIStudents = async (): Promise<Student[]> => {
  try {
    const response = await fetch(`${API_BASE}/alunos/verAlunos`, {
      headers: { 
        'ngrok-skip-browser-warning': 'true',
        'Content-Type': 'application/json'
      }
    })
    if (!response.ok) throw new Error('Falha na comunicação com a API')
    
    const responseData = await response.json()
    const dataArray = Array.isArray(responseData) ? responseData : (responseData.alunos || responseData.data || [])
    
    return dataArray.map((aluno: any, index: number) => {
      const nomeStr = aluno.nome || 'Aluno Não Identificado'
      const fotoAPI = aluno.imagem_url || '';
      
      // Inteligência de Descompactação da Turma (Ex: "DSM_2026_1_VES")
      const turmaParts = aluno.id_turma ? aluno.id_turma.split('_') : [];
      const courseStr = turmaParts[0] || 'Indefinido';
      let periodStr = 'Noturno'; // Padrão
      if (turmaParts.length >= 4) {
        if (turmaParts[3] === 'VES') periodStr = 'Vespertino';
        else if (turmaParts[3] === 'MAT') periodStr = 'Matutino';
        else if (turmaParts[3] === 'NOT') periodStr = 'Noturno';
      }

      return {
        id: aluno.id || Date.now() + index,
        name: nomeStr,
        period: periodStr,
        course: courseStr,
        registration: String(aluno.ra || `1460282113${index.toString().padStart(3, '0')}`),
        contact: aluno.email_institucional || aluno.email_pessoal || 'sem-email@fatec.sp.gov.br',
        avatar: fotoAPI || `https://api.dicebear.com/8.x/avataaars/svg?seed=${encodeURIComponent(nomeStr)}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`,
        status: aluno.status_ativo ? 'Ativo' : 'Inativo',
        semester: Number(aluno.ciclo_atual || 1)
      }
    })
  } catch (error) {
    console.error("Erro ao buscar dados externos:", error)
    return [] 
  }
}

const loadStudents = async () => {
  isLoadingData.value = true
  const apiStudents = await fetchAPIStudents()
  const storedData = localStorage.getItem('cifa_students')
  const persistedStudents = storedData ? JSON.parse(storedData) : []
  
  const formattedPersisted = persistedStudents.map((s: any) => ({ 
    ...s, 
    semester: s.semester || 1,
    avatar: s.avatar || s.imagem_url || `https://api.dicebear.com/8.x/avataaars/svg?seed=${encodeURIComponent(s.name)}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`
  }))
  
  const deletedIds: string[] = JSON.parse(localStorage.getItem('cifa_deleted_ids') || '[]').map(String)
  const validApiStudents = apiStudents.filter(s => !deletedIds.includes(String(s.id)))

  allStudents.value = [...validApiStudents, ...formattedPersisted].sort((a, b) => a.name.localeCompare(b.name))
  isLoadingData.value = false
  checkUrlForStudent()
}

const checkUrlForStudent = () => {
  const queryId = route.query.id
  if (queryId) {
    const student = allStudents.value.find(s => String(s.id) === String(queryId))
    if (student) {
      selectedStudent.value = student
      isDetailModalOpen.value = true
    }
  }
}

onMounted(() => {
  loadStudents()
})

watch(() => route.query.id, () => {
  checkUrlForStudent()
})

const filteredStudents = computed(() => {
  return allStudents.value.filter(student => {
    const matchesSearch = student.name.toLowerCase().includes(searchQuery.value.toLowerCase()) || 
                          student.registration.includes(searchQuery.value)
    
    let matchesTab = true
    if (activeTab.value === 'inativos') matchesTab = student.status === 'Inativo'
    else if (typeof activeTab.value === 'number') matchesTab = student.semester === activeTab.value

    const matchesPeriod = filters.value.period === 'todos' || student.period === filters.value.period
    const matchesCourse = filters.value.course === 'todos' || student.course === filters.value.course
    const matchesStatus = filters.value.status === 'todos' || student.status === filters.value.status
    
    return matchesSearch && matchesTab && matchesPeriod && matchesCourse && matchesStatus
  })
})

const getCount = (tabId: TabType) => {
  if (tabId === 'todos') return allStudents.value.length
  if (tabId === 'inativos') return allStudents.value.filter(s => s.status === 'Inativo').length
  return allStudents.value.filter(s => s.semester === tabId).length
}

const openStudentDetail = (student: Student) => {
  selectedStudent.value = student
  isDetailModalOpen.value = true
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

const exportToCSV = () => {
  if (filteredStudents.value.length === 0) return
  const headers = ['Nome', 'Status', 'Semestre', 'Periodo', 'Curso', 'Matricula', 'Contato']
  const rows = filteredStudents.value.map(s => [
    `"${s.name}"`, 
    s.status, 
    `${s.semester}º`, 
    s.period, 
    `"${s.course}"`, 
    s.registration, 
    `"${s.contact}"`
  ])
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
              <th>Semestre</th>
              <th>Período</th>
              <th>Curso</th>
              <th>Matrícula</th>
              <th>Contato</th>
            </tr>
          </thead>
          <tbody>
  `
  filteredStudents.value.forEach(s => {
    htmlStr += `
      <tr>
        <td>${s.name}</td>
        <td>${s.status}</td>
        <td>${s.semester}º</td>
        <td>${s.period}</td>
        <td>${s.course}</td>
        <td>${s.registration}</td>
        <td>${s.contact}</td>
      </tr>
    `
  })

  htmlStr += `</tbody></table></body></html>`
  printWindow.document.write(htmlStr)
  printWindow.document.close()
  printWindow.onload = () => { printWindow.focus(); printWindow.print(); }
}
</script>

<template>
  <div class="flex flex-1 flex-col min-h-0 gap-2 font-poppins">
    
    <div class="flex flex-col sm:flex-row justify-between items-center gap-4 py-2 shrink-0">
      <div class="flex items-center gap-3 w-full sm:w-auto">
        <div class="relative w-full sm:w-96">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input 
            v-model="searchQuery"
            placeholder="Pesquisar aluno por nome ou RA..." 
            class="pl-10 h-11 border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#0A102E] bg-white w-full transition-all text-sm outline-none shadow-sm"
          />
        </div>
        
        <Button variant="outline" @click="isFilterDialogOpen = true" class="h-11 px-5 border-slate-200 rounded-xl bg-white text-slate-700 hover:bg-slate-50 gap-2 shadow-sm shrink-0 font-semibold">
          <Filter class="w-4 h-4" />
          <span class="hidden sm:inline">Filtros</span>
        </Button>
      </div>

      <DropdownMenu>
        <DropdownMenuTrigger as-child>
          <Button 
            :disabled="filteredStudents.length === 0 || isLoadingData"
            variant="outline" 
            class="h-11 px-6 border-slate-200 rounded-xl bg-white text-slate-700 hover:bg-slate-50 gap-2 shadow-sm shrink-0 font-semibold disabled:opacity-50"
          >
            <Download class="w-4 h-4" /> 
            <span>Exportar Relatório</span>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" class="w-44 rounded-xl border-none shadow-xl font-poppins">
          <DropdownMenuItem @click="exportToCSV" class="cursor-pointer gap-2 font-medium text-slate-700">
            <FileSpreadsheet class="w-4 h-4 text-emerald-600" /> Planilha (CSV)
          </DropdownMenuItem>
          <DropdownMenuItem @click="exportToPDF" class="cursor-pointer gap-2 font-medium text-slate-700">
            <FileText class="w-4 h-4 text-red-600" /> Documento (PDF)
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>

    <div class="w-full flex shrink-0 border-b border-slate-200 mt-2 mb-4 bg-slate-50/50 rounded-t-xl overflow-hidden">
      <button
        v-for="tab in tabs" 
        :key="tab.id"
        @click="activeTab = tab.id"
        class="flex-1 min-w-0 pb-3 px-1 sm:px-2 pt-3 text-[10px] sm:text-xs lg:text-sm font-semibold transition-all duration-300 relative flex justify-center items-center gap-1.5 sm:gap-2 group overflow-hidden"
        :class="activeTab === tab.id ? 'text-[#0A102E] bg-white' : 'text-slate-500 hover:text-slate-800 hover:bg-white/50'"
      >
        <span class="truncate">{{ tab.label }}</span>
        <span 
          class="px-1.5 sm:px-2 py-0.5 rounded-full text-[9px] sm:text-[10px] font-bold transition-colors shrink-0"
          :class="activeTab === tab.id ? 'bg-[#0A102E] text-white' : 'bg-slate-200 text-slate-500 group-hover:bg-slate-300'"
        >
          {{ getCount(tab.id) }}
        </span>
        <div 
          v-if="activeTab === tab.id" 
          class="absolute bottom-0 left-0 w-full h-[3px] bg-[#0A102E] rounded-t-full"
        ></div>
      </button>
    </div>

    <div class="flex-1 overflow-y-auto custom-scrollbar bg-white border border-slate-200 rounded-[2rem] p-4 shadow-sm relative">
      
      <div v-auto-animate class="flex flex-col gap-3">
        
        <template v-if="isLoadingData">
          <div v-for="n in 6" :key="`skeleton-${n}`" class="flex flex-col sm:flex-row sm:items-center justify-between p-4 sm:p-5 rounded-2xl border border-slate-100 bg-white">
            <div class="flex items-center gap-4 w-full">
              <div class="w-12 h-12 rounded-full bg-slate-200 animate-pulse shrink-0"></div>
              <div class="flex flex-col gap-2 w-full max-w-[200px]">
                <div class="h-4 bg-slate-200 rounded animate-pulse w-3/4"></div>
                <div class="h-3 bg-slate-200 rounded animate-pulse w-full"></div>
              </div>
            </div>
            <div class="flex items-center justify-between sm:justify-end gap-6 mt-4 sm:mt-0 w-full sm:w-auto border-t sm:border-none pt-4 sm:pt-0 border-slate-100">
              <div class="flex items-center gap-3">
                <div class="w-20 h-3 bg-slate-200 rounded animate-pulse hidden sm:block"></div>
                <div class="w-14 h-5 bg-slate-200 rounded animate-pulse"></div>
              </div>
              <div class="w-8 h-8 rounded-full bg-slate-200 animate-pulse shrink-0"></div>
            </div>
          </div>
        </template>

        <template v-else>
          <div v-for="student in filteredStudents" :key="student.id" class="flex flex-col sm:flex-row sm:items-center justify-between p-4 sm:p-5 rounded-2xl border border-slate-100 hover:border-slate-300 hover:shadow-md transition-all cursor-pointer bg-slate-50/50 hover:bg-white group" @click="openStudentDetail(student)">
            <div class="flex items-center gap-4">
              <img 
                :src="student.avatar" 
                class="w-12 h-12 rounded-full object-cover bg-slate-100 shrink-0 border border-slate-200 group-hover:border-indigo-100 transition-colors" 
              />
              <div class="flex flex-col">
                <span class="font-bold text-slate-800 text-base tracking-tight group-hover:text-indigo-950 transition-colors">{{ student.name }}</span>
                <div class="flex items-center gap-2 mt-1 text-xs text-slate-500 font-medium">
                  <span>RA: <span class="font-mono text-slate-700">{{ student.registration }}</span></span>
                  <span class="w-1 h-1 rounded-full bg-slate-300"></span>
                  <span>{{ student.course }} ({{ student.period }})</span>
                </div>
              </div>
            </div>

            <div class="flex items-center justify-between sm:justify-end gap-6 mt-4 sm:mt-0 w-full sm:w-auto border-t sm:border-none pt-4 sm:pt-0 border-slate-100">
              <div class="flex items-center gap-3">
                <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">
                  {{ student.semester }}º Semestre
                </span>
                <Badge :class="getStatusStyle(student.status)" class="border-none font-bold text-[10px] uppercase shadow-none px-3 py-1">
                  {{ student.status }}
                </Badge>
              </div>
              <div class="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-400 group-hover:bg-[#0A102E] group-hover:text-white transition-colors shrink-0">
                <ChevronRight class="w-4 h-4" />
              </div>
            </div>
          </div>

          <div v-if="filteredStudents.length === 0" class="flex flex-col items-center justify-center py-16 text-slate-400">
            <Users2 class="w-12 h-12 mb-4 opacity-20" />
            <span class="text-sm font-medium italic">Nenhum aluno encontrado para os filtros e aba selecionados.</span>
          </div>
        </template>

      </div>
    </div>

    <Dialog v-model:open="isDetailModalOpen">
      <DialogContent class="rounded-[2.5rem] sm:max-w-[500px] border-none shadow-2xl p-0 overflow-hidden font-poppins">
        <div class="bg-[#0A102E] p-8 text-center relative overflow-hidden">
          <div class="absolute -top-10 -left-10 w-32 h-32 bg-indigo-500/20 rounded-full blur-2xl"></div>
          <div class="absolute -bottom-10 -right-10 w-32 h-32 bg-purple-500/20 rounded-full blur-2xl"></div>
          
          <div class="relative z-10 flex flex-col items-center">
            <img :src="selectedStudent?.avatar" class="w-28 h-28 rounded-full object-cover border-4 border-white/10 bg-slate-100 shadow-2xl mb-4" />
            <h2 class="text-2xl font-bold text-white tracking-tight leading-tight">{{ selectedStudent?.name }}</h2>
            <Badge class="mt-2 bg-white/10 text-white border-none font-bold uppercase text-[10px] tracking-widest px-4 py-1">
              {{ selectedStudent?.registration }}
            </Badge>
          </div>
        </div>

        <div class="p-8 grid grid-cols-1 sm:grid-cols-2 gap-6 bg-white">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-2xl bg-slate-50 flex items-center justify-center border border-slate-100">
              <GraduationCap class="w-5 h-5 text-slate-400" />
            </div>
            <div class="flex flex-col">
              <span class="text-[9px] font-black uppercase text-slate-400 tracking-widest leading-none mb-1">Semestre Atual</span>
              <span class="font-bold text-slate-800">{{ selectedStudent?.semester }}º Semestre</span>
            </div>
          </div>

          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-2xl bg-slate-50 flex items-center justify-center border border-slate-100">
              <CalendarDays class="w-5 h-5 text-slate-400" />
            </div>
            <div class="flex flex-col">
              <span class="text-[9px] font-black uppercase text-slate-400 tracking-widest leading-none mb-1">Período</span>
              <span class="font-bold text-slate-800">{{ selectedStudent?.period }}</span>
            </div>
          </div>

          <div class="flex items-center gap-3 sm:col-span-2">
            <div class="w-10 h-10 rounded-2xl bg-slate-50 flex items-center justify-center border border-slate-100">
              <Hash class="w-5 h-5 text-slate-400" />
            </div>
            <div class="flex flex-col">
              <span class="text-[9px] font-black uppercase text-slate-400 tracking-widest leading-none mb-1">Formação</span>
              <span class="font-bold text-slate-800">{{ selectedStudent?.course }}</span>
            </div>
          </div>

          <div class="flex items-center gap-3 sm:col-span-2">
            <div class="w-10 h-10 rounded-2xl bg-slate-50 flex items-center justify-center border border-slate-100">
              <Contact2 class="w-5 h-5 text-slate-400" />
            </div>
            <div class="flex flex-col">
              <span class="text-[9px] font-black uppercase text-slate-400 tracking-widest leading-none mb-1">Contato</span>
              <span class="font-bold text-slate-800 break-all">{{ selectedStudent?.contact }}</span>
            </div>
          </div>
        </div>

        <DialogFooter class="p-6 pt-0 bg-white">
          <Button @click="isDetailModalOpen = false" class="w-full bg-[#0A102E] hover:bg-slate-800 text-white rounded-2xl h-12 font-bold transition-all active:scale-95 shadow-lg">
            Fechar Detalhes
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <Dialog v-model:open="isFilterDialogOpen">
      <DialogContent class="rounded-[2.5rem] max-w-[90vw] sm:max-w-[425px] border-none shadow-2xl font-poppins">
        <DialogHeader>
          <DialogTitle class="font-bold">Filtros Avançados</DialogTitle>
        </DialogHeader>
        <div class="grid gap-5 py-4">
          <div class="space-y-1">
            <label class="text-[10px] font-bold uppercase text-slate-400 ml-1">Status</label>
            <Select v-model="filters.status">
              <SelectTrigger class="rounded-xl h-11"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="todos">Todos</SelectItem>
                <SelectItem value="Ativo">Ativo</SelectItem>
                <SelectItem value="Inativo">Inativo</SelectItem>
                <SelectItem value="Bloqueado">Bloqueado</SelectItem>
                <SelectItem value="Visitante">Visitante</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-1">
            <label class="text-[10px] font-bold uppercase text-slate-400 ml-1">Curso</label>
            <Select v-model="filters.course">
              <SelectTrigger class="rounded-xl h-11"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="todos">Todos</SelectItem>
                <SelectItem value="DSM">DSM</SelectItem>
                <SelectItem value="ADS">ADS</SelectItem>
                <SelectItem value="COMEX">COMEX</SelectItem>
                <SelectItem value="GEEM">GEEM</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter class="gap-2">
          <Button variant="ghost" @click="isFilterDialogOpen = false" class="rounded-xl">Cancelar</Button>
          <Button @click="isFilterDialogOpen = false" class="bg-[#0A102E] text-white rounded-xl px-8">Aplicar</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

  </div>
</template>

<style scoped>
.font-poppins {
  font-family: 'Poppins', sans-serif;
}

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
</style>