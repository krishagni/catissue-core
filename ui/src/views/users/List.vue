<template>
  <div>
    <h3> Users List View </h3>
    <div>
      <table class="os-table">
        <thead class="os-table-head">
          <tr class="row">
            <th class="col">Name</th>
            <th class="col">Email Address</th>
            <th class="col">Login Name</th>
            <th class="col">Active Since</th>
          </tr>
        </thead> 
        <tbody class="os-table-body">
          <tr class="row" v-for="user in ctx.users" :key="user.id">
            <td>{{user.firstName}} {{user.lastName}}</td>
            <td>{{user.emailAddress}}</td>
            <td>{{user.loginName}}</td>
            <td>{{user.creationDate}}</td>
          </tr>
        </tbody>
      </table>
      {{ctx.users.length}} users
    </div>
  </div>
</template>

<script>
import { reactive } from 'vue';

import http from '@/services/HttpClient.js';

export default {
  name: 'UsersList',

  components: {
  },

  setup() {
    let ctx = reactive({
      users: []
    });

    http.get('users').then(resp => ctx.users = resp);

    return {
      ctx
    };
  }
}
</script>
