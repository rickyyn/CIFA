<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  UserPlus, ArrowLeft, Camera, Save, Loader2, UploadCloud, 
  CheckCircle2, DownloadCloud, ShieldAlert, ImagePlus, 
  BookOpen, Presentation, Users, FileSpreadsheet, Building,
  Pencil, Trash2, X, Search, MoreHorizontal
} from 'lucide-vue-next'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Separator } from '@/components/ui/separator'
import { useToast } from '@/components/ui/toast/use-toast'

const router = useRouter()
const { toast } = useToast()

const API_BASE = 'https://reply-imprint-skier.ngrok-free.dev'
const headers = { 'ngrok-skip-browser-warning': 'true' }

// ==========================================
// ESTADOS GERAIS DE NAVEGAÇÃO
// ==========================================
type EntityType = 'alunos' | 'turmas' | 'cursos'
const activeEntity = ref<EntityType>('alunos')
const activeAlunoMode = ref<'manual' | 'lote'>('manual')

// ==========================================
// 1. LÓGICA DE ALUNOS
// ==========================================
const isSubmittingStudent = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const imagePreview = ref<string | null>(null)
const selectedImageFile = ref<File | null>(null)

// CORREÇÃO: Variáveis padronizadas como String para bater com o Select do Shadcn
const studentForm = ref({
  nome: '', ra: '', id_turma: '', email: '', status: 'Ativo', semestre: '1', validade: ''
})

const triggerImageSelect = () => fileInputRef.value?.click()

const handleImageChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    selectedImageFile.value = file
    const reader = new FileReader()
    reader.onload = (e) => { imagePreview.value = e.target?.result as string }
    reader.readAsDataURL(file)
  }
}

const submitStudentManual = async () => {
  if (!studentForm.value.nome || !studentForm.value.ra || !studentForm.value.id_turma) {
    toast({ title: "Dados Incompletos", description: "Nome, RA e ID da Turma são obrigatórios.", variant: "destructive" })
    return
  }
  isSubmittingStudent.value = true
  try {
    const formData = new FormData()
    formData.append('nome', studentForm.value.nome)
    formData.append('ra', studentForm.value.ra)
    formData.append('id_turma', studentForm.value.id_turma)
    formData.append('email_institucional', studentForm.value.email)
    formData.append('email_pessoal', studentForm.value.email)
    formData.append('status_ativo', String(studentForm.value.status === 'Ativo'))
    
    // CORREÇÃO: Forçando fallback para garantir que nunca envia null ou undefined
    formData.append('ciclo_atual', String(Number(studentForm.value.semestre) || 1))
    
    formData.append('validade_acesso', studentForm.value.validade)
    formData.append('rfid_tag', "")
    formData.append('esta_no_campus', "false")
    
    if (selectedImageFile.value) formData.append('foto', selectedImageFile.value)

    const res = await fetch(`${API_BASE}/alunos/adicionarAluno`, { method: 'POST', headers, body: formData })
    if (!res.ok) throw new Error('Erro na resposta do servidor')
    
    toast({ title: "Aluno Cadastrado!", description: "Sincronização com o banco de dados concluída." })
    studentForm.value = { nome: '', ra: '', id_turma: '', email: '', status: 'Ativo', semestre: '1', validade: '' }
    imagePreview.value = null; selectedImageFile.value = null
  } catch (error) {
    toast({ title: "Falha na Transmissão", description: "O servidor rejeitou os dados.", variant: "destructive" })
  } finally { isSubmittingStudent.value = false }
}

// Lote (CSV)
const isImportingCsv = ref(false)
const csvInputRef = ref<HTMLInputElement | null>(null)
const selectedCsvFile = ref<File | null>(null)

const handleCsvChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    if (!file.name.endsWith('.csv')) {
      toast({ title: "Formato Inválido", description: "Carregue estritamente ficheiros .csv", variant: "destructive" })
      return
    }
    selectedCsvFile.value = file
  }
}

