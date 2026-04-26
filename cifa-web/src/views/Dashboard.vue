<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { User, UserCheck, UserX, FileText } from 'lucide-vue-next'

const kpiData = { total: 1248, autorizados: 1190, negados: 58 }
const animatedKpis = ref({ total: 0, autorizados: 0, negados: 0 })
const isMounted = ref(false)

const barChartData = [
  { day: 'Seg', value: 25 }, { day: 'Ter', value: 45 }, { day: 'Qua', value: 27 },
  { day: 'Qui', value: 38 }, { day: 'Sex', value: 45 }, { day: 'Sáb', value: 12 }, { day: 'Dom', value: 10 },
]
const maxBarValue = Math.max(...barChartData.map(d => d.value))

const doughnutData = [
  { label: 'ADS', value: 30, color: '#FF4D6D' }, { label: 'COMEX', value: 20, color: '#FF9100' },
  { label: 'DSM', value: 25, color: '#FFEA00' }, { label: 'GEEM', value: 15, color: '#00E676' },
  { label: 'OUTROS', value: 10, color: '#00C4FF' },
]

const radius = 35
const circumference = 2 * Math.PI * radius 

const getDashArray = (value: number) => `${(value / 100) * circumference} ${circumference}`

const getOffset = (index: number) => {
  let accumulatedValue = 0
  for (let i = 0; i < index; i++) accumulatedValue += doughnutData[i].value
  return -(accumulatedValue / 100) * circumference 
}

const animateValue = (target: number, key: keyof typeof animatedKpis.value, duration = 1500) => {
  let startTimestamp: number | null = null;
  const step = (timestamp: number) => {
    if (!startTimestamp) startTimestamp = timestamp;
    const progress = Math.min((timestamp - startTimestamp) / duration, 1);
    const easeOutExpo = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress);
    animatedKpis.value[key] = Math.floor(easeOutExpo * target);
    if (progress < 1) window.requestAnimationFrame(step);
  };
  window.requestAnimationFrame(step);
}

onMounted(() => {
  setTimeout(() => { isMounted.value = true }, 100)
  animateValue(kpiData.total, 'total');
  animateValue(kpiData.autorizados, 'autorizados');
  animateValue(kpiData.negados, 'negados');
})
</script>

<template>
  <div class="flex flex-col gap-4 pb-10">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      
      <Card class="bg-[#0A102E] text-white border-none rounded-2xl shadow-lg">
        <CardContent class="p-4 flex flex-col justify-between h-28">
          <div class="flex items-center gap-2.5">
            <User class="w-5 h-5 text-slate-300 opacity-70" />
            <h3 class="font-semibold text-sm opacity-80 tracking-wide">Total de acessos</h3>
          </div>
          <p class="text-3xl font-bold">{{ animatedKpis.total.toLocaleString('pt-BR') }}</p>
        </CardContent>
      </Card>
      
      <Card class="bg-[#0A102E] text-white border-none rounded-2xl shadow-lg">
        <CardContent class="p-4 flex flex-col justify-between h-28">
          <div class="flex items-center gap-2.5">
            <UserCheck class="w-5 h-5 text-slate-300 opacity-70" />
            <h3 class="font-semibold text-sm opacity-80 tracking-wide">Acessos autorizados</h3>
          </div>
          <p class="text-3xl font-bold">{{ animatedKpis.autorizados.toLocaleString('pt-BR') }}</p>
        </CardContent>
      </Card>

      <Card class="bg-[#FF3B3B] text-white border-none rounded-2xl shadow-lg">
        <CardContent class="p-4 flex flex-col justify-between h-28">
          <div class="flex items-center gap-2.5">
            <UserX class="w-5 h-5 text-white/80" />
            <h3 class="font-semibold text-sm opacity-90 tracking-wide">Acessos negados</h3>
          </div>
          <p class="text-3xl font-bold">{{ animatedKpis.negados }}</p>
        </CardContent>
      </Card>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
      
      <Card class="lg:col-span-2 bg-[#0A102E] border-none rounded-2xl shadow-xl flex flex-col h-72 lg:h-auto">
        <CardHeader class="flex flex-row items-center gap-2 space-y-0 p-4 pb-1">
          <FileText class="w-4 h-4 text-slate-300" />
          <CardTitle class="text-base font-bold text-white tracking-wide">Acessos da semana</CardTitle>
        </CardHeader>
        <CardContent class="flex-1 flex flex-col p-4 pt-0">
          <div class="flex-1 w-full flex items-end justify-between px-1 md:px-2 mt-2">
            <div 
              v-for="(item, index) in barChartData" 
              :key="index" 
              class="h-full flex flex-col items-center justify-end gap-1 w-full"
            >
              <span class="text-white text-[10px] md:text-xs font-bold pb-0.5">
                {{ item.value }}
              </span>
              
              <div class="relative flex-1 flex items-end w-6 md:w-10">
                <div 
                  class="w-full bg-white rounded-t-md transition-all duration-[1500ms] ease-out hover:bg-slate-300" 
                  :style="{ height: isMounted ? `${(item.value / maxBarValue) * 100}%` : '0%' }"
                ></div>
              </div>
              <span class="text-slate-400 text-[10px] md:text-[11px] font-semibold">{{ item.day }}</span>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="bg-[#0A102E] border-none rounded-2xl shadow-xl flex flex-col p-6">
        <CardContent class="flex-1 flex flex-col items-center justify-center p-0">
          
          <div class="relative w-full max-w-[140px] aspect-square mb-6">
            <svg viewBox="0 0 100 100" class="w-full h-full transform -rotate-90 overflow-visible">
              <circle 
                v-for="(segment, index) in doughnutData" 
                :key="index"
                cx="50" cy="50" r="35" fill="none" :stroke="segment.color" stroke-width="25" 
                :stroke-dasharray="getDashArray(segment.value)" 
                :stroke-dashoffset="isMounted ? getOffset(index) : circumference" 
                class="transition-all duration-[1500ms] ease-out"
              ></circle>
              <circle cx="50" cy="50" r="22" fill="#0A102E"></circle>
            </svg>
            <div class="absolute inset-0 flex items-center justify-center flex-col pointer-events-none">
              <span class="text-white text-[0.6rem] font-bold uppercase tracking-widest opacity-50">Cursos</span>
            </div>
          </div>

          <div class="w-full grid grid-cols-2 gap-y-3 gap-x-2">
            <div v-for="item in doughnutData" :key="item.label" class="flex items-center gap-2">
              <div class="w-2.5 h-2.5 rounded-full shadow-sm shrink-0" :style="{ backgroundColor: item.color }"></div>
              <div class="flex items-baseline gap-1.5 truncate">
                <span class="text-slate-300 text-xs font-bold tracking-tight">{{ item.label }}</span>
                <span class="text-white text-xs font-black">{{ item.value }}%</span>
              </div>
            </div>
          </div>

        </CardContent>
      </Card>

    </div>
  </div>
</template>