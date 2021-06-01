
<template>
  <span>
    <Button :label="label" right-icon="caret-down" @click="toggle" />
    <dropdown-menu ref="menu" :model="items" :popup="true" />
  </span>
</template>

<script>
import Menu from 'primevue/menu';
import Button from '@/common/components/Button.vue';

export default {
  props: ['label', 'options'],

  components: {
    Button,
    'dropdown-menu': Menu
  },

  methods: {
    toggle(event) {
      this.$refs.menu.toggle(event);
    }
  },

  computed: {
    items: function() {
      return this.options.map(({caption, onSelect}) => { return {label: caption, command: onSelect} } );
    }
  }
}
</script>
