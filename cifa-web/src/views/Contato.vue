<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, Mail, Trash2, Reply, CheckCircle2, 
  Clock, ShieldAlert, Send, AlertCircle, Smartphone, KeyRound, ShieldCheck, X, MoreHorizontal, MessageSquare, Loader2
} from 'lucide-vue-next'
import { 
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow 
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { 
  Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle 
} from '@/components/ui/dialog'
import { 
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger 
} from '@/components/ui/dropdown-menu'
import { useToast } from '@/components/ui/toast/use-toast'
import { Separator } from '@/components/ui/separator'

const router = useRouter()
const { toast } = useToast()

// URL base da API atualizada
const API_BASE = 'https://reply-imprint-skier.ngrok-free.dev'
const headers = { 'ngrok-skip-browser-warning': 'true', 'Content-Type': 'application/json' }

// ==========================================
// CONTROLE DE ABAS MESTRES
// ==========================================
type SupportView = 'mensagens' | 'senhas'
const activeSupportView = ref<SupportView>('mensagens')

// ==========================================
// ESTADOS DA TELA SIMULADA DO GMAIL
// ==========================================
const isGmailScreenActive = ref(false)
const gmailData = ref({
  to: '',
  subject: '',
  body: '',
  date: new Date()
})

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
const selectedMessage = ref<ContactMessage | null>(null)
const isMessageModalOpen = ref(false)
const isLoadingData = ref(true)
const messages = ref<ContactMessage[]>([])

const isReplying = ref(false)
const replyText = ref('')
const isSendingReply = ref(false)

const fetchMessages = async () => {
  isLoadingData.value = true
  try {
    const response = await fetch(`${API_BASE}/mensagem/exibirContatos`, { headers })
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
    console.error("Erro na API de Mensagens", error)
  } finally {
    isLoadingData.value = false
  }
}

const updateMessageInAPI = async (msg: ContactMessage) => {
  try {
    const payload = { ...msg._rawData, lida: msg.isRead, respondida: msg.isReplied, resposta: msg.replyText }
    await fetch(`${API_BASE}/mensagem/editarContato/${msg.id}`, { method: 'PUT', headers, body: JSON.stringify(payload) })
  } catch (error) { console.error("Erro no PUT de Mensagens", error) }
}

const filteredMessages = computed(() => {
  return messages.value.filter(msg => {
    return msg.senderName.toLowerCase().includes(searchQuery.value.toLowerCase()) || 
           msg.subject.toLowerCase().includes(searchQuery.value.toLowerCase())
  })
})

const unreadCount = computed(() => messages.value.filter(m => !m.isRead).length)

const openMessageModal = (msg: ContactMessage) => {
  selectedMessage.value = msg
  isMessageModalOpen.value = true
  if (!msg.isRead) {
    msg.isRead = true
    updateMessageInAPI(msg)
  }
}

const closeMessageModal = () => {
  isMessageModalOpen.value = false
  setTimeout(() => {
    selectedMessage.value = null
    isReplying.value = false
    replyText.value = ''
  }, 300)
}

const deleteMessage = (id: string) => {
  messages.value = messages.value.filter(m => m.id !== id)
  if (selectedMessage.value?.id === id) closeMessageModal()
  toast({ title: "Mensagem Ocultada", description: "A solicitação foi removida da visão atual." })
}

const startReply = () => { isReplying.value = true }
const cancelReply = () => { isReplying.value = false; replyText.value = '' }

const sendReply = async () => {
  if (!replyText.value.trim() || !selectedMessage.value) return
  isSendingReply.value = true
  
  try {
    const msgRef = selectedMessage.value
    
    // 1. Gravação do status no banco de dados
    await updateMessageInAPI({ ...msgRef, isReplied: true, replyText: replyText.value })

    // 2. Disparo para o endpoint correto
    const emailRes = await fetch(`${API_BASE}/mensagem/responderContato`, {
      method: 'POST',
      headers,
      body: JSON.stringify({
        email: msgRef.senderEmail,
        assunto: `Resposta CIFA Suporte: ${msgRef.subject}`,
        mensagem: replyText.value
      })
    })

    if (!emailRes.ok) throw new Error("Falha no servidor")
    
    // 3. Sucesso: Limpeza e Comprovante
    gmailData.value = {
      to: msgRef.senderEmail,
      subject: `Re: ${msgRef.subject}`,
      body: replyText.value,
      date: new Date()
    }
    
    isGmailScreenActive.value = true
    isMessageModalOpen.value = false
    toast({ title: "Resposta Enviada!", description: "O e-mail foi processado." })
    
  } catch (error) {
    toast({ title: "Erro", description: "Falha ao enviar e-mail.", variant: "destructive" })
  } finally {
    // ESTA É A LINHA QUE GARANTE QUE VOCÊ POSSA MANDAR OUTRO DEPOIS
    isSendingReply.value = false
    replyText.value = ''
    isReplying.value = false
  }
}

// ==========================================
// 2. LÓGICA DE SENHAS (API)
// ==========================================
interface PasswordRequest {
  id: string
  studentName: string
  ra: string
  email: string
  timestamp: Date
  status: 'Pendente' | 'Resolvido'
  _rawData?: any
}

const selectedPasswordRequest = ref<PasswordRequest | null>(null)
const isPasswordModalOpen = ref(false)
const isResettingPassword = ref(false)
const isLoadingPasswords = ref(true)
const passwordRequests = ref<PasswordRequest[]>([])

const fetchPasswordRequests = async () => {
  isLoadingPasswords.value = true
  try {
    const response = await fetch(`${API_BASE}/mensagem/exibirsSolicitacoesSenha`, { headers })
    if (!response.ok) throw new Error('Falha na API')
    
    const data = await response.json()
    console.log("DEBUG - Dados de Senha:", data)

    passwordRequests.value = data.map((req: any) => ({
      id: req.id,
      studentName: 'Aluno Solicitante', // A API não retorna o nome, usaremos um padrão
      ra: 'Não informado',            // A API não retorna o RA
      email: req.email,
      timestamp: new Date(),          // A API não retorna a data
      status: req.status === 'Resolvido' ? 'Resolvido' : 'Pendente',
      _rawData: req
    }))
  } catch (error) {
    console.error("Erro Senhas:", error)
  } finally {
    isLoadingPasswords.value = false
  }
}

const filteredPasswordRequests = computed(() => {
  return passwordRequests.value.filter(req => {
    return req.studentName.toLowerCase().includes(searchQuery.value.toLowerCase()) || req.ra.includes(searchQuery.value)
  }).sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime())
})

