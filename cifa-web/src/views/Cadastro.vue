<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { 
  UserPlus, 
  ArrowLeft, 
  Camera, 
  Save, 
  Loader2 
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

// Estado do Formulário
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

// Função de Validação e Persistência
const handleSubmit = async () => {
  // Validação rigorosa
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
    // Simulação de latência de rede
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // Lógica de persistência local para teste funcional
    const existingData = JSON.parse(localStorage.getItem('cifa_students') || '[]')
    existingData.push({ ...studentForm.value, id: Date.now() })
    localStorage.setItem('cifa_students', JSON.stringify(existingData))
    
    toast({
      title: "Registro Salvo",
      description: `O perfil de ${studentForm.value.name} foi armazenado com sucesso no banco de dados.`,
    })

    // Limpeza do formulário
    studentForm.value = {
      id: Date.now(),
      name: '',
      registration: '',
      course: '',
      period: '',
      status: 'Ativo',
      contact: '',
      avatar: ''
    }

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

const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="flex flex-col h-full space-y-4 font-nunito animate-in fade-in duration-500 overflow-hidden">
    
    <div class="flex items-center gap-3">
      <Button 
        variant="ghost" 
        size="icon" 
        @click="goBack"
        class="rounded-full h-8 w-8 hover:bg-slate-200"
      >
        <ArrowLeft class="w-4 h-4 text-slate-600" />
      </Button>
      <div>
        <h2 class="text-xl font-bold text-slate-900 tracking-tight">Novo Cadastro</h2>
        <p class="text-slate-500 text-[9px] uppercase font-bold tracking-[0.2em]">Entrada de dados de alunos e visitantes</p>
      </div>
    </div>

    <div class="flex-1 min-h-0 overflow-y-auto custom-scrollbar pr-2">
      <div class="w-full max-w-5xl mx-auto bg-white border border-slate-200 rounded-[2rem] shadow-sm p-6 flex flex-col lg:flex-row gap-8">
        
        <div class="flex flex-col items-center space-y-4 w-full lg:w-1/3 border-r border-slate-100 lg:pr-8">
          <div class="relative">
            <div class="w-32 h-32 rounded-full bg-slate-100 overflow-hidden flex items-center justify-center">
              <img v-if="studentForm.avatar" :src="studentForm.avatar" class="w-full h-full object-cover" />
              <Camera v-else class="w-10 h-10 text-slate-300" />
            </div>
            <div class="absolute bottom-1 right-1 bg-[#1A1A3A] text-white p-2 rounded-full shadow-md">
              <UserPlus class="w-3 h-3" />
            </div>
          </div>
          
          <div class="w-full space-y-1.5">
            <label class="text-[0.6rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Status Operacional</label>
            <Select v-model="studentForm.status">
              <SelectTrigger class="h-10 rounded-xl border-slate-200 bg-slate-50/50">
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
          
          <p class="text-[10px] text-slate-400 text-center leading-normal px-2">
            Certifique-se de que a URL da imagem de perfil seja acessível publicamente para a renderização do avatar.
          </p>
        </div>

        <div class="flex-1 space-y-5">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            
            <div class="flex flex-col space-y-1.5 md:col-span-2">
              <label class="text-[0.6rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Nome Completo</label>
              <Input 
                v-model="studentForm.name"
                placeholder="Nome do aluno ou visitante" 
                class="h-10 rounded-xl border-slate-200 bg-slate-50 focus-visible:ring-[#1A1A3A]"
              />
            </div>

            <div class="flex flex-col space-y-1.5">
              <label class="text-[0.6rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Registro Acadêmico (RA)</label>
              <Input 
                v-model="studentForm.registration"
                placeholder="Ex: 146028..." 
                class="h-10 rounded-xl border-slate-200 bg-slate-50 font-mono"
              />
            </div>

            <div class="flex flex-col space-y-1.5">
              <label class="text-[0.6rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Período Letivo</label>
              <Select v-model="studentForm.period">
                <SelectTrigger class="h-10 rounded-xl border-slate-200 bg-slate-50">
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Matutino">Matutino</SelectItem>
                  <SelectItem value="Vespertino">Vespertino</SelectItem>
                  <SelectItem value="Noturno">Noturno</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div class="flex flex-col space-y-1.5 md:col-span-2">
              <label class="text-[0.6rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Curso / Área de Atuação</label>
              <Select v-model="studentForm.course">
                <SelectTrigger class="h-10 rounded-xl border-slate-200 bg-slate-50">
                  <SelectValue placeholder="Selecione o curso correspondente" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="DSM">DSM - Desenvolvimento de Software Multiplataforma</SelectItem>
                  <SelectItem value="ADS">ADS - Análise e Desenvolvimento de Sistemas</SelectItem>
                  <SelectItem value="COMEX">COMEX - Comércio Exterior</SelectItem>
                  <SelectItem value="GEEM">GEEM - Gestão Empresarial</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div class="flex flex-col space-y-1.5 md:col-span-2">
              <label class="text-[0.6rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Contato (E-mail ou Telefone)</label>
              <Input 
                v-model="studentForm.contact"
                placeholder="exemplo@fatec.sp.gov.br" 
                class="h-10 rounded-xl border-slate-200 bg-slate-50"
              />
            </div>

            <div class="flex flex-col space-y-1.5 md:col-span-2">
              <label class="text-[0.6rem] font-bold text-slate-400 uppercase tracking-widest ml-1">Link da Imagem de Perfil</label>
              <Input 
                v-model="studentForm.avatar"
                placeholder="https://servidor-imagens.com/perfil.jpg" 
                class="h-10 rounded-xl border-slate-200 bg-slate-50 text-[11px]"
              />
            </div>
          </div>

          <div class="pt-2 flex justify-end">
            <Button 
              @click="handleSubmit"
              :disabled="isSubmitting"
              class="bg-[#1A1A3A] hover:bg-[#0F0F24] text-white px-8 h-12 rounded-xl font-bold transition-all shadow-sm gap-2 active:scale-[0.98]"
            >
              <Loader2 v-if="isSubmitting" class="w-4 h-4 animate-spin" />
              <Save v-else class="w-4 h-4" />
              {{ isSubmitting ? 'Salvando...' : 'Salvar Registro' }}
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.font-nunito {
  font-family: 'Nunito', sans-serif;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 10px;
}

input:focus {
  background-color: white !important;
}
</style>