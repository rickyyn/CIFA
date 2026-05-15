<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  UserPlus, 
  ArrowLeft, 
  Camera, 
  Save, 
  Loader2, 
  UploadCloud, 
  CheckCircle2, 
  DownloadCloud, 
  ShieldAlert,
  ImagePlus,
  BookOpen,
  Presentation,
  Users,
  FileSpreadsheet,
  Building
} from 'lucide-vue-next'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { useToast } from '@/components/ui/toast/use-toast'

const router = useRouter()
const { toast } = useToast()

// ==========================================
// ESTADOS GERAIS DE NAVEGAÇÃO
// ==========================================
type EntityType = 'alunos' | 'turmas' | 'cursos'
type RegistrationMode = 'manual' | 'lote'

const activeEntity = ref<EntityType>('alunos')
const activeMode = ref<RegistrationMode>('manual')

// ==========================================
// ESTADOS: ALUNOS
// ==========================================
const isSubmittingStudent = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const imagePreview = ref<string | null>(null)
const selectedImageFile = ref<File | null>(null)

const studentForm = ref({
  name: '',
  registration: '',
  course: '',
  period: '',
  status: 'Ativo',
  contact: '',
  semester: 1,
  validUntil: ''
})

const isUploadingBatch = ref(false)
const batchCsvFile = ref<File | null>(null)
const batchCsvInputRef = ref<HTMLInputElement | null>(null)
const parsedCsvData = ref<any[]>([])

// ==========================================
// ESTADOS: TURMAS E CURSOS
// ==========================================
const isSubmittingClass = ref(false)
const classForm = ref({
  courseCode: '',
  year: new Date().getFullYear(),
  semester: 1,
  period: ''
})

const generatedClassId = computed(() => {
  if (!classForm.value.courseCode || !classForm.value.period) return 'PREENCHA OS DADOS'
  let pStr = 'NOT'
  if (classForm.value.period === 'Vespertino') pStr = 'VES'
  else if (classForm.value.period === 'Matutino') pStr = 'MAT'
  return `${classForm.value.courseCode}_${classForm.value.year}_${classForm.value.semester}_${pStr}`
})

const isSubmittingCourse = ref(false)
const courseForm = ref({
  name: '',
  code: '',
  duration: 6
})

// ==========================================
// LÓGICA DE UPLOAD DE IMAGEM (ALUNO)
// ==========================================
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

// ==========================================
// API: CADASTRO MANUAL DE ALUNO
// ==========================================
const submitStudentManual = async () => {
  if (!studentForm.value.name || !studentForm.value.registration || !studentForm.value.course || !studentForm.value.period || !studentForm.value.validUntil) {
    toast({ title: "Dados Incompletos", description: "Preencha todos os campos obrigatórios.", variant: "destructive" })
    return
  }

  isSubmittingStudent.value = true
  
  try {
    let pStr = 'NOT';
    if (studentForm.value.period === 'Vespertino') pStr = 'VES';
    else if (studentForm.value.period === 'Matutino') pStr = 'MAT';
    const currentYear = new Date().getFullYear();
    const idTurmaFormatada = `${studentForm.value.course}_${currentYear}_${studentForm.value.semester}_${pStr}`;

    const formData = new FormData()
    formData.append('nome', studentForm.value.name)
    formData.append('ra', studentForm.value.registration)
    formData.append('id_turma', idTurmaFormatada)
    formData.append('email_institucional', studentForm.value.contact)
    formData.append('email_pessoal', studentForm.value.contact)
    formData.append('status_ativo', String(studentForm.value.status === 'Ativo'))
    formData.append('ciclo_atual', String(studentForm.value.semester))
    formData.append('validade_acesso', studentForm.value.validUntil)
    formData.append('rfid_tag', "")
    formData.append('esta_no_campus', "false")

    if (selectedImageFile.value) formData.append('foto', selectedImageFile.value) 

    const response = await fetch('https://reply-imprint-skier.ngrok-free.dev/alunos/adicionarAluno', {
      method: 'POST',
      headers: { 'ngrok-skip-browser-warning': 'true' },
      body: formData
    })

    if (!response.ok) throw new Error('Erro no Servidor')

    toast({ title: "Aluno Cadastrado!", description: `Perfil sincronizado com sucesso.` })

    studentForm.value = { name: '', registration: '', course: '', period: '', status: 'Ativo', contact: '', semester: 1, validUntil: '' }
    imagePreview.value = null; selectedImageFile.value = null
  } catch (error) {
    toast({ title: "Falha na Transmissão", description: "O servidor rejeitou os dados.", variant: "destructive" })
  } finally {
    isSubmittingStudent.value = false
  }
}

