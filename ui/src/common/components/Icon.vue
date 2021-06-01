
<template>
  <span class="os-icon-wrapper">
    <fa-icon :icon="name" />
  </span>
</template>

<script>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

export default {
  props: ['name'],

  components: {
    'fa-icon': FontAwesomeIcon
  }
}
</script>

<style scoped>
  .os-icon-wrapper.pad-left {
    padding-left: 4px;
  }

  .os-icon-wrapper.pad-right {
    padding-right: 4px;
  }
</style>
