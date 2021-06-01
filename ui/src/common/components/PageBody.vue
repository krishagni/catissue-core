
<template>
  <div class="os-page-body">
    <slot></slot>
  </div>
</template>

<style scoped>
  .os-page-body {
    padding: 10px 15px 0px;
  }

  .os-page-body :deep(.os-page-toolbar) {
    max-height: 55px;
  }

  .os-page-body :deep(.os-page-toolbar + div) {
    height: calc(100% - 55px);
  }
</style>