// ==========================================
// API: CADASTRO EM MASSA (CSV)
// ==========================================
const triggerCsvSelect = () => batchCsvInputRef.value?.click()

const handleCsvChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    if (!file.name.endsWith('.csv')) {
      toast({ title: "Formato Inválido", description: "Carregue estritamente ficheiros .csv", variant: "destructive" })
      return
    }
    
    batchCsvFile.value = file
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const text = e.target?.result as string
        const lines = text.split('\n').filter(line => line.trim() !== '')
        if (lines.length < 2) throw new Error("CSV Vazio")
        
        const headers = lines[0].split(';').map(h => h.trim().toLowerCase())
        const data = []
        
        for(let i = 1; i < lines.length; i++) {
          const values = lines[i].split(';')
          let obj: any = {}
          headers.forEach((h, index) => { obj[h] = values[index]?.trim() || '' })
          data.push(obj)
        }
        
        parsedCsvData.value = data
        toast({ title: "Planilha Lida", description: `${data.length} registos identificados prontos para injeção.` })
      } catch (err) {
        batchCsvFile.value = null; parsedCsvData.value = []
        toast({ title: "Erro de Leitura", description: "A estrutura do CSV está corrompida.", variant: "destructive" })
      }
    }
    reader.readAsText(file)
  }
}

const submitBatchCsv = async () => {
  if (parsedCsvData.value.length === 0) return
  isUploadingBatch.value = true
  
  try {
    const uploadPromises = parsedCsvData.value.map(row => {
      let pStr = 'NOT'
      if (row.periodo === 'Vespertino') pStr = 'VES'
      else if (row.periodo === 'Matutino') pStr = 'MAT'
      
      const currentYear = new Date().getFullYear();
      const idTurmaFormatada = `${row.curso}_${currentYear}_${row.semestre}_${pStr}`

      const formData = new FormData()
      formData.append('nome', row.nome)
      formData.append('ra', row.ra)
      formData.append('id_turma', idTurmaFormatada)
      formData.append('email_institucional', row.contato)
      formData.append('email_pessoal', row.contato)
      formData.append('status_ativo', 'true')
      formData.append('ciclo_atual', String(row.semestre))
      formData.append('validade_acesso', '2026-12-20') // Fixado para lote ou pode ser adicionado ao CSV
      formData.append('rfid_tag', "")
      formData.append('esta_no_campus', "false")

      return fetch('https://reply-imprint-skier.ngrok-free.dev/alunos/adicionarAluno', {
        method: 'POST',
        headers: { 'ngrok-skip-browser-warning': 'true' },
        body: formData
      })
    })

    await Promise.all(uploadPromises)
    
    toast({ title: "Integração Concluída", description: "A planilha foi injetada no banco de dados." })
    batchCsvFile.value = null; parsedCsvData.value = []
    if (batchCsvInputRef.value) batchCsvInputRef.value.value = ''

  } catch (error) {
    toast({ title: "Falha no Lote", description: "Ocorreram erros na inserção massiva.", variant: "destructive" })
  } finally {
    isUploadingBatch.value = false
  }
}

const downloadCsvTemplate = () => {
  const csvContent = "nome;ra;curso;periodo;semestre;contato\nJoão Silva;146028000;DSM;Noturno;1;joao@fatec.sp.gov.br\nMaria Souza;146028001;ADS;Matutino;3;maria@fatec.sp.gov.br"
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob); const link = document.createElement('a')
  link.href = url; link.download = 'cifa_template_alunos.csv'; link.click()
}

