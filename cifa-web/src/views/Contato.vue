<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ArrowLeft, Search, Mail, MailOpen, Trash2, Reply, CheckCircle2, 
  Clock, User, ShieldAlert, Filter, Send, AlertCircle, Info, Smartphone,
  KeyRound, ShieldCheck, MailWarning
} from 'lucide-vue-next'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { useToast } from '@/components/ui/toast/use-toast'
import { Separator } from '@/components/ui/separator'

const router = useRouter()
const { toast } = useToast()

const API_BASE = 'http://localhost:8080'
const headers = { 'ngrok-skip-browser-warning': 'true', 'Content-Type': 'application/json' }

// ==========================================
// CONTROLE DE ABAS MESTRES
// ==========================================
type SupportView = 'mensagens' | 'senhas'
const activeSupportView = ref<SupportView>('mensagens')

// ==========================================
// 1. LÓGICA DE MENSAGENS (API)
// ==========================================
interface ContactMessage {
  id: string
  senderName: string
  senderEmail: string
  ra: string | null
  subject: string
  message: string
  timestamp: Date
  isRead: boolean
  category: string
  source: string
  isReplied: boolean
  replyText: string
  replyTimestamp?: Date
  _rawData?: any 
}

const searchQuery = ref('')
const activeFilter = ref<'todas' | 'nao_lidas' | 'lidas'>('todas')
const selectedMessage = ref<ContactMessage | null>(null)
const isLoadingData = ref(true)
const messages = ref<ContactMessage[]>([])

const isReplying = ref(false)
const replyText = ref('')
const isSendingReply = ref(false)

const fetchMessages = async () => {
  isLoadingData.value = true
  try {
    const response = await fetch(`${API_BASE}/mensagem/exibirContatos`, { headers: { 'ngrok-skip-browser-warning': 'true' } })
    if (!response.ok) throw new Error('Falha ao conectar')
    
    const data = await response.json()
    const dataArray = Array.isArray(data) ? data : []

    messages.value = dataArray.map((msg: any) => {
      let msgDate = new Date()
      if (msg.timestamp && msg.timestamp.seconds) msgDate = new Date(msg.timestamp.seconds * 1000)
      else if (msg.data_envio || msg.createdAt) msgDate = new Date(msg.data_envio || msg.createdAt)

      return {
        id: msg.id || Math.random().toString(36).substring(7),
        senderName: msg.nome || 'Usuário Desconhecido',
        senderEmail: msg.email || 'sem-email@fatec.sp.gov.br',
        ra: msg.ra || null,
        subject: msg.assunto || 'Sem Assunto',
        message: msg.mensagem || msg.texto || '',
        timestamp: msgDate,
        isRead: msg.lida || msg.isRead || false,
        category: msg.categoria || 'Suporte',
        source: msg.origem || 'App Mobile',
        isReplied: msg.respondida || msg.isReplied || false,
        replyText: msg.resposta || '',
        replyTimestamp: msg.data_resposta ? new Date(msg.data_resposta) : undefined,
        _rawData: msg
      }
    }).sort((a: ContactMessage, b: ContactMessage) => b.timestamp.getTime() - a.timestamp.getTime())

  } catch (error: any) {
    console.error(error)
  } finally {
    isLoadingData.value = false
  }
}

const updateMessageInAPI = async (msg: ContactMessage) => {
  try {
    const payload = { ...msg._rawData, lida: msg.isRead, respondida: msg.isReplied, resposta: msg.replyText }
    await fetch(`${API_BASE}/mensagem/editarContato/${msg.id}`, { method: 'PUT', headers, body: JSON.stringify(payload) })
  } catch (error) { console.error("Erro no PUT", error) }
}

const filteredMessages = computed(() => {
  return messages.value.filter(msg => {
    const matchesSearch = msg.senderName.toLowerCase().includes(searchQuery.value.toLowerCase()) || msg.subject.toLowerCase().includes(searchQuery.value.toLowerCase())
    let matchesFilter = true
    if (activeFilter.value === 'nao_lidas') matchesFilter = !msg.isRead
    if (activeFilter.value === 'lidas') matchesFilter = msg.isRead
    return matchesSearch && matchesFilter
  })
})

