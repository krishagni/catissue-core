import { createApp } from 'vue'
import App from './App.vue'
import PrimeVue from 'primevue/config';
import router from './router'

const app = createApp(App)
  .use(router)
  .use(PrimeVue);
app.mount('#app')

var ui = {};
app.provide('ui', ui);

window.parent.postMessage({op: 'getGlobalProps', requestor: 'vueapp'}, '*');
window.addEventListener('message', function(event) {
  if (event.data.op == 'getGlobalProps') {
    ui.os = event.data.resp.os || {};
  }
});
