<template>
  <div id="basicLayout">
    <el-container style="height: 100vh">
      <el-header class="header">
        <GlobalHeader/>
      </el-header>
      <el-main class="content">
        <router-view/>
      </el-main>
      <el-footer class="footer">
        <a
            href="https://gitee.com/taoshaolo"
            target="_blank"
            style="text-decoration: none"
        >
          作者：涛少
        </a>
      </el-footer>
    </el-container>
  </div>
</template>

<script>
import GlobalHeader from "./GlobalHeader.vue";
export default {
  components: {GlobalHeader}
}
</script>

<style scoped>
#basicLayout .header {
  margin-bottom: 16px;
  box-shadow: #eee 1px 1px 5px;
}

#basicLayout .content {
  margin-bottom: 28px;
  padding: 20px;
  background: linear-gradient(to right, #fefefe, #fff);
}

.footer {
  padding: 16px;
  text-align: center;
  background: #efefef;
}
</style>