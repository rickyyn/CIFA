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
  DownloadCloud
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

// Controlo de Modos de Cadastro
type RegistrationMode = 'manual' | 'lote'
const activeMode = ref<RegistrationMode>('manual')

// ESTADO: Modo Manual
const isSubmitting = ref(false)
const studentForm = ref({
  id: Date.now(),
  name: '',
  registration: '',
  course: '',
  period: '',
  status: 'Ativo',
  contact: '',
  avatar: ''
})

// ESTADO: Modo em Lote (Upload JSON)
const isUploadingBatch = ref(false)
const selectedFile = ref<File | null>(null)
const parsedBatchData = ref<any[]>([])
const fileInput = ref<HTMLInputElement | null>(null)

// ==========================================
// FUNÇÕES: MODO MANUAL
// ==========================================
const handleSubmit = async () => {
  if (!studentForm.value.name || !studentForm.value.registration || !studentForm.value.course || !studentForm.value.period) {
    toast({
      title: "Erro de Validação",
      description: "Preencha todos os campos obrigatórios antes de prosseguir.",
      variant: "destructive"
    })
    return
  }

  isSubmitting.value = true
  
  try {
    await new Promise(resolve => setTimeout(resolve, 800))
    
    const existingData = JSON.parse(localStorage.getItem('cifa_students') || '[]')
    existingData.push({ ...studentForm.value, id: Date.now() })
    localStorage.setItem('cifa_students', JSON.stringify(existingData))
    
    toast({
      title: "Registro Salvo",
      description: `O perfil de ${studentForm.value.name} foi armazenado com sucesso.`,
    })

    studentForm.value = { id: Date.now(), name: '', registration: '', course: '', period: '', status: 'Ativo', contact: '', avatar: '' }
  } catch (error) {
    toast({
      title: "Falha no Sistema",
      description: "Ocorreu um erro interno ao tentar processar o registro.",
      variant: "destructive"
    })
  } finally {
    isSubmitting.value = false
  }
}

// ==========================================
// FUNÇÕES: MODO EM LOTE (JSON)
// ==========================================
const triggerFileInput = () => {
  if (fileInput.value) fileInput.value.click()
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    const file = target.files[0]
    if (file.type !== 'application/json' && !file.name.endsWith('.json')) {
      toast({ title: "Formato Inválido", description: "Por favor, carregue apenas arquivos .json", variant: "destructive" })
      return
    }
    
    selectedFile.value = file
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const json = JSON.parse(e.target?.result as string)
        if (!Array.isArray(json)) throw new Error("O JSON precisa ser um array de estudantes.")
        parsedBatchData.value = json
        toast({ title: "Ficheiro Lido", description: `${json.length} registos identificados e prontos para importação.` })
      } catch (err) {
        selectedFile.value = null
        parsedBatchData.value = []
        toast({ title: "Erro de Leitura", description: "Estrutura JSON inválida. Certifique-se de usar o template.", variant: "destructive" })
      }
    }
    reader.readAsText(file)
  }
}

const processBatchUpload = async () => {
  if (parsedBatchData.value.length === 0) return

  isUploadingBatch.value = true
  
  try {
    // Latência simulada baseada na quantidade de dados
    await new Promise(resolve => setTimeout(resolve, 1000 + (parsedBatchData.value.length * 50)))
    
    const existingData = JSON.parse(localStorage.getItem('cifa_students') || '[]')
    
    // Tratamento e injeção de IDs únicos para o lote
    const newStudents = parsedBatchData.value.map((student, index) => ({
      ...student,
      id: Date.now() + index, // Garante IDs únicos
      status: student.status || 'Ativo',
      avatar: student.avatar || '',
      semester: student.semester || 1
    }))

    const mergedData = [...existingData, ...newStudents]
    localStorage.setItem('cifa_students', JSON.stringify(mergedData))
    
    toast({
      title: "Importação Concluída",
      description: `${newStudents.length} estudantes cadastrados simultaneamente no sistema.`,
    })

    // Limpar o estado do ficheiro após sucesso
    selectedFile.value = null
    parsedBatchData.value = []
    if (fileInput.value) fileInput.value.value = ''

  } catch (error) {
    toast({
      title: "Falha na Importação",
      description: "Ocorreu um erro ao processar o lote no banco de dados.",
      variant: "destructive"
    })
  } finally {
    isUploadingBatch.value = false
  }
}

