<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, 
  MoreHorizontal, 
  Pencil, 
  Trash2, 
  UserPlus, 
  Camera,
  Loader2,
  ImagePlus,
  X
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

const API_BASE = 'http://localhost:8080'

interface Student {
  id: string
  name: string
  period: string
  course: string
  registration: string
  contact: string
  avatar: string
  status: 'Ativo' | 'Inativo'
  semester: number
  id_turma: string
  email_institucional: string
  email_pessoal: string
  rfid_tag: string
  ciclo_atual: number
  status_ativo: boolean
  esta_no_campus: boolean
  imagem_url: string
}

const allStudents = ref<Student[]>([])
const searchQuery = ref('')
const isLoadingData = ref(true)

const isEditModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const studentToEdit = ref<Student | null>(null)
const studentToDelete = ref<Student | null>(null)
const isSavingEdit = ref(false)


const editFileInputRef = ref<HTMLInputElement | null>(null)
const editImagePreview = ref<string | null>(null)
const editSelectedFile = ref<File | null>(null)

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

const fetchStudents = async (): Promise<Student[]> => {
  try {
    const url = `${API_BASE}/alunos/verAlunos?_=${Date.now()}`
    const response = await fetch(url, {
      headers: {
        'ngrok-skip-browser-warning': 'true',
        'Cache-Control': 'no-cache, no-store, must-revalidate',
        'Pragma': 'no-cache'
      }
    })
    if (!response.ok) throw new Error('Falha ao buscar alunos')
    
    const data = await response.json()
    const dataArray = Array.isArray(data) ? data : (data.alunos || data.data || [])
    
    return dataArray.map((aluno: any) => {
      const nomeStr = aluno.nome || 'Aluno Não Identificado'
      const fotoAPI = aluno.imagem_url || aluno.foto || aluno.avatar
      const turmaParts = aluno.id_turma ? aluno.id_turma.split('_') : []
      const courseStr = turmaParts[0] || 'Indefinido'
      let periodStr = 'Noturno'
      if (turmaParts.length >= 4) {
        if (turmaParts[3] === 'VES') periodStr = 'Vespertino'
        else if (turmaParts[3] === 'MAT') periodStr = 'Matutino'
        else if (turmaParts[3] === 'NOT') periodStr = 'Noturno'
      }

      return {
        id: String(aluno.id || aluno.idAluno || aluno.ra || ''),
        name: nomeStr,
        period: periodStr,
        course: courseStr,
        registration: String(aluno.ra || ''),
        contact: aluno.email_institucional || aluno.email_pessoal || 'sem-email@fatec.sp.gov.br',
        avatar: fotoAPI || `https://api.dicebear.com/8.x/avataaars/svg?seed=${encodeURIComponent(nomeStr)}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`,
        status: aluno.status_ativo ? 'Ativo' : 'Inativo',
        semester: Number(aluno.ciclo_atual || 1),
        id_turma: aluno.id_turma || '',
        email_institucional: aluno.email_institucional || '',
        email_pessoal: aluno.email_pessoal || '',
        rfid_tag: aluno.rfid_tag || '',
        ciclo_atual: Number(aluno.ciclo_atual || 1),
        status_ativo: Boolean(aluno.status_ativo),
        esta_no_campus: Boolean(aluno.esta_no_campus),
        imagem_url: fotoAPI || ''
      }
    })
  } catch (error) {
    console.error("Erro ao buscar dados da API:", error)
    return []
  }
}

