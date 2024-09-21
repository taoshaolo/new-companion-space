<template>
  <div id="app">
    <BasicLayout v-if="showBasicLayout"/>
    <router-view v-else/>
  </div>
  <div class="van-safe-area-bottom"></div>
</template>

<script lang="ts" setup>
import BasicLayout from "./layouts/BasicLayout.vue";
import {computed} from 'vue';
import {useRoute} from "vue-router";

const route = useRoute();
const showBasicLayout = computed(() => {
  return !route.meta.noLayout;
});
</script>

<style scoped>
#app {
  padding: 0 0;
  margin: 0 auto;
}
</style>
