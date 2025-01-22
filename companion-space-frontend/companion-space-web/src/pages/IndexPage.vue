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
      <van-dropdown-menu>
        <van-dropdown-item v-model="value1" :options="option1" @change="handleDropdownChange"/>
      </van-dropdown-menu>
      <div v-if="value1 === 1 || value1 === 2">
        <div v-if="!matchUsers||matchUsers.length <=0" class="null">
          <van-empty image="search" description="暂无数据"/>
        </div>
        <div v-if="matchUsers.length >0 && matchUsers">
          <div v-for="user in matchUsers" id="card" class="card">
            <van-swipe-cell>
              <van-card
                  :desc="user.userDesc ?'简介：'+ user.userDesc:'简介：该用户比较懒,暂时没有设置'"
                  :thumb="user.userAvatarUrl ? user.userAvatarUrl :defaultPicture "
                  :title="user.username"
                  @click="showUser(user.id)"
              >
                <template #tags>
                  <div v-if="user.tags.length<7" style="margin-bottom: 12px"></div>
                  标签：<br>
                  <van-tag v-for="tag in user.tags" style="color: rgb(245, 67, 67)" class="tag" plain type="primary">
                    {{ tag }}
                  </van-tag>
                  <span v-if="!user.tags||user.tags.length<=0">该用户暂时没有设置</span>
                </template>
                <template #num>
                  <div v-if="value1 === 2" style="margin-top: 10px;">
                    距离您: {{ user.distance }} 米
                  </div>
                </template>
              </van-card>
            </van-swipe-cell>
            <div style="padding-top: 5px"></div>
          </div>
        </div>
      </div>
      <div v-else>
        <user-list/>
      </div>
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
import {onMounted, ref, watch} from "vue";
import UserList from "../components/UserList.vue";
import BlogList from "../components/BlogList.vue";
import request from "../service/myAxios";
import {useRoute, useRouter} from "vue-router";
import {defaultPicture, jsonParseTag} from "../common/userCommon";

const matchUsers = ref([]);
const searching = ref(false);
const listLoading = ref(false);
const active = ref(0);
const refreshLoading = ref(false);
const blogList = ref([]);
const blogListFinished = ref(false);
const blogCurrentPage = ref(0);
const blogPageSize = ref(10);
const blogSearch = ref("");
const offset = ref({x: 303, y: 500});
const router = useRouter();
const route = useRoute();
const images = ref([
  "https://img0.baidu.com/it/u=3358848204,1936258606&fm=253&fmt=auto&app=120&f=JPEG?w=1421&h=800",
  "https://img2.baidu.com/it/u=3012806272,1276873993&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500",
  "https://img0.baidu.com/it/u=741268616,1401664941&fm=253&fmt=auto&app=138&f=JPEG?w=748&h=500",
  "https://img1.baidu.com/it/u=2389614815,1145894179&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800",
]);

const value1 = ref(0);
const option1 = ref([
  {text: "全部", value: 0},
  {text: '标签匹配', value: 1},
  {text: '附近匹配', value: 2},
]);

onMounted(async () => {
  if (sessionStorage.getItem("tabIndex") === "1") {
    active.value = 1;
  }
  // 恢复 value1 的状态
  const savedValue1 = sessionStorage.getItem("selectedOption");
  if (savedValue1 !== null) {
    value1.value = parseInt(savedValue1, 10);
  }
});

const handleDropdownChange = async () => {
  if (value1.value === 1) {
    // 标签匹配
    await matchUser();
  } else if (value1.value === 2) {
    // 附近匹配
    sessionStorage.setItem("selectedOption", value1.value);
    router.push('/mapContainer');
  }
}

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

const matchUser = async () => {
  const res = await request.get("/user/match", {
    params: {
      num: 10,
    }
  });
  if (res) {
    matchUsers.value = res;
    jsonParseTag(res)
  }
}

const matchByGeo = async (longitude, latitude) => {
  const res = await request.get("/user/matchByGeo", {
    params: {
      num: 10,
      radius: 100000,
      longitude,
      latitude
    }
  });
  if (res) {
    matchUsers.value = res;
    jsonParseTag(res);
  }
};

// 监听路由参数的变化
watch(
    () => route.query,
    (newQuery) => {
      if (newQuery.longitude && newQuery.latitude) {
        matchByGeo(newQuery.longitude, newQuery.latitude);
      }
    },
    {immediate: true}
);

const showUser = (id) => {
  router.push({
    name: 'userShow',
    params: {
      userId: id
    }
  })
}

const getBlogList = async (currentPage) => {
  let res = await request.get("/blog/list", {
    params: {
      currentPage: currentPage,
      pageSize: blogPageSize.value,
      title: blogSearch.value,
    },
  });
  if (res) {
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
  /*padding-top: 7px;*/
}

.my-swipe {
  width: 100%;
  height: 200px;
}

</style>