const loadStudents = async () => {
  isLoadingData.value = true
  try {
    const freshList = await fetchStudents()
    allStudents.value = freshList.sort((a, b) => a.name.localeCompare(b.name))
    console.log(`✅ Lista carregada com ${allStudents.value.length} alunos`)
  } catch (error) {
    console.error('❌ Erro ao carregar lista', error)
    toast({ title: "Erro", description: "Não foi possível carregar os dados", variant: "destructive" })
  } finally {
    isLoadingData.value = false
  }
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

const openEdit = (student: Student) => {
  studentToEdit.value = JSON.parse(JSON.stringify(student))
  editImagePreview.value = student.avatar
  editSelectedFile.value = null
  isEditModalOpen.value = true
}

const handleEditImageChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    editSelectedFile.value = file
    const reader = new FileReader()
    reader.onload = (e) => { editImagePreview.value = e.target?.result as string }
    reader.readAsDataURL(file)
  }
}


const saveEdit = async () => {
  if (!studentToEdit.value) return
  isSavingEdit.value = true

  try {
    const s = studentToEdit.value
    const alunoId = s.id || s.registration
    if (!alunoId) throw new Error('ID do aluno não encontrado')

    const alunoJson = {
      nome: s.name,
      ra: Number(s.registration),
      id_turma: s.id_turma,
      email_institucional: s.email_institucional,
      email_pessoal: s.email_pessoal,
      status_ativo: s.status === 'Ativo',
      ciclo_atual: s.semester,
      rfid_tag: s.rfid_tag || '',
      esta_no_campus: s.esta_no_campus,
      imagem_url: s.imagem_url || ''
    }

    const formData = new FormData()
    formData.append('aluno', new Blob([JSON.stringify(alunoJson)], { type: 'application/json' }))
    if (editSelectedFile.value) {
      formData.append('imagem', editSelectedFile.value)
    }

    const url = `${API_BASE}/alunos/editarAluno/${alunoId}`
    console.log('🔵 [PUT] Enviando para:', url)

    const response = await fetch(url, {
      method: 'PUT',
      headers: { 'ngrok-skip-browser-warning': 'true' },
      body: formData,
      redirect: 'error' 
    })

    console.log('🟢 Status:', response.status)

    if (!response.ok) {
      const errText = await response.text()
      throw new Error(`${response.status} - ${errText}`)
    }

    
    const index = allStudents.value.findIndex(student => student.id === s.id)
    if (index !== -1) {
      allStudents.value[index] = { ...allStudents.value[index], ...s }
      if (editSelectedFile.value && editImagePreview.value) {
        allStudents.value[index].avatar = editImagePreview.value
      }
      allStudents.value = [...allStudents.value].sort((a, b) => a.name.localeCompare(b.name))
      console.log('✅ Aluno atualizado localmente')
    } else {
      console.warn('Aluno não encontrado no array local, recarregando lista...')
      await loadStudents()
    }

    toast({ title: "Perfil Atualizado", description: "Dados salvos com sucesso." })
    isEditModalOpen.value = false
  } catch (error: any) {
    console.error('❌ Erro no PUT:', error)
    toast({ title: "Falha na Edição", description: error.message, variant: "destructive" })
    await loadStudents()
  } finally {
    isSavingEdit.value = false
  }
}


const openDelete = (student: Student) => {
  studentToDelete.value = student
  isDeleteModalOpen.value = true
}

const confirmDelete = async () => {
  if (!studentToDelete.value) return

  const idToDelete = studentToDelete.value.id || studentToDelete.value.registration
  if (!idToDelete) {
    toast({ title: "Erro", description: "ID do aluno não identificado.", variant: "destructive" })
    return
  }

  try {
    const url = `${API_BASE}/alunos/excluirAluno/${idToDelete}`
    console.log('🔴 [DELETE] Enviando para:', url)

    const response = await fetch(url, {
      method: 'DELETE',
      headers: { 'ngrok-skip-browser-warning': 'true' },
      redirect: 'error'
    })

    console.log('🟢 Status:', response.status)

    if (!response.ok) {
      const errText = await response.text()
      throw new Error(`${response.status} - ${errText}`)
    }

    
    const index = allStudents.value.findIndex(student => student.id === studentToDelete.value!.id)
    if (index !== -1) {
      allStudents.value.splice(index, 1)
      console.log('✅ Aluno removido localmente')
    } else {
      console.warn('Aluno não encontrado no array local, recarregando lista...')
      await loadStudents()
    }

    toast({ title: "Removido", description: "Aluno excluído.", variant: "destructive" })
    isDeleteModalOpen.value = false
  } catch (error: any) {
    console.error('❌ Erro no DELETE:', error)
    toast({ title: "Erro", description: error.message, variant: "destructive" })
    await loadStudents()
  }
}