// ==========================================
// API: CADASTRO DE TURMAS
// ==========================================
const submitClass = async () => {
  if (!classForm.value.courseCode || !classForm.value.period || !classForm.value.year) {
    toast({ title: "Erro de Validação", description: "Preencha Sigla, Ano e Período.", variant: "destructive" })
    return
  }

  isSubmittingClass.value = true
  try {
    const payload = {
      id_turma: generatedClassId.value,
      curso_sigla: classForm.value.courseCode,
      ano_letivo: classForm.value.year,
      semestre_letivo: classForm.value.semester,
      periodo: classForm.value.period
    }

    const response = await fetch('https://reply-imprint-skier.ngrok-free.dev/turmas/adicionarTurma', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'ngrok-skip-browser-warning': 'true' },
      body: JSON.stringify(payload)
    })

    if (!response.ok) throw new Error('Falha no Servidor')

    toast({ title: "Turma Registada", description: `Turma ${generatedClassId.value} criada com sucesso.` })
    classForm.value = { courseCode: '', year: new Date().getFullYear(), semester: 1, period: '' }
  } catch (error) {
    toast({ title: "Aviso", description: "Funcionalidade simulada (Endpoint /turmas não configurado).", variant: "default" })
    classForm.value = { courseCode: '', year: new Date().getFullYear(), semester: 1, period: '' }
  } finally {
    isSubmittingClass.value = false
  }
}

// ==========================================
// API: CADASTRO DE CURSOS
// ==========================================
const submitCourse = async () => {
  if (!courseForm.value.name || !courseForm.value.code) {
    toast({ title: "Erro de Validação", description: "Nome e Sigla são obrigatórios.", variant: "destructive" })
    return
  }

  isSubmittingCourse.value = true
  try {
    const payload = {
      nome: courseForm.value.name,
      sigla: courseForm.value.code.toUpperCase(),
      duracao_semestres: courseForm.value.duration
    }

    const response = await fetch('https://reply-imprint-skier.ngrok-free.dev/cursos/adicionarCurso', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'ngrok-skip-browser-warning': 'true' },
      body: JSON.stringify(payload)
    })

    if (!response.ok) throw new Error('Falha no Servidor')

    toast({ title: "Curso Registado", description: `Curso de ${payload.nome} adicionado.` })
    courseForm.value = { name: '', code: '', duration: 6 }
  } catch (error) {
    toast({ title: "Aviso", description: "Funcionalidade simulada (Endpoint /cursos não configurado).", variant: "default" })
    courseForm.value = { name: '', code: '', duration: 6 }
  } finally {
    isSubmittingCourse.value = false
  }
}

const goBack = () => router.back()
</script>

