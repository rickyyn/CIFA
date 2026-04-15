<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, 
  Filter, 
  Download, 
  ChevronLeft, 
  ChevronRight, 
  FileText,
  FileSpreadsheet,
  ArrowUpDown
} from 'lucide-vue-next'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Separator } from '@/components/ui/separator'

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

// Estados de Filtro e Paginação
const searchQuery = ref('')
const isFilterDialogOpen = ref(false)
const currentPage = ref(1)
const itemsPerPage = 8

const filters = ref({
  period: 'todos',
  status: 'todos',
  course: 'todos',
  timeType: 'todos'
})

// Banco de dados robusto (20 registros variados para teste)
const mockLogs = ref<AccessLog[]>([
  { id: 'LOG-001', timestamp: new Date(2026, 3, 14, 18, 45), studentId: 1, studentName: 'Leonardo Mendonça', avatar: 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?q=80&w=200&fit=crop', period: 'Noturno', course: 'DSM', status: 'Autorizado', type: 'Entrada' },
  { id: 'LOG-002', timestamp: new Date(2026, 3, 14, 18, 42), studentId: 5, studentName: 'Carlos Eduardo Santos', avatar: 'https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?q=80&w=200&fit=crop', period: 'Noturno', course: 'DSM', status: 'Negado', type: 'Entrada' },
  { id: 'LOG-003', timestamp: new Date(2026, 3, 14, 13, 15), studentId: 4, studentName: 'Beatriz Lima', avatar: 'https://images.unsplash.com/photo-1540573133985-87b6da6d54a9?q=80&w=200&fit=crop', period: 'Vespertino', course: 'GEEM', status: 'Autorizado', type: 'Saída' },
  { id: 'LOG-004', timestamp: new Date(2026, 3, 14, 7, 30), studentId: 2, studentName: 'Ana Clara Souza', avatar: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?q=80&w=200&fit=crop', period: 'Matutino', course: 'ADS', status: 'Autorizado', type: 'Entrada' },
  { id: 'LOG-005', timestamp: new Date(2026, 3, 13, 22, 40), studentId: 3, studentName: 'Felipe Mendes', avatar: 'https://images.unsplash.com/photo-1474511320723-9a56873867b5?q=80&w=200&fit=crop', period: 'Noturno', course: 'COMEX', status: 'Autorizado', type: 'Saída' },
  { id: 'LOG-006', timestamp: new Date(2026, 3, 13, 10, 20), studentId: 1, studentName: 'Leonardo Mendonça', avatar: 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?q=80&w=200&fit=crop', period: 'Noturno', course: 'DSM', status: 'Autorizado', type: 'Entrada' },
  { id: 'LOG-007', timestamp: new Date(2026, 3, 12, 19, 0), studentId: 5, studentName: 'Carlos Eduardo Santos', avatar: 'https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?q=80&w=200&fit=crop', period: 'Noturno', course: 'DSM', status: 'Autorizado', type: 'Entrada' },
  { id: 'LOG-008', timestamp: new Date(2026, 3, 10, 8, 15), studentId: 2, studentName: 'Ana Clara Souza', avatar: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?q=80&w=200&fit=crop', period: 'Matutino', course: 'ADS', status: 'Negado', type: 'Entrada' },
  { id: 'LOG-009', timestamp: new Date(2026, 3, 5, 21, 30), studentId: 3, studentName: 'Felipe Mendes', avatar: 'https://images.unsplash.com/photo-1474511320723-9a56873867b5?q=80&w=200&fit=crop', period: 'Noturno', course: 'COMEX', status: 'Autorizado', type: 'Entrada' },
  { id: 'LOG-010', timestamp: new Date(2026, 3, 1, 14, 20), studentId: 4, studentName: 'Beatriz Lima', avatar: 'https://images.unsplash.com/photo-1540573133985-87b6da6d54a9?q=80&w=200&fit=crop', period: 'Vespertino', course: 'GEEM', status: 'Autorizado', type: 'Entrada' },
  { id: 'LOG-011', timestamp: new Date(2026, 2, 28, 9, 0), studentId: 2, studentName: 'Ana Clara Souza', avatar: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?q=80&w=200&auto=format&fit=crop', period: 'Matutino', course: 'ADS', status: 'Autorizado', type: 'Entrada' },
  { id: 'LOG-012', timestamp: new Date(2026, 2, 15, 20, 0), studentId: 3, studentName: 'Felipe Mendes', avatar: 'https://images.unsplash.com/photo-1474511320723-9a56873867b5?q=80&w=200&auto=format&fit=crop', period: 'Noturno', course: 'COMEX', status: 'Negado', type: 'Entrada' },
  { id: 'LOG-013', timestamp: new Date(2026, 3, 14, 22, 10), studentId: 1, studentName: 'Leonardo Mendonça', avatar: 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?q=80&w=200&auto=format&fit=crop', period: 'Noturno', course: 'DSM', status: 'Autorizado', type: 'Saída' },
  { id: 'LOG-014', timestamp: new Date(2026, 3, 14, 21, 55), studentId: 5, studentName: 'Carlos Eduardo Santos', avatar: 'https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?q=80&w=200&auto=format&fit=crop', period: 'Noturno', course: 'DSM', status: 'Autorizado', type: 'Saída' },
  { id: 'LOG-015', timestamp: new Date(2026, 3, 14, 12, 30), studentId: 4, studentName: 'Beatriz Lima', avatar: 'https://images.unsplash.com/photo-1540573133985-87b6da6d54a9?q=80&w=200&auto=format&fit=crop', period: 'Vespertino', course: 'GEEM', status: 'Autorizado', type: 'Entrada' },
  { id: 'LOG-016', timestamp: new Date(2026, 3, 14, 11, 40), studentId: 2, studentName: 'Ana Clara Souza', avatar: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?q=80&w=200&auto=format&fit=crop', period: 'Matutino', course: 'ADS', status: 'Autorizado', type: 'Saída' },
  { id: 'LOG-017', timestamp: new Date(2026, 3, 14, 8, 0), studentId: 3, studentName: 'Felipe Mendes', avatar: 'https://images.unsplash.com/photo-1474511320723-9a56873867b5?q=80&w=200&auto=format&fit=crop', period: 'Noturno', course: 'COMEX', status: 'Autorizado', type: 'Entrada' },
  { id: 'LOG-018', timestamp: new Date(2026, 3, 13, 20, 15), studentId: 1, studentName: 'Leonardo Mendonça', avatar: 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?q=80&w=200&auto=format&fit=crop', period: 'Noturno', course: 'DSM', status: 'Autorizado', type: 'Entrada' },
  { id: 'LOG-019', timestamp: new Date(2026, 3, 13, 19, 30), studentId: 5, studentName: 'Carlos Eduardo Santos', avatar: 'https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?q=80&w=200&auto=format&fit=crop', period: 'Noturno', course: 'DSM', status: 'Negado', type: 'Entrada' },
  { id: 'LOG-020', timestamp: new Date(2026, 3, 13, 9, 20), studentId: 2, studentName: 'Ana Clara Souza', avatar: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?q=80&w=200&auto=format&fit=crop', period: 'Matutino', course: 'ADS', status: 'Autorizado', type: 'Entrada' }
])

const filteredLogs = computed(() => {
  return mockLogs.value.filter(log => {
    // 1. Busca Global (ID ou Nome)
    const matchesSearch = log.studentName.toLowerCase().includes(searchQuery.value.toLowerCase()) || 
                          log.id.toLowerCase().includes(searchQuery.value.toLowerCase())
    
    // 2. Filtros de Categoria
    const matchesPeriod = filters.value.period === 'todos' || log.period === filters.value.period
    const matchesStatus = filters.value.status === 'todos' || log.status === filters.value.status
    const matchesCourse = filters.value.course === 'todos' || log.course === filters.value.course

    // 3. Filtro de Tempo Preciso
    let matchesTime = true
    const logDate = new Date(log.timestamp)
    const today = new Date()

    if (filters.value.timeType === 'hoje') {
      matchesTime = logDate.toDateString() === today.toDateString()
    } else if (filters.value.timeType === 'semana') {
      const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
      matchesTime = logDate >= weekAgo
    } else if (filters.value.timeType === 'mes') {
      matchesTime = logDate.getMonth() === today.getMonth() && logDate.getFullYear() === today.getFullYear()
    }

    return matchesSearch && matchesPeriod && matchesStatus && matchesCourse && matchesTime
  })
})

// Paginação
const totalPages = computed(() => Math.ceil(filteredLogs.value.length / itemsPerPage) || 1)
const paginatedLogs = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredLogs.value.slice(start, start + itemsPerPage)
})

const formatDate = (date: Date) => {
  return date.toLocaleDateString('pt-BR') + ' - ' + date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

const clearFilters = () => {
  filters.value = { period: 'todos', status: 'todos', course: 'todos', timeType: 'todos' }
  searchQuery.value = ''
  currentPage.value = 1
}

const hasActiveFilters = computed(() => {
  return filters.value.period !== 'todos' || filters.value.status !== 'todos' || 
         filters.value.course !== 'todos' || filters.value.timeType !== 'todos'
})

// Navegação para tela de estudantes
const goToStudent = (studentId: number) => {
  router.push({ path: '/admin/estudantes', query: { id: studentId } })
}

// Exportações
const exportToCSV = () => {
  if (filteredLogs.value.length === 0) return
  const headers = ['ID', 'Data', 'Estudante', 'Periodo', 'Curso', 'Status']
  const rows = filteredLogs.value.map(l => [l.id, formatDate(l.timestamp), `"${l.studentName}"`, l.period, l.course, l.status])
  const csv = [headers.join(';'), ...rows.map(r => r.join(';'))].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a'); link.href = URL.createObjectURL(blob); link.download = 'logs_cifa.csv'; link.click()
}

const exportToPDF = () => {
  if (filteredLogs.value.length === 0) return
  const printWindow = window.open('', '_blank')
  if (!printWindow) return
  let html = `<html><head><style>body{font-family:sans-serif;} table{width:100%;border-collapse:collapse;} th,td{border:1px solid #ddd;padding:8px;font-size:11px;}</style></head><body>`
  html += `<h2>Relatório de Logs - CIFA</h2><table><tr><th>ID</th><th>Data</th><th>Estudante</th><th>Curso</th><th>Status</th></tr>`
  filteredLogs.value.forEach(l => { html += `<tr><td>${l.id}</td><td>${formatDate(l.timestamp)}</td><td>${l.studentName}</td><td>${l.course}</td><td>${l.status}</td></tr>` })
  html += `</table></body></html>`
  printWindow.document.write(html); printWindow.document.close(); printWindow.print()
}
</script>

<template>
  <div class="flex flex-col h-full space-y-4 font-poppins">
    
    <div class="flex flex-col sm:flex-row justify-between items-center gap-4 py-1">
      <div class="flex flex-col sm:flex-row items-center gap-3 w-full sm:w-auto">
        
        <div class="relative w-full sm:w-80">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input 
            v-model="searchQuery"
            placeholder="Pesquisar por nome ou ID..." 
            class="pl-10 h-10 border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#0A102E] bg-white w-full transition-all text-sm outline-none shadow-sm"
          />
        </div>

        <Dialog v-model:open="isFilterDialogOpen">
          <DialogTrigger as-child>
            <Button variant="outline" class="h-10 px-6 border-slate-200 rounded-xl bg-white text-slate-700 hover:bg-slate-50 gap-2 relative shadow-sm w-full sm:w-auto">
              <Filter class="w-4 h-4" />
              Filtros
              <span v-if="hasActiveFilters" class="absolute -top-1 -right-1 flex h-3 w-3">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-3 w-3 bg-indigo-600 border border-white"></span>
              </span>
            </Button>
          </DialogTrigger>
          <DialogContent class="rounded-[2.5rem] w-[95vw] sm:max-w-[450px] shadow-2xl border-none font-poppins">
            <DialogHeader>
              <DialogTitle class="text-xl font-bold">Refinar Relatórios</DialogTitle>
              <DialogDescription>Ajuste as categorias e o período de tempo.</DialogDescription>
            </DialogHeader>
            
            <div class="grid gap-5 py-2">
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div class="space-y-2">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest">Status</label>
                  <Select v-model="filters.status">
                    <SelectTrigger class="rounded-xl h-10"><SelectValue /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="todos">Todos</SelectItem>
                      <SelectItem value="Autorizado">Autorizado</SelectItem>
                      <SelectItem value="Negado">Negado</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div class="space-y-2">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest">Período</label>
                  <Select v-model="filters.period">
                    <SelectTrigger class="rounded-xl h-10"><SelectValue /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="todos">Todos</SelectItem>
                      <SelectItem value="Matutino">Matutino</SelectItem>
                      <SelectItem value="Vespertino">Vespertino</SelectItem>
                      <SelectItem value="Noturno">Noturno</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div class="space-y-2">
                <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest">Curso</label>
                <Select v-model="filters.course">
                  <SelectTrigger class="rounded-xl h-10"><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="todos">Todos os Cursos</SelectItem>
                    <SelectItem value="DSM">DSM</SelectItem>
                    <SelectItem value="ADS">ADS</SelectItem>
                    <SelectItem value="COMEX">COMEX</SelectItem>
                    <SelectItem value="GEEM">GEEM</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <Separator />

              <div class="space-y-2">
                <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest">Filtro de Tempo</label>
                <Select v-model="filters.timeType">
                  <SelectTrigger class="rounded-xl h-10"><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="todos">Todo o histórico</SelectItem>
                    <SelectItem value="hoje">Hoje</SelectItem>
                    <SelectItem value="semana">Últimos 7 dias</SelectItem>
                    <SelectItem value="mes">Este mês</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <DialogFooter class="flex flex-col sm:flex-row sm:justify-between gap-3 border-t pt-4">
              <Button variant="ghost" @click="clearFilters" class="text-slate-500 hover:text-red-500 rounded-xl w-full sm:w-auto">Limpar Tudo</Button>
              <Button @click="isFilterDialogOpen = false; currentPage = 1" class="bg-[#1A1A3A] hover:bg-[#0F0F24] rounded-xl px-10 text-white shadow-lg w-full sm:w-auto">Aplicar</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <DropdownMenu>
        <DropdownMenuTrigger as-child>
          <Button :disabled="filteredLogs.length === 0" variant="outline" class="h-10 px-6 border-slate-200 rounded-xl bg-white text-slate-700 hover:bg-slate-50 gap-2 shadow-sm disabled:opacity-50 w-full sm:w-auto">
            <Download class="w-4 h-4" /> Exportar
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" class="w-44 rounded-xl shadow-xl border-none font-poppins">
          <DropdownMenuItem @click="exportToCSV" class="cursor-pointer gap-2 font-medium">
            <FileSpreadsheet class="w-4 h-4 text-emerald-600" /> CSV
          </DropdownMenuItem>
          <DropdownMenuItem @click="exportToPDF" class="cursor-pointer gap-2 font-medium">
            <FileText class="w-4 h-4 text-red-600" /> PDF
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>

    <div class="flex-1 bg-white border border-slate-200 rounded-[2.5rem] shadow-sm overflow-hidden flex flex-col min-h-0">
      <div class="flex-1 overflow-x-auto overflow-y-auto custom-scrollbar">
        
        <Table class="min-w-[800px] w-full">
          <TableHeader class="bg-slate-50/80 sticky top-0 z-10 backdrop-blur-sm">
            <TableRow class="border-b-slate-200">
              <TableHead class="text-slate-600 font-bold px-6 h-12">Data & Tempo <ArrowUpDown class="w-3 h-3 inline ml-1" /></TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Usuário</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Período</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12 text-center">Ação</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Status</TableHead>
            </TableRow>
          </TableHeader>
          
          <TableBody v-auto-animate>
            <template v-if="paginatedLogs.length > 0">
              <TableRow v-for="log in paginatedLogs" :key="log.id" class="hover:bg-slate-50/80 transition-colors border-b-slate-100 group">
                <TableCell class="py-4 px-6 font-mono text-[11px] text-slate-500 whitespace-nowrap">
                  {{ formatDate(log.timestamp) }}
                </TableCell>
                <TableCell class="py-4 px-6 whitespace-nowrap">
                  <div class="flex items-center gap-3">
                    <img :src="log.avatar" class="w-9 h-9 rounded-full object-cover bg-slate-100" />
                    <span 
                      @click="goToStudent(log.studentId)" 
                      class="font-semibold text-slate-800 hover:text-indigo-600 hover:underline cursor-pointer transition-colors"
                    >
                      {{ log.studentName }}
                    </span>
                  </div>
                </TableCell>
                <TableCell class="py-4 px-6 text-sm text-slate-600">{{ log.period }}</TableCell>
                <TableCell class="py-4 px-6 text-sm font-semibold text-center" :class="log.type === 'Entrada' ? 'text-blue-600' : 'text-amber-600'">
                  {{ log.type }}
                </TableCell>
                <TableCell class="py-4 px-6">
                  <Badge 
                    :class="log.status === 'Autorizado' ? 'bg-emerald-100 text-emerald-700 border-none' : 'bg-red-100 text-red-700 border-none'" 
                    class="font-bold text-[10px] uppercase shadow-none"
                  >
                    {{ log.status }}
                  </Badge>
                </TableCell>
              </TableRow>
            </template>
            <template v-else>
              <TableRow>
                <TableCell colspan="5" class="h-64 text-center text-slate-400 italic font-medium">
                  Nenhum registro encontrado.
                </TableCell>
              </TableRow>
            </template>
          </TableBody>
        </Table>

      </div>

      <div class="bg-slate-50/50 border-t border-slate-200 p-4 px-4 sm:px-8 flex items-center justify-between text-sm shrink-0">
        <span class="text-slate-500 font-medium">Página {{ currentPage }} de {{ totalPages }}</span>
        <div class="flex items-center gap-2">
          <Button @click="currentPage--" :disabled="currentPage === 1" variant="outline" size="sm" class="rounded-xl h-9 w-9 p-0 bg-white shadow-sm">
            <ChevronLeft class="w-4 h-4" />
          </Button>
          <Button @click="currentPage++" :disabled="currentPage === totalPages || filteredLogs.length === 0" variant="outline" size="sm" class="rounded-xl h-9 w-9 p-0 bg-white shadow-sm">
            <ChevronRight class="w-4 h-4" />
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.font-poppins {
  font-family: 'Poppins', sans-serif;
}

.custom-scrollbar::-webkit-scrollbar { 
  width: 6px; 
  height: 6px; 
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