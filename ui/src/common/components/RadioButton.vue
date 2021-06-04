
<template>
  <div class="os-radio-buttons">
    <span class="p-field-radiobutton" v-for="(option, idx) of options" :key="idx">
      <label>
        <RadioButton :name="name" :value="option.value" v-model="inputValue" />
        <span>{{option.caption}}</span>
      </label>
    </span>
  </div>
</template>

<script>

import RadioButton from 'primevue/radiobutton';

export default {
  props: ['name', 'options', 'modelValue'],

  emits: ['change', 'update:modelValue'],

  components: {
    RadioButton
  },

  computed: {
    inputValue: {
      get() {
        return this.modelValue;
      },

      set(value) {
        this.$emit('update:modelValue', value);
        this.$emit('change',  value);
      }
    }
  }
}

</script>

<style scoped>
  .os-radio-buttons.inline .p-field-radiobutton {
    float: left;
  }

  .os-radio-buttons.inline:after {
    content: ' ';
    display: inline-block;
    clear: both;
  }

  .os-radio-buttons label span {
    margin-left: 0.5rem;
  }

  .os-radio-buttons label {
    line-height: 1.30rem;
    margin-left: 0rem;
    cursor: pointer;
  }

  .os-radio-buttons.inline label span {
    margin-left: 0.25rem;
    margin-right: 0.75rem;
  }

</style>
