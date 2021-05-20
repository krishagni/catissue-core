
<template>
  <div class="os-md-input" v-if="$attrs['md-type']">
    <label v-show="showLabel">{{$attrs.placeholder}}</label>
    <input type="text" class="form-control" v-model="inputVal" v-bind="$attrs">
  </div>
  <span v-else>
    <input type="text" class="form-control" v-model="inputVal" v-bind="$attrs">
  </span>
</template>

<script>
export default {
  props: ['modelValue'],

  data() {
    return {
    }
  },

  computed: {
    inputVal: {
      get() {
        return this.modelValue;
      },

      set(value) {
        this.$emit('update:modelValue', value);
      }
    },

    showLabel: function() {
      return !!this.modelValue;
    }
  },

  methods: {
  }
}
</script>

<style scoped>
  .os-md-input {
    position: relative;
  }

  .os-md-input > label {
    position: absolute;
    color: rgba(0, 0, 0, 0.6);
    font-size: 0.8em;
    font-weight: normal;
    top: -1.25em;
  }

  .os-md-input input {
    border: 0;
    height: auto!important;
    box-shadow: none !important;
    border-radius: 0;
    border-bottom: 2px solid #ddd;
    margin-top: 15px;
    outline: none;
    display: block;
    width: 100%;
    padding: 2px 0px;
  }

  .os-md-input input:focus {
    border-bottom: 2px solid #66afe9!important;
    background: #fff!important;
    border-color: #66afe9!important;
  }
</style>
