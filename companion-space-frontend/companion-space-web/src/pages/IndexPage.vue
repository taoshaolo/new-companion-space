<template>
  <div class="div-my-swipe">
    <van-swipe class="my-swipe" :autoplay="3000" indicator-color="white">
      <van-swipe-item v-for="image in images" :key="image">
        <img :src="image" style="width: 100%;" alt="无"/>
      </van-swipe-item>
    </van-swipe>
  </div>
  <van-tabs v-model:active="active" @change="onClickTab">
    <van-tab title="👑 匹配用户">
      <user-list/>
    </van-tab>
    <van-tab title="📚 热门博文">
      <van-pull-refresh v-model="refreshLoading" success-text="刷新成功" @refresh="blogRefresh">
        <van-search v-model="blogSearch" show-action placeholder="请输入搜索关键词" @search="searchBlog">
          <template #action>
            <div style="color: #1989fa" @click="onClickButton">搜索</div>
          </template>
        </van-search>
        <van-list v-model:loading="listLoading" :finished="blogListFinished" offset="0" @load="blogLoad"
                  style="margin: 15px">
          <template #finished>
            <span v-if="!searching">没有更多了</span>
          </template>
          <blog-list :blog-list="blogList"/>
        </van-list>
        <van-back-top right="20px" bottom="60px"/>
        <van-empty v-if="(!blogList ||　blogList.length===0) && !listLoading &&!searching" image="search"
                   description="暂无博文"/>
        <van-floating-bubble
            v-if="active === 1"
            v-model:offset="offset"
            axis="xy"
            icon="plus"
            magnetic="x"
            @click="toAddBlog"
        />
      </van-pull-refresh>
    </van-tab>
  </van-tabs>
</template>

<script setup>
import {onMounted, ref} from "vue";
import UserList from "../components/UserList.vue";
import BlogList from "../components/BlogList.vue";
import request from "../service/myAxios";
import {useRouter} from "vue-router";

const searching = ref(false);
const listLoading = ref(false);
const active = ref(0);
const refreshLoading = ref(false);
const blogList = ref([]);
const blogListFinished = ref(false);
const blogCurrentPage = ref(0);
const blogSearch = ref("");
const offset = ref({x: 303, y: 500});
const router = useRouter();
const images = ref([
  "https://img0.baidu.com/it/u=3358848204,1936258606&fm=253&fmt=auto&app=120&f=JPEG?w=1421&h=800",
  "https://img2.baidu.com/it/u=3012806272,1276873993&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500",
  "https://img0.baidu.com/it/u=741268616,1401664941&fm=253&fmt=auto&app=138&f=JPEG?w=748&h=500",
  "https://img1.baidu.com/it/u=2389614815,1145894179&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800",
]);

onMounted(async () => {
  if (sessionStorage.getItem("tabIndex") === "1") {
    active.value = 1;
  }
});

// 跳转到创建博客页面
const toAddBlog = () => {
  router.push('/blog/edit')
}


const onClickTab = (index) => {
  sessionStorage.setItem("tabIndex", index);
};

const blogLoad = async () => {
  blogCurrentPage.value++;
  await getBlogList(blogCurrentPage.value);
};

const getBlogList = async (currentPage) => {
  let res = await request.get("/blog/list", {
    params: {
      currentPage: currentPage,
      title: blogSearch.value,
    },
  });
  if (res) {
    console.log(res)
    if (res.records.length > 0) {
      res.records.forEach(item => blogList.value.push(item));
    } else {
      blogListFinished.value = true;
    }
    listLoading.value = false;
  }
};

const blogRefresh = async () => {
  blogCurrentPage.value = 1;
  blogList.value = [];
  blogListFinished.value = false;
  await getBlogList(blogCurrentPage.value);
  refreshLoading.value = false;
  listLoading.value = false;
};
const searchBlog = async () => {
  searching.value = true;
  blogList.value = [];
  blogCurrentPage.value = 1;
  await getBlogList(blogCurrentPage.value);
  searching.value = false;
};
const onClickButton = async () => {
  searching.value = true;
  blogList.value = [];
  blogCurrentPage.value = 1;
  await getBlogList(blogCurrentPage.value);
  searching.value = false;
};

</script>

<style scoped>
.div-my-swipe {
  background-color: #eef0f3;
  padding-top: 7px;
}

.my-swipe {
  width: 96%;
  height: 200px;
  margin: 0 auto;
  border-radius: 10px;
}

</style>