<template>
  <div class="os-table">
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
</template>

<script>

import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

export default {
  props: [ 'data', 'columns' ],

  components: {
    DataTable,
    Column
  },

  data() {
    return {}
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
  }
}
</script>

<style scoped>

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
