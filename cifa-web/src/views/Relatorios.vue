<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, Filter, Download, ChevronLeft, ChevronRight, FileText, FileSpreadsheet, ArrowUpDown, Clock, User, ShieldCheck, ShieldAlert, ArrowRightCircle, ArrowLeftCircle, AlertTriangle
} from 'lucide-vue-next'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { useToast } from '@/components/ui/toast/use-toast'

const router = useRouter()
const { toast } = useToast()

const API_BASE = 'https://reply-imprint-skier.ngrok-free.dev'
const fetchOptions = { 
  headers: { 'ngrok-skip-browser-warning': 'true', 'Content-Type': 'application/json' },
  mode: 'cors' as RequestMode
}

interface AccessLog {
  id: string | number
  timestamp: Date
  studentId: string | number
  studentName: string
  avatar: string
  period: string
  course: string
  status: 'Autorizado' | 'Negado'
  type: 'Entrada' | 'Saída'
}

const searchQuery = ref('')
const isFilterDialogOpen = ref(false)
const isDetailModalOpen = ref(false)
const selectedLog = ref<AccessLog | null>(null)
const currentPage = ref(1)
const itemsPerPage = 8

const isLoadingData = ref(true)
const apiErrorMessage = ref('')

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

const filters = ref({ period: 'todos', course: 'todos' })
const allLogs = ref<AccessLog[]>([])

// ============================================================
// 1. BUSCA PRIMÁRIA: RELATÓRIOS (Dados Estáticos do Acesso)
// ============================================================
const fetchAPIReports = async () => {
  isLoadingData.value = true
  apiErrorMessage.value = ''
  
  try {
    const response = await fetch(`${API_BASE}/relatorio/exibirTodos`, fetchOptions)
    
    if (!response.ok) {
      throw new Error(`O servidor respondeu com erro ${response.status}.`)
    }

    const dataArray = await response.json()
    
    if (!Array.isArray(dataArray)) {
      throw new Error("A API não devolveu uma lista válida.")
    }
    
    const mappedLogs: AccessLog[] = dataArray.map((log: any, index: number) => {
      let periodStr = 'Noturno'
      if (log.turma) {
        if (log.turma.includes('_VES')) periodStr = 'Vespertino'
        else if (log.turma.includes('_MAT')) periodStr = 'Matutino'
      }

      let logDate = new Date()
      if (log.timestamp && log.timestamp.seconds) {
        logDate = new Date(log.timestamp.seconds * 1000)
      } else if (log.data) {
        logDate = new Date(log.data)
      }

      const tipoFluxo = String(log.tipo || 'Entrada').toLowerCase()
      const tipoFinal = (tipoFluxo.includes('saida') || tipoFluxo.includes('saída')) ? 'Saída' : 'Entrada'
      const statusFinal = log.status_acesso === false ? 'Negado' : 'Autorizado'

      const nomeLimpo = log.nome || 'Visitante/Desconhecido'
      const raLimpo = String(log.ra || 'X')

      return {
        id: `LOG-${raLimpo}-${index}`,
        timestamp: logDate,
        studentId: log.ra || 0,
        studentName: nomeLimpo,
        avatar: `https://api.dicebear.com/8.x/avataaars/svg?seed=${encodeURIComponent(nomeLimpo)}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`,
        period: periodStr,
        course: log.curso || 'Indefinido',
        status: statusFinal as 'Autorizado' | 'Negado',
        type: tipoFinal as 'Entrada' | 'Saída'
      }
    })

    allLogs.value = mappedLogs.sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime())
    
    // Inicia a sincronização de dados atualizados logo após carregar os estáticos
    if (allLogs.value.length > 0) {
      fetchLiveStudentDataBackground()
    }
    
  } catch (error: any) { 
    console.error("ERRO NOS RELATÓRIOS:", error)
    apiErrorMessage.value = error.message || "Erro de conexão."
  } finally { 
    isLoadingData.value = false 
  }
}

