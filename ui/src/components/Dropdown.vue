
<template>
  <div class="os-dropdown">
    <div class="p-float-label" v-if="$attrs['md-type']">
      <Dropdown
        v-model="selected"
        :options="ctx.options"
        :option-label="displayProp"
        :option-value="selectProp"
        :filter="true"
        :show-clear="showClear"
        @focus="searchOptions"
        @filter="searchOptions($event)">
      </Dropdown>
      <label>{{$attrs.placeholder}}</label>
    </div>
    <div v-else>
      <span>Under construction</span>
    </div>
  </div>
</template>

<script>
import { reactive } from 'vue';
import Dropdown from 'primevue/dropdown';

export default {
  props: ['modelValue', 'listSource'],

  emits: ['update:modelValue'],

  components: {
    Dropdown
  },

  setup() {
    let ctx = reactive({
      defOptions: null,
      options: []
    });

    return {
      ctx
    }
  },

  data() {
    return { };
  },

  methods: {
    searchOptions(event) {
      let query = event && event.value;
      if (!query) {
        if (this.ctx.defOptions) {
          this.ctx.options = this.ctx.defOptions;
        } else {
          if (this.listSource.options) {
            this.ctx.defOptions = this.ctx.options = this.listSource.options;
          } else if (typeof this.listSource.loadFn == 'function') {
            let self = this;
            this.listSource.loadFn({maxResults: 100}).then(
              function(options) {
                self.ctx.defOptions = self.ctx.options = options;
              }
            );
          }
        }
      } else {
        query = query.toLowerCase();
        if (this.ctx.defOptions && this.ctx.defOptions.length < 100) {
          this.ctx.options = this.filterOptions(this.ctx.defOptions, query);
        } else {
          if (this.listSource.options) {
            this.ctx.options = this.filterOptions(this.listSource.options, query);
          } else if (typeof this.listSource.loadFn == 'function') {
            let self = this;
            this.listSource.loadFn({query: query, maxResults: 100}).then(
              function(options) {
                self.ctx.options = options;
              }
            );
          }
        }
      }
    },

    filterOptions(inputOptions, query) {
      let self = this;
      return (inputOptions || []).filter(
        function(option) {
          if (typeof option == 'object') {
            return (option[self.listSource.displayProp] || '').toString().toLowerCase().indexOf(query) > -1 ||
              (option[self.listSource.selectProp] || '').toString().toLowerCase().indexOf(query) > -1;
          } else {
            return (option || '').toString().toLowerCase().indexOf(query) > -1;
          }
        }
      );
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
    },

    displayProp: function() {
      return this.listSource.displayProp;
    },

    selectProp: function() {
      return this.listSource.selectProp;
    },

    showClear: function() {
      return true;
    }
  },

  watch: {
    selected(newVal, oldVal) {
      if (!oldVal && !!newVal) {
        this.searchOptions({value: newVal});
      }
    }
  }
}
</script>

<style scoped>
  .os-dropdown .p-float-label {
    margin-top: 10px;
  }

  .os-dropdown .p-float-label :deep(label) {
    left: 0rem;
  }

  .os-dropdown .p-float-label :deep(.p-dropdown) {
    border: 0px;
    border-bottom: 2px solid #ced4da;
    border-radius: 0px;
    box-shadow: none;
  }

  .os-dropdown .p-float-label :deep(.p-dropdown .p-inputtext) {
    padding: 2px 0px;
  }

  .os-dropdown .p-float-label :deep(.p-dropdown.p-inputwrapper-focus) {
    border-bottom-color: #007bff;
  }

  .os-dropdown .p-float-label :deep(.p-dropdown:not(.p-inputwrapper-focus) ~ label) {
    color: #999;
  }

  .os-dropdown .p-float-label :deep(.p-dropdown.p-inputwrapper-filled .p-inputtext) {
    padding: 2px 0px;
  }

  .os-dropdown .p-float-label :deep(.p-dropdown-trigger-icon) {
    opacity: 0.5;
    font-size: 0.75rem;
  }

  .os-dropdown .p-float-label :deep(.p-dropdown-clear-icon) {
    opacity: 0.5;
    font-size: 0.75rem;
    margin-top: -0.40rem;
  }
</style>