const unreadCount = computed(() => messages.value.filter(m => !m.isRead).length)

const selectMessage = (msg: ContactMessage) => {
  selectedMessage.value = msg
  if (!msg.isRead) {
    msg.isRead = true
    updateMessageInAPI(msg)
  }
}

watch(selectedMessage, () => { isReplying.value = false; replyText.value = '' })

const deleteMessage = (id: string) => {
  messages.value = messages.value.filter(m => m.id !== id)
  if (selectedMessage.value?.id === id) selectedMessage.value = null
  toast({ title: "Mensagem Ocultada", description: "A solicitação foi removida da sua visão." })
}

const startReply = () => { isReplying.value = true }
const cancelReply = () => { isReplying.value = false; replyText.value = '' }

const sendReply = async () => {
  if (!replyText.value.trim() || !selectedMessage.value) return
  isSendingReply.value = true
  try {
    const msgRef = selectedMessage.value
    msgRef.isReplied = true; msgRef.replyText = replyText.value; msgRef.replyTimestamp = new Date()
    await updateMessageInAPI(msgRef)
    isReplying.value = false; replyText.value = ''
    toast({ title: "Resposta Registrada!", description: "A resposta foi salva com sucesso no banco de dados." })
  } catch (error) {
    toast({ title: "Erro ao Responder", description: "Não foi possível gravar a resposta.", variant: "destructive" })
  } finally { isSendingReply.value = false }
}

// ==========================================
// 2. LÓGICA DE SENHAS (MOCK FICTÍCIO)
// ==========================================
interface PasswordRequest {
  id: string
  studentName: string
  ra: string
  email: string
  timestamp: Date
  status: 'Pendente' | 'Resolvido'
}

const passwordSearchQuery = ref('')
const activePasswordFilter = ref<'todas' | 'pendentes' | 'resolvidos'>('pendentes')
const selectedPasswordRequest = ref<PasswordRequest | null>(null)
const isResettingPassword = ref(false)

const passwordRequests = ref<PasswordRequest[]>([
  {
    id: 'PWD-001',
    studentName: 'Marcos Vinícius',
    ra: '1460282113000',
    email: 'marcos.gouveia@fatec.sp.gov.br',
    timestamp: new Date(Date.now() - 1000 * 60 * 15),
    status: 'Pendente'
  },
  {
    id: 'PWD-002',
    studentName: 'Ana Beatriz Souza',
    ra: '1460282113444',
    email: 'ana.souza@fatec.sp.gov.br',
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 3),
    status: 'Pendente'
  },
  {
    id: 'PWD-003',
    studentName: 'Lucas Almeida',
    ra: '1460282113999',
    email: 'lucas.almeida@fatec.sp.gov.br',
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24),
    status: 'Resolvido'
  }
])

const filteredPasswordRequests = computed(() => {
  return passwordRequests.value.filter(req => {
    const matchesSearch = req.studentName.toLowerCase().includes(passwordSearchQuery.value.toLowerCase()) || req.ra.includes(passwordSearchQuery.value)
    let matchesFilter = true
    if (activePasswordFilter.value === 'pendentes') matchesFilter = req.status === 'Pendente'
    if (activePasswordFilter.value === 'resolvidos') matchesFilter = req.status === 'Resolvido'
    return matchesSearch && matchesFilter
  }).sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime())
})

const pendingPasswordCount = computed(() => passwordRequests.value.filter(r => r.status === 'Pendente').length)

const selectPasswordRequest = (req: PasswordRequest) => {
  selectedPasswordRequest.value = req
}

const approvePasswordReset = () => {
  if (!selectedPasswordRequest.value) return
  isResettingPassword.value = true
  
  setTimeout(() => {
    const req = passwordRequests.value.find(r => r.id === selectedPasswordRequest.value?.id)
    if (req) req.status = 'Resolvido'
    isResettingPassword.value = false
    toast({ title: "Link Enviado!", description: `Instruções de redefinição enviadas para ${req?.email}` })
  }, 1000)
}

