
<template>
  <AutoComplete v-bind="$attrs"
    ref="autoCompl"
    v-model="selected" 
    :dropdown="true"
    :suggestions="options"
    @click="searchOptions"
    @complete="searchOptions($event)"/>
</template>

<script>
import AutoComplete from 'primevue/autocomplete';

export default {
  props: ['modelValue', 'options'],

  components: {
    AutoComplete
  },

  data() {
    return { };
  },

  methods: {
    searchOptions(event) {
      alert('search options');
      console.log(event.query);
      this.$emit('search', event && event.query);
    }
  },

  computed: {
    selected: {
      get() {
        return this.modelValue;
      },

      set(value) {
        this.$emit('update:modelValue', value);
      }
    }
  }
}
</script>