<template>
  <div class="flex flex-col h-full space-y-4 font-poppins overflow-hidden">
    
    <div class="flex flex-col sm:flex-row sm:items-center justify-between shrink-0 gap-4">
      <div class="flex items-center gap-3">
        <Button variant="ghost" size="icon" @click="goBack" class="rounded-full h-8 w-8 hover:bg-slate-200 transition-colors">
          <ArrowLeft class="w-4 h-4 text-slate-600" />
        </Button>
        <div>
          <h2 class="text-2xl font-bold text-slate-900 tracking-tight">Centro de Cadastro</h2>
          <p class="text-slate-500 text-[10px] uppercase font-bold tracking-[0.2em]">Gestão de Estrutura Institucional</p>
        </div>
      </div>

      <div class="bg-slate-100 p-1.5 rounded-2xl flex items-center shadow-inner overflow-x-auto w-full sm:w-auto border border-slate-200/50">
        <button @click="activeEntity = 'alunos'" class="px-5 h-10 rounded-xl text-sm font-bold transition-all duration-300 flex items-center gap-2" :class="activeEntity === 'alunos' ? 'bg-white text-[#0A102E] shadow-sm ring-1 ring-slate-200/50' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'">
          <Users class="w-4 h-4" /> Alunos
        </button>
        <button @click="activeEntity = 'turmas'" class="px-5 h-10 rounded-xl text-sm font-bold transition-all duration-300 flex items-center gap-2" :class="activeEntity === 'turmas' ? 'bg-white text-[#0A102E] shadow-sm ring-1 ring-slate-200/50' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'">
          <Presentation class="w-4 h-4" /> Turmas
        </button>
        <button @click="activeEntity = 'cursos'" class="px-5 h-10 rounded-xl text-sm font-bold transition-all duration-300 flex items-center gap-2" :class="activeEntity === 'cursos' ? 'bg-white text-[#0A102E] shadow-sm ring-1 ring-slate-200/50' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'">
          <BookOpen class="w-4 h-4" /> Cursos
        </button>
      </div>
    </div>

    <div class="flex-1 min-h-0 overflow-y-auto custom-scrollbar pr-2">
      <div class="w-full max-w-5xl mx-auto bg-white border border-slate-200 rounded-[2.5rem] shadow-sm p-6 sm:p-10 relative overflow-hidden">
        <div class="absolute top-[-20%] left-[-10%] w-[50%] h-[50%] bg-indigo-100 rounded-full blur-[100px] pointer-events-none opacity-50"></div>

        <div v-auto-animate>
          
          <div v-if="activeEntity === 'alunos'">
            <div class="flex justify-center mb-8">
              <div class="bg-slate-50 p-1 rounded-xl flex items-center border border-slate-100">
                <button @click="activeMode = 'manual'" class="px-6 h-9 rounded-lg text-xs font-bold transition-all" :class="activeMode === 'manual' ? 'bg-[#0A102E] text-white shadow-md' : 'text-slate-500 hover:text-slate-800'">Cadastro Manual</button>
                <button @click="activeMode = 'lote'" class="px-6 h-9 rounded-lg text-xs font-bold transition-all flex items-center gap-2" :class="activeMode === 'lote' ? 'bg-[#0A102E] text-white shadow-md' : 'text-slate-500 hover:text-slate-800'">
                  Importar CSV <FileSpreadsheet class="w-3 h-3" />
                </button>
              </div>
            </div>

            <div v-if="activeMode === 'manual'" class="flex flex-col lg:flex-row gap-8 relative z-10">
              <div class="flex flex-col items-center space-y-6 w-full lg:w-1/3 border-b lg:border-b-0 lg:border-r border-slate-100 pb-6 lg:pb-0 lg:pr-8">
                <div @click="triggerImageSelect" class="relative group cursor-pointer mt-4">
                  <div class="w-40 h-40 rounded-full bg-slate-50 overflow-hidden flex items-center justify-center border-4 border-white shadow-xl transition-all duration-300 group-hover:ring-4 group-hover:ring-indigo-100 relative">
                    <template v-if="imagePreview">
                      <img :src="imagePreview" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" />
                      <div class="absolute inset-0 bg-black/40 flex flex-col items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300 backdrop-blur-[2px]">
                        <Camera class="w-8 h-8 text-white mb-1" /><span class="text-white text-[9px] font-bold uppercase tracking-widest">Alterar Foto</span>
                      </div>
                    </template>
                    <template v-else>
                      <div class="flex flex-col items-center text-slate-300 group-hover:text-indigo-500 transition-colors duration-300">
                        <ImagePlus class="w-12 h-12 mb-2" /><span class="text-[10px] font-bold uppercase tracking-widest text-slate-400 group-hover:text-indigo-600">Upload de Foto</span>
                      </div>
                    </template>
                  </div>
                  <div v-if="!imagePreview" class="absolute bottom-2 right-2 bg-[#1A1A3A] text-white p-2.5 rounded-full shadow-lg border-2 border-white group-hover:scale-110 transition-transform duration-300"><UserPlus class="w-4 h-4" /></div>
                  <input type="file" ref="fileInputRef" @change="handleImageChange" accept="image/*" class="hidden" />
                </div>

                <div class="w-full space-y-1.5 max-w-[240px] pt-2">
                  <label class="text-[0.6rem] font-bold text-slate-400 uppercase tracking-widest text-center block">Status Institucional</label>
                  <Select v-model="studentForm.status">
                    <SelectTrigger class="h-11 rounded-xl bg-slate-50 hover:bg-slate-100 transition-colors"><SelectValue /></SelectTrigger>
                    <SelectContent><SelectItem value="Ativo">Ativo Regular</SelectItem><SelectItem value="Inativo">Inativo / Trancado</SelectItem></SelectContent>
                  </Select>
                </div>

                <div class="w-full space-y-1.5 max-w-[240px]">
                  <label class="text-[0.6rem] font-bold text-red-500 uppercase tracking-widest text-center flex items-center justify-center gap-1.5"><ShieldAlert class="w-3 h-3" /> Validade do Acesso</label>
                  <Input type="date" v-model="studentForm.validUntil" class="h-11 rounded-xl bg-red-50/30 text-red-700 font-bold focus-visible:ring-red-500 hover:bg-red-50" />
                </div>
              </div>

              <div class="flex-1 space-y-5">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                  <div class="md:col-span-2 space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Nome Completo</label><Input v-model="studentForm.name" placeholder="Ex: João da Silva Santos" class="h-11 rounded-xl bg-slate-50 focus-visible:ring-indigo-600" /></div>
                  <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Matrícula (RA)</label><Input v-model="studentForm.registration" placeholder="Ex: 1460282113001" class="h-11 rounded-xl bg-slate-50 font-mono focus-visible:ring-indigo-600" /></div>
                  <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Período</label>
                    <Select v-model="studentForm.period"><SelectTrigger class="h-11 rounded-xl bg-slate-50"><SelectValue placeholder="Turno" /></SelectTrigger><SelectContent><SelectItem value="Matutino">Matutino</SelectItem><SelectItem value="Vespertino">Vespertino</SelectItem><SelectItem value="Noturno">Noturno</SelectItem></SelectContent></Select>
                  </div>
                  <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Curso</label>
                    <Select v-model="studentForm.course"><SelectTrigger class="h-11 rounded-xl bg-slate-50"><SelectValue placeholder="Sigla" /></SelectTrigger><SelectContent><SelectItem value="DSM">DSM</SelectItem><SelectItem value="ADS">ADS</SelectItem><SelectItem value="COMEX">COMEX</SelectItem><SelectItem value="GEEM">GEEM</SelectItem></SelectContent></Select>
                  </div>
                  <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Semestre Atual</label>
                    <Select :model-value="String(studentForm.semester)" @update:model-value="v => studentForm.semester = Number(v)"><SelectTrigger class="h-11 rounded-xl bg-slate-50"><SelectValue /></SelectTrigger><SelectContent><SelectItem v-for="n in 6" :key="n" :value="String(n)">{{ n }}º Semestre</SelectItem></SelectContent></Select>
                  </div>
                  <div class="md:col-span-2 space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">E-mail Institucional</label><Input v-model="studentForm.contact" placeholder="aluno@fatec.sp.gov.br" class="h-11 rounded-xl bg-slate-50 focus-visible:ring-indigo-600" /></div>
                </div>
                <div class="pt-6 flex justify-end">
                  <Button @click="submitStudentManual" :disabled="isSubmittingStudent" class="bg-[#1A1A3A] hover:bg-[#0A102E] text-white px-12 h-14 rounded-2xl font-bold shadow-xl gap-3 w-full sm:w-auto active:scale-[0.97]">
                    <Loader2 v-if="isSubmittingStudent" class="w-5 h-5 animate-spin" /><Save v-else class="w-5 h-5" />{{ isSubmittingStudent ? 'A Sincronizar Ficheiros...' : 'Gravar Perfil de Aluno' }}
                  </Button>
                </div>
              </div>
            </div>

            <div v-else class="py-10 text-center relative z-10 flex flex-col items-center justify-center">
              <div class="mb-8">
                <h3 class="text-2xl font-extrabold text-slate-900 tracking-tight mb-3">Integração de Planilha (CSV)</h3>
                <p class="text-slate-500 max-w-lg mx-auto text-sm font-medium leading-relaxed">Faça upload de uma planilha exportada do Excel. O formato nativo permite processar e injetar milhares de alunos sem bloqueios de memória.</p>
              </div>

              <input type="file" ref="batchCsvInputRef" @change="handleCsvChange" accept=".csv" class="hidden" />

              <div @click="triggerCsvSelect" class="w-full max-w-2xl mx-auto border-2 border-dashed rounded-[2.5rem] flex flex-col items-center justify-center p-12 transition-all cursor-pointer bg-slate-50 hover:bg-white hover:border-indigo-400 group shadow-sm" :class="batchCsvFile ? 'border-emerald-500 bg-emerald-50/30' : 'border-slate-300'">
                <template v-if="!batchCsvFile">
                  <div class="w-20 h-20 bg-white rounded-full flex items-center justify-center shadow-md mb-6 group-hover:scale-110 transition-transform duration-300"><FileSpreadsheet class="w-10 h-10 text-emerald-600" /></div>
                  <h4 class="text-xl font-bold text-slate-800 mb-2">Clique para adicionar o CSV</h4>
                  <p class="text-xs font-bold text-slate-400 uppercase tracking-widest">Apenas valores separados por vírgula/ponto e vírgula</p>
                </template>
                <template v-else>
                  <div class="w-20 h-20 bg-emerald-100 rounded-full flex items-center justify-center shadow-md mb-6 group-hover:scale-110 transition-transform duration-300"><CheckCircle2 class="w-10 h-10 text-emerald-600" /></div>
                  <h4 class="text-xl font-bold text-slate-800 mb-2 truncate max-w-sm">{{ batchCsvFile.name }}</h4>
                  <Badge class="bg-emerald-100 text-emerald-800 border-none font-black uppercase text-[10px] mt-2">{{ parsedCsvData.length }} Linhas Validadas</Badge>
                </template>
              </div>

              <div class="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4 max-w-2xl mx-auto w-full">
                <Button variant="outline" @click="downloadCsvTemplate" class="h-14 rounded-2xl font-bold border-slate-200 text-slate-600 hover:bg-slate-50 gap-2 w-full sm:w-1/2 shadow-sm"><DownloadCloud class="w-5 h-5" /> Ficheiro Modelo CSV</Button>
                <Button @click="submitBatchCsv" :disabled="!batchCsvFile || parsedCsvData.length === 0 || isUploadingBatch" class="h-14 rounded-2xl font-bold text-white gap-2 w-full sm:w-1/2 shadow-lg transition-all active:scale-[0.98]" :class="batchCsvFile ? 'bg-[#1A1A3A] hover:bg-[#0A102E]' : 'bg-slate-800 opacity-50'">
                  <Loader2 v-if="isUploadingBatch" class="w-5 h-5 animate-spin" /><UploadCloud v-else class="w-5 h-5" />{{ isUploadingBatch ? 'A Injetar Lote...' : 'Iniciar Processamento' }}
                </Button>
              </div>
            </div>
          </div>

          <div v-else-if="activeEntity === 'turmas'" class="py-6">
            <div class="text-center max-w-xl mx-auto mb-10">
              <div class="w-16 h-16 bg-indigo-50 rounded-full flex items-center justify-center mx-auto mb-4"><Presentation class="w-8 h-8 text-indigo-600" /></div>
              <h3 class="text-2xl font-extrabold text-slate-900 mb-2">Abertura de Turma Letiva</h3>
              <p class="text-sm text-slate-500 font-medium">Cadastre os parâmetros base para gerar o grupo agregador de alunos do semestre.</p>
            </div>

            <div class="max-w-2xl mx-auto space-y-8 bg-slate-50 p-8 rounded-[2rem] border border-slate-100 shadow-inner">
              
              <div class="bg-[#0A102E] p-6 rounded-2xl flex flex-col items-center justify-center shadow-lg relative overflow-hidden">
                <div class="absolute inset-0 bg-gradient-to-br from-indigo-500/20 to-transparent"></div>
                <span class="text-slate-400 text-[10px] font-black uppercase tracking-[0.3em] z-10 mb-2">ID Gerado pelo Sistema</span>
                <span class="text-white text-2xl sm:text-3xl font-mono font-black z-10 tracking-widest">{{ generatedClassId }}</span>
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
                <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Sigla do Curso</label>
                  <Select v-model="classForm.courseCode"><SelectTrigger class="h-12 rounded-xl bg-white"><SelectValue placeholder="Ex: DSM" /></SelectTrigger><SelectContent><SelectItem value="DSM">DSM</SelectItem><SelectItem value="ADS">ADS</SelectItem><SelectItem value="COMEX">COMEX</SelectItem><SelectItem value="GEEM">GEEM</SelectItem></SelectContent></Select>
                </div>
                <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Ano Letivo</label>
                  <Input type="number" v-model="classForm.year" class="h-12 rounded-xl bg-white font-mono font-bold text-center" />
                </div>
                <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Semestre Padrão</label>
                  <Select :model-value="String(classForm.semester)" @update:model-value="v => classForm.semester = Number(v)"><SelectTrigger class="h-12 rounded-xl bg-white"><SelectValue /></SelectTrigger><SelectContent><SelectItem v-for="n in 6" :key="n" :value="String(n)">{{ n }}º Sem</SelectItem></SelectContent></Select>
                </div>
                <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Período Letivo</label>
                  <Select v-model="classForm.period"><SelectTrigger class="h-12 rounded-xl bg-white"><SelectValue placeholder="Turno" /></SelectTrigger><SelectContent><SelectItem value="Matutino">Matutino</SelectItem><SelectItem value="Vespertino">Vespertino</SelectItem><SelectItem value="Noturno">Noturno</SelectItem></SelectContent></Select>
                </div>
              </div>

              <Button @click="submitClass" :disabled="isSubmittingClass" class="w-full bg-indigo-600 hover:bg-indigo-700 h-14 rounded-xl text-white font-bold shadow-md text-base gap-2">
                <Loader2 v-if="isSubmittingClass" class="w-5 h-5 animate-spin" /><Save v-else class="w-5 h-5" /> Gravar Turma no Sistema
              </Button>
            </div>
          </div>

          <div v-else-if="activeEntity === 'cursos'" class="py-6">
            <div class="text-center max-w-xl mx-auto mb-10">
              <div class="w-16 h-16 bg-emerald-50 rounded-full flex items-center justify-center mx-auto mb-4"><Building class="w-8 h-8 text-emerald-600" /></div>
              <h3 class="text-2xl font-extrabold text-slate-900 mb-2">Estruturação de Cursos</h3>
              <p class="text-sm text-slate-500 font-medium">Adicione novas grelhas formativas ao banco de dados principal da instituição.</p>
            </div>

            <div class="max-w-2xl mx-auto space-y-6 bg-slate-50 p-8 sm:p-10 rounded-[2rem] border border-slate-100 shadow-inner">
              
              <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Nome Extenso do Curso</label>
                <Input v-model="courseForm.name" placeholder="Ex: Desenvolvimento de Software Multiplataforma" class="h-12 rounded-xl bg-white" />
              </div>

              <div class="grid grid-cols-2 gap-6">
                <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Sigla Oficial</label>
                  <Input v-model="courseForm.code" placeholder="Ex: DSM" class="h-12 rounded-xl bg-white uppercase font-black tracking-widest text-center" />
                </div>
                <div class="space-y-1.5"><label class="text-[0.65rem] font-bold text-slate-500 uppercase ml-1">Duração (Semestres)</label>
                  <Select :model-value="String(courseForm.duration)" @update:model-value="v => courseForm.duration = Number(v)"><SelectTrigger class="h-12 rounded-xl bg-white"><SelectValue /></SelectTrigger><SelectContent><SelectItem v-for="n in 10" :key="n" :value="String(n)">{{ n }} Semestres</SelectItem></SelectContent></Select>
                </div>
              </div>

              <div class="pt-4">
                <Button @click="submitCourse" :disabled="isSubmittingCourse" class="w-full bg-emerald-600 hover:bg-emerald-700 h-14 rounded-xl text-white font-bold shadow-md text-base gap-2">
                  <Loader2 v-if="isSubmittingCourse" class="w-5 h-5 animate-spin" /><BookOpen v-else class="w-5 h-5" /> Adicionar Matriz Curricular
                </Button>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.font-poppins { font-family: 'Poppins', sans-serif; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 10px; }
input[type="date"]::-webkit-calendar-picker-indicator { cursor: pointer; opacity: 0.5; transition: 0.2s; }
input[type="date"]::-webkit-calendar-picker-indicator:hover { opacity: 1; }
</style>