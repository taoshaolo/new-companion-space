<template>
  <van-sticky>
    <van-nav-bar
        left-arrow
        title="所有评论"
        @click-left="onClickLeft"
    />

    <van-tabs v-model:active="active" @change="changeTab">
      <van-tab title="评论我的"/>
      <van-tab title="我评论的"/>
    </van-tabs>

  </van-sticky>
  <van-pull-refresh
      v-model="refreshLoading"
      success-text="刷新成功"
      @refresh="commentReload">
    <!--    todo 用户搜索-->
    <van-list
        v-model:loading="listLoading"
        :finished="listFinished"
        offset="0"
        @load="commentLoad"
        style="margin: 15px;height: 100%"
    >
      <template #finished>
        <span v-if="commentList && commentList.length!==0">没有更多了</span>
      </template>
      <template #loading>
        <van-loading vertical>
          <template #icon>
            <van-icon name="star-o" size="30"/>
          </template>
          加载中...
        </van-loading>
      </template>
      <user-comment-list :comment-list="commentList" @refresh="refreshComments"/>
    </van-list>
    <van-back-top right="20px" bottom="60px"/>
    <van-empty
        v-if="(!commentList || commentList.length===0) && listLoading===false &&active===1"
        image="https://fastly.jsdelivr.net/npm/@vant/assets/custom-empty-image.png"
        image-size="80"
        description="您还没有写过评论"
    />
    <van-empty
        v-if="(!commentList || commentList.length===0) && active===0"
        image="https://fastly.jsdelivr.net/npm/@vant/assets/custom-empty-image.png"
        image-size="80"
        description="您还没有被评论过"
    />
  </van-pull-refresh>
</template>

<script setup>
import {onMounted, ref} from "vue";
import request from "../service/myAxios";
import {useRouter} from "vue-router";
import UserCommentList from "../components/UserCommentList.vue";

const active = ref(0);
const loading = ref(true);
const commentList = ref([]);

const refreshLoading = ref(false);
const listLoading = ref(false);
const listFinished = ref(false);
const commentCurrentPage = ref(0);

let router = useRouter();

onMounted(async () => {
  let res = await request.get("/comments/list/commented");
  if (res) {
    commentList.value = res;
  }
  refreshLoading.value = false;
  listLoading.value = false;
});
const getComments = async (currentPage) => {
  let res = await request.get("/comments/list/my", {
    params: {
      currentPage: currentPage,
    },
  });
  if (res) {
    if (res.records.length > 0) {
      res.records.forEach(item => commentList.value.push(item));
    } else {
      listFinished.value = true;
    }
    listLoading.value = false;
  }
};
const commentLoad = async () => {
  if (active.value === 1) {
    commentCurrentPage.value++;
    await getComments(commentCurrentPage.value);
  } else {
    commentList.value = [];
    let res = await request.get("/comments/list/commented");
    if (res) {
      commentList.value = res;
    }
    listFinished.value = true;
  }
};
const commentReload = async () => {
  if (active.value === 0) {
    commentList.value = [];
    let res = await request.get("/comments/list/commented");
    if (res) {
      commentList.value = res;
    }
    refreshLoading.value = false;
    listLoading.value = false;
  } else {
    commentCurrentPage.value = 1;
    commentList.value = [];
    listFinished.value = false;
    await getComments(commentCurrentPage.value);
    refreshLoading.value = false;
    listLoading.value = false;
  }
};
const changeTab = async (index) => {
  await commentReload();
};

const refreshComments = async () => {
  await commentReload();
};

const onClickLeft = () => {
  router.back()
};
</script>

<style scoped>
:deep(.van-tab) {
  margin-left: 10px;
}
</style>