// ============================================================
// 2. SINCRONIZAÇÃO EM TEMPO REAL: RESOLUÇÃO DA DESSINCRONIZAÇÃO
// ============================================================
const fetchLiveStudentDataBackground = async () => {
  try {
    const response = await fetch(`${API_BASE}/alunos/verAlunos`, fetchOptions)
    if (!response.ok) return 

    const studentsData = await response.json()
    const alunosArray = Array.isArray(studentsData) ? studentsData : (studentsData.alunos || [])
    
    const studentDictionary = new Map<string, any>()
    
    alunosArray.forEach((aluno: any) => {
      if (aluno.ra) {
        studentDictionary.set(String(aluno.ra), aluno)
      }
    })

    // Atualiza reativamente os logs com os dados MAIS RECENTES (Nome, Foto, Curso)
    allLogs.value = allLogs.value.map(log => {
      const alunoAtualizado = studentDictionary.get(String(log.studentId))
      
      if (alunoAtualizado) {
        const fotoReal = alunoAtualizado.imagem_url || alunoAtualizado.foto || alunoAtualizado.avatar || log.avatar
        const nomeReal = alunoAtualizado.nome || log.studentName
        const turmaReal = alunoAtualizado.id_turma ? alunoAtualizado.id_turma.split('_')[0] : log.course
        
        let periodReal = log.period
        if (alunoAtualizado.id_turma) {
          if (alunoAtualizado.id_turma.includes('_VES')) periodReal = 'Vespertino'
          else if (alunoAtualizado.id_turma.includes('_MAT')) periodReal = 'Matutino'
          else if (alunoAtualizado.id_turma.includes('_NOT')) periodReal = 'Noturno'
        }

        return { 
          ...log, 
          avatar: fotoReal,
          studentName: nomeReal,
          course: turmaReal,
          period: periodReal
        }
      }
      return log
    })

  } catch (error) {
    console.warn("Aviso: A sincronização com Estudantes falhou silenciosamente.", error)
  }
}

onMounted(() => {
  fetchAPIReports()
})

// ============================================================
// LÓGICA DE FILTRAGEM
// ============================================================
const filteredLogs = computed(() => allLogs.value.filter(log => {
  const matchesSearch = log.studentName.toLowerCase().includes(searchQuery.value.toLowerCase())
  
  let matchesTab = true
  const today = new Date()
  
  if (activeTab.value === 'hoje') matchesTab = new Date(log.timestamp).toDateString() === today.toDateString()
  else if (activeTab.value === 'semana') matchesTab = new Date(log.timestamp) >= new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
  else if (activeTab.value === 'entradas') matchesTab = log.type === 'Entrada'
  else if (activeTab.value === 'saidas') matchesTab = log.type === 'Saída'
  else if (activeTab.value === 'negados') matchesTab = log.status === 'Negado'
  
  const matchesPeriod = filters.value.period === 'todos' || log.period === filters.value.period
  const matchesCourse = filters.value.course === 'todos' || log.course === filters.value.course

  return matchesSearch && matchesTab && matchesPeriod && matchesCourse
}))

const formatDate = (date: Date) => date.toLocaleDateString('pt-BR') + ' - ' + date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
const openLogDetail = (log: AccessLog) => { selectedLog.value = log; isDetailModalOpen.value = true }
const goToStudentProfile = (id: string | number) => { isDetailModalOpen.value = false; router.push({ path: '/admin/estudantes', query: { id } }) }
const paginatedLogs = computed(() => filteredLogs.value.slice((currentPage.value - 1) * itemsPerPage, currentPage.value * itemsPerPage))
const totalPages = computed(() => Math.ceil(filteredLogs.value.length / itemsPerPage) || 1)

watch([searchQuery, activeTab, filters], () => { currentPage.value = 1 }, { deep: true })
</script>

