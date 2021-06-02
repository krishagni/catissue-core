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

            <AssignGroup @addToGroup="addToGroup" />

            <Button left-icon="archive" label="Archive" @click="archiveUsers" />

            <Button left-icon="check" label="Reactivate" @click="reactivateUsers" />

            <Button left-icon="trash" label="Delete" @click="deleteUsers" />

            <Button left-icon="lock" label="Lock" @click="lockUsers" />

            <Button left-icon="unlock" label="Unlock" @click="unlockUsers" />

            <Button left-icon="thumbs-up" label="Approve" @click="approveUsers" />
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

      <ConfirmDelete ref="deleteDialog">
        <template #message>
          <span>Are you sure you want to delete the selected users?</span>
        </template>
      </ConfirmDelete>
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
import ConfirmDelete from '@/common/components/ConfirmDelete.vue';

import instituteSvc from '@/administrative/services/Institute.js';
import userGrpSvc from '@/administrative/services/UserGroup.js';
import userSvc from '@/administrative/services/User.js';

import alertSvc from '@/common/services/Alerts.js';
import exportSvc from '@/common/services/ExportService.js';
import itemsSvc from '@/common/services/ItemsHolder.js';
import routerSvc from '@/common/services/Router.js';
import userGroupSvc from '@/administrative/services/UserGroup.js';

import AssignGroup from '@/administrative/user-groups/AssignGroup.vue';

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
    Menu,
    ConfirmDelete,
    AssignGroup
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
        { name: 'institute', type: 'dropdown', caption: 'Institute',
          listSource: {
            displayProp: 'name',
            selectProp: 'name',
            loadFn: (opts) => instituteSvc.getInstitutes(opts)
          }
        },
        { name: 'group', type: 'dropdown', caption: 'User Group',
          listSource: {
            displayProp: 'name',
            selectProp: 'name',
            loadFn: (opts) => userGrpSvc.getUserGroups(opts)
          }
        },
        { name: 'activityStatus', type: 'dropdown', caption: 'Activity Status',
          listSource: {
            options: ['Active', 'Archived', 'Expired', 'Locked', 'Pending']
          }
        },
        { name: 'type', type: 'dropdown', caption: 'Type',
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
      userSvc.getUsers(filters).then(resp => this.ctx.users = resp);
    },

    onUsersSelection: function(selection) {
      this.ctx.selectedUsers = selection;
    },

    bulkEdit: function() {
      let users = this.ctx.selectedUsers.map(user => ({id: user.rowObject.id}));
      itemsSvc.ngSetItems('users', users);
      routerSvc.ngGoto('user-bulk-edit');
    },

    updateStatus: function(fromStatuses, toStatus, msg) {
      let users = this.ctx.selectedUsers
        .map(user => user.rowObject)
        .filter(user => !fromStatuses || fromStatuses.length == 0 || fromStatuses.indexOf(user.activityStatus) != -1);

      let usersMap = {};
      users.forEach(u => usersMap[u.id] = u);

      let self = this;
      userSvc.bulkUpdate({detail: {activityStatus: toStatus}, ids: Object.keys(usersMap)}).then(
        function(saved) {
          alertSvc.success(saved.length + (saved.length != 1 ? ' users ' : ' user ') + msg);
          self.$refs.listView.reload();
        }
      );
    },

    addToGroup: function(group) {
      let users = this.ctx.selectedUsers.map(user => user.rowObject);
      if (users.length == 0) {
        return;
      }

      let instituteId = users[0].instituteId;
      for (let user of users) {
        if (user.instituteId != instituteId) {
          alertSvc.error('Users of multiple institutes cannot be added to the group.');
          return;
        }
      }

      if (group) {
        userGroupSvc.addUsers(group, users).then(() => alertSvc.success('Users added to the group ' + group.name));
      } else {
        itemsSvc.ngSetItems(
          'users',
          users.map(user => ({id: user.id, insituteId: user.instituteId, instituteName: user.instituteName}))
        );
        routerSvc.ngGoto('user-group-addedit', {groupId: ''});
      }
    },

    archiveUsers: function() {
      this.updateStatus(['Locked', 'Active', 'Expired'], 'Closed', 'archived');
    },

    reactivateUsers: function() {
      this.updateStatus(['Closed'], 'Active', 'reactivated');
    },

    lockUsers: function() {
      this.updateStatus(['Active'], 'Locked', 'locked');
    },

    unlockUsers: function() {
      this.updateStatus(['Locked'], 'Active', 'unlocked');
    },

    approveUsers: function() {
      this.updateStatus(['Pending'], 'Active', 'sign-up request approved');
    },

    deleteUsers: function() {
      let users = this.ctx.selectedUsers.map(user => user.rowObject);

      if (!this.ui.currentUser.admin) {
        let admins = users.filter(user => user.admin == true)
          .map(user => user.firstName + ' ' + user.lastName)
          .join(',');

        if (admins.length > 0) {
          alertSvc.error('Super administrator rights required to delete admin users: ' + admins);
          return;
        }
      }

      this.$refs.deleteDialog.open().then(() => this.updateStatus([], 'Disabled', 'deleted'));
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
