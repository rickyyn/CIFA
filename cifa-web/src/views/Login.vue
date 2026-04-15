<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Loader2 } from 'lucide-vue-next'

const router = useRouter()
const identifier = ref('')
const password = ref('')
const isLoading = ref(false)

const handleLogin = () => {
  // Impede submissões vazias (simulação básica de validação)
  if (!identifier.value || !password.value) return

  isLoading.value = true

  // Simulando uma requisição de API com delay de 1.2 segundos
  setTimeout(() => {
    // Gravando o token de acesso simulado
    localStorage.setItem('cifa_auth_token', 'true')
    isLoading.value = false
    // Redirecionando para o painel
    router.push('/admin/dashboard')
  }, 1200)
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
          Bem vindo <br /> de volta! <span class="inline-block origin-bottom-right hover:animate-wave cursor-default">👋</span>
        </h2>
        <p class="text-xl text-slate-300 leading-relaxed font-light">
          Simplifique processos, organize entradas e saídas e tenha o controle da Fatec na palma da mão.
        </p>
      </div>

      <div class="relative z-10">
        <p class="text-sm text-slate-400 font-medium">CIFA &copy; 2026</p>
      </div>
    </aside>

    <section class="w-full lg:w-1/2 flex items-center justify-center p-8 sm:p-12">
      <div class="w-full max-w-md flex flex-col space-y-8">
        
        <div>
          <h3 class="text-2xl font-semibold text-slate-900">Entrar no CIFA</h3>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-5">
          
          <div class="space-y-1.5">
            <Label for="identifier" class="sr-only">E-mail ou Telefone</Label>
            <Input 
              id="identifier" 
              type="text" 
              v-model="identifier"
              :disabled="isLoading"
              placeholder="E-mail ou Telefone"
              class="h-12 px-4 text-base bg-white border-slate-300 focus-visible:ring-indigo-600"
            />
          </div>

          <div class="space-y-1.5">
            <Label for="password" class="sr-only">Senha</Label>
            <Input 
              id="password" 
              type="password" 
              v-model="password"
              :disabled="isLoading"
              placeholder="Senha"
              class="h-12 px-4 text-base bg-white border-slate-300 focus-visible:ring-indigo-600"
            />
          </div>

          <div class="flex items-center justify-start pt-1">
            <a href="#" class="text-sm font-medium text-slate-500 hover:text-indigo-600 transition-colors">
              Esqueceu a senha?
            </a>
          </div>

          <Button 
            type="submit" 
            :disabled="isLoading"
            v-auto-animate
            class="w-full h-12 mt-2 bg-[#1A1A3A] hover:bg-[#0F0F24] text-white font-medium text-base rounded-lg transition-all shadow-sm"
          >
            <div v-if="isLoading" class="flex items-center justify-center gap-2">
              <Loader2 class="w-5 h-5 animate-spin" />
              <span>Autenticando...</span>
            </div>
            <span v-else>Entrar</span>
          </Button>

        </form>

      </div>
    </section>

  </main>
</template>

<style scoped>
/* Tipografia Poppins importada/garantida para a tela de login */
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