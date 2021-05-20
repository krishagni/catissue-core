<template>
  <div class="os-table" :class="{'show-filters': showFilters}">
    <div class="results">
      <div class="results-inner">
        <DataTable :value="list">
          <Column v-for="column of columns" :header="column.caption" :key="column.name">
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
          </Column>
        </DataTable>
      </div>
    </div>
    <div class="filters">
      <div class="filters-inner">
        <div class="title">
          <span>Filters</span>
        </div>
        <div class="body">
          <div class="form-group" v-for="filter of filters" :key="filter.name">
            <span v-if="filter.type == 'text'">
              <InputText md-type="true" :placeholder="filter.caption" v-model="filterValues[filter.name]"/>
            </span>
          </div>

          <div class="form-group">
            <Dropdown v-model="filterValues.dd" :options="cities" @search="searchCity"/>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from '@/components/InputText.vue';
import Dropdown from '@/components/Dropdown.vue';

export default {
  props: [ 'data', 'columns', 'filters' ],

  components: {
    DataTable,
    Column,
    InputText,
    Dropdown
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

      filterValues: { },

      cities: []
    }
  },

  methods: {
    toggleShowFilters: function() {
      this.showFilters = !this.showFilters;
    },

    searchCity: function(query) {
      alert(query);

      let citiesRepo = ['Pune', 'Kolhapur', 'Nippani', 'Belgaum', 'Dharwad', 'Hubli'];

      if (!query) {
        this.cities = citiesRepo;
      } else {
        this.cities = citiesRepo.filter(city => city.toLowerCase().indexOf((query || '').toLowerCase()) > -1);
      }
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

      handler(newVal) {
        let self = this;
        this.debounce(() => self.$emit('filtersUpdated', newVal));
      }
    }
  }
}
</script>

<style scoped>
  
.os-table {
  overflow: auto;
  width: 100%;
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
}

.os-table .filters .filters-inner {
  position: absolute;
  top: 0px;
  left: 0px;
  right: 0px;
  bottom: 0px;
  overflow: auto;
  margin: 0px 15px 15px
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
