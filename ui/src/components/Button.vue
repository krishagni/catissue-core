
<template>
  <button type="button" class="btn">
    <span>{{$attrs.label}}</span>
  </button>
</template>

<style scoped>

.os-page-toolbar button,
button {
  color: #666666;
  background: #f5f5f5;
  border: 1px solid #ccc;
  vertical-align: initial;
  border-radius: 3px;
  padding: 4px 10px;
  display: inline-block;
  font-size: inherit;
  line-height: inherit;
}

.os-page-toolbar button:hover,
button:hover {
  background: #ddd;
  border: 1px solid #a5a5a5;
}


</style>
