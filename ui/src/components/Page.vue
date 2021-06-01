
<template>
  <div class="os-page">
    <slot></slot>
  </div>
</template>

<style scoped>
  .os-page {
    height: 100%;
  }

  .os-page :deep(.os-page-body) {
    height: calc(100% - 50px);
    overflow-y: auto;
  }
</style>
