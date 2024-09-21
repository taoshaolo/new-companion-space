import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/css/global.css'
import routes from "./plugins/routes";
import * as VueRouter from 'vue-router'
import "echarts";

const app = createApp(App)
const router = VueRouter.createRouter({
    history: VueRouter.createWebHistory(),
    routes,
})

app.use(ElementPlus)
app.use(router)
app.mount('#app')
