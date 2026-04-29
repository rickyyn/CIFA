<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, 
  Filter, 
  Download, 
  ChevronLeft, 
  ChevronRight, 
  FileText,
  FileSpreadsheet,
  ArrowUpDown,
  Clock,
  User,
  ShieldCheck,
  ShieldAlert,
  ArrowRightCircle,
  ArrowLeftCircle,
  Loader2
} from 'lucide-vue-next'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'

const router = useRouter()

interface AccessLog {
  id: string
  timestamp: Date
  studentId: number
  studentName: string
  avatar: string
  period: string
  course: string
  status: 'Autorizado' | 'Negado'
  type: 'Entrada' | 'Saída'
}

// Estados
const searchQuery = ref('')
const isFilterDialogOpen = ref(false)
const isDetailModalOpen = ref(false)
const selectedLog = ref<AccessLog | null>(null)
const currentPage = ref(1)
const itemsPerPage = 8
const isLoadingData = ref(true)

// Configuração de Abas Formais
type TabType = 'todos' | 'hoje' | 'semana' | 'entradas' | 'saidas' | 'negados'
const activeTab = ref<TabType>('todos')

const tabs: { id: TabType, label: string }[] = [
  { id: 'todos', label: 'Histórico Geral' },
  { id: 'hoje', label: 'Hoje' },
  { id: 'semana', label: 'Últimos 7 Dias' },
  { id: 'entradas', label: 'Apenas Entradas' },
  { id: 'saidas', label: 'Apenas Saídas' },
  { id: 'negados', label: 'Acessos Negados' }
]

const filters = ref({
  period: 'todos',
  course: 'todos'
})

const allLogs = ref<AccessLog[]>([])

// ============================================================
// INTEGRAÇÃO DE API E GERAÇÃO DINÂMICA DE LOGS
// ============================================================
const fetchAndGenerateLogs = async () => {
  isLoadingData.value = true
  try {
    const response = await fetch('https://jsonplaceholder.typicode.com/users')
    const users = await response.json()
    
    // Mapeia os estudantes para garantir consistência com a tela de Estudantes
    const students = users.map((user: any) => ({
      id: user.id,
      name: user.name,
      period: user.id % 2 === 0 ? 'Noturno' : 'Matutino',
      course: user.id % 3 === 0 ? 'DSM' : (user.id % 2 === 0 ? 'ADS' : 'COMEX'),
      avatar: `https://api.dicebear.com/8.x/avataaars/svg?seed=${encodeURIComponent(user.name)}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`
    }))

    // Gera 25 logs aleatórios baseados nesses estudantes
    const generatedLogs: AccessLog[] = []
    const now = new Date()

    for (let i = 1; i <= 25; i++) {
      const student = students[Math.floor(Math.random() * students.length)]
      const daysAgo = Math.floor(Math.random() * 10)
      const hoursAgo = Math.floor(Math.random() * 24)
      const logDate = new Date(now.getTime() - (daysAgo * 24 * 60 * 60 * 1000) - (hoursAgo * 60 * 60 * 1000))

      generatedLogs.push({
        id: `LOG-${i.toString().padStart(3, '0')}`,
        timestamp: logDate,
        studentId: student.id,
        studentName: student.name,
        avatar: student.avatar,
        period: student.period,
        course: student.course,
        status: Math.random() > 0.15 ? 'Autorizado' : 'Negado',
        type: Math.random() > 0.5 ? 'Entrada' : 'Saída'
      })
    }

    // Ordena por data (mais recente primeiro)
    allLogs.value = generatedLogs.sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime())
    
  } catch (error) {
    console.error("Erro ao sincronizar relatórios:", error)
  } finally {
    isLoadingData.value = false
  }
}

onMounted(() => {
  fetchAndGenerateLogs()
})

const filteredLogs = computed(() => {
  return allLogs.value.filter(log => {
    const matchesSearch = log.studentName.toLowerCase().includes(searchQuery.value.toLowerCase()) || 
                          log.id.toLowerCase().includes(searchQuery.value.toLowerCase())
    
    let matchesTab = true
    const logDate = new Date(log.timestamp)
    const today = new Date()

    if (activeTab.value === 'hoje') {
      matchesTab = logDate.toDateString() === today.toDateString()
    } else if (activeTab.value === 'semana') {
      const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
      matchesTab = logDate >= weekAgo
    } else if (activeTab.value === 'entradas') {
      matchesTab = log.type === 'Entrada'
    } else if (activeTab.value === 'saidas') {
      matchesTab = log.type === 'Saída'
    } else if (activeTab.value === 'negados') {
      matchesTab = log.status === 'Negado'
    }

    const matchesPeriod = filters.value.period === 'todos' || log.period === filters.value.period
    const matchesCourse = filters.value.course === 'todos' || log.course === filters.value.course

    return matchesSearch && matchesTab && matchesPeriod && matchesCourse
  })
})

