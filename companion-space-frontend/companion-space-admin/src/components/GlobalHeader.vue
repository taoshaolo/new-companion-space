<template>
  <el-menu
      flex="auto"
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="horizontal"
      @select="handleSelect"
  >
    <div class="titleBar">
      <img class="logo" src="../../public/favicon.ico"/>
      <div class="title">伙伴空间后台</div>
    </div>
    <template v-for="route in routes" :key="route.path">
      <el-menu-item v-if="!route.children && !route.meta?.hideInMenu" :index="route.path">
        {{ route.name }}
      </el-menu-item>
      <el-sub-menu v-if="route.children && !route.meta?.hideInMenu" :index="route.path">
        <template #title>{{ route.name }}</template>
        <el-menu-item
            v-for="child in route.children"
            :key="child.path"
            :index="child.path"
        >
          {{ child.name }}
        </el-menu-item>
      </el-sub-menu>
      <el-sub-menu class="right-align-menu">
        <template #title>
          <el-avatar :src="userAvatarUrl"></el-avatar>
          <span class="user-name">{{ username }}</span>
        </template>
        <el-menu-item @click="logout">退出系统</el-menu-item>
      </el-sub-menu>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {useRouter} from "vue-router";
import routes from "../plugins/routes";
import request from "../plugins/request";
import {ElMessage, ElMessageBox} from "element-plus";

const router = useRouter();

const userAvatarUrl = ref("");
const username = ref("");
const activeIndex = ref(["/home"]);
router.afterEach((to, from, failure) => {
  activeIndex.value = [to.path];
});
// 将嵌套路由平铺为一维数组，便于 v-for 遍历
const handleSelect = (index) => {
  router.push({
    path: index,
  });
}
onMounted(async () => {
  const res = await request.get("/user/current");
  console.log(res)
  if (res.data.code === 0) {
    userAvatarUrl.value = res.data.data.userAvatarUrl;
    username.value = res.data.data.username;
  }
})

const logout = async () => {
  await ElMessageBox.confirm(
      "确认退出",
      {
        confirmButtonText: "OK",
        cancelButtonText: "取消",
        type: "warning",
      },
  )
  const res = await request.post("/user/loginOut");
  if (res.data.code === 0) {
    ElMessage({
      message: "退出成功",
      type: "success",
    });
    router.push("/login");
  }
}

</script>

<style scoped>
.titleBar {
  display: flex;
  align-items: center;
}

.title {
  margin-left: 16px;
  color: black;
}

.logo {
  height: 48px;
}

.right-align-menu {
  position: absolute;
  right: 0;
  height: 100%;
}
</style>