const importCsv = async () => {
  if (!selectedCsvFile.value) return
  isImportingCsv.value = true
  try {
    const formData = new FormData()
    formData.append('arquivo', selectedCsvFile.value)
    
    const res = await fetch(`${API_BASE}/alunos/importar`, { method: 'POST', headers, body: formData })
    if (!res.ok) throw new Error('Erro no processamento do CSV')
    
    toast({ title: "Importação Concluída", description: "A planilha foi processada pelo servidor." })
    selectedCsvFile.value = null
    if (csvInputRef.value) csvInputRef.value.value = ''
  } catch (error) {
    toast({ title: "Erro na Importação", description: "Verifique o formato do ficheiro CSV.", variant: "destructive" })
  } finally { isImportingCsv.value = false }
}

// ==========================================
// 2. LÓGICA DE TURMAS
// ==========================================
const listTurmas = ref<any[]>([])
const isLoadingTurmas = ref(false)

// CORREÇÃO: Strings como padrão para manter o Data Binding do Vue
const classForm = ref({ 
  sigla: '', 
  ano: String(new Date().getFullYear()), 
  semestre: '1', 
  periodo: '' 
})
const isSubmittingClass = ref(false)

const isEditTurmaOpen = ref(false)
const isDeleteTurmaOpen = ref(false)
const turmaToEdit = ref<any>(null)
const turmaToDelete = ref<any>(null)

const generatedClassId = computed(() => {
  if (!classForm.value.sigla || !classForm.value.periodo) return 'ID AUTOMÁTICO'
  let pStr = 'NOT'
  if (classForm.value.periodo === 'Vespertino') pStr = 'VES'
  else if (classForm.value.periodo === 'Matutino') pStr = 'MAT'
  return `${classForm.value.sigla}_${classForm.value.ano}_${classForm.value.semestre}_${pStr}`
})

const fetchTurmas = async () => {
  isLoadingTurmas.value = true
  try {
    const res = await fetch(`${API_BASE}/turmas/verTurmas`, { headers })
    if (res.ok) listTurmas.value = await res.json()
  } catch { 
    // Silenciado até o backend criar a rota
  } finally { isLoadingTurmas.value = false }
}