const getStatusStyle = (status: string) => {
  switch (status) {
    case 'Ativo': return 'bg-emerald-100 text-emerald-700'
    case 'Inativo': return 'bg-slate-200 text-slate-700'
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
            
            <template v-if="isLoadingData">
              <TableRow v-for="n in 8" :key="'skel-admin-' + n" class="border-b-slate-100">
                <TableCell class="py-4 px-6">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-slate-200 animate-pulse shrink-0"></div>
                    <div class="flex flex-col gap-2">
                      <div class="h-3 w-32 bg-slate-200 rounded animate-pulse"></div>
                      <div class="h-2 w-24 bg-slate-200 rounded animate-pulse"></div>
                    </div>
                  </div>
                </TableCell>
                <TableCell class="py-4 px-6"><div class="h-3 w-24 bg-slate-200 rounded animate-pulse"></div></TableCell>
                <TableCell class="py-4 px-6"><div class="h-3 w-12 bg-slate-200 rounded animate-pulse"></div></TableCell>
                <TableCell class="py-4 px-6"><div class="h-3 w-20 bg-slate-200 rounded animate-pulse"></div></TableCell>
                <TableCell class="py-4 px-6"><div class="h-5 w-14 bg-slate-200 rounded-full animate-pulse"></div></TableCell>
                <TableCell class="py-4 px-6 text-center"><div class="h-8 w-8 bg-slate-200 rounded-full animate-pulse mx-auto"></div></TableCell>
              </TableRow>
            </template>

            <template v-else-if="filteredStudents.length > 0">
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
            <template v-else>
              <TableRow><TableCell colspan="6" class="h-64 text-center text-slate-400 italic font-medium">Nenhum registro para esta categoria.</TableCell></TableRow>
            </template>
          </TableBody>
        </Table>
      </div>
    </div>

    
    <Dialog v-model:open="isEditModalOpen">
      <DialogContent class="rounded-[2.5rem] w-[95vw] sm:max-w-[650px] border-none shadow-2xl p-0 overflow-hidden font-poppins">
        
        <div class="bg-slate-50 p-6 border-b border-slate-100">
          <DialogHeader>
            <DialogTitle class="text-xl font-bold text-slate-900">Atualizar Cadastro</DialogTitle>
            <DialogDescription class="text-xs">Modifique os dados acadêmicos e a foto do aluno.</DialogDescription>
          </DialogHeader>
        </div>

        <div v-if="studentToEdit" class="p-8 max-h-[70vh] overflow-y-auto custom-scrollbar">
          <div class="flex flex-col md:flex-row gap-8">
            
            <div class="flex flex-col items-center gap-4">
              <div @click="editFileInputRef?.click()" class="relative group cursor-pointer">
                <div class="w-32 h-32 rounded-full bg-white border-4 border-white shadow-lg overflow-hidden relative">
                  <img :src="editImagePreview || studentToEdit.avatar" class="w-full h-full object-cover transition-transform group-hover:scale-105" />
                  <div class="absolute inset-0 bg-black/40 flex flex-col items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                    <Camera class="w-6 h-6 text-white" />
                  </div>
                </div>
                <div class="absolute -bottom-1 -right-1 bg-indigo-600 text-white p-2 rounded-full border-2 border-white shadow-md">
                  <ImagePlus class="w-3 h-3" />
                </div>
                <input type="file" ref="editFileInputRef" @change="handleEditImageChange" accept="image/*" class="hidden" />
              </div>
              <Badge :class="getStatusStyle(studentToEdit.status)" class="uppercase text-[9px] font-black px-4">{{ studentToEdit.status }}</Badge>
            </div>

            <div class="flex-1 grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="sm:col-span-2 space-y-1"><label class="text-[10px] font-bold uppercase text-slate-400 ml-1">Nome Completo</label><Input v-model="studentToEdit.name" class="rounded-xl bg-slate-50 border-slate-200" /></div>
              <div class="space-y-1"><label class="text-[10px] font-bold uppercase text-slate-400 ml-1">Matrícula</label><Input v-model="studentToEdit.registration" class="rounded-xl bg-slate-50 border-slate-200 font-mono" /></div>
              <div class="space-y-1"><label class="text-[10px] font-bold uppercase text-slate-400 ml-1">Semestre</label>
                <Select :model-value="String(studentToEdit.semester)" @update:model-value="v => studentToEdit!.semester = Number(v)"><SelectTrigger class="rounded-xl bg-slate-50"><SelectValue /></SelectTrigger><SelectContent><SelectItem v-for="n in 6" :key="n" :value="String(n)">{{ n }}º Semestre</SelectItem></SelectContent></Select>
              </div>
              <div class="space-y-1"><label class="text-[10px] font-bold uppercase text-slate-400 ml-1">Período</label>
                <Select v-model="studentToEdit.period"><SelectTrigger class="rounded-xl bg-slate-50"><SelectValue /></SelectTrigger><SelectContent><SelectItem value="Matutino">Matutino</SelectItem><SelectItem value="Vespertino">Vespertino</SelectItem><SelectItem value="Noturno">Noturno</SelectItem></SelectContent></Select>
              </div>
              <div class="space-y-1"><label class="text-[10px] font-bold uppercase text-slate-400 ml-1">Status</label>
                <Select v-model="studentToEdit.status"><SelectTrigger class="rounded-xl bg-slate-50"><SelectValue /></SelectTrigger><SelectContent><SelectItem value="Ativo">Ativo</SelectItem><SelectItem value="Inativo">Inativo</SelectItem></SelectContent></Select>
              </div>
            </div>
          </div>
        </div>

        <DialogFooter class="p-6 bg-slate-50 border-t border-slate-100 gap-3">
          <Button variant="ghost" @click="isEditModalOpen = false" class="rounded-xl font-bold">Cancelar</Button>
          <Button @click="saveEdit" :disabled="isSavingEdit" class="bg-[#1A1A3A] hover:bg-[#0A102E] text-white rounded-xl px-8 font-bold shadow-lg transition-all active:scale-95">
            <Loader2 v-if="isSavingEdit" class="w-4 h-4 animate-spin mr-2" />
            Salvar Alterações
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <Dialog v-model:open="isDeleteModalOpen">
      <DialogContent class="rounded-[2.5rem] w-[95vw] sm:max-w-[400px] border-none shadow-2xl text-center p-8 font-poppins">
        <div class="mx-auto flex items-center justify-center h-20 w-20 rounded-full bg-red-50 mb-6 border-4 border-white shadow-inner"><Trash2 class="h-10 w-10 text-red-600" /></div>
        <DialogHeader class="mb-8">
          <DialogTitle class="text-2xl font-bold text-slate-900 text-center">Remover Aluno?</DialogTitle>
          <DialogDescription class="text-center text-sm leading-relaxed">Você está prestes a excluir <strong>{{ studentToDelete?.name }}</strong>. O acesso será revogado permanentemente.</DialogDescription>
        </DialogHeader>
        <div class="flex gap-3 w-full">
          <Button variant="outline" @click="isDeleteModalOpen = false" class="rounded-2xl flex-1 h-12 font-bold">Cancelar</Button>
          <Button @click="confirmDelete" class="bg-red-600 hover:bg-red-700 rounded-2xl flex-1 h-12 text-white font-bold shadow-lg transition-all active:scale-95">Sim, Excluir</Button>
        </div>
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