<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { 
  UserPlus, 
  ArrowLeft, 
  Camera, 
  Save, 
  Loader2, 
  FileJson, 
  UploadCloud, 
  CheckCircle2, 
  DownloadCloud, 
  CalendarDays, 
  ShieldAlert,
  ImagePlus
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

// Controlo de Modos
type RegistrationMode = 'manual' | 'lote'
const activeMode = ref<RegistrationMode>('manual')

// ESTADO: Formulário Manual
const isSubmitting = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const imagePreview = ref<string | null>(null)
const selectedFile = ref<File | null>(null)

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

// ESTADO: Lote
const isUploadingBatch = ref(false)
const batchFile = ref<File | null>(null)
const batchInputRef = ref<HTMLInputElement | null>(null)
const batchExpirationDate = ref('')

// ==========================================
// LÓGICA DE IMAGEM (UPLOAD & CONVERSÃO)
// ==========================================
const triggerImageSelect = () => {
  fileInputRef.value?.click()
}

const handleImageChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    selectedFile.value = file
    
    // Gera preview para a UI
    const reader = new FileReader()
    reader.onload = (e) => {
      imagePreview.value = e.target?.result as string
    }
    reader.readAsDataURL(file)
  }
}

// ==========================================
// INTEGRAÇÃO COM BACKEND (MULTIPART/FORM-DATA)
// ==========================================
const handleSubmit = async () => {
  if (!studentForm.value.name || !studentForm.value.registration || !studentForm.value.course || !studentForm.value.period || !studentForm.value.validUntil) {
    toast({ 
      title: "Dados Incompletos", 
      description: "Por favor, preencha todos os campos obrigatórios e a validade.", 
      variant: "destructive" 
    })
    return
  }

  isSubmitting.value = true
  
  try {
    // Montagem inteligente da Turma
    let pStr = 'NOT';
    if (studentForm.value.period === 'Vespertino') pStr = 'VES';
    else if (studentForm.value.period === 'Matutino') pStr = 'MAT';
    const currentYear = new Date().getFullYear();
    const idTurmaFormatada = `${studentForm.value.course}_${currentYear}_1_${pStr}`;

    // CONSTRUÇÃO DO FORM DATA
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

    // Injeção do Ficheiro Binário
    if (selectedFile.value) {
      formData.append('foto', selectedFile.value) 
    }

    const response = await fetch('https://reply-imprint-skier.ngrok-free.dev/alunos/adicionarAluno', {
      method: 'POST',
      headers: {
        'ngrok-skip-browser-warning': 'true'
      },
      body: formData
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(errorText || 'Erro no Servidor')
    }

    toast({ 
      title: "Aluno Cadastrado!", 
      description: `O perfil foi sincronizado na base de dados com sucesso.` 
    })

    // Reset total da interface
    studentForm.value = { name: '', registration: '', course: '', period: '', status: 'Ativo', contact: '', semester: 1, validUntil: '' }
    imagePreview.value = null
    selectedFile.value = null
    
  } catch (error) {
    console.error(error)
    toast({ 
      title: "Falha na Transmissão", 
      description: "O servidor rejeitou o ficheiro ou os dados enviados. Tente novamente.", 
      variant: "destructive" 
    })
  } finally {
    isSubmitting.value = false
  }
}

const goBack = () => router.back()
</script>

<template>
  <div class="flex flex-col h-full space-y-4 font-poppins overflow-hidden">
    
    <div class="flex items-center justify-between shrink-0">
      <div class="flex items-center gap-3">
        <Button variant="ghost" size="icon" @click="goBack" class="rounded-full h-8 w-8 hover:bg-slate-200 transition-colors">
          <ArrowLeft class="w-4 h-4 text-slate-600" />
        </Button>
        <div>
          <h2 class="text-xl font-bold text-slate-900 tracking-tight">Registro de Identidade</h2>
          <p class="text-slate-500 text-[9px] uppercase font-bold tracking-[0.2em]">Injeção de Dados & Mídia na API</p>
        </div>
      </div>

      <div class="bg-slate-200 p-1 rounded-xl flex items-center shadow-inner">
        <button @click="activeMode = 'manual'" class="px-4 h-8 rounded-lg text-xs font-bold transition-all duration-300" :class="activeMode === 'manual' ? 'bg-white text-[#0A102E] shadow-sm' : 'text-slate-500 hover:text-slate-700'">Manual</button>
        <button @click="activeMode = 'lote'" class="px-4 h-8 rounded-lg text-xs font-bold transition-all duration-300" :class="activeMode === 'lote' ? 'bg-[#0A102E] text-white shadow-sm' : 'text-slate-500 hover:text-slate-700'">Sync Lote</button>
      </div>
    </div>

    <div class="flex-1 min-h-0 overflow-y-auto custom-scrollbar pr-2">
      <div class="w-full max-w-5xl mx-auto bg-white border border-slate-200 rounded-[2.5rem] shadow-sm p-6 sm:p-10 relative overflow-hidden">
        
        <div class="absolute top-[-20%] left-[-10%] w-[50%] h-[50%] bg-indigo-100 rounded-full blur-[100px] pointer-events-none opacity-50"></div>

        <div v-auto-animate>
          
          <div v-if="activeMode === 'manual'" class="flex flex-col lg:flex-row gap-8 relative z-10">
            
            <div class="flex flex-col items-center space-y-6 w-full lg:w-1/3 border-b lg:border-b-0 lg:border-r border-slate-100 pb-6 lg:pb-0 lg:pr-8">
              
              <div @click="triggerImageSelect" class="relative group cursor-pointer mt-4">
                <div class="w-40 h-40 rounded-full bg-slate-50 overflow-hidden flex items-center justify-center border-4 border-white shadow-xl transition-all duration-300 group-hover:ring-4 group-hover:ring-indigo-100 group-hover:shadow-indigo-200/50 relative">
                  
                  <template v-if="imagePreview">
                    <img :src="imagePreview" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" />
                    <div class="absolute inset-0 bg-black/40 flex flex-col items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300 backdrop-blur-[2px]">
                      <Camera class="w-8 h-8 text-white mb-1" />
                      <span class="text-white text-[9px] font-bold uppercase tracking-widest">Alterar Foto</span>
                    </div>
                  </template>
                  
                  <template v-else>
                    <div class="flex flex-col items-center text-slate-300 group-hover:text-indigo-500 transition-colors duration-300">
                      <ImagePlus class="w-12 h-12 mb-2" />
                      <span class="text-[10px] font-bold uppercase tracking-widest text-slate-400 group-hover:text-indigo-600">Upload de Foto</span>
                    </div>
                  </template>

                </div>
                
                <div v-if="!imagePreview" class="absolute bottom-2 right-2 bg-[#1A1A3A] text-white p-2.5 rounded-full shadow-lg border-2 border-white group-hover:scale-110 transition-transform duration-300">
                  <UserPlus class="w-4 h-4" />
                </div>
                
                <input type="file" ref="fileInputRef" @change="handleImageChange" accept="image/*" class="hidden" />
              </div>

              <div class="w-full space-y-1.5 max-w-[240px] pt-2">
                <label class="text-[0.6rem] font-bold text-slate-400 uppercase tracking-widest ml-1 text-center block">Status Institucional</label>
                <Select v-model="studentForm.status">
                  <SelectTrigger class="h-11 rounded-xl border-slate-200 bg-slate-50 hover:bg-slate-100 transition-colors"><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Ativo">Ativo Regular</SelectItem>
                    <SelectItem value="Inativo">Inativo / Trancado</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div class="w-full space-y-1.5 max-w-[240px]">
                <label class="text-[0.6rem] font-bold text-red-500 uppercase tracking-widest ml-1 text-center flex items-center justify-center gap-1.5">
                  <ShieldAlert class="w-3 h-3" /> Validade do Acesso
                </label>
                <Input type="date" v-model="studentForm.validUntil" class="h-11 rounded-xl border-slate-200 bg-red-50/30 text-red-700 font-bold focus-visible:ring-red-500 hover:bg-red-50 transition-colors" />
              </div>
            </div>

            <div class="flex-1 space-y-5">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                
                <div class="flex flex-col space-y-1.5 md:col-span-2">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Nome Completo</label>
                  <Input v-model="studentForm.name" placeholder="Ex: João da Silva Santos" class="h-11 rounded-xl bg-slate-50 border-slate-200 focus-visible:ring-indigo-600 transition-all hover:bg-white" />
                </div>

                <div class="flex flex-col space-y-1.5">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Matrícula Institucional (RA)</label>
                  <Input v-model="studentForm.registration" placeholder="Ex: 1460282113001" class="h-11 rounded-xl bg-slate-50 font-mono border-slate-200 focus-visible:ring-indigo-600 transition-all hover:bg-white" />
                </div>

                <div class="flex flex-col space-y-1.5">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Período de Estudo</label>
                  <Select v-model="studentForm.period">
                    <SelectTrigger class="h-11 rounded-xl bg-slate-50 border-slate-200 hover:bg-white transition-colors"><SelectValue placeholder="Selecione o turno" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Matutino">Matutino</SelectItem>
                      <SelectItem value="Vespertino">Vespertino</SelectItem>
                      <SelectItem value="Noturno">Noturno</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div class="flex flex-col space-y-1.5">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Curso Registado</label>
                  <Select v-model="studentForm.course">
                    <SelectTrigger class="h-11 rounded-xl bg-slate-50 border-slate-200 hover:bg-white transition-colors"><SelectValue placeholder="Sigla do curso" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="DSM">DSM (Dev. de Software Multiplataforma)</SelectItem>
                      <SelectItem value="ADS">ADS (Análise e Desenv. de Sistemas)</SelectItem>
                      <SelectItem value="COMEX">COMEX (Comércio Exterior)</SelectItem>
                      <SelectItem value="GEEM">GEEM (Gestão Empresarial)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div class="flex flex-col space-y-1.5">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Semestre Atual</label>
                  <Select :model-value="String(studentForm.semester)" @update:model-value="v => studentForm.semester = Number(v)">
                    <SelectTrigger class="h-11 rounded-xl bg-slate-50 border-slate-200 hover:bg-white transition-colors"><SelectValue /></SelectTrigger>
                    <SelectContent>
                      <SelectItem v-for="n in 6" :key="n" :value="String(n)">{{ n }}º Semestre / Ciclo</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div class="flex flex-col space-y-1.5 md:col-span-2">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Correio Eletrónico (E-mail)</label>
                  <Input v-model="studentForm.contact" placeholder="aluno@fatec.sp.gov.br" class="h-11 rounded-xl bg-slate-50 border-slate-200 focus-visible:ring-indigo-600 transition-all hover:bg-white" />
                </div>
              </div>

              <div class="pt-6 flex justify-end">
                <Button 
                  @click="handleSubmit" :disabled="isSubmitting"
                  class="bg-[#1A1A3A] hover:bg-[#0A102E] text-white px-12 h-14 rounded-2xl font-bold transition-all shadow-xl shadow-[#1A1A3A]/20 gap-3 w-full sm:w-auto active:scale-[0.97]"
                >
                  <Loader2 v-if="isSubmitting" class="w-5 h-5 animate-spin" />
                  <Save v-else class="w-5 h-5" />
                  {{ isSubmitting ? 'A Sincronizar Ficheiros...' : 'Gravar Perfil no Sistema' }}
                </Button>
              </div>
            </div>
          </div>

          <div v-else class="py-20 text-center relative z-10 flex flex-col items-center justify-center">
            <div class="w-24 h-24 bg-slate-50 rounded-full flex items-center justify-center border border-slate-200 mb-6">
              <UploadCloud class="w-10 h-10 text-slate-400" />
            </div>
            <h3 class="text-2xl font-extrabold text-slate-900 tracking-tight mb-3">Injeção em Massa Indisponível</h3>
            <p class="text-slate-500 max-w-md mx-auto text-sm font-medium mb-8 leading-relaxed">
              O processamento em lote para requisições do tipo <code class="bg-slate-100 text-slate-700 px-1.5 py-0.5 rounded text-xs font-bold">multipart/form-data</code> exige alocação sequencial. Utilize o formulário manual para garantir o upload seguro e correto dos avatares para o Cloudinary.
            </p>
            <Button disabled variant="outline" class="rounded-xl h-11 px-8 border-slate-200 text-slate-400 font-bold bg-slate-50">
              Funcionalidade Suspensa
            </Button>
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