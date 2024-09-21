<template>
  <van-nav-bar
      left-arrow
      title="点赞"
      @click-left="onClickLeft"
  />
    <van-tabs v-model:active="active" @change="onClickTab">
      <van-tab title="点赞我的">
        <user-like-list/>
      </van-tab>
      <van-tab title="我点赞的">
        <my-like-list/>
      </van-tab>
    </van-tabs>

</template>

<script setup>

import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import UserLikeList from "../components/UserLikeList.vue";
import MyLikeList from "../components/MyLikeList.vue";

const active = ref(0);
const router = useRouter();

onMounted(async () => {
  if (sessionStorage.getItem("tabIndex") === "1") {
    active.value = 1;
  }
});

const onClickLeft = () => {
  router.back();
};
const onClickTab = (index) => {
  sessionStorage.setItem("tabIndex", index);
};
</script>

<style scoped>
:deep(.van-tab) {
  margin-left: 10px;
}
</style>