const downloadTemplate = () => {
  const template = [
    { name: "Nome Exemplo", registration: "146000000", course: "DSM", period: "Noturno", contact: "email@fatec.sp.gov.br", status: "Ativo", semester: 1, avatar: "" },
    { name: "Outro Exemplo", registration: "146000001", course: "ADS", period: "Matutino", contact: "(13) 90000-0000", status: "Visitante", semester: 3, avatar: "" }
  ]
  const blob = new Blob([JSON.stringify(template, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'template_cifa_estudantes.json'
  link.click()
}

const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="flex flex-col h-full space-y-4 font-poppins overflow-hidden">
    
    <div class="flex items-center justify-between shrink-0">
      <div class="flex items-center gap-3">
        <Button 
          variant="ghost" 
          size="icon" 
          @click="goBack"
          class="rounded-full h-8 w-8 hover:bg-slate-200 transition-colors"
        >
          <ArrowLeft class="w-4 h-4 text-slate-600" />
        </Button>
        <div>
          <h2 class="text-xl font-bold text-slate-900 tracking-tight">Inserção de Dados</h2>
          <p class="text-slate-500 text-[9px] uppercase font-bold tracking-[0.2em]">
            Entrada de dados de alunos
          </p>
        </div>
      </div>

      <div class="bg-slate-200 p-1 rounded-xl flex items-center shadow-inner">
        <button 
          @click="activeMode = 'manual'"
          class="px-4 h-8 rounded-lg text-xs font-bold transition-all duration-300"
          :class="activeMode === 'manual' ? 'bg-white text-[#0A102E] shadow-sm' : 'text-slate-500 hover:text-slate-700'"
        >
          Individual
        </button>
        <button 
          @click="activeMode = 'lote'"
          class="px-4 h-8 rounded-lg text-xs font-bold transition-all duration-300 flex items-center gap-1.5"
          :class="activeMode === 'lote' ? 'bg-[#0A102E] text-white shadow-sm' : 'text-slate-500 hover:text-slate-700'"
        >
          Em Massa
          <span v-if="activeMode === 'lote'" class="px-1.5 py-[1px] rounded bg-indigo-500/30 text-indigo-200 text-[9px] uppercase tracking-widest ml-1">JSON</span>
        </button>
      </div>
    </div>

    <div class="flex-1 min-h-0 overflow-y-auto custom-scrollbar pr-2">
      <div class="w-full max-w-5xl mx-auto bg-white border border-slate-200 rounded-[2.5rem] shadow-sm p-6 sm:p-10 relative overflow-hidden">
        
        <div class="absolute top-[-20%] left-[-10%] w-[50%] h-[50%] bg-indigo-100 rounded-full blur-[100px] pointer-events-none opacity-50"></div>

        <div v-auto-animate>
          
          <div v-if="activeMode === 'manual'" class="flex flex-col lg:flex-row gap-8 relative z-10">
            <div class="flex flex-col items-center space-y-4 w-full lg:w-1/3 border-b lg:border-b-0 lg:border-r border-slate-100 pb-6 lg:pb-0 lg:pr-8">
              
              <div class="relative group mt-4">
                <div class="w-32 h-32 rounded-full bg-slate-100 overflow-hidden flex items-center justify-center border-4 border-white shadow-lg transition-transform group-hover:scale-105">
                  <img v-if="studentForm.avatar" :src="studentForm.avatar" class="w-full h-full object-cover" />
                  <Camera v-else class="w-10 h-10 text-slate-300" />
                </div>
                <div class="absolute bottom-1 right-1 bg-[#1A1A3A] text-white p-2.5 rounded-full shadow-md">
                  <UserPlus class="w-4 h-4" />
                </div>
              </div>
              
              <div class="w-full space-y-1.5 max-w-[240px] pt-4">
                <label class="text-[0.6rem] font-bold text-slate-400 uppercase tracking-widest ml-1 text-center block">
                  Status Inicial
                </label>
                <Select v-model="studentForm.status">
                  <SelectTrigger class="h-11 rounded-xl border-slate-200 bg-slate-50/50">
                    <SelectValue placeholder="Status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Ativo">Ativo</SelectItem>
                    <SelectItem value="Inativo">Inativo</SelectItem>
                    <SelectItem value="Bloqueado">Bloqueado</SelectItem>
                    <SelectItem value="Visitante">Visitante</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div class="flex-1 space-y-5">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div class="flex flex-col space-y-1.5 md:col-span-2">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Nome Completo</label>
                  <Input v-model="studentForm.name" placeholder="Nome do aluno" class="h-11 rounded-xl border-slate-200 bg-slate-50 focus-visible:ring-[#1A1A3A]" />
                </div>

                <div class="flex flex-col space-y-1.5">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Matrícula (RA)</label>
                  <Input v-model="studentForm.registration" placeholder="Ex: 146028..." class="h-11 rounded-xl border-slate-200 bg-slate-50 font-mono focus-visible:ring-[#1A1A3A]" />
                </div>

                <div class="flex flex-col space-y-1.5">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Período Letivo</label>
                  <Select v-model="studentForm.period">
                    <SelectTrigger class="h-11 rounded-xl border-slate-200 bg-slate-50"><SelectValue placeholder="Selecione" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Matutino">Matutino</SelectItem>
                      <SelectItem value="Vespertino">Vespertino</SelectItem>
                      <SelectItem value="Noturno">Noturno</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div class="flex flex-col space-y-1.5 md:col-span-2">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Curso Formativo</label>
                  <Select v-model="studentForm.course">
                    <SelectTrigger class="h-11 rounded-xl border-slate-200 bg-slate-50"><SelectValue placeholder="Selecione" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="DSM">DSM - Desenvolvimento de Software Multiplataforma</SelectItem>
                      <SelectItem value="ADS">ADS - Análise e Desenvolvimento de Sistemas</SelectItem>
                      <SelectItem value="COMEX">COMEX - Comércio Exterior</SelectItem>
                      <SelectItem value="GEEM">GEEM - Gestão Empresarial</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div class="flex flex-col space-y-1.5 md:col-span-2">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Contato Acadêmico</label>
                  <Input v-model="studentForm.contact" placeholder="exemplo@fatec.sp.gov.br" class="h-11 rounded-xl border-slate-200 bg-slate-50 focus-visible:ring-[#1A1A3A]" />
                </div>

                <div class="flex flex-col space-y-1.5 md:col-span-2">
                  <label class="text-[0.65rem] font-bold text-slate-400 uppercase tracking-widest ml-1">URL da Imagem</label>
                  <Input v-model="studentForm.avatar" placeholder="https://..." class="h-11 rounded-xl border-slate-200 bg-slate-50 text-xs focus-visible:ring-[#1A1A3A]" />
                </div>
              </div>

              <div class="pt-4 flex justify-end">
                <Button 
                  @click="handleSubmit" :disabled="isSubmitting"
                  class="bg-[#1A1A3A] hover:bg-[#0F0F24] text-white px-10 h-12 rounded-xl font-bold transition-all shadow-md gap-2 w-full sm:w-auto active:scale-[0.98]"
                >
                  <Loader2 v-if="isSubmitting" class="w-4 h-4 animate-spin" />
                  <Save v-else class="w-4 h-4" />
                  {{ isSubmitting ? 'Processando...' : 'Cadastrar Aluno' }}
                </Button>
              </div>
            </div>
          </div>

          <div v-else class="flex flex-col relative z-10 py-6 min-h-[450px]">
            
            <div class="text-center max-w-lg mx-auto mb-10">
              <h3 class="text-2xl font-extrabold tracking-tight text-slate-900 mb-2">Importação Simultânea</h3>
              <p class="text-sm text-slate-500 font-medium">Cadastre turmas inteiras de uma só vez fazendo upload de um ficheiro JSON devidamente estruturado.</p>
            </div>

            <input type="file" ref="fileInput" @change="handleFileChange" accept=".json" class="hidden" />

            <div 
              @click="triggerFileInput"
              class="w-full max-w-2xl mx-auto border-2 border-dashed rounded-[2.5rem] flex flex-col items-center justify-center p-12 transition-all cursor-pointer bg-slate-50/50 hover:bg-slate-50 hover:border-indigo-400 group"
              :class="selectedFile ? 'border-emerald-500 bg-emerald-50/30' : 'border-slate-300'"
            >
              
              <template v-if="!selectedFile">
                <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center shadow-sm mb-4 group-hover:scale-110 transition-transform duration-300">
                  <UploadCloud class="w-8 h-8 text-indigo-500" />
                </div>
                <h4 class="text-lg font-bold text-slate-800 mb-1">Clique para procurar o ficheiro</h4>
                <p class="text-xs font-bold text-slate-400 uppercase tracking-widest">Apenas formato .JSON suportado</p>
              </template>
              
              <template v-else>
                <div class="w-16 h-16 bg-emerald-100 rounded-full flex items-center justify-center shadow-sm mb-4 group-hover:scale-110 transition-transform duration-300">
                  <FileJson class="w-8 h-8 text-emerald-600" />
                </div>
                <h4 class="text-lg font-bold text-slate-800 mb-1 truncate max-w-xs">{{ selectedFile.name }}</h4>
                <div class="flex items-center gap-2 mt-2">
                  <Badge class="bg-emerald-100 text-emerald-700 border-none font-bold uppercase tracking-widest text-[9px] shadow-none">
                    {{ parsedBatchData.length }} Estudantes Lidos
                  </Badge>
                  <button @click.stop="selectedFile = null; parsedBatchData = []; if(fileInput) fileInput.value = ''" class="text-xs font-bold text-slate-400 hover:text-red-500 transition-colors uppercase tracking-widest ml-2">
                    Trocar ficheiro
                  </button>
                </div>
              </template>

            </div>

            <div class="mt-auto pt-10 flex flex-col sm:flex-row items-center justify-center gap-4 max-w-2xl mx-auto w-full">
              <Button 
                variant="outline" 
                @click="downloadTemplate"
                class="h-12 rounded-xl font-bold border-slate-200 text-slate-600 hover:bg-slate-50 gap-2 w-full sm:w-1/2"
              >
                <DownloadCloud class="w-4 h-4" /> Baixar Template JSON
              </Button>
              
              <Button 
                @click="processBatchUpload" 
                :disabled="!selectedFile || parsedBatchData.length === 0 || isUploadingBatch"
                class="h-12 rounded-xl font-bold text-white gap-2 w-full sm:w-1/2 shadow-md transition-all active:scale-[0.98]"
                :class="selectedFile ? 'bg-emerald-600 hover:bg-emerald-700' : 'bg-slate-800 opacity-50'"
              >
                <Loader2 v-if="isUploadingBatch" class="w-5 h-5 animate-spin" />
                <CheckCircle2 v-else class="w-5 h-5" />
                {{ isUploadingBatch ? 'Injetando Dados...' : 'Processar Importação' }}
              </Button>
            </div>

          </div>

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
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 10px;
}
.custom-scrollbar:hover::-webkit-scrollbar-thumb {
  background-color: #94a3b8;
}

input:focus {
  background-color: white !important;
}
</style>