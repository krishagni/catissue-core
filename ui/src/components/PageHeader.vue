
<template>
  <div class="os-page-header">
    <div class="content">
      <div class="left">
        <slot></slot>
      </div>
      <div class="right">
        <slot name="right"></slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .os-page-header {
    padding: 10px 15px 0px;
    border-bottom: 1px solid #ddd;
  }

  .os-page-header:after {
    content: ' ';
    display: block;
    clear: both;
  }

  .os-page-header .nav {
    float: left;
    width: 36px;
  }

  .os-page-header .content {
    float: left;
    margin-top: 7px;
    width: calc(100% - 65px);
  }

  .os-page-header .content:after {
    content: ' ';
    display: block;
    clear: both;
  }

  .os-page-header .content .left {
    float: left;
    width: 65%;
  }

  .os-page-header .content .right {
    float: right;
    width: 20%;
  }

  .os-page-header /deep/ h3 {
    margin-top: 0px;
    margin-bottom: 10px;
    font-size: 24px;
    font-family: inherit;
    font-weight: 500;
    line-height: 1.1;
    color: inherit;
  }
</style>
