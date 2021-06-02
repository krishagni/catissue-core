import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/users',
    name: 'UsersList',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "users-list" */ '../administrative/users/List.vue'),
    props: (route) => ({filters: route.query.filters, groupId: route.query.groupId})
  },
  {
    path: '/user-addedit/:userId',
    name: 'UserAddEdit',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "user-addedit" */ '../administrative/users/AddEdit.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