// ==========================================
// UTILITÁRIOS VISUAIS
// ==========================================
const formatTime = (date: Date) => date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
const formatRelativeDate = (date: Date) => {
  const now = new Date(); const diffInHours = Math.abs(now.getTime() - date.getTime()) / 36e5
  if (diffInHours < 24 && now.getDate() === date.getDate()) return formatTime(date)
  else if (diffInHours < 48 && now.getDate() !== date.getDate()) return 'Ontem'
  else return date.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' })
}

const getCategoryStyle = (category: string) => {
  const normCat = category.toLowerCase()
  if (normCat.includes('suporte')) return 'bg-blue-100 text-blue-700'
  if (normCat.includes('acesso') || normCat.includes('senha')) return 'bg-emerald-100 text-emerald-700'
  if (normCat.includes('dúvida') || normCat.includes('duvida')) return 'bg-indigo-100 text-indigo-700'
  if (normCat.includes('reporte') || normCat.includes('erro')) return 'bg-amber-100 text-amber-700'
  return 'bg-slate-100 text-slate-700'
}

onMounted(() => {
  fetchMessages()
})
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
          <h2 class="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
            Central de Suporte 
          </h2>
          <p class="text-slate-500 text-[10px] uppercase font-bold tracking-[0.2em]">Atendimento e Segurança</p>
        </div>
      </div>

      <div class="bg-slate-100 p-1.5 rounded-2xl flex items-center shadow-inner overflow-x-auto w-full sm:w-auto border border-slate-200/50 shrink-0">
        <button @click="activeSupportView = 'mensagens'" class="px-5 h-10 rounded-xl text-sm font-bold transition-all duration-300 flex items-center gap-2" :class="activeSupportView === 'mensagens' ? 'bg-white text-[#0A102E] shadow-sm ring-1 ring-slate-200/50' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'">
          <Mail class="w-4 h-4" /> Inbox
          <Badge v-if="unreadCount > 0" class="bg-indigo-600 text-white border-none rounded-full px-1.5 py-0 text-[9px] ml-1">{{ unreadCount }}</Badge>
        </button>
        <button @click="activeSupportView = 'senhas'" class="px-5 h-10 rounded-xl text-sm font-bold transition-all duration-300 flex items-center gap-2" :class="activeSupportView === 'senhas' ? 'bg-white text-[#0A102E] shadow-sm ring-1 ring-slate-200/50' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'">
          <KeyRound class="w-4 h-4" /> Senhas
          <Badge v-if="pendingPasswordCount > 0" class="bg-amber-500 text-white border-none rounded-full px-1.5 py-0 text-[9px] ml-1">{{ pendingPasswordCount }}</Badge>
        </button>
      </div>
    </div>

    <div class="flex-1 bg-white border border-slate-200 rounded-[2rem] shadow-sm overflow-hidden flex min-h-0 relative">
      
      <template v-if="activeSupportView === 'mensagens'">
        <div class="w-full md:w-1/3 lg:w-[30%] flex flex-col border-r border-slate-100 bg-slate-50/30 shrink-0 transition-all duration-300" :class="{ 'hidden md:flex': selectedMessage }">
          
          <div class="p-4 border-b border-slate-100 shrink-0 space-y-3">
            <div class="relative w-full">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400" />
              <input v-model="searchQuery" placeholder="Buscar mensagens..." class="pl-9 h-9 border border-slate-200 rounded-xl bg-white w-full text-xs outline-none focus:border-indigo-400" />
            </div>
            <div class="bg-slate-200/60 p-1 rounded-xl flex items-center shadow-inner">
              <button @click="activeFilter = 'todas'" class="flex-1 h-7 rounded-lg text-[10px] font-bold transition-all" :class="activeFilter === 'todas' ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-500 hover:text-slate-700'">Todas</button>
              <button @click="activeFilter = 'nao_lidas'" class="flex-1 h-7 rounded-lg text-[10px] font-bold transition-all" :class="activeFilter === 'nao_lidas' ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'">Não Lidas</button>
              <button @click="activeFilter = 'lidas'" class="flex-1 h-7 rounded-lg text-[10px] font-bold transition-all" :class="activeFilter === 'lidas' ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-500 hover:text-slate-700'">Lidas</button>
            </div>
          </div>

          <div class="flex-1 overflow-y-auto custom-scrollbar relative">
            <div v-if="isLoadingData" class="absolute inset-0 flex flex-col items-center justify-center bg-slate-50/50 backdrop-blur-[1px] z-10">
              <Loader2 class="w-8 h-8 text-indigo-500 animate-spin mb-2" /><span class="text-xs font-bold text-slate-400">Sincronizando...</span>
            </div>
            <div v-if="filteredMessages.length === 0 && !isLoadingData" class="flex flex-col items-center justify-center h-full p-8 text-center opacity-60">
              <MailOpen class="w-12 h-12 text-slate-300 mb-3" /><p class="text-sm font-bold text-slate-500">Caixa Vazia</p>
            </div>

            <div v-else v-auto-animate>
              <div 
                v-for="msg in filteredMessages" :key="msg.id" @click="selectMessage(msg)"
                class="p-4 border-b border-slate-100 cursor-pointer transition-all duration-200 relative group"
                :class="[selectedMessage?.id === msg.id ? 'bg-indigo-50/50 border-l-4 border-l-indigo-600' : 'hover:bg-slate-50 border-l-4 border-l-transparent', !msg.isRead ? 'bg-white' : 'opacity-80']"
              >
                <div v-if="!msg.isRead" class="absolute top-5 right-4 w-2 h-2 bg-indigo-500 rounded-full shadow-sm shadow-indigo-200"></div>
                <CheckCircle2 v-else-if="msg.isReplied" class="absolute top-4 right-4 w-3.5 h-3.5 text-emerald-500" />
                <div class="flex items-start gap-3">
                  <img :src="`https://api.dicebear.com/8.x/avataaars/svg?seed=${encodeURIComponent(msg.senderName)}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`" class="w-10 h-10 rounded-full bg-slate-200 border border-slate-200 shrink-0" />
                  <div class="flex-1 min-w-0">
                    <h4 class="text-sm truncate pr-4 mb-0.5" :class="!msg.isRead ? 'font-bold text-slate-900' : 'font-semibold text-slate-700'">{{ msg.senderName }}</h4>
                    <p class="text-xs truncate mb-1" :class="!msg.isRead ? 'font-bold text-slate-800' : 'font-medium text-slate-600'">{{ msg.subject }}</p>
                    <p class="text-[11px] text-slate-500 truncate">{{ msg.message }}</p>
                    <div class="flex items-center justify-between mt-2">
                      <Badge :class="getCategoryStyle(msg.category)" class="text-[8px] font-black uppercase tracking-wider px-2 shadow-none border-none">{{ msg.category }}</Badge>
                      <span class="text-[9px] font-bold text-slate-400">{{ formatRelativeDate(msg.timestamp) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex-1 flex flex-col min-w-0 bg-white" :class="{ 'hidden md:flex': !selectedMessage }">
          <div v-if="!selectedMessage" class="flex-1 flex flex-col items-center justify-center p-10 text-center">
            <div class="w-24 h-24 bg-slate-50 rounded-full flex items-center justify-center mb-6 border border-slate-100"><Mail class="w-10 h-10 text-slate-300" /></div>
            <h3 class="text-xl font-bold text-slate-800">Selecione uma Mensagem</h3>
          </div>

          <template v-else>
            <div class="h-16 border-b border-slate-100 flex items-center justify-between px-6 shrink-0 bg-white">
              <div class="flex items-center gap-2">
                <Button variant="ghost" size="icon" @click="selectedMessage = null" class="md:hidden rounded-full mr-2"><ArrowLeft class="w-5 h-5 text-slate-600" /></Button>
                <Badge variant="outline" class="text-[10px] font-bold uppercase text-slate-500 border-slate-200 tracking-widest gap-1 bg-slate-50">
                  <Smartphone v-if="selectedMessage.source === 'App Mobile'" class="w-3 h-3" /><AlertCircle v-else class="w-3 h-3" /> Origem: {{ selectedMessage.source }}
                </Badge>
                <Badge v-if="selectedMessage.isReplied" class="bg-emerald-100 text-emerald-700 border-none font-bold text-[10px] uppercase ml-2">Respondida</Badge>
              </div>
              <Button variant="ghost" size="icon" @click="deleteMessage(selectedMessage.id)" class="rounded-full text-slate-400 hover:text-red-600"><Trash2 class="w-4 h-4" /></Button>
            </div>

            <div class="flex-1 overflow-y-auto custom-scrollbar p-6 md:p-10 relative">
              <div class="max-w-3xl mx-auto space-y-8" v-auto-animate>
                <div>
                  <h1 class="text-2xl md:text-3xl font-bold text-slate-900 tracking-tight leading-tight mb-6">{{ selectedMessage.subject }}</h1>
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-4">
                      <img :src="`https://api.dicebear.com/8.x/avataaars/svg?seed=${encodeURIComponent(selectedMessage.senderName)}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`" class="w-12 h-12 rounded-full border border-slate-200 bg-slate-50" />
                      <div>
                        <h3 class="font-bold text-slate-900 text-base flex items-center gap-2">{{ selectedMessage.senderName }}<Badge v-if="selectedMessage.ra" class="bg-slate-100 text-slate-600 border-none font-mono text-[9px] px-1.5 shadow-none">RA: {{ selectedMessage.ra }}</Badge></h3>
                        <p class="text-xs font-medium text-slate-500 mt-0.5">&lt;{{ selectedMessage.senderEmail }}&gt;</p>
                      </div>
                    </div>
                    <div class="text-right flex flex-col items-end">
                      <span class="text-xs font-bold text-slate-400 flex items-center gap-1.5"><Clock class="w-3 h-3" /> {{ formatRelativeDate(selectedMessage.timestamp) }}</span>
                      <span class="text-[10px] text-slate-400 mt-1">{{ formatTime(selectedMessage.timestamp) }}</span>
                    </div>
                  </div>
                </div>

                <Separator class="opacity-50" />
                <div class="prose prose-sm md:prose-base prose-slate max-w-none font-medium leading-relaxed text-slate-700 whitespace-pre-wrap pb-4">{{ selectedMessage.message }}</div>

                <div v-if="selectedMessage.isReplied && selectedMessage.replyText" class="bg-slate-50 border border-slate-200 rounded-2xl p-6 relative">
                  <div class="absolute -top-3 left-8 bg-white px-2 py-0.5 rounded-full border border-slate-200 text-[9px] font-bold text-slate-500 uppercase tracking-widest flex items-center gap-1"><Reply class="w-3 h-3" /> Sua Resposta na API</div>
                  <div class="flex justify-between items-start mb-4"><div class="font-bold text-sm text-slate-900">Admin CIFA</div><div class="text-xs font-semibold text-slate-400">{{ selectedMessage.replyTimestamp ? formatRelativeDate(selectedMessage.replyTimestamp) : 'Visualizada recentemente' }}</div></div>
                  <div class="text-sm text-slate-700 leading-relaxed font-medium whitespace-pre-wrap">{{ selectedMessage.replyText }}</div>
                </div>

                <div v-else-if="!isReplying" class="pt-4">
                  <Button @click="startReply" class="bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold rounded-xl h-12 px-6 shadow-none gap-2 border border-indigo-200/50"><Reply class="w-4 h-4" /> Escrever Resposta</Button>
                </div>

                <div v-if="isReplying && !selectedMessage.isReplied" class="bg-white border-2 border-indigo-100 rounded-2xl p-4 shadow-lg shadow-indigo-100/50 animate-in slide-in-from-bottom-4">
                  <div class="flex items-center gap-2 mb-3 px-2"><Reply class="w-4 h-4 text-indigo-600" /><span class="text-xs font-bold text-indigo-900 uppercase tracking-widest">Respondendo a {{ selectedMessage.senderName.split(' ')[0] }}</span></div>
                  <textarea v-model="replyText" placeholder="Escreva sua resposta aqui. Ao enviar, ela será salva na base de dados..." class="w-full min-h-[160px] p-3 text-sm text-slate-700 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all resize-y custom-scrollbar mb-4" :disabled="isSendingReply"></textarea>
                  <div class="flex items-center justify-between">
                    <Button variant="ghost" @click="cancelReply" :disabled="isSendingReply" class="text-slate-500 font-bold rounded-xl h-10 px-4">Cancelar</Button>
                    <Button @click="sendReply" :disabled="!replyText.trim() || isSendingReply" class="bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl h-10 px-8 shadow-md gap-2"><Loader2 v-if="isSendingReply" class="w-4 h-4 animate-spin" /><Send v-else class="w-4 h-4" /> {{ isSendingReply ? 'Gravando...' : 'Salvar Resposta' }}</Button>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </template>

      <template v-else-if="activeSupportView === 'senhas'">
        <div class="w-full md:w-1/3 lg:w-[30%] flex flex-col border-r border-slate-100 bg-slate-50/30 shrink-0 transition-all duration-300" :class="{ 'hidden md:flex': selectedPasswordRequest }">
          <div class="p-4 border-b border-slate-100 shrink-0 space-y-3">
            <div class="relative w-full">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400" />
              <input v-model="passwordSearchQuery" placeholder="Buscar por RA ou Nome..." class="pl-9 h-9 border border-slate-200 rounded-xl bg-white w-full text-xs outline-none focus:border-amber-400" />
            </div>
            <div class="bg-slate-200/60 p-1 rounded-xl flex items-center shadow-inner">
              <button @click="activePasswordFilter = 'todas'" class="flex-1 h-7 rounded-lg text-[10px] font-bold transition-all" :class="activePasswordFilter === 'todas' ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-500 hover:text-slate-700'">Todas</button>
              <button @click="activePasswordFilter = 'pendentes'" class="flex-1 h-7 rounded-lg text-[10px] font-bold transition-all" :class="activePasswordFilter === 'pendentes' ? 'bg-white text-amber-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'">Pendentes</button>
              <button @click="activePasswordFilter = 'resolvidos'" class="flex-1 h-7 rounded-lg text-[10px] font-bold transition-all" :class="activePasswordFilter === 'resolvidos' ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-500 hover:text-slate-700'">Resolvidos</button>
            </div>
          </div>

          <div class="flex-1 overflow-y-auto custom-scrollbar">
            <div v-if="filteredPasswordRequests.length === 0" class="flex flex-col items-center justify-center h-full p-8 text-center opacity-60">
              <KeyRound class="w-12 h-12 text-slate-300 mb-3" /><p class="text-sm font-bold text-slate-500">Nenhum Pedido</p>
            </div>
            <div v-else v-auto-animate>
              <div 
                v-for="req in filteredPasswordRequests" :key="req.id" @click="selectPasswordRequest(req)"
                class="p-4 border-b border-slate-100 cursor-pointer transition-all duration-200 relative group"
                :class="[selectedPasswordRequest?.id === req.id ? 'bg-amber-50/50 border-l-4 border-l-amber-500' : 'hover:bg-slate-50 border-l-4 border-l-transparent', req.status === 'Pendente' ? 'bg-white' : 'opacity-70']"
              >
                <div v-if="req.status === 'Pendente'" class="absolute top-5 right-4 w-2 h-2 bg-amber-500 rounded-full shadow-sm"></div>
                <div class="flex items-start gap-3">
                  <div class="w-10 h-10 rounded-full bg-amber-100 text-amber-600 flex items-center justify-center shrink-0 border border-amber-200"><KeyRound class="w-5 h-5" /></div>
                  <div class="flex-1 min-w-0">
                    <h4 class="text-sm truncate pr-4 font-bold text-slate-900 mb-0.5">{{ req.studentName }}</h4>
                    <p class="text-xs truncate font-mono text-slate-600 mb-1">RA: {{ req.ra }}</p>
                    <div class="flex items-center justify-between mt-2">
                      <Badge :class="req.status === 'Pendente' ? 'bg-amber-100 text-amber-700' : 'bg-slate-100 text-slate-600'" class="text-[8px] font-black uppercase tracking-wider px-2 shadow-none border-none">{{ req.status }}</Badge>
                      <span class="text-[9px] font-bold text-slate-400">{{ formatRelativeDate(req.timestamp) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex-1 flex flex-col min-w-0 bg-white" :class="{ 'hidden md:flex': !selectedPasswordRequest }">
          <div v-if="!selectedPasswordRequest" class="flex-1 flex flex-col items-center justify-center p-10 text-center relative overflow-hidden">
            <div class="absolute top-[10%] right-[10%] w-64 h-64 bg-amber-50 rounded-full blur-[80px] pointer-events-none opacity-60"></div>
            <div class="w-24 h-24 bg-slate-50 rounded-full flex items-center justify-center mb-6 border border-slate-100"><ShieldAlert class="w-10 h-10 text-slate-300" /></div>
            <h3 class="text-xl font-bold text-slate-800">Gerenciador de Credenciais</h3>
            <p class="text-sm text-slate-500 mt-2 max-w-sm">Aprove redefinições de senha para alunos que perderam o acesso ao App Mobile.</p>
          </div>

          <template v-else>
            <div class="h-16 border-b border-slate-100 flex items-center justify-between px-6 shrink-0 bg-white">
              <div class="flex items-center gap-2">
                <Button variant="ghost" size="icon" @click="selectedPasswordRequest = null" class="md:hidden rounded-full mr-2"><ArrowLeft class="w-5 h-5 text-slate-600" /></Button>
                <Badge variant="outline" class="text-[10px] font-bold uppercase text-slate-500 border-slate-200 tracking-widest gap-1 bg-slate-50"><ShieldAlert class="w-3 h-3" /> Solicitação de Segurança</Badge>
              </div>
            </div>

            <div class="flex-1 overflow-y-auto custom-scrollbar p-6 md:p-10 flex flex-col justify-center items-center relative">
              <div class="max-w-md w-full text-center space-y-6" v-auto-animate>
                
                <div class="w-20 h-20 mx-auto rounded-full flex items-center justify-center mb-2" :class="selectedPasswordRequest.status === 'Pendente' ? 'bg-amber-100 text-amber-600 border border-amber-200' : 'bg-emerald-100 text-emerald-600 border border-emerald-200'">
                  <ShieldCheck v-if="selectedPasswordRequest.status === 'Resolvido'" class="w-10 h-10" />
                  <KeyRound v-else class="w-10 h-10" />
                </div>

                <div>
                  <h1 class="text-2xl font-bold text-slate-900 tracking-tight">{{ selectedPasswordRequest.studentName }}</h1>
                  <p class="text-sm font-mono text-slate-500 mt-1">RA: {{ selectedPasswordRequest.ra }}</p>
                </div>

                <div class="bg-slate-50 border border-slate-200 rounded-2xl p-6 text-left">
                  <p class="text-sm text-slate-600 leading-relaxed font-medium">O aluno está solicitando a recuperação do acesso ao sistema CIFA. Ao aprovar, um e-mail com instruções e um link temporário será disparado para a caixa de entrada oficial.</p>
                  <div class="mt-4 flex items-center gap-2 text-xs font-bold text-slate-800 bg-white p-3 rounded-lg border border-slate-100">
                    <MailWarning class="w-4 h-4 text-slate-400" /> {{ selectedPasswordRequest.email }}
                  </div>
                </div>

                <div v-if="selectedPasswordRequest.status === 'Pendente'" class="pt-4">
                  <Button @click="approvePasswordReset" :disabled="isResettingPassword" class="w-full bg-amber-500 hover:bg-amber-600 text-white font-bold rounded-2xl h-14 shadow-lg shadow-amber-500/20 text-base gap-2">
                    <Loader2 v-if="isResettingPassword" class="w-5 h-5 animate-spin" />
                    <Send v-else class="w-5 h-5" />
                    {{ isResettingPassword ? 'Enviando Autorização...' : 'Aprovar Redefinição de Senha' }}
                  </Button>
                </div>

                <div v-else class="pt-4 text-center">
                  <Badge class="bg-emerald-100 text-emerald-700 font-bold px-4 py-1.5 uppercase tracking-widest text-[10px] border-none shadow-none">Link Enviado ao Aluno</Badge>
                  <p class="text-xs text-slate-400 mt-3 font-medium">Aguardando o aluno definir a nova senha no App.</p>
                </div>

              </div>
            </div>
          </template>
        </div>

      </template>

    </div>
  </div>
</template>

<style scoped>
.font-poppins { font-family: 'Poppins', sans-serif; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 20px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background-color: #94a3b8; }
</style>