const pendingPasswordCount = computed(() => passwordRequests.value.filter(r => r.status === 'Pendente').length)

const openPasswordModal = (req: PasswordRequest) => {
  selectedPasswordRequest.value = req
  isPasswordModalOpen.value = true
}

const closePasswordModal = () => {
  isPasswordModalOpen.value = false
  setTimeout(() => { selectedPasswordRequest.value = null }, 300)
}

const approvePasswordReset = async () => {
  if (!selectedPasswordRequest.value) return
  isResettingPassword.value = true
  
  try {
    const req = selectedPasswordRequest.value
    const payload = { ...req._rawData, resolvida: true, isResolved: true, status: 'Resolvido' }
    
    await fetch(`${API_BASE}/mensagem/editarSolicitacao/${req.id}`, { 
      method: 'PUT', 
      headers, 
      body: JSON.stringify(payload) 
    })

    req.status = 'Resolvido'
    toast({ title: "Status Atualizado!", description: `A solicitação de ${req.studentName} foi marcada como resolvida.` })
    closePasswordModal()
  } catch (error) {
    toast({ title: "Erro na Operação", description: "Não foi possível atualizar o banco de dados.", variant: "destructive" })
  } finally {
    isResettingPassword.value = false
  }
}

// ==========================================
// UTILITÁRIOS VISUAIS
// ==========================================
const formatTime = (date: Date) => date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
const formatDate = (date: Date) => date.toLocaleDateString('pt-BR') + ' ' + formatTime(date)
const getCategoryStyle = (category: string) => {
  const normCat = category.toLowerCase()
  if (normCat.includes('suporte')) return 'bg-blue-100 text-blue-700'
  if (normCat.includes('acesso')) return 'bg-emerald-100 text-emerald-700'
  if (normCat.includes('dúvida')) return 'bg-indigo-100 text-indigo-700'
  if (normCat.includes('reporte')) return 'bg-amber-100 text-amber-700'
  return 'bg-slate-100 text-slate-700'
}

