<template>
  <Page>
    <PageHeader>
      <template #default>
        <h3>Users</h3>
      </template>
    </PageHeader>
    <PageBody>
      <PageToolbar>
        <template #default>
          <span v-if="ctx.selectedUsers.length == 0">
            <Button left-icon="plus" label="Create" @click="ngGoto('user-addedit', {userId: ''})" />

            <Button left-icon="users" label="User Groups" @click="ngGoto('user-groups')" />

            <Menu label="Import" :options="importOpts" />

            <Menu label="Export" :options="exportOpts" />
          </span>

          <span v-if="ctx.selectedUsers.length > 0">
            <Button left-icon="edit" label="Edit" @click="bulkEdit" />
          </span>
        </template>

        <template #right>
          <Button left-icon="search" label="Search" @click="openSearch" />
        </template>
      </PageToolbar>

      <ListView
        :data="ctx.users"
        :columns="ctx.columns"
        :filters="ctx.filters"
        :query="ctx.query"
        @filtersUpdated="loadUsers"
        allowSelection="true"
        @selectedRows="onUsersSelection"
        ref="listView"
      >
      </ListView>
    </PageBody>
  </Page>
</template>

<script>
import { reactive, inject } from 'vue';
import { format } from 'date-fns';

import ListView from '@/common/components/ListView.vue';
import Page from '@/common/components/Page.vue';
import PageHeader from '@/common/components/PageHeader.vue';
import PageBody from '@/common/components/PageBody.vue';
import PageToolbar from '@/common/components/PageToolbar.vue';
import Button from '@/common/components/Button.vue';
import Menu from '@/common/components/Menu.vue';

import http from '@/common/services/HttpClient.js';
import routerSvc from '@/common/services/Router.js';
import exportSvc from '@/common/services/ExportService.js';
import itemsSvc from '@/common/services/ItemsHolder.js';

export default {
  name: 'UsersList',

  inject: ['ui'],

  props: ['filters'],

  components: {
    Page,
    PageHeader,
    PageBody,
    PageToolbar,
    Button,
    ListView,
    Menu
  },

  setup(props) {
    const ui = inject('ui');

    let ctx = reactive({
      users: [],

      selectedUsers: [],

      columns: [
        {
          name: 'name',
          caption: 'Name',
          value: function (user) {
            return user.firstName + ' ' + user.lastName;
          },
          href: function (user) {
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
          value: function (user) {
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
            loadFn: (opts) => http.get('institutes', opts || { maxResults: 100 })
          }
        },
        {
          name: 'group',
          type: 'dropdown',
          caption: 'User Group',
          listSource: {
            displayProp: 'name',
            selectProp: 'name',
            loadFn: (opts) => http.get('user-groups', opts || { maxResults: 100 })
          }
        },
        {
          name: 'activityStatus',
          type: 'dropdown',
          caption: 'Activity Status',
          listSource: {
            options: ['Active', 'Archived', 'Expired', 'Locked', 'Pending']
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
              { name: 'SUPER', caption: 'Super Admin' },
              { name: 'INSTITUTE', caption: 'Institute Admin' },
              { name: 'CONTACT', caption: 'Contact' },
              { name: 'NONE', caption: 'Regular' }
            ]
          }
        }
      ],

      query: props.filters
    });

    return {
      ctx
    };
  },

  methods: {
    openSearch: function () {
      this.$refs.listView.toggleShowFilters();
    },

    loadUsers: function ({filters, uriEncoding}) {
      routerSvc.ngGoto(undefined, {filters: uriEncoding}, {notify: false});
      http.get('users', filters).then(resp => this.ctx.users = resp);
    },

    onUsersSelection: function(selection) {
      this.ctx.selectedUsers = selection;
    },

    bulkEdit: function() {
      let users = this.ctx.selectedUsers.map(user => ({id: user.rowObject.id}));
      itemsSvc.ngSetItems('users', users);
      routerSvc.ngGoto('user-bulk-edit');
    },

    ngGoto: routerSvc.ngGoto
  },

  computed: {
    importOpts: function() {
      return [
        { caption: 'Users', onSelect: () => this.ngGoto('user-import', {objectType: 'user'}) },
        { caption: 'User Roles', onSelect: () => this.ngGoto('user-import', {objectType: 'userRoles'}) },
        { caption: 'Forms', onSelect: () => this.ngGoto('user-import', {objectType: 'extensions'}) },
        { caption: 'View Past Imports', onSelect: () => this.ngGoto('user-import-jobs') }
      ]
    },

    exportOpts: function() {
      return [
        { caption: 'Users', onSelect: () => exportSvc.exportRecords({objectType: 'user'}) },
        { caption: 'User Roles', onSelect: () => exportSvc.exportRecords({objectType: 'userRoles'}) },
        { caption: 'User Forms', onSelect: () => this.ngGoto('user-export-forms') }
      ]
    }
  }
}
</script>
