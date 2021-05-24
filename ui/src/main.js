import { createApp } from 'vue'
import PrimeVue from 'primevue/config';

// import 'primevue/resources/themes/saga-blue/theme.css';
import 'primevue/resources/themes/bootstrap4-light-blue/theme.css';
import 'primevue/resources/primevue.css';
import 'primeflex/primeflex.css';
import 'primeicons/primeicons.css';

import ToastService from 'primevue/toastservice';

import router from './router'
import App from './App.vue'
import http from '@/services/HttpClient.js';
import alerts from '@/services/Alerts.js';

const ui = {
  ngServer: 'http://localhost:9000/'
};

window.parent.postMessage({op: 'getGlobalProps', requestor: 'vueapp'}, '*');
window.parent.postMessage({op: 'getAuthToken', requestor: 'vueapp'}, '*');
window.addEventListener('message', function(event) {
  if (event.data.op == 'getGlobalProps') {
    ui.os = event.data.resp.os || {};

    let server = ui.os.server || {};
    http.protocol = server.secure ? 'https' : 'http';
    http.host = server.hostname;
    http.port = server.port;
    http.path = server.app || '';
    if (http.path) {
      http.path += '/';
    }

    http.path += 'rest/ng'
  } else if (event.data.op == 'getAuthToken') {
    ui.token = event.data.resp;
    http.headers['X-OS-API-TOKEN'] = ui.token; // localStorage.getItem('osAuthToken');
  }
});

const app = createApp(App)
  .use(router)
  .use(PrimeVue)
  .use(ToastService);

app.mount('#app')
app.provide('ui', ui);

alerts.toastSvc = app.config.globalProperties.$toast;

