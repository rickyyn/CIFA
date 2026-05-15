<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Loader2, ArrowRight, UserCircle } from 'lucide-vue-next'

const router = useRouter()

// Estados do Fluxo
const step = ref<'login' | 'name'>('login')
const isLoading = ref(false)
const errorMessage = ref('')

// Dados do Formulário
const identifier = ref('')
const password = ref('')
const adminName = ref('')

const handleLogin = () => {
  errorMessage.value = ''
  if (!identifier.value || !password.value) return

  isLoading.value = true

  setTimeout(() => {
    isLoading.value = false
    
    // Validação Real (Mock)
    if (identifier.value === 'admin' && password.value === '1234') {
      const storedName = localStorage.getItem('cifa_admin_name')
      
      if (storedName) {
        // Se já tem nome, vai direto para o sistema
        localStorage.setItem('cifa_auth_token', 'true')
        router.push('/admin/dashboard')
      } else {
        // Primeiro acesso: pede o nome
        step.value = 'name'
      }
    } else {
      errorMessage.value = 'Acesso negado. Credenciais inválidas.'
    }
  }, 1000)
}

const handleSaveName = () => {
  if (!adminName.value.trim()) return

  isLoading.value = true

  setTimeout(() => {
    localStorage.setItem('cifa_admin_name', adminName.value.trim())
    localStorage.setItem('cifa_auth_token', 'true')
    isLoading.value = false
    router.push('/admin/dashboard')
  }, 800)
}
</script>

