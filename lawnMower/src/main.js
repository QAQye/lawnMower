import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
const app = createApp(App)

app.use(router)        // ✅ 关键：注入路由
app.use(ElementPlus)

app.mount('#app')