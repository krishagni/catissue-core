<template>
  <Page>
    <PageHeader>
      <template #default>
        <h3>Users</h3>
      </template>
    </PageHeader>
    <PageBody>
      <PageToolbar>
        <template #right>
          <Button label="Search" @click="openSearch"/>
        </template>
      </PageToolbar>

      <ListView :data="ctx.users" :columns="ctx.columns" :filters="ctx.filters" @filtersUpdated="loadUsers"
        ref="listView">
      </ListView>
    </PageBody>
  </Page>
</template>

<script>
import { reactive, inject } from 'vue';
import { format } from 'date-fns';

import ListView from '@/components/ListView.vue';
import Page from '@/components/Page.vue';
import PageHeader from '@/components/PageHeader.vue';
import PageBody from '@/components/PageBody.vue';
import PageToolbar from '@/components/PageToolbar.vue';
import Button from '@/components/Button.vue';
import http from '@/services/HttpClient.js';

export default {
  name: 'UsersList',

  inject: ['ui'],

  components: {
    Page,
    PageHeader,
    PageBody,
    PageToolbar,
    Button,
    ListView,
  },

  setup() {
    const ui = inject('ui');

    let ctx = reactive({
      users: [],

      columns: [
        {
          name: 'name',
          caption: 'Name',
          value: function(user) {
            return user.firstName + ' ' + user.lastName;
          },
          href: function(user) {
            return ui.ngServer + '#/users/' + user.rowObject.id + '/overview';
          },
          hrefTarget: '_parent'
        },
        { name: 'emailAddress', caption: 'Email Address' },
        { name: 'loginName', caption: 'Login Name' },
        { name: 'instituteName', caption: 'Institute' },
        { name: 'primarySite', caption: 'Primary Site' },
        {
          name: 'activeSince',
          caption: 'Active Since',
          value: function(user) {
            if (user.creationDate) {
              return format(new Date(user.creationDate), ui.os.global.dateFmt);
            }
            return undefined;
          }
        }
      ],

      filters: [
        { name: 'name', type: 'text', caption: 'Name' },
        { name: 'loginName', type: 'text', caption: 'Login Name' },
        {
          name: 'institute',
          type: 'dropdown',
          caption: 'Institute',
          listSource: {
            displayProp: 'name',
            selectProp: 'name',
            loadFn: (opts) => http.get('institutes', opts || {maxResults: 100})
          }
        },
        {
          name: 'group',
          type: 'dropdown',
          caption: 'User Group',
          listSource: {
            displayProp: 'name',
            selectProp: 'name',
            loadFn: (opts) => http.get('user-groups', opts || {maxResults: 100})
          }
        },
        {
          name: 'activityStatus',
          type: 'dropdown',
          caption: 'Activity Status',
          listSource: {
            options: [ 'Active', 'Archived', 'Expired', 'Locked', 'Pending' ]
          }
        },
        {
          name: 'type',
          type: 'dropdown',
          caption: 'Type',
          listSource: {
            selectProp: 'name',
            displayProp: 'caption',
            options: [
              {name: 'SUPER', caption: 'Super Admin'},
              {name: 'INSTITUTE', caption: 'Institute Admin'},
              {name: 'CONTACT', caption: 'Contact'},
              {name: 'NONE', caption: 'Regular'}
            ]
          }
        }
      ]
    });

    http.get('users').then(resp => ctx.users = resp);

    return {
      ctx
    };
  },

  methods: {
    openSearch: function() {
      this.$refs.listView.toggleShowFilters();
    },

    loadUsers: function(filters) {
      http.get('users', filters).then(resp => this.ctx.users = resp);
    }
  }
}
</script>