onMounted(() => {
  fetchMessages()
  fetchPasswordRequests()
})
</script>

<template>
  <div class="flex flex-col h-full space-y-4 font-poppins shrink-0">
    
    <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
      <div>
        <h2 class="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
          Central de Suporte
          <Badge v-if="unreadCount > 0 && activeSupportView === 'mensagens'" class="bg-indigo-600 hover:bg-indigo-700 text-white border-none rounded-full px-2 py-0.5 text-[10px]">{{ unreadCount }} Novas</Badge>
          <Badge v-if="pendingPasswordCount > 0 && activeSupportView === 'senhas'" class="bg-amber-500 hover:bg-amber-600 text-white border-none rounded-full px-2 py-0.5 text-[10px]">{{ pendingPasswordCount }} Pendentes</Badge>
        </h2>
        <p class="text-slate-500 text-[10px] uppercase font-bold tracking-[0.2em]">Atendimento e Segurança</p>
      </div>

      <div class="bg-slate-200 p-1 rounded-xl flex items-center shadow-inner">
        <button @click="activeSupportView = 'mensagens'" class="px-5 h-9 rounded-lg text-xs font-bold transition-all duration-300 flex items-center gap-2" :class="activeSupportView === 'mensagens' ? 'bg-white text-[#0A102E] shadow-sm' : 'text-slate-500 hover:text-slate-700'">
          <Mail class="w-4 h-4" /> Mensagens
        </button>
        <button @click="activeSupportView = 'senhas'" class="px-5 h-9 rounded-lg text-xs font-bold transition-all duration-300 flex items-center gap-2" :class="activeSupportView === 'senhas' ? 'bg-white text-[#0A102E] shadow-sm' : 'text-slate-500 hover:text-slate-700'">
          <KeyRound class="w-4 h-4" /> Senhas
        </button>
      </div>
    </div>

    <div class="flex flex-col sm:flex-row items-center gap-4 py-2 shrink-0">
      <div class="relative w-full sm:w-96">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
        <input v-model="searchQuery" :placeholder="activeSupportView === 'mensagens' ? 'Buscar por remetente ou assunto...' : 'Buscar por RA ou Nome...'" class="pl-10 h-10 border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#0A102E] bg-white w-full transition-all text-sm outline-none shadow-sm" />
      </div>
    </div>

    <div class="flex-1 bg-white border border-slate-200 rounded-[2.5rem] shadow-sm overflow-hidden flex flex-col min-h-0 relative">
      <div class="flex-1 overflow-x-auto overflow-y-auto custom-scrollbar">
        
        <Table v-if="activeSupportView === 'mensagens'" class="min-w-[900px] w-full">
          <TableHeader class="bg-slate-50/80 sticky top-0 z-10 backdrop-blur-sm">
            <TableRow class="border-b-slate-200">
              <TableHead class="text-slate-600 font-bold px-6 h-12">Remetente</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Assunto</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Categoria</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Data / Hora</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Status</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12 text-center w-24">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody v-auto-animate>
            <template v-if="isLoadingData">
              <TableRow v-for="n in 5" :key="n" class="border-b-slate-100">
                <TableCell colspan="6" class="py-4 px-6"><div class="h-8 bg-slate-100 animate-pulse rounded-lg w-full"></div></TableCell>
              </TableRow>
            </template>
            <template v-else-if="filteredMessages.length > 0">
              <TableRow v-for="msg in filteredMessages" :key="msg.id" class="hover:bg-slate-50/80 transition-colors border-b-slate-100 cursor-pointer" @click="openMessageModal(msg)">
                <TableCell class="py-4 px-6">
                  <div class="flex items-center gap-3">
                    <img :src="`https://api.dicebear.com/8.x/avataaars/svg?seed=${encodeURIComponent(msg.senderName)}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`" class="w-10 h-10 rounded-full border border-slate-200 bg-slate-100" />
                    <div class="flex flex-col">
                      <span class="text-sm tracking-tight leading-tight" :class="!msg.isRead ? 'font-black text-slate-900' : 'font-semibold text-slate-700'">{{ msg.senderName }}</span>
                      <span class="text-[10px] text-slate-500 font-medium">{{ msg.source }}</span>
                    </div>
                  </div>
                </TableCell>
                <TableCell class="py-4 px-6 text-sm" :class="!msg.isRead ? 'font-bold text-slate-800' : 'text-slate-600'">{{ msg.subject }}</TableCell>
                <TableCell class="py-4 px-6"><Badge :class="getCategoryStyle(msg.category)" class="border-none font-bold text-[10px] uppercase shadow-none px-3">{{ msg.category }}</Badge></TableCell>
                <TableCell class="py-4 px-6 text-xs text-slate-600 font-mono">{{ formatDate(msg.timestamp) }}</TableCell>
                <TableCell class="py-4 px-6">
                  <div class="flex items-center gap-2">
                    <div v-if="!msg.isRead" class="w-2 h-2 bg-indigo-500 rounded-full"></div>
                    <Badge v-if="msg.isReplied" class="bg-emerald-100 text-emerald-700 border-none font-bold text-[10px] uppercase shadow-none px-2">Respondido</Badge>
                    <span v-else-if="msg.isRead" class="text-xs text-slate-400 font-semibold">Lido</span>
                    <span v-else class="text-xs text-indigo-600 font-bold">Novo</span>
                  </div>
                </TableCell>
                <TableCell class="py-4 px-6 text-center" @click.stop>
                  <DropdownMenu>
                    <DropdownMenuTrigger as-child><Button variant="ghost" class="h-8 w-8 p-0 rounded-full hover:bg-slate-200"><MoreHorizontal class="w-4 h-4 text-slate-600" /></Button></DropdownMenuTrigger>
                    <DropdownMenuContent align="end" class="w-48 rounded-xl border-none shadow-xl font-poppins">
                      <DropdownMenuItem @click="openMessageModal(msg)" class="cursor-pointer gap-2 font-medium"><MessageSquare class="w-4 h-4 text-indigo-600" /> Ver Solicitação</DropdownMenuItem>
                      <Separator class="my-1" />
                      <DropdownMenuItem @click="deleteMessage(msg.id)" class="cursor-pointer gap-2 font-bold text-red-600 focus:text-red-700 focus:bg-red-50"><Trash2 class="w-4 h-4" /> Excluir</DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </TableCell>
              </TableRow>
            </template>
            <template v-else><TableRow><TableCell colspan="6" class="h-64 text-center text-slate-400 italic font-medium">Caixa de entrada vazia.</TableCell></TableRow></template>
          </TableBody>
        </Table>

        <Table v-else-if="activeSupportView === 'senhas'" class="min-w-[900px] w-full">
          <TableHeader class="bg-slate-50/80 sticky top-0 z-10 backdrop-blur-sm">
            <TableRow class="border-b-slate-200">
              <TableHead class="text-slate-600 font-bold px-6 h-12">Aluno</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Matrícula (RA)</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">E-mail de Cadastro</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Data da Solicitação</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12">Status</TableHead>
              <TableHead class="text-slate-600 font-bold px-6 h-12 text-center w-24">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody v-auto-animate>
            <template v-if="isLoadingPasswords">
              <TableRow v-for="n in 3" :key="'pwd-'+n" class="border-b-slate-100">
                <TableCell colspan="6" class="py-4 px-6"><div class="h-8 bg-slate-100 animate-pulse rounded-lg w-full"></div></TableCell>
              </TableRow>
            </template>
            <template v-else-if="filteredPasswordRequests.length > 0">
              <TableRow v-for="req in filteredPasswordRequests" :key="req.id" class="hover:bg-slate-50/80 transition-colors border-b-slate-100 cursor-pointer" @click="openPasswordModal(req)">
                <TableCell class="py-4 px-6 font-bold text-slate-800">{{ req.studentName }}</TableCell>
                <TableCell class="py-4 px-6 font-mono text-xs font-semibold text-slate-700">{{ req.ra }}</TableCell>
                <TableCell class="py-4 px-6 text-sm text-slate-600">{{ req.email }}</TableCell>
                <TableCell class="py-4 px-6 text-xs text-slate-600 font-mono">{{ formatDate(req.timestamp) }}</TableCell>
                <TableCell class="py-4 px-6">
                  <Badge :class="req.status === 'Pendente' ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700'" class="border-none font-bold text-[10px] uppercase shadow-none px-3">{{ req.status }}</Badge>
                </TableCell>
                <TableCell class="py-4 px-6 text-center" @click.stop>
                  <Button variant="ghost" size="sm" @click="openPasswordModal(req)" class="font-bold" :class="req.status === 'Pendente' ? 'text-amber-600 hover:text-amber-700 hover:bg-amber-50' : 'text-slate-400'">
                    {{ req.status === 'Pendente' ? 'Avaliar' : 'Ver Detalhe' }}
                  </Button>
                </TableCell>
              </TableRow>
            </template>
            <template v-else><TableRow><TableCell colspan="6" class="h-64 text-center text-slate-400 italic font-medium">Nenhuma solicitação de senha encontrada.</TableCell></TableRow></template>
          </TableBody>
        </Table>
      </div>
    </div>

    <Dialog v-model:open="isMessageModalOpen">
      <DialogContent class="rounded-[2.5rem] w-[95vw] sm:max-w-[700px] border-none shadow-2xl p-0 overflow-hidden font-poppins">
        
        <div class="bg-slate-50 p-6 flex flex-col justify-between border-b border-slate-100 relative">
          <Button variant="ghost" size="icon" @click="closeMessageModal" class="absolute top-6 right-6 rounded-full"><X class="w-4 h-4" /></Button>
          <div class="flex items-center gap-4 pr-10">
            <img :src="`https://api.dicebear.com/8.x/avataaars/svg?seed=${encodeURIComponent(selectedMessage?.senderName || '')}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`" class="w-14 h-14 rounded-full border-2 border-white shadow-sm" />
            <div>
              <DialogTitle class="text-xl font-bold text-slate-900 leading-none">{{ selectedMessage?.senderName }}</DialogTitle>
              <DialogDescription class="text-xs mt-1">{{ selectedMessage?.senderEmail }}</DialogDescription>
            </div>
          </div>
        </div>

        <div v-if="selectedMessage" class="p-8 max-h-[60vh] overflow-y-auto custom-scrollbar flex flex-col">
          
          <div class="mb-6 flex flex-col gap-2">
            <h3 class="text-xl font-bold text-slate-800">{{ selectedMessage.subject }}</h3>
            <div class="flex flex-wrap gap-2 text-xs font-medium text-slate-500">
              <Badge variant="outline" class="bg-slate-50 text-[9px] uppercase tracking-widest">{{ selectedMessage.source }}</Badge>
              <Badge v-if="selectedMessage.ra" variant="outline" class="bg-slate-50 text-[9px] uppercase tracking-widest font-mono">RA: {{ selectedMessage.ra }}</Badge>
              <span class="flex items-center gap-1"><Clock class="w-3 h-3" /> {{ formatDate(selectedMessage.timestamp) }}</span>
            </div>
          </div>

          <div class="prose prose-sm prose-slate max-w-none font-medium leading-relaxed text-slate-700 whitespace-pre-wrap pb-6 border-b border-slate-100">
            {{ selectedMessage.message }}
          </div>

          <div v-if="selectedMessage.isReplied && selectedMessage.replyText" class="bg-slate-50 border border-slate-200 rounded-2xl p-5 mt-6 relative">
            <div class="absolute -top-3 left-6 bg-white px-2 py-0.5 rounded-full border border-slate-200 text-[9px] font-bold text-slate-500 uppercase tracking-widest flex items-center gap-1"><Reply class="w-3 h-3" /> Sua Resposta na API</div>
            <div class="flex justify-between items-start mb-2">
              <div class="font-bold text-sm text-slate-900">Administração CIFA</div>
              <div class="text-xs font-semibold text-slate-400">{{ selectedMessage.replyTimestamp ? formatDate(selectedMessage.replyTimestamp) : 'Visualizada' }}</div>
            </div>
            <div class="text-sm text-slate-700 leading-relaxed font-medium whitespace-pre-wrap">{{ selectedMessage.replyText }}</div>
          </div>

          <div v-else class="mt-6">
            <div v-if="!isReplying">
              <Button @click="startReply" class="bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold rounded-xl h-11 shadow-none gap-2 border border-indigo-200/50"><Reply class="w-4 h-4" /> Responder Solicitação</Button>
            </div>
            <div v-else class="animate-in slide-in-from-bottom-2">
              <textarea v-model="replyText" placeholder="Escreva a resposta. Ela será gravada no banco de dados da plataforma..." class="w-full min-h-[120px] p-3 text-sm text-slate-700 bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#0A102E] transition-all resize-y custom-scrollbar mb-3" :disabled="isSendingReply"></textarea>
              <div class="flex items-center justify-end gap-2">
                <Button variant="ghost" @click="cancelReply" :disabled="isSendingReply" class="font-bold rounded-xl h-10">Cancelar</Button>
                <Button @click="sendReply" :disabled="!replyText.trim() || isSendingReply" class="bg-[#1A1A3A] hover:bg-[#0A102E] text-white font-bold rounded-xl h-10 shadow-md gap-2"><Loader2 v-if="isSendingReply" class="w-4 h-4 animate-spin" /><Send v-else class="w-4 h-4" /> {{ isSendingReply ? 'Gravando...' : 'Salvar e Enviar' }}</Button>
              </div>
            </div>
          </div>

        </div>
      </DialogContent>
    </Dialog>

    <Dialog v-model:open="isPasswordModalOpen">
      <DialogContent class="rounded-[2.5rem] w-[95vw] sm:max-w-[450px] border-none shadow-2xl p-0 overflow-hidden font-poppins text-center">
        <div class="bg-slate-50 p-8 flex flex-col items-center border-b border-slate-100 relative">
          <Button variant="ghost" size="icon" @click="closePasswordModal" class="absolute top-4 right-4 rounded-full"><X class="w-4 h-4" /></Button>
          <div class="w-16 h-16 rounded-full flex items-center justify-center mb-4 shadow-sm bg-white" :class="selectedPasswordRequest?.status === 'Pendente' ? 'text-amber-500 border border-amber-100' : 'text-emerald-500 border border-emerald-100'">
            <ShieldCheck v-if="selectedPasswordRequest?.status === 'Resolvido'" class="w-8 h-8" />
            <KeyRound v-else class="w-8 h-8" />
          </div>
          <DialogTitle class="text-xl font-bold text-slate-900">{{ selectedPasswordRequest?.studentName }}</DialogTitle>
          <DialogDescription class="text-xs mt-1 font-mono">RA: {{ selectedPasswordRequest?.ra }}</DialogDescription>
        </div>

        <div class="p-8">
          <div class="text-sm text-slate-600 leading-relaxed font-medium mb-6">
            O aluno solicitou a redefinição de credenciais do aplicativo. Verifique o status da solicitação na base de dados:
            <div class="mt-3 font-bold text-slate-900 bg-slate-50 py-2 rounded-lg border border-slate-100">{{ selectedPasswordRequest?.email }}</div>
          </div>

          <Button v-if="selectedPasswordRequest?.status === 'Pendente'" @click="approvePasswordReset" :disabled="isResettingPassword" class="w-full bg-[#1A1A3A] hover:bg-[#0A102E] text-white font-bold rounded-2xl h-12 shadow-lg gap-2 transition-all">
            <Loader2 v-if="isResettingPassword" class="w-5 h-5 animate-spin" /><Send v-else class="w-4 h-4" /> {{ isResettingPassword ? 'Aprovando na API...' : 'Marcar como Resolvido' }}
          </Button>

          <div class="bg-emerald-50 text-emerald-700 border border-emerald-100 font-bold rounded-2xl h-12 flex items-center justify-center gap-2">
            <CheckCircle2 class="w-5 h-5" /> Autorização Concluída
          </div>
        </div>
      </DialogContent>
    </Dialog>

    <Dialog v-model:open="isGmailScreenActive">
      <DialogContent class="rounded-[2rem] w-[95vw] sm:max-w-[750px] border-none shadow-2xl p-0 overflow-hidden font-sans">
        
        <div class="bg-[#F6F8FC] px-6 py-4 flex items-center justify-between border-b border-slate-200">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-lg bg-[#EA4335] flex items-center justify-center font-black text-white text-sm shadow-sm select-none">
              M
            </div>
            <span class="text-xs font-bold text-slate-600 tracking-wider">cifasuporte@gmail.com — Caixa de Saída</span>
          </div>
          <Button variant="ghost" size="icon" @click="isGmailScreenActive = false" class="rounded-full h-8 w-8 hover:bg-slate-200">
            <X class="w-4 h-4 text-slate-500" />
          </Button>
        </div>

        <div class="p-8 bg-white space-y-6">
          <div>
            <h2 class="text-xl font-normal text-slate-900 mb-4">{{ gmailData.subject }}</h2>
            
            <div class="flex items-start justify-between text-sm">
              <div class="flex gap-3">
                <div class="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center font-bold text-slate-600 border border-slate-200 select-none">
                  CS
                </div>
                <div>
                  <div class="font-bold text-slate-800 text-sm">CIFA Suporte <span class="font-normal text-slate-500 text-xs">&lt;cifasuporte@gmail.com&gt;</span></div>
                  <div class="text-xs text-slate-500 mt-1">para {{ gmailData.to }}</div>
                </div>
              </div>
              <div class="text-xs text-slate-400 font-mono">
                {{ formatDate(gmailData.date) }}
              </div>
            </div>
          </div>

          <Separator class="bg-slate-100" />

          <div class="text-sm text-slate-800 leading-relaxed whitespace-pre-wrap font-sans min-h-[160px] p-4 bg-slate-50 rounded-xl border border-slate-200/60 shadow-inner">
            {{ gmailData.body }}
          </div>
        </div>

        <DialogFooter class="p-5 bg-[#F6F8FC] border-t border-slate-200">
          <Button @click="isGmailScreenActive = false" class="bg-[#1A1A3A] hover:bg-[#0A102E] text-white rounded-xl font-bold px-6 h-11 shadow-md">
            Voltar para o Suporte
          </Button>
        </DialogFooter>
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