<template>
  <div class="flex flex-col h-full space-y-2 font-poppins min-h-0">
    
    <div v-if="apiErrorMessage" class="bg-red-50 border border-red-200 text-red-700 p-4 rounded-2xl flex items-start gap-3 shadow-sm shrink-0">
      <AlertTriangle class="w-5 h-5 shrink-0 mt-0.5" />
      <div>
        <h4 class="font-bold text-sm">Falha de Conexão com o Backend</h4>
        <p class="text-xs font-medium mt-1">{{ apiErrorMessage }}</p>
        <p class="text-xs mt-1 opacity-80">Pressione F12 para inspecionar os logs detalhados do erro.</p>
      </div>
      <Button @click="fetchAPIReports" variant="outline" size="sm" class="ml-auto bg-white border-red-200 hover:bg-red-50 text-red-700">Tentar Novamente</Button>
    </div>

    <div class="flex flex-col sm:flex-row justify-between items-center gap-4 py-2 shrink-0">
      <div class="flex items-center gap-3 w-full sm:w-auto">
        <div class="relative w-full sm:w-96">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input v-model="searchQuery" placeholder="Pesquisar acesso por nome..." class="pl-10 h-11 border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#0A102E] bg-white w-full transition-all text-sm outline-none shadow-sm" />
        </div>
        <Button variant="outline" @click="isFilterDialogOpen = true" class="h-11 px-5 border-slate-200 rounded-xl bg-white text-slate-700 hover:bg-slate-50 gap-2 shadow-sm shrink-0 font-semibold"><Filter class="w-4 h-4" /><span class="hidden sm:inline">Filtros</span></Button>
      </div>
      <DropdownMenu>
        <DropdownMenuTrigger as-child><Button :disabled="filteredLogs.length === 0 || isLoadingData" variant="outline" class="h-11 px-6 border-slate-200 rounded-xl bg-white text-slate-700 hover:bg-slate-50 gap-2 shadow-sm disabled:opacity-50 shrink-0 font-semibold"><Download class="w-4 h-4" /><span>Exportar Relatório</span></Button></DropdownMenuTrigger>
        <DropdownMenuContent align="end" class="w-44 rounded-xl border-none shadow-xl font-poppins">
          <DropdownMenuItem class="cursor-pointer gap-2 font-medium"><FileSpreadsheet class="w-4 h-4 text-emerald-600" /> Planilha (CSV)</DropdownMenuItem>
          <DropdownMenuItem class="cursor-pointer gap-2 font-medium"><FileText class="w-4 h-4 text-red-600" /> Documento (PDF)</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>

    <div class="w-full flex shrink-0 border-b border-slate-200 mt-2 mb-4 bg-slate-50/50 rounded-t-xl overflow-hidden">
      <button v-for="tab in tabs" :key="tab.id" @click="activeTab = tab.id" class="flex-1 min-w-0 pb-3 px-1 sm:px-2 pt-3 text-[10px] sm:text-xs lg:text-sm font-semibold transition-all duration-300 relative flex justify-center items-center gap-1.5 sm:gap-2 group overflow-hidden" :class="activeTab === tab.id ? 'text-[#0A102E] bg-white' : 'text-slate-500 hover:text-slate-800 hover:bg-white/50'">
        <span class="truncate">{{ tab.label }}</span>
        <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 w-full h-[3px] bg-[#0A102E] rounded-t-full"></div>
      </button>
    </div>

    <div class="flex-1 bg-white border border-slate-200 rounded-[2rem] shadow-sm overflow-hidden flex flex-col min-h-0 relative">
      <div class="flex-1 overflow-x-auto overflow-y-auto custom-scrollbar">
        <Table class="min-w-[800px] w-full">
          <TableHeader class="bg-slate-50/80 sticky top-0 z-10 backdrop-blur-sm">
            <TableRow class="border-b-slate-200">
              <TableHead class="text-slate-600 font-bold px-6 h-12">Data & Tempo</TableHead><TableHead class="text-slate-600 font-bold px-6 h-12">Usuário Registado</TableHead><TableHead class="text-slate-600 font-bold px-6 h-12">Período Letivo</TableHead><TableHead class="text-slate-600 font-bold px-6 h-12 text-center">Tipo de Fluxo</TableHead><TableHead class="text-slate-600 font-bold px-6 h-12">Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody v-auto-animate>
            <template v-if="isLoadingData">
              <TableRow v-for="n in 8" :key="'skel-rel-' + n" class="border-b-slate-100">
                <TableCell class="py-4 px-6"><div class="h-3 w-28 bg-slate-200 rounded animate-pulse"></div></TableCell>
                <TableCell class="py-4 px-6"><div class="flex items-center gap-3"><div class="w-9 h-9 rounded-full bg-slate-200 animate-pulse shrink-0"></div><div class="flex flex-col gap-2"><div class="h-3 w-32 bg-slate-200 rounded animate-pulse"></div><div class="h-2 w-12 bg-slate-200 rounded animate-pulse"></div></div></div></TableCell>
                <TableCell class="py-4 px-6"><div class="h-3 w-16 bg-slate-200 rounded animate-pulse"></div></TableCell>
                <TableCell class="py-4 px-6"><div class="h-5 w-20 bg-slate-200 rounded-full animate-pulse mx-auto"></div></TableCell>
                <TableCell class="py-4 px-6"><div class="h-5 w-16 bg-slate-200 rounded-full animate-pulse"></div></TableCell>
              </TableRow>
            </template>
            <template v-else-if="paginatedLogs.length > 0">
              <TableRow v-for="log in paginatedLogs" :key="log.id" @click="openLogDetail(log)" class="hover:bg-slate-50 transition-colors border-b-slate-100 group cursor-pointer">
                <TableCell class="py-4 px-6 font-mono text-[11px] text-slate-500 whitespace-nowrap">{{ formatDate(log.timestamp) }}</TableCell>
                <TableCell class="py-4 px-6 whitespace-nowrap"><div class="flex items-center gap-3"><img :src="log.avatar" class="w-9 h-9 rounded-full object-cover bg-slate-100 border border-slate-200 group-hover:border-indigo-200 transition-colors" /><div class="flex flex-col"><span class="font-semibold text-slate-800 group-hover:text-indigo-600 transition-colors">{{ log.studentName }}</span><span class="text-[9px] uppercase tracking-widest text-slate-400 font-bold mt-0.5">{{ log.course }}</span></div></div></TableCell>
                <TableCell class="py-4 px-6 text-sm text-slate-600 font-medium">{{ log.period }}</TableCell>
                <TableCell class="py-4 px-6 text-sm font-semibold text-center"><div class="flex items-center justify-center gap-1.5" :class="log.type === 'Entrada' ? 'text-blue-600' : 'text-amber-600'"><ArrowRightCircle v-if="log.type === 'Entrada'" class="w-4 h-4" /><ArrowLeftCircle v-else class="w-4 h-4" />{{ log.type }}</div></TableCell>
                <TableCell class="py-4 px-6"><Badge :class="log.status === 'Autorizado' ? 'bg-emerald-100 text-emerald-700 border-none px-3' : 'bg-red-100 text-red-700 border-none px-3'" class="font-bold text-[10px] uppercase shadow-none">{{ log.status }}</Badge></TableCell>
              </TableRow>
            </template>
            <template v-else>
              <TableRow>
                <TableCell colspan="5" class="h-64 text-center text-slate-400 font-medium">
                  {{ apiErrorMessage ? 'Falha ao buscar dados.' : 'Nenhum registro de acesso encontrado.' }}
                </TableCell>
              </TableRow>
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
          <div class="absolute top-4 right-6 bg-white px-3 py-1 rounded-full shadow-sm font-mono text-[10px] font-bold text-slate-400 tracking-widest border border-slate-100">LOG DETALHADO</div>
          <div class="relative mt-4 mb-4">
            <img :src="selectedLog?.avatar" class="w-24 h-24 rounded-full object-cover border-4 border-white shadow-md bg-white" />
            <div class="absolute bottom-0 -right-2 p-1.5 rounded-full text-white shadow-lg border-2 border-white" :class="selectedLog?.status === 'Autorizado' ? 'bg-emerald-500' : 'bg-red-500'"><ShieldCheck v-if="selectedLog?.status === 'Autorizado'" class="w-5 h-5" /><ShieldAlert v-else class="w-5 h-5" /></div>
          </div>
          <h2 class="text-xl font-bold text-slate-900 tracking-tight text-center">{{ selectedLog?.studentName }}</h2>
          <p class="text-[10px] font-bold uppercase tracking-widest text-slate-400 mt-1">{{ selectedLog?.course }} • {{ selectedLog?.period }}</p>
        </div>
        <div class="p-8 grid grid-cols-2 gap-y-6 gap-x-4 bg-white">
          <div class="flex flex-col"><span class="text-[9px] font-black uppercase text-slate-400 tracking-widest flex items-center gap-1.5 mb-1"><Clock class="w-3 h-3" /> Horário</span><span class="font-bold text-slate-800 text-sm">{{ selectedLog ? formatDate(selectedLog.timestamp) : '' }}</span></div>
          <div class="flex flex-col border-l border-slate-100 pl-4"><span class="text-[9px] font-black uppercase text-slate-400 tracking-widest flex items-center gap-1.5 mb-1"><ArrowUpDown class="w-3 h-3" /> Fluxo</span><span class="font-bold text-sm" :class="selectedLog?.type === 'Entrada' ? 'text-blue-600' : 'text-amber-600'">{{ selectedLog?.type }} Identificada</span></div>
        </div>
        <DialogFooter class="p-6 pt-0 bg-white gap-2 flex-col sm:flex-row">
          <Button variant="outline" @click="isDetailModalOpen = false" class="w-full rounded-2xl h-12 font-bold border-slate-200">Voltar</Button>
          <Button @click="selectedLog && goToStudentProfile(selectedLog.studentId)" class="w-full bg-[#1A1A3A] hover:bg-[#0F0F24] text-white rounded-2xl h-12 font-bold shadow-md gap-2"><User class="w-4 h-4" /> Ver Perfil</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
    
    <Dialog v-model:open="isFilterDialogOpen">
      <DialogContent class="rounded-[2.5rem] w-[95vw] sm:max-w-[450px] shadow-2xl border-none font-poppins">
        <DialogHeader><DialogTitle class="text-xl font-bold">Filtros Avançados</DialogTitle></DialogHeader>
        <div class="grid gap-5 py-4">
          <div class="space-y-2"><label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Curso / Área</label><Select v-model="filters.course"><SelectTrigger class="rounded-xl h-11 bg-slate-50 border-slate-200"><SelectValue /></SelectTrigger><SelectContent><SelectItem value="todos">Todos os Cursos</SelectItem><SelectItem value="DSM">DSM</SelectItem><SelectItem value="ADS">ADS</SelectItem><SelectItem value="COMEX">COMEX</SelectItem><SelectItem value="GEEM">GEEM</SelectItem></SelectContent></Select></div>
          <div class="space-y-2"><label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Período Letivo</label><Select v-model="filters.period"><SelectTrigger class="rounded-xl h-11 bg-slate-50 border-slate-200"><SelectValue /></SelectTrigger><SelectContent><SelectItem value="todos">Todos os Períodos</SelectItem><SelectItem value="Matutino">Matutino</SelectItem><SelectItem value="Vespertino">Vespertino</SelectItem><SelectItem value="Noturno">Noturno</SelectItem></SelectContent></Select></div>
        </div>
        <DialogFooter class="flex flex-col sm:flex-row gap-3 border-t pt-4"><Button variant="ghost" @click="filters.course = 'todos'; filters.period = 'todos'" class="text-slate-500 hover:text-red-500 rounded-xl">Limpar</Button><Button @click="isFilterDialogOpen = false" class="bg-[#1A1A3A] hover:bg-[#0F0F24] rounded-xl px-10 text-white shadow-md">Aplicar</Button></DialogFooter>
      </DialogContent>
    </Dialog>

  </div>
</template>

<style scoped>
.font-poppins { font-family: 'Poppins', sans-serif; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 20px; }
</style>