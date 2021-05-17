<template>
  <router-view/>
</template>

<script>
import http from '@/services/HttpClient.js';

export default {
  name: 'App',

  setup() {
    http.protocol = 'http';
    http.host = 'localhost';
    http.port = 8080;
    http.path = 'openspecimen/rest/ng';
    http.headers['X-OS-API-TOKEN'] = localStorage.getItem('osAuthToken');
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#nav {
  padding: 30px;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #42b983;
}
</style>
