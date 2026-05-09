<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, Filter, Download, ChevronLeft, ChevronRight, FileText, FileSpreadsheet, ArrowUpDown, Clock, User, ShieldCheck, ShieldAlert, ArrowRightCircle, ArrowLeftCircle
} from 'lucide-vue-next'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'

const router = useRouter()

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

const searchQuery = ref(''); const isFilterDialogOpen = ref(false); const isDetailModalOpen = ref(false); const selectedLog = ref<AccessLog | null>(null); const currentPage = ref(1); const itemsPerPage = 8; const isLoadingData = ref(true)

type TabType = 'todos' | 'hoje' | 'semana' | 'entradas' | 'saidas' | 'negados'
const activeTab = ref<TabType>('todos')
const tabs: { id: TabType, label: string }[] = [{ id: 'todos', label: 'Histórico Geral' }, { id: 'hoje', label: 'Hoje' }, { id: 'semana', label: 'Últimos 7 Dias' }, { id: 'entradas', label: 'Apenas Entradas' }, { id: 'saidas', label: 'Apenas Saídas' }, { id: 'negados', label: 'Acessos Negados' }]
const filters = ref({ period: 'todos', course: 'todos' }); const allLogs = ref<AccessLog[]>([])

// ============================================================
// LÊ RELATÓRIOS VERDADEIROS DA API (FIM DOS DADOS FALSOS)
// ============================================================
const fetchAPIReports = async () => {
  isLoadingData.value = true
  try {
    const response = await fetch('https://reply-imprint-skier.ngrok-free.dev/relatorios/exibirTodos', { 
      headers: { 'ngrok-skip-browser-warning': 'true' } 
    })
    
    if (!response.ok) throw new Error('Falha na comunicação com a API de Relatórios')

    const responseData = await response.json()
    const dataArray = Array.isArray(responseData) ? responseData : (responseData.relatorios || responseData.data || [])
    
    // Adaptador Defensivo (Caso as chaves da API não sejam óbvias)
    const logsFormatados = dataArray.map((log: any, index: number) => {
      const nomeStr = log.nome_aluno || log.nome || log.studentName || 'Visitante/Desconhecido';
      const fotoAPI = log.imagem_url || log.foto || log.avatar || '';
      
      const statusAcesso = String(log.status_acesso || log.status || log.autorizado || 'Autorizado').toLowerCase();
      const statusFinal = statusAcesso.includes('negad') || statusAcesso === 'false' ? 'Negado' : 'Autorizado';

      const tipoFluxo = String(log.tipo_fluxo || log.tipo || log.fluxo || log.type || 'Entrada').toLowerCase();
      const tipoFinal = tipoFluxo.includes('saida') || tipoFluxo.includes('saída') ? 'Saída' : 'Entrada';

      // Tratamento de Data seguro
      const dataBruta = log.data_hora || log.timestamp || log.criado_em || new Date();
      const dataOficial = new Date(dataBruta);

      return {
        id: log.id || log.id_relatorio || `LOG-${index}`,
        timestamp: isNaN(dataOficial.getTime()) ? new Date() : dataOficial,
        studentId: log.id_aluno || log.ra || log.studentId || 0,
        studentName: nomeStr,
        period: log.periodo || 'Indefinido',
        course: log.curso || 'N/A',
        avatar: fotoAPI || `https://api.dicebear.com/8.x/avataaars/svg?seed=${encodeURIComponent(nomeStr)}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`,
        status: statusFinal as 'Autorizado' | 'Negado',
        type: tipoFinal as 'Entrada' | 'Saída'
      }
    })

    // Ordena por data (Mais recente primeiro)
    allLogs.value = logsFormatados.sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime())
    
  } catch (error) { 
    console.error("Não foi possível carregar os relatórios:", error) 
  } finally { 
    isLoadingData.value = false 
  }
}

onMounted(() => fetchAPIReports())

const filteredLogs = computed(() => allLogs.value.filter(log => {
  const matchesSearch = log.studentName.toLowerCase().includes(searchQuery.value.toLowerCase())
  let matchesTab = true; const today = new Date()
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
</script>

<template>
  <div class="flex flex-col h-full space-y-2 font-poppins min-h-0">
    <div class="flex flex-col sm:flex-row justify-between items-center gap-4 py-2 shrink-0">
      <div class="flex items-center gap-3 w-full sm:w-auto">
        <div class="relative w-full sm:w-96">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input v-model="searchQuery" placeholder="Pesquisar por ID do registro ou aluno..." class="pl-10 h-11 border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#0A102E] bg-white w-full transition-all text-sm outline-none shadow-sm" />
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
            <template v-else><TableRow><TableCell colspan="5" class="h-64 text-center text-slate-400 italic font-medium">Nenhum registro de acesso obtido da API.</TableCell></TableRow></template>
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
            <div class="absolute bottom-0 -right-2 p-1.5 rounded-full text-white shadow-lg border-2 border-white" :class="selectedLog?.status === 'Autorizado' ? 'bg-emerald-500' : 'bg-red-500'"><ShieldCheck v-if="selectedLog?.status === 'Autorizado'" class="w-5 h-5" /><ShieldAlert v-else class="w-5 h-5" /></div>
          </div>
          <h2 class="text-xl font-bold text-slate-900 tracking-tight text-center">{{ selectedLog?.studentName }}</h2>
        </div>
        <DialogFooter class="p-6 bg-white gap-2 flex-col sm:flex-row">
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