<template>
  <main class="min-h-screen w-full flex bg-white font-poppins overflow-hidden">
    
    <aside class="hidden lg:flex flex-col justify-between w-1/2 relative p-12 text-white bg-[#0A0B1A]">
      <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div class="absolute -top-[20%] -left-[10%] w-[70%] h-[70%] rounded-full bg-purple-800/40 blur-[120px] mix-blend-screen"></div>
        <div class="absolute top-[30%] -right-[20%] w-[80%] h-[80%] rounded-full bg-blue-700/30 blur-[150px] mix-blend-screen"></div>
        <div class="absolute -bottom-[20%] left-[10%] w-[60%] h-[60%] rounded-full bg-indigo-900/50 blur-[100px] mix-blend-screen"></div>
      </div>

      <div class="relative z-10 flex items-center gap-3">
        <h1 class="text-4xl font-extrabold tracking-wider">CIFA</h1>
        <div class="text-[0.65rem] leading-tight text-slate-300 font-semibold uppercase tracking-widest border-l border-slate-500 pl-3">
          <p>Controle Inteligente</p>
          <p>de Fluxo Acadêmico</p>
        </div>
      </div>

      <div class="relative z-10 max-w-lg mt-12">
        <h2 class="text-6xl font-bold leading-tight mb-6 tracking-tight">
          Bem-vindo <br /> de volta! <span class="inline-block origin-bottom-right hover:animate-wave cursor-default">👋</span>
        </h2>
        <p class="text-xl text-slate-300 leading-relaxed font-light">
          Simplifique processos, organize acessos e mantenha a gestão académica da instituição de forma centralizada.
        </p>
      </div>

      <div class="relative z-10">
        <p class="text-sm text-slate-400 font-medium">CIFA &copy; 2026</p>
      </div>
    </aside>

    <section class="w-full lg:w-1/2 flex items-center justify-center p-8 sm:p-12">
      <div class="w-full max-w-md flex flex-col space-y-8" v-auto-animate>
        
        <div v-if="step === 'login'" key="login-step" class="w-full">
          <div class="mb-8">
            <h3 class="text-2xl font-bold text-slate-900 tracking-tight">Entrar no CIFA</h3>
            <p class="text-sm text-slate-500 mt-2 font-medium">Insira as suas credenciais de administrador.</p>
          </div>

          <form @submit.prevent="handleLogin" class="space-y-5">
            <div class="space-y-1.5">
              <Label for="identifier" class="text-xs font-bold text-slate-400 uppercase tracking-widest ml-1">Utilizador</Label>
              <Input 
                id="identifier" 
                type="text" 
                v-model="identifier"
                :disabled="isLoading"
                placeholder="Ex: admin"
                class="h-12 px-4 text-base bg-slate-50 border-slate-200 focus-visible:ring-indigo-600 rounded-xl transition-all"
                :class="{'border-red-300 bg-red-50': errorMessage}"
              />
            </div>

            <div class="space-y-1.5">
              <Label for="password" class="text-xs font-bold text-slate-400 uppercase tracking-widest ml-1">Palavra-passe</Label>
              <Input 
                id="password" 
                type="password" 
                v-model="password"
                :disabled="isLoading"
                placeholder="••••••••"
                class="h-12 px-4 text-base bg-slate-50 border-slate-200 focus-visible:ring-indigo-600 rounded-xl transition-all"
                :class="{'border-red-300 bg-red-50': errorMessage}"
              />
            </div>

            <p v-if="errorMessage" class="text-xs font-bold text-red-500 text-center animate-in fade-in slide-in-from-top-2">
              {{ errorMessage }}
            </p>

            <Button 
              type="submit" 
              :disabled="isLoading"
              class="w-full h-14 mt-4 bg-[#1A1A3A] hover:bg-[#0A102E] text-white font-bold text-base rounded-2xl transition-all shadow-xl shadow-[#1A1A3A]/20 active:scale-[0.98]"
            >
              <Loader2 v-if="isLoading" class="w-5 h-5 animate-spin" />
              <span v-else>Autenticar Acesso</span>
            </Button>
          </form>
        </div>

        <div v-else-if="step === 'name'" key="name-step" class="w-full">
          <div class="mb-8 flex flex-col items-center text-center">
            <div class="w-16 h-16 bg-indigo-50 rounded-full flex items-center justify-center mb-6 border border-indigo-100 shadow-inner">
              <UserCircle class="w-8 h-8 text-indigo-600" />
            </div>
            <h3 class="text-2xl font-bold text-slate-900 tracking-tight">Primeiro Acesso</h3>
            <p class="text-sm text-slate-500 mt-2 font-medium max-w-xs leading-relaxed">
              Para personalizarmos a sua experiência no painel, como gostaria de ser chamado?
            </p>
          </div>

          <form @submit.prevent="handleSaveName" class="space-y-6">
            <div class="space-y-1.5">
              <Input 
                id="adminName" 
                type="text" 
                v-model="adminName"
                :disabled="isLoading"
                placeholder="O seu primeiro nome ou apelido"
                class="h-14 px-4 text-center text-lg font-bold text-slate-800 bg-slate-50 border-slate-200 focus-visible:ring-indigo-600 rounded-2xl shadow-inner placeholder:font-medium placeholder:text-sm"
              />
            </div>

            <Button 
              type="submit" 
              :disabled="isLoading || !adminName.trim()"
              class="w-full h-14 bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-base rounded-2xl transition-all shadow-xl shadow-indigo-600/20 active:scale-[0.98] gap-2"
            >
              <Loader2 v-if="isLoading" class="w-5 h-5 animate-spin" />
              <template v-else>
                <span>Entrar no Dashboard</span>
                <ArrowRight class="w-5 h-5" />
              </template>
            </Button>
          </form>
        </div>

      </div>
    </section>

  </main>
</template>

<style scoped>
.font-poppins {
  font-family: 'Poppins', sans-serif;
}

@keyframes wave {
  0%, 60%, 100% { transform: rotate(0deg); }
  10%, 30% { transform: rotate(14deg); }
  20% { transform: rotate(-8deg); }
  40% { transform: rotate(-4deg); }
  50% { transform: rotate(10deg); }
}
.hover\:animate-wave:hover { animation: wave 2.5s infinite; }
</style>