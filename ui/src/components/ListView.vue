<template>
  <div class="os-table" :class="{'show-filters': showFilters}">
    <div class="results">
      <div class="results-inner">
        <data-table :value="list">
          <column v-for="column of columns" :header="column.caption" :key="column.name">
            <template #body="slotProps">
              <span v-if="column.href">
                <a :href="column.href(slotProps.data)" :target="column.hrefTarget">
                  <span v-text="slotProps.data[column.name]"></span>
                </a>
              </span>
              <span v-else>
                <span v-text="slotProps.data[column.name]"></span>
              </span>
            </template>
          </column>
        </data-table>
      </div>
    </div>
    <div class="filters">
      <div class="filters-inner">
        <div class="title">
          <span>Filters</span>
        </div>
        <div class="body">
          <form-group dense v-for="filter of filters" :key="filter.name">
            <cell :width="12">
              <span v-if="filter.type == 'text'">
                <input-text md-type="true" :placeholder="filter.caption" v-model="filterValues[filter.name]"/>
              </span>
              <span v-if="filter.type == 'dropdown'">
                <dropdown md-type="true" :placeholder="filter.caption" v-model="filterValues[filter.name]"
                  :list-source="filter.listSource">
                </dropdown>
              </span>
            </cell>
          </form-group>
          <form-group>
            <cell :width="12">
              <Button style="width: 100%" label="Clear Filters" @click="clearFilters"/>
            </cell>
          </form-group>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

import FormGroup from '@/components/FormGroup.vue';
import Col from '@/components/Col.vue';
import InputText from '@/components/InputText.vue';
import Dropdown from '@/components/Dropdown.vue';
import Button from '@/components/Button.vue';

export default {
  props: [ 'data', 'columns', 'filters', 'query' ],

  components: {
    'data-table': DataTable,
    'column': Column,
    'form-group': FormGroup,
    'cell': Col,
    'input-text': InputText,
    'dropdown': Dropdown,
    Button
  },

  setup() {
    return {
      debounce: (function() {
        let timeout = null;
        return function(fn, delayMs) {
          clearTimeout(timeout);
          timeout = setTimeout(() => { fn(); }, delayMs || 500);
        };
      })()
    }
  },

  data() {
    return {
      showFilters: false,

      filterValues: { }
    }
  },

  mounted() {
    let values = {};
    if (this.query) {
      values = JSON.parse(decodeURIComponent(atob(this.query)));
    }

    Object.assign(this.filterValues, values);
    if (Object.keys(values).length > 0) {
      this.showFilters = true;
    } else {
      this.emitFiltersUpdated();
    }
  },

  methods: {
    toggleShowFilters: function() {
      this.showFilters = !this.showFilters;
    },

    clearFilters: function() {
      this.filters.forEach((filter) => this.filterValues[filter.name] = undefined);
    },

    emitFiltersUpdated: function() {
      let fb = undefined;
      if (this.filterValues && Object.keys(this.filterValues).length > 0) {
        let curatedFilters = {};
        for (const [key, value] of Object.entries(this.filterValues)) {
          if (value) {
            curatedFilters[key] = value;
          }
        }

        if (Object.keys(curatedFilters).length > 0) {
          fb = btoa(encodeURIComponent(JSON.stringify(curatedFilters)));
        }
      }

      this.$emit('filtersUpdated', {filters: this.filterValues, uriEncoding: fb});
    }
  },

  computed: {
    list() {
      let input      = this.data || [];
      let columnDefs = this.columns || [];

      let result = [];
      for (let rowIdx = 0; rowIdx < input.length; ++rowIdx) {
        let row = {rowObject: input[rowIdx]};

        for (let colIdx = 0; colIdx < columnDefs.length; ++colIdx) {
          let cd = columnDefs[colIdx];
          if (cd.value) {
            row[cd.name] = cd.value(input[rowIdx]);
          } else {
            row[cd.name] = input[rowIdx][cd.name];
          }
        }

        result.push(row);
      }

      return result;
    }
  },

  watch: {
    filterValues: {
      deep: true,

      handler() {
        let self = this;
        this.debounce(() => self.emitFiltersUpdated());
      }
    }
  }
}
</script>

<style scoped>
  
.os-table {
  overflow: auto;
  margin-right: -15px;
}

.os-table:after {
  content: ' ';
  clear: both;
  display: block;
}

.os-table .results {
  float: left;
  width: 100%;
  height: 100%;
  position: relative;
}

.os-table.show-filters .results {
  width: 75%;
}

.os-table .results .results-inner {
  position: absolute;
  top: 0px;
  bottom: 0px;
  left: 0px;
  right: 0px;
  overflow: auto;
  padding-right: 15px;
}

.os-table .filters {
  float: left;
  display: none;
  position: relative;
  height: 100%;
  width: 25%;
}

.os-table.show-filters .filters {
  display: block;
  border-left: 1px solid #ddd;
}

.os-table .filters .filters-inner {
  position: absolute;
  top: 0px;
  left: 0px;
  right: 0px;
  bottom: 0px;
  overflow: auto;
  padding: 0px 15px 15px
}

.filters .title {
  margin: 0px;
  font-size: 14px;
  font-weight: bold;
  color: #333!important;
  line-height: 1.42;
  padding: 8px 0px;
  border-bottom: 2px solid #ddd;
}

.filters .body {
  margin-top: 25px;
}

.filters .body .form-group {
  margin-bottom: 30px;
}

.os-table /deep/ table {
  width: 100%;
  margin-bottom: 20px;
  display: table;
  border-collapse: collapse;
  table-layout: inherit;
}

.os-table /deep/ tr {
  margin-right: 0px;
  margin-left: 0px;
}

.os-table /deep/ thead tr th,
.os-table /deep/ tbody tr td {
  padding: 8px;
  line-height: 1.42857143;
  vertical-align: top;
  border-top: 1px solid #ddd;
  word-break: break-word;
}        
        
.os-table /deep/ thead tr th {
  vertical-align: bottom;
  border-bottom: 1px solid #ddd;
  font-weight: bold;
}
    
.os-table /deep/ thead tr:first-child th {
  border-top: 0;
}   

.os-table-hover /deep/ tbody tr:hover {
  background: #f7f7f7;
}
</style>