const submitTurma = async () => {
  if (!classForm.value.sigla || !classForm.value.periodo) return
  isSubmittingClass.value = true
  try {
    // CORREÇÃO: Cast rigoroso para Number com Fallbacks para evitar nullPointerException no Java
    const payload = {
      id: generatedClassId.value,
      curso_sigla: classForm.value.sigla,
      ano_letivo: Number(classForm.value.ano) || new Date().getFullYear(),
      semestre_letivo: Number(classForm.value.semestre) || 1,
      periodo: classForm.value.periodo
    }
    const res = await fetch(`${API_BASE}/turmas/adicionarTurma`, {
      method: 'POST', headers: { ...headers, 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    })
    
    if (!res.ok) {
      const errorTxt = await res.text()
      throw new Error(errorTxt)
    }
    
    toast({ title: "Turma Registada", description: `Turma ${generatedClassId.value} criada com sucesso.` })
    classForm.value = { sigla: '', ano: String(new Date().getFullYear()), semestre: '1', periodo: '' }
    fetchTurmas()
  } catch (error: any) { 
    toast({ title: "Erro na API", description: "Servidor crashou ao salvar. Peça para checar os campos da Turma.", variant: "destructive" }) 
  } finally { isSubmittingClass.value = false }
}

const confirmEditTurma = async () => {
  try {
    const idToEdit = turmaToEdit.value.id || turmaToEdit.value.id_turma
    const payload = {
      id: idToEdit,
      curso_sigla: turmaToEdit.value.curso_sigla,
      ano_letivo: Number(turmaToEdit.value.ano_letivo),
      semestre_letivo: Number(turmaToEdit.value.semestre_letivo),
      periodo: turmaToEdit.value.periodo
    }

    const res = await fetch(`${API_BASE}/turmas/editarTurma/${idToEdit}`, {
      method: 'PUT', headers: { ...headers, 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    })
    if (!res.ok) throw new Error()
    toast({ title: "Turma Atualizada", description: "Dados gravados." })
    isEditTurmaOpen.value = false; fetchTurmas()
  } catch { toast({ title: "Erro", description: "Não foi possível editar.", variant: "destructive" }) }
}

const confirmDeleteTurma = async () => {
  try {
    const res = await fetch(`${API_BASE}/turmas/excluirTurma/${turmaToDelete.value.id || turmaToDelete.value.id_turma}`, { method: 'DELETE', headers })
    if (!res.ok) throw new Error()
    toast({ title: "Turma Excluída", description: "Registro apagado." })
    isDeleteTurmaOpen.value = false; fetchTurmas()
  } catch { toast({ title: "Erro", description: "Não foi possível apagar.", variant: "destructive" }) }
}

// ==========================================
// 3. LÓGICA DE CURSOS
// ==========================================
const listCursos = ref<any[]>([])
const isLoadingCursos = ref(false)

// CORREÇÃO: Duração como string inicial
const courseForm = ref({ nome: '', sigla: '', duracao: '6' })
const isSubmittingCourse = ref(false)

const isEditCursoOpen = ref(false)
const isDeleteCursoOpen = ref(false)
const cursoToEdit = ref<any>(null)
const cursoToDelete = ref<any>(null)

const fetchCursos = async () => {
  isLoadingCursos.value = true
  try {
    const res = await fetch(`${API_BASE}/cursos/verCursos`, { headers })
    if (res.ok) listCursos.value = await res.json()
  } catch { 
    // Silenciado até o backend criar a rota
  } finally { isLoadingCursos.value = false }
}

const submitCourse = async () => {
  if (!courseForm.value.nome || !courseForm.value.sigla) return
  isSubmittingCourse.value = true
  try {
    // CORREÇÃO: Cast seguro para Number
    const payload = { 
      id: courseForm.value.sigla.toUpperCase(),
      nome: courseForm.value.nome, 
      total_ciclos: Number(courseForm.value.duracao) || 6 
    }
    
    const res = await fetch(`${API_BASE}/cursos/adicionarCurso`, {
      method: 'POST', headers: { ...headers, 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    })
    
    if (!res.ok) {
      const errorTxt = await res.text()
      throw new Error(errorTxt)
    }

    toast({ title: "Curso Registado", description: `Curso adicionado ao sistema.` })
    courseForm.value = { nome: '', sigla: '', duracao: '6' }
    fetchCursos()
  } catch (error: any) { 
    toast({ title: "Erro na API", description: "Servidor crashou ao salvar. Verifique as rotas.", variant: "destructive" }) 
  } finally { isSubmittingCourse.value = false }
}

const confirmEditCurso = async () => {
  try {
    const payload = {
      id: cursoToEdit.value.id || cursoToEdit.value.sigla,
      nome: cursoToEdit.value.nome,
      total_ciclos: Number(cursoToEdit.value.total_ciclos || cursoToEdit.value.duracao_semestres)
    }

    const res = await fetch(`${API_BASE}/cursos/editarCurso/${payload.id}`, {
      method: 'PUT', headers: { ...headers, 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    })
    if (!res.ok) throw new Error()
    toast({ title: "Curso Atualizado", description: "Dados gravados." })
    isEditCursoOpen.value = false; fetchCursos()
  } catch { toast({ title: "Erro", description: "Não foi possível editar.", variant: "destructive" }) }
}

const confirmDeleteCurso = async () => {
  try {
    const res = await fetch(`${API_BASE}/cursos/excluirCurso/${cursoToDelete.value.id}`, { method: 'DELETE', headers })
    if (!res.ok) throw new Error()
    toast({ title: "Curso Excluída", description: "Matriz apagada." })
    isDeleteCursoOpen.value = false; fetchCursos()
  } catch { toast({ title: "Erro", description: "Não foi possível apagar.", variant: "destructive" }) }
}

onMounted(() => {
  fetchTurmas()
  fetchCursos()
})

const goBack = () => router.back()
</script>

<template>
  <div class="flex flex-col h-full space-y-4 font-poppins overflow-hidden">
    
    <div class="flex flex-col sm:flex-row sm:items-center justify-between shrink-0 gap-4">
      <div class="flex items-center gap-3">
        <Button variant="ghost" size="icon" @click="goBack" class="rounded-full h-8 w-8 hover:bg-slate-200 transition-colors"><ArrowLeft class="w-4 h-4 text-slate-600" /></Button>
        <div>
          <h2 class="text-2xl font-bold text-slate-900 tracking-tight">Centro de Gestão</h2>
          <p class="text-slate-500 text-[10px] uppercase font-bold tracking-[0.2em]">Configuração de Estruturas</p>
        </div>
      </div>

      <div class="bg-slate-100 p-1.5 rounded-2xl flex items-center shadow-inner overflow-x-auto w-full sm:w-auto border border-slate-200/50">
        <button @click="activeEntity = 'alunos'" class="px-5 h-10 rounded-xl text-sm font-bold transition-all duration-300 flex items-center gap-2" :class="activeEntity === 'alunos' ? 'bg-white text-[#0A102E] shadow-sm ring-1 ring-slate-200/50' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'"><Users class="w-4 h-4" /> Alunos</button>
        <button @click="activeEntity = 'turmas'" class="px-5 h-10 rounded-xl text-sm font-bold transition-all duration-300 flex items-center gap-2" :class="activeEntity === 'turmas' ? 'bg-white text-[#0A102E] shadow-sm ring-1 ring-slate-200/50' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'"><Presentation class="w-4 h-4" /> Turmas</button>
        <button @click="activeEntity = 'cursos'" class="px-5 h-10 rounded-xl text-sm font-bold transition-all duration-300 flex items-center gap-2" :class="activeEntity === 'cursos' ? 'bg-white text-[#0A102E] shadow-sm ring-1 ring-slate-200/50' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'"><BookOpen class="w-4 h-4" /> Cursos</button>
      </div>
    </div>

    <div class="flex-1 min-h-0 overflow-y-auto custom-scrollbar pr-2 pb-10">
      
      <div v-auto-animate class="w-full max-w-6xl mx-auto space-y-8">
        
        <div v-if="activeEntity === 'alunos'" class="bg-white border border-slate-200 rounded-[2.5rem] shadow-sm p-6 sm:p-10 relative overflow-hidden">
          <div class="absolute top-[-20%] left-[-10%] w-[50%] h-[50%] bg-indigo-100 rounded-full blur-[100px] pointer-events-none opacity-50"></div>
          
          <div class="flex justify-center mb-8 relative z-10">
            <div class="bg-slate-50 p-1 rounded-xl flex items-center border border-slate-100">
              <button @click="activeAlunoMode = 'manual'" class="px-6 h-9 rounded-lg text-xs font-bold transition-all" :class="activeAlunoMode === 'manual' ? 'bg-[#0A102E] text-white shadow-md' : 'text-slate-500 hover:text-slate-800'">Manual</button>
              <button @click="activeAlunoMode = 'lote'" class="px-6 h-9 rounded-lg text-xs font-bold transition-all flex items-center gap-2" :class="activeAlunoMode === 'lote' ? 'bg-[#0A102E] text-white shadow-md' : 'text-slate-500 hover:text-slate-800'">Importar CSV</button>
            </div>
          </div>

          <div v-if="activeAlunoMode === 'manual'" class="flex flex-col lg:flex-row gap-8 relative z-10">
            <div class="flex flex-col items-center space-y-6 w-full lg:w-1/3 border-b lg:border-b-0 lg:border-r border-slate-100 pb-6 lg:pb-0 lg:pr-8">
              <div @click="triggerImageSelect" class="relative group cursor-pointer mt-4">
                <div class="w-40 h-40 rounded-full bg-slate-50 overflow-hidden flex items-center justify-center border-4 border-white shadow-xl transition-all duration-300 group-hover:ring-4 group-hover:ring-indigo-100 relative">
                  <template v-if="imagePreview">
                    <img :src="imagePreview" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" />
                    <div class="absolute inset-0 bg-black/40 flex flex-col items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"><Camera class="w-8 h-8 text-white mb-1" /><span class="text-white text-[9px] font-bold uppercase tracking-widest">Alterar Foto</span></div>
                  </template>
                  <template v-else>
                    <div class="flex flex-col items-center text-slate-300 group-hover:text-indigo-500 transition-colors"><ImagePlus class="w-12 h-12 mb-2" /><span class="text-[10px] font-bold uppercase tracking-widest text-slate-400 group-hover:text-indigo-600">Upload Foto</span></div>
                  </template>
                </div>
                <div v-if="!imagePreview" class="absolute bottom-2 right-2 bg-[#1A1A3A] text-white p-2.5 rounded-full shadow-lg border-2 border-white"><UserPlus class="w-4 h-4" /></div>
                <input type="file" ref="fileInputRef" @change="handleImageChange" accept="image/*" class="hidden" />
              </div>

              <div class="w-full space-y-1.5 max-w-[240px] pt-2">
                <label class="text-[0.6rem] font-bold text-slate-400 uppercase text-center block">Status Institucional</label>
                <Select v-model="studentForm.status"><SelectTrigger class="h-11 rounded-xl bg-slate-50"><SelectValue /></SelectTrigger><SelectContent><SelectItem value="Ativo">Ativo Regular</SelectItem><SelectItem value="Inativo">Inativo / Trancado</SelectItem></SelectContent></Select>
              </div>

              <div class="w-full space-y-1.5 max-w-[240px]">
                <label class="text-[0.6rem] font-bold text-red-500 uppercase text-center flex items-center justify-center gap-1.5"><ShieldAlert class="w-3 h-3" /> Validade do Acesso</label>
                <Input type="date" v-model="studentForm.validade" class="h-11 rounded-xl bg-red-50/30 text-red-700 font-bold focus-visible:ring-red-500 hover:bg-red-50" />
              </div>
            </div>

            <div class="flex-1 space-y-5">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div class="md:col-span-2 space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-400 uppercase">Nome Completo</label><Input v-model="studentForm.nome" placeholder="Ex: João Silva" class="h-11 rounded-xl bg-slate-50" /></div>
                <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-400 uppercase">Matrícula (RA)</label><Input v-model="studentForm.ra" placeholder="Ex: 146028..." class="h-11 rounded-xl bg-slate-50 font-mono" /></div>
                <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-400 uppercase">ID Turma</label><Input v-model="studentForm.id_turma" placeholder="Ex: DSM_2026_1_VES" class="h-11 rounded-xl bg-slate-50 uppercase" /></div>
                <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-400 uppercase">Semestre Atual</label>
                  <Select v-model="studentForm.semestre"><SelectTrigger class="h-11 rounded-xl bg-slate-50"><SelectValue /></SelectTrigger><SelectContent><SelectItem v-for="n in 6" :key="n" :value="String(n)">{{ n }}º Semestre</SelectItem></SelectContent></Select>
                </div>
                <div class="md:col-span-2 space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-400 uppercase">E-mail Institucional</label><Input v-model="studentForm.email" placeholder="aluno@fatec.sp.gov.br" class="h-11 rounded-xl bg-slate-50" /></div>
              </div>
              <div class="pt-6 flex justify-end">
                <Button @click="submitStudentManual" :disabled="isSubmittingStudent" class="bg-[#1A1A3A] hover:bg-[#0A102E] text-white px-12 h-14 rounded-2xl font-bold shadow-xl gap-3 w-full sm:w-auto active:scale-[0.97]">
                  <Loader2 v-if="isSubmittingStudent" class="w-5 h-5 animate-spin" /><Save v-else class="w-5 h-5" /> Salvar Aluno
                </Button>
              </div>
            </div>
          </div>

          <div v-else class="py-10 text-center relative z-10 flex flex-col items-center justify-center">
            <div class="mb-8">
              <h3 class="text-2xl font-extrabold text-slate-900 mb-3">Importação Nativa de CSV</h3>
              <p class="text-slate-500 max-w-lg mx-auto text-sm leading-relaxed">Envie o ficheiro CSV para o endpoint seguro do backend realizar a injeção diretamente no Firebase.</p>
            </div>
            <input type="file" ref="csvInputRef" @change="handleCsvChange" accept=".csv" class="hidden" />
            <div @click="() => csvInputRef?.click()" class="w-full max-w-2xl mx-auto border-2 border-dashed rounded-[2.5rem] flex flex-col items-center justify-center p-12 cursor-pointer bg-slate-50 hover:bg-white hover:border-indigo-400 transition-all shadow-sm" :class="selectedCsvFile ? 'border-emerald-500 bg-emerald-50/30' : 'border-slate-300'">
              <template v-if="!selectedCsvFile">
                <div class="w-20 h-20 bg-white rounded-full flex items-center justify-center shadow-md mb-6"><FileSpreadsheet class="w-10 h-10 text-indigo-400" /></div>
                <h4 class="text-xl font-bold text-slate-800 mb-2">Clique para adicionar o CSV</h4>
              </template>
              <template v-else>
                <div class="w-20 h-20 bg-emerald-100 rounded-full flex items-center justify-center shadow-md mb-6"><CheckCircle2 class="w-10 h-10 text-emerald-600" /></div>
                <h4 class="text-xl font-bold text-slate-800 mb-2 truncate">{{ selectedCsvFile.name }}</h4>
              </template>
            </div>
            <div class="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4 max-w-2xl mx-auto w-full">
              <Button @click="importCsv" :disabled="!selectedCsvFile || isImportingCsv" class="h-14 rounded-2xl font-bold text-white gap-2 w-full shadow-lg transition-all" :class="selectedCsvFile ? 'bg-[#1A1A3A]' : 'bg-slate-800 opacity-50'">
                <Loader2 v-if="isImportingCsv" class="w-5 h-5 animate-spin" /><UploadCloud v-else class="w-5 h-5" /> Enviar para Processamento
              </Button>
            </div>
          </div>
        </div>

        <div v-else-if="activeEntity === 'turmas'" class="space-y-6">
          <div class="bg-white border border-slate-200 rounded-[2.5rem] shadow-sm p-8 sm:p-10 relative overflow-hidden">
            <div class="text-center mb-8 flex flex-col items-center"><div class="w-16 h-16 bg-indigo-50 rounded-full flex items-center justify-center mb-4"><Presentation class="w-8 h-8 text-indigo-600" /></div><h3 class="text-2xl font-extrabold text-slate-900 mb-2">Nova Turma</h3></div>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-6 max-w-3xl mx-auto">
              <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Sigla do Curso</label><Input v-model="classForm.sigla" placeholder="Ex: DSM" class="h-12 rounded-xl bg-slate-50 uppercase" /></div>
              <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Ano Letivo</label><Input type="number" v-model="classForm.ano" class="h-12 rounded-xl bg-slate-50" /></div>
              <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Semestre Padrão</label><Select v-model="classForm.semestre"><SelectTrigger class="h-12 rounded-xl bg-slate-50"><SelectValue /></SelectTrigger><SelectContent><SelectItem v-for="n in 6" :key="n" :value="String(n)">{{ n }}º Sem</SelectItem></SelectContent></Select></div>
              <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Período Letivo</label><Select v-model="classForm.periodo"><SelectTrigger class="h-12 rounded-xl bg-slate-50"><SelectValue placeholder="Turno" /></SelectTrigger><SelectContent><SelectItem value="Matutino">Matutino</SelectItem><SelectItem value="Vespertino">Vespertino</SelectItem><SelectItem value="Noturno">Noturno</SelectItem></SelectContent></Select></div>
            </div>
            <div class="max-w-3xl mx-auto mt-6 flex flex-col sm:flex-row gap-4 items-center">
              <div class="flex-1 bg-[#0A102E] h-14 rounded-xl flex items-center justify-center px-4 w-full"><span class="text-slate-400 text-[10px] uppercase font-bold mr-3 hidden sm:inline">ID Gerado:</span><span class="text-white font-mono font-black tracking-wider">{{ generatedClassId }}</span></div>
              <Button @click="submitTurma" :disabled="isSubmittingClass" class="w-full sm:w-auto px-10 bg-indigo-600 hover:bg-indigo-700 h-14 rounded-xl text-white font-bold shadow-md"><Loader2 v-if="isSubmittingClass" class="w-5 h-5 animate-spin mr-2" /> Salvar Turma</Button>
            </div>
          </div>

          <div class="bg-white border border-slate-200 rounded-[2rem] shadow-sm overflow-hidden p-6">
            <h4 class="text-lg font-bold text-slate-800 mb-4 px-2">Turmas Registadas</h4>
            <div class="overflow-x-auto rounded-xl border border-slate-100">
              <Table>
                <TableHeader class="bg-slate-50"><TableRow><TableHead>ID da Turma</TableHead><TableHead>Curso</TableHead><TableHead>Período</TableHead><TableHead class="text-right">Ações</TableHead></TableRow></TableHeader>
                <TableBody>
                  <TableRow v-for="t in listTurmas" :key="t.id || t.id_turma">
                    <TableCell class="font-mono font-bold">{{ t.id || t.id_turma }}</TableCell>
                    <TableCell>{{ t.curso_sigla }}</TableCell>
                    <TableCell>{{ t.periodo }}</TableCell>
                    <TableCell class="text-right">
                      <DropdownMenu>
                        <DropdownMenuTrigger as-child><Button variant="ghost" class="h-8 w-8 p-0 rounded-full"><MoreHorizontal class="w-4 h-4" /></Button></DropdownMenuTrigger>
                        <DropdownMenuContent align="end" class="rounded-xl font-poppins">
                          <DropdownMenuItem @click="() => { turmaToEdit = {...t}; isEditTurmaOpen = true }" class="cursor-pointer"><Pencil class="w-4 h-4 mr-2 text-indigo-600" /> Editar</DropdownMenuItem>
                          <Separator class="my-1" />
                          <DropdownMenuItem @click="() => { turmaToDelete = t; isDeleteTurmaOpen = true }" class="cursor-pointer text-red-600"><Trash2 class="w-4 h-4 mr-2" /> Excluir</DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                  <TableRow v-if="listTurmas.length === 0"><TableCell colspan="4" class="text-center py-10 text-slate-400">Nenhuma turma encontrada na API.</TableCell></TableRow>
                </TableBody>
              </Table>
            </div>
          </div>
        </div>

        <div v-else-if="activeEntity === 'cursos'" class="space-y-6">
          <div class="bg-white border border-slate-200 rounded-[2.5rem] shadow-sm p-8 sm:p-10 relative overflow-hidden">
            <div class="text-center mb-8 flex flex-col items-center"><div class="w-16 h-16 bg-emerald-50 rounded-full flex items-center justify-center mb-4"><Building class="w-8 h-8 text-emerald-600" /></div><h3 class="text-2xl font-extrabold text-slate-900 mb-2">Novo Curso</h3></div>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-6 max-w-3xl mx-auto">
              <div class="sm:col-span-2 space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Nome Extenso</label><Input v-model="courseForm.nome" placeholder="Ex: Comércio Exterior" class="h-12 rounded-xl bg-slate-50" /></div>
              <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Sigla / ID</label><Input v-model="courseForm.sigla" placeholder="Ex: COMEX" class="h-12 rounded-xl bg-slate-50 uppercase" /></div>
              <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Total de Ciclos (Semestres)</label><Input type="number" v-model="courseForm.duracao" class="h-12 rounded-xl bg-slate-50" /></div>
            </div>
            <div class="max-w-3xl mx-auto mt-6 flex justify-end">
              <Button @click="submitCourse" :disabled="isSubmittingCourse" class="w-full sm:w-auto px-12 bg-emerald-600 hover:bg-emerald-700 h-14 rounded-xl text-white font-bold shadow-md"><Loader2 v-if="isSubmittingCourse" class="w-5 h-5 animate-spin mr-2" /> Salvar Curso</Button>
            </div>
          </div>

          <div class="bg-white border border-slate-200 rounded-[2rem] shadow-sm overflow-hidden p-6">
            <h4 class="text-lg font-bold text-slate-800 mb-4 px-2">Matrizes Curriculares</h4>
            <div class="overflow-x-auto rounded-xl border border-slate-100">
              <Table>
                <TableHeader class="bg-slate-50"><TableRow><TableHead>ID (Sigla)</TableHead><TableHead>Nome do Curso</TableHead><TableHead>Total de Ciclos</TableHead><TableHead class="text-right">Ações</TableHead></TableRow></TableHeader>
                <TableBody>
                  <TableRow v-for="c in listCursos" :key="c.id">
                    <TableCell class="font-bold text-emerald-700">{{ c.id }}</TableCell>
                    <TableCell>{{ c.nome }}</TableCell>
                    <TableCell>{{ c.total_ciclos }} Semestres</TableCell>
                    <TableCell class="text-right">
                      <DropdownMenu>
                        <DropdownMenuTrigger as-child><Button variant="ghost" class="h-8 w-8 p-0 rounded-full"><MoreHorizontal class="w-4 h-4" /></Button></DropdownMenuTrigger>
                        <DropdownMenuContent align="end" class="rounded-xl font-poppins">
                          <DropdownMenuItem @click="() => { cursoToEdit = {...c}; isEditCursoOpen = true }" class="cursor-pointer"><Pencil class="w-4 h-4 mr-2 text-indigo-600" /> Editar</DropdownMenuItem>
                          <Separator class="my-1" />
                          <DropdownMenuItem @click="() => { cursoToDelete = c; isDeleteCursoOpen = true }" class="cursor-pointer text-red-600"><Trash2 class="w-4 h-4 mr-2" /> Excluir</DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                  <TableRow v-if="listCursos.length === 0"><TableCell colspan="4" class="text-center py-10 text-slate-400">Nenhum curso encontrado na API.</TableCell></TableRow>
                </TableBody>
              </Table>
            </div>
          </div>
        </div>

      </div>
    </div>

    <Dialog v-model:open="isEditTurmaOpen">
      <DialogContent class="rounded-[2rem] p-6 font-poppins">
        <DialogHeader><DialogTitle>Editar Turma</DialogTitle></DialogHeader>
        <div class="space-y-4 py-4" v-if="turmaToEdit">
          <Input v-model="turmaToEdit.id" disabled class="bg-slate-100" />
          <Select v-model="turmaToEdit.periodo"><SelectTrigger><SelectValue/></SelectTrigger><SelectContent><SelectItem value="MAT">Matutino</SelectItem><SelectItem value="VES">Vespertino</SelectItem><SelectItem value="NOT">Noturno</SelectItem></SelectContent></Select>
        </div>
        <DialogFooter><Button variant="ghost" @click="isEditTurmaOpen = false">Cancelar</Button><Button @click="confirmEditTurma" class="bg-[#1A1A3A] text-white">Salvar</Button></DialogFooter>
      </DialogContent>
    </Dialog>
    <Dialog v-model:open="isDeleteTurmaOpen">
      <DialogContent class="rounded-[2rem] p-8 text-center font-poppins">
        <Trash2 class="w-12 h-12 text-red-500 mx-auto mb-4" />
        <h3 class="text-xl font-bold mb-2">Excluir Turma?</h3>
        <p class="text-sm text-slate-500 mb-6">A turma {{ turmaToDelete?.id || turmaToDelete?.id_turma }} será apagada.</p>
        <div class="flex gap-3"><Button variant="outline" class="flex-1" @click="isDeleteTurmaOpen = false">Cancelar</Button><Button class="flex-1 bg-red-600 text-white" @click="confirmDeleteTurma">Excluir</Button></div>
      </DialogContent>
    </Dialog>

    <Dialog v-model:open="isEditCursoOpen">
      <DialogContent class="rounded-[2rem] p-6 font-poppins">
        <DialogHeader><DialogTitle>Editar Curso</DialogTitle></DialogHeader>
        <div class="space-y-4 py-4" v-if="cursoToEdit">
          <Input v-model="cursoToEdit.nome" placeholder="Nome Completo" />
          <div class="grid grid-cols-2 gap-4">
            <Input v-model="cursoToEdit.id" disabled class="bg-slate-100 uppercase" placeholder="ID/Sigla" />
            <Input type="number" v-model="cursoToEdit.total_ciclos" placeholder="Total Ciclos" />
          </div>
        </div>
        <DialogFooter><Button variant="ghost" @click="isEditCursoOpen = false">Cancelar</Button><Button @click="confirmEditCurso" class="bg-emerald-600 text-white hover:bg-emerald-700">Salvar</Button></DialogFooter>
      </DialogContent>
    </Dialog>
    <Dialog v-model:open="isDeleteCursoOpen">
      <DialogContent class="rounded-[2rem] p-8 text-center font-poppins">
        <Trash2 class="w-12 h-12 text-red-500 mx-auto mb-4" />
        <h3 class="text-xl font-bold mb-2">Excluir Curso?</h3>
        <p class="text-sm text-slate-500 mb-6">O curso {{ cursoToDelete?.nome }} será apagado.</p>
        <div class="flex gap-3"><Button variant="outline" class="flex-1" @click="isDeleteCursoOpen = false">Cancelar</Button><Button class="flex-1 bg-red-600 text-white" @click="confirmDeleteCurso">Excluir</Button></div>
      </DialogContent>
    </Dialog>

  </div>
</template>

<style scoped>
.font-poppins { font-family: 'Poppins', sans-serif; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 10px; }
input[type="date"]::-webkit-calendar-picker-indicator { cursor: pointer; opacity: 0.5; }
</style>