watch([activeTab, searchQuery, filters], () => {
  currentPage.value = 1
}, { deep: true })

const totalPages = computed(() => Math.ceil(filteredLogs.value.length / itemsPerPage) || 1)
const paginatedLogs = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredLogs.value.slice(start, start + itemsPerPage)
})

const getCount = (tabId: TabType) => {
  if (tabId === 'todos') return allLogs.value.length
  const today = new Date()
  return allLogs.value.filter(log => {
    const logDate = new Date(log.timestamp)
    if (tabId === 'hoje') return logDate.toDateString() === today.toDateString()
    if (tabId === 'semana') return logDate >= new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
    if (tabId === 'entradas') return log.type === 'Entrada'
    if (tabId === 'saidas') return log.type === 'Saída'
    if (tabId === 'negados') return log.status === 'Negado'
    return true
  }).length
}

const formatDate = (date: Date) => {
  return date.toLocaleDateString('pt-BR') + ' - ' + date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

const openLogDetail = (log: AccessLog) => {
  selectedLog.value = log
  isDetailModalOpen.value = true
}

const goToStudentProfile = (studentId: number) => {
  isDetailModalOpen.value = false
  router.push({ path: '/admin/estudantes', query: { id: studentId } })
}

const exportToCSV = () => {
  if (filteredLogs.value.length === 0) return
  const headers = ['ID Registro', 'Data', 'Estudante', 'Periodo', 'Curso', 'Tipo', 'Status']
  const rows = filteredLogs.value.map(l => [l.id, formatDate(l.timestamp), `"${l.studentName}"`, l.period, l.course, l.type, l.status])
  const csv = [headers.join(';'), ...rows.map(r => r.join(';'))].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a'); link.href = URL.createObjectURL(blob); link.download = 'relatorio_acessos_cifa.csv'; link.click()
}

const exportToPDF = () => {
  if (filteredLogs.value.length === 0) return
  const printWindow = window.open('', '_blank')
  if (!printWindow) return
  let html = `<html><head><style>body{font-family:sans-serif;} table{width:100%;border-collapse:collapse;} th,td{border:1px solid #ddd;padding:8px;font-size:11px;text-align:left;} th{background-color:#f4f4f5;}</style></head><body>`
  html += `<h2>Relatório de Acessos - CIFA</h2><p>Emitido em: ${new Date().toLocaleDateString('pt-BR')} ${new Date().toLocaleTimeString('pt-BR')}</p><table><tr><th>ID</th><th>Data/Hora</th><th>Estudante</th><th>Curso</th><th>Tipo</th><th>Status</th></tr>`
  filteredLogs.value.forEach(l => { html += `<tr><td>${l.id}</td><td>${formatDate(l.timestamp)}</td><td>${l.studentName}</td><td>${l.course}</td><td>${l.type}</td><td>${l.status}</td></tr>` })
  html += `</table></body></html>`
  printWindow.document.write(html); printWindow.document.close(); printWindow.print()
}
</script>

<template>
  <div class="flex flex-col h-full space-y-2 font-poppins min-h-0">
    
    <div class="flex flex-col sm:flex-row justify-between items-center gap-4 py-2 shrink-0">
      <div class="flex items-center gap-3 w-full sm:w-auto">
        <div class="relative w-full sm:w-96">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input 
            v-model="searchQuery"
            placeholder="Pesquisar por ID do registro ou aluno..." 
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
            :disabled="filteredLogs.length === 0 || isLoadingData" 
            variant="outline" 
            class="h-11 px-6 border-slate-200 rounded-xl bg-white text-slate-700 hover:bg-slate-50 gap-2 shadow-sm disabled:opacity-50 shrink-0 font-semibold"
          >
            <Download class="w-4 h-4" /> 
            <span>Exportar Relatório</span>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" class="w-44 rounded-xl border-none shadow-xl font-poppins">
          <DropdownMenuItem @click="exportToCSV" class="cursor-pointer gap-2 font-medium">
            <FileSpreadsheet class="w-4 h-4 text-emerald-600" /> Planilha (CSV)
          </DropdownMenuItem>
          <DropdownMenuItem @click="exportToPDF" class="cursor-pointer gap-2 font-medium">
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
        <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 w-full h-[3px] bg-[#0A102E] rounded-t-full"></div>
      </button>
    </div>

    <div class="flex-1 bg-white border border-slate-200 rounded-[2rem] shadow-sm overflow-hidden flex flex-col min-h-0 relative">
      
      <div v-if="isLoadingData" class="absolute inset-0 flex flex-col items-center justify-center bg-white/80 backdrop-blur-sm z-10">
        <Loader2 class="w-10 h-10 animate-spin text-[#0A102E] mb-4" />
        <span class="text-sm font-bold text-slate-500 uppercase tracking-widest">Sincronizando Relatórios...</span>
      </div>

      <div class="flex-1 overflow-x-auto overflow-y-auto custom-scrollbar">
        <Table class="min-w-[800px] w-full">
          <TableHeader class="bg-slate-50/80 sticky top-0 z-10 backdrop-blur-sm">
            <TableRow class="border-b-slate-200">
              <TableHead class="text-slate-600 font-bold px-6 h-12">Data & Tempo <ArrowUpDown class="w-3 h-3 inline ml-1" /></TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Usuário Registado</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Período Letivo</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12 text-center">Tipo de Fluxo</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Status de Acesso</TableHead>
            </TableRow>
          </TableHeader>
          
          <TableBody v-auto-animate>
            <template v-if="paginatedLogs.length > 0">
              <TableRow 
                v-for="log in paginatedLogs" 
                :key="log.id" 
                @click="openLogDetail(log)"
                class="hover:bg-slate-50 transition-colors border-b-slate-100 group cursor-pointer"
              >
                <TableCell class="py-4 px-6 font-mono text-[11px] text-slate-500 whitespace-nowrap">{{ formatDate(log.timestamp) }}</TableCell>
                <TableCell class="py-4 px-6 whitespace-nowrap">
                  <div class="flex items-center gap-3">
                    <img :src="log.avatar" class="w-9 h-9 rounded-full object-cover bg-slate-100 border border-slate-200 group-hover:border-indigo-200 transition-colors" />
                    <div class="flex flex-col">
                      <span class="font-semibold text-slate-800 group-hover:text-indigo-600 transition-colors">{{ log.studentName }}</span>
                      <span class="text-[9px] uppercase tracking-widest text-slate-400 font-bold mt-0.5">{{ log.course }}</span>
                    </div>
                  </div>
                </TableCell>
                <TableCell class="py-4 px-6 text-sm text-slate-600 font-medium">{{ log.period }}</TableCell>
                <TableCell class="py-4 px-6 text-sm font-semibold text-center">
                  <div class="flex items-center justify-center gap-1.5" :class="log.type === 'Entrada' ? 'text-blue-600' : 'text-amber-600'">
                    <ArrowRightCircle v-if="log.type === 'Entrada'" class="w-4 h-4" />
                    <ArrowLeftCircle v-else class="w-4 h-4" />
                    {{ log.type }}
                  </div>
                </TableCell>
                <TableCell class="py-4 px-6">
                  <Badge :class="log.status === 'Autorizado' ? 'bg-emerald-100 text-emerald-700 border-none px-3' : 'bg-red-100 text-red-700 border-none px-3'" class="font-bold text-[10px] uppercase shadow-none">{{ log.status }}</Badge>
                </TableCell>
              </TableRow>
            </template>
            <template v-else-if="!isLoadingData">
              <TableRow><TableCell colspan="5" class="h-64 text-center text-slate-400 italic font-medium">Nenhum registro de acesso encontrado.</TableCell></TableRow>
            </template>
          </TableBody>
        </Table>
      </div>

      <div class="bg-slate-50/50 border-t border-slate-200 p-4 px-8 flex items-center justify-between text-sm shrink-0">
        <span class="text-slate-500 font-medium">Página {{ currentPage }} de {{ totalPages }}</span>
        <div class="flex items-center gap-2">
          <Button @click="currentPage--" :disabled="currentPage === 1" variant="outline" size="sm" class="rounded-xl h-9 w-9 p-0 bg-white shadow-sm"><ChevronLeft class="w-4 h-4" /></Button>
          <Button @click="currentPage++" :disabled="currentPage === totalPages || filteredLogs.length === 0" variant="outline" size="sm" class="rounded-xl h-9 w-9 p-0 bg-white shadow-sm"><ChevronRight class="w-4 h-4" /></Button>
        </div>
      </div>
    </div>

    <Dialog v-model:open="isDetailModalOpen">
      <DialogContent class="rounded-[2.5rem] sm:max-w-[450px] border-none shadow-2xl p-0 overflow-hidden font-poppins">
        <div class="bg-slate-50 p-8 flex flex-col items-center justify-center border-b border-slate-200 relative">
          <div class="absolute top-4 right-6 bg-white px-3 py-1 rounded-full shadow-sm font-mono text-[10px] font-bold text-slate-400 tracking-widest border border-slate-100">{{ selectedLog?.id }}</div>
          <div class="relative mt-4 mb-4">
            <img :src="selectedLog?.avatar" class="w-24 h-24 rounded-full object-cover border-4 border-white shadow-md bg-white" />
            <div class="absolute bottom-0 -right-2 p-1.5 rounded-full text-white shadow-lg border-2 border-white" :class="selectedLog?.status === 'Autorizado' ? 'bg-emerald-500' : 'bg-red-500'">
              <ShieldCheck v-if="selectedLog?.status === 'Autorizado'" class="w-5 h-5" />
              <ShieldAlert v-else class="w-5 h-5" />
            </div>
          </div>
          <h2 class="text-xl font-bold text-slate-900 tracking-tight text-center">{{ selectedLog?.studentName }}</h2>
          <p class="text-[10px] font-bold uppercase tracking-widest text-slate-400 mt-1">{{ selectedLog?.course }} • {{ selectedLog?.period }}</p>
        </div>
        <div class="p-8 grid grid-cols-2 gap-y-6 gap-x-4 bg-white">
          <div class="flex flex-col"><span class="text-[9px] font-black uppercase text-slate-400 tracking-widest flex items-center gap-1.5 mb-1"><Clock class="w-3 h-3" /> Horário</span><span class="font-bold text-slate-800 text-sm">{{ selectedLog ? formatDate(selectedLog.timestamp) : '' }}</span></div>
          <div class="flex flex-col border-l border-slate-100 pl-4"><span class="text-[9px] font-black uppercase text-slate-400 tracking-widest flex items-center gap-1.5 mb-1"><ArrowUpDown class="w-3 h-3" /> Fluxo</span><span class="font-bold text-sm" :class="selectedLog?.type === 'Entrada' ? 'text-blue-600' : 'text-amber-600'">{{ selectedLog?.type }} Identificada</span></div>
        </div>
        <DialogFooter class="p-6 pt-0 bg-white gap-2 flex-col sm:flex-row">
          <Button variant="outline" @click="isDetailModalOpen = false" class="w-full rounded-2xl h-12 font-bold border-slate-200 text-slate-600 hover:bg-slate-50">Voltar</Button>
          <Button @click="selectedLog && goToStudentProfile(selectedLog.studentId)" class="w-full bg-[#1A1A3A] hover:bg-[#0F0F24] text-white rounded-2xl h-12 font-bold transition-all shadow-md gap-2"><User class="w-4 h-4" /> Ver Perfil</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <Dialog v-model:open="isFilterDialogOpen">
      <DialogContent class="rounded-[2.5rem] w-[95vw] sm:max-w-[450px] shadow-2xl border-none font-poppins">
        <DialogHeader><DialogTitle class="text-xl font-bold">Filtros Avançados</DialogTitle></DialogHeader>
        <div class="grid gap-5 py-4">
          <div class="space-y-2"><label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Curso / Área</label>
            <Select v-model="filters.course"><SelectTrigger class="rounded-xl h-11 bg-slate-50 border-slate-200"><SelectValue /></SelectTrigger>
            <SelectContent><SelectItem value="todos">Todos os Cursos</SelectItem><SelectItem value="DSM">DSM</SelectItem><SelectItem value="ADS">ADS</SelectItem><SelectItem value="COMEX">COMEX</SelectItem><SelectItem value="GEEM">GEEM</SelectItem></SelectContent></Select>
          </div>
          <div class="space-y-2"><label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Período Letivo</label>
            <Select v-model="filters.period"><SelectTrigger class="rounded-xl h-11 bg-slate-50 border-slate-200"><SelectValue /></SelectTrigger>
            <SelectContent><SelectItem value="todos">Todos os Períodos</SelectItem><SelectItem value="Matutino">Matutino</SelectItem><SelectItem value="Vespertino">Vespertino</SelectItem><SelectItem value="Noturno">Noturno</SelectItem></SelectContent></Select>
          </div>
        </div>
        <DialogFooter class="flex flex-col sm:flex-row gap-3 border-t pt-4">
          <Button variant="ghost" @click="filters.course = 'todos'; filters.period = 'todos'" class="text-slate-500 hover:text-red-500 rounded-xl">Limpar</Button>
          <Button @click="isFilterDialogOpen = false" class="bg-[#1A1A3A] hover:bg-[#0F0F24] rounded-xl px-10 text-white shadow-md">Aplicar</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

  </div>
</template>

<style scoped>
.font-poppins { font-family: 'Poppins', sans-serif; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 20px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background-color: #94a3b8; }
</style>