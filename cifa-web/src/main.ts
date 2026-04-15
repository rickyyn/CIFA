import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { autoAnimatePlugin } from '@formkit/auto-animate/vue'

// Importação do CSS Global (Tailwind)
import './assets/index.css' 

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Injetando animações suaves globais (zero-config)
app.use(autoAnimatePlugin)

app.mount('#app')