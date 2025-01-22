<template>
  <van-search
      v-model="searchText"
      placeholder="请输入搜索关键词"
      show-action
      @search="onSearch"
  >
    <template #action>
      <div style="color: #1989fa" @click="onSearch">搜索</div>
    </template>
  </van-search>
  <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
  >
    <div v-if="!users||users.length <=0" class="null">
      <van-empty image="search" description="暂无数据"/>
    </div>
    <div v-if="users.length >0 && users">
      <div v-for="user in users" id="card" class="card">
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
          </van-card>
        </van-swipe-cell>
        <div style="padding-top: 5px"></div>
      </div>
    </div>
  </van-list>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";
import {showFailToast} from "vant";
import {useRoute, useRouter} from "vue-router";
import {defaultPicture, jsonParseTag} from "../common/userCommon";
import request from "../service/myAxios";
import qs from 'qs'
import {UserType} from "../model/user";

const route = useRoute()
const router = useRouter()
const users = ref<UserType[]>([])
const searchText = ref('');
const loading = ref(false);
const finished = ref(false);
const pageNum = ref(1);
const pageSize = ref(10);

const loginUser = ref({})
const {tags} = route.query

// 搜索用户
const onSearch = async () => {
  pageNum.value = 1;
  users.value = [];
  finished.value = false;
  await fetchUsers();
};

// 跳转到用户详情
const showUser = (id: number) => {
  router.push({
    name: 'userShow',
    params: {
      userId: id
    }
  })
}

// 获取用户列表
const fetchUsers = async () => {
  // 去除空格
  searchText.value = searchText.value.trim()
  loading.value = true;
  const res: { records: UserType[], total: number } = await request.get("/user/list/page", {
    params: {
      searchText: searchText.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  })
  if (res.records.length <= 0) {
    showFailToast("无搜索用户");
    finished.value = true;
  } else {
    users.value = users.value.concat(res.records);
    jsonParseTag(res.records);
  }
  loading.value = false;
  if (users.value.length >= res.total) {
    finished.value = true;
  }
}

// 加载更多
const onLoad = async () => {
  if (finished.value) return;
  pageNum.value++;
  await fetchUsers();
};

// 根据标签搜索用户
const fetchUsersByTags = async () => {
  const searchTagsList: UserType[] = await request.get(`/user/search/tags`, {
    params: {
      tagNameList: tags
    },
    paramsSerializer: {
      serialize: (params) => {
        return qs.stringify(params, { indices: false });
      }
    }
  });
  users.value = searchTagsList;
  jsonParseTag(searchTagsList);
  finished.value = true; // 标签搜索不需要分页
};

// 页面加载时获取用户列表
onMounted(async () => {
  if (tags) {
    await fetchUsersByTags();
  } else {
    await fetchUsers();
  }
  loginUser.value = sessionStorage.getItem("longUser") ? JSON.parse(sessionStorage.getItem("longUser")) : undefined;
});

</script>

<style scoped>
@import "../assets/css/userList.css";
@import "../assets/css/public.css";

:deep(.van-search__field) {
  flex: 1;
  align-items: center;
  padding: 0 var(--van-padding-xs) 0 0;
  height: var(--van-search-input-height);
  background-color: transparent;
}
</style>
