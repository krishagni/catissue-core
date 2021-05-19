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
html {
  height: 100%;
}

body {
  margin: 0px;
  height: calc(100% - 15px);
}

#app {
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 14px;
  line-height: 1.42857143;
  color: #333333;
  background-color: #fff;
  height: 100%;
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
