<template>
  <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
  >
  <van-cell-group v-for="like in userLikeList" :border="false"  style="margin: 15px;">
    <div v-if="like.type===0">
      <van-cell center :border="true" :title="like.fromUser.username"
                :label="`${moment(like.createTime).format('YYYY-MM-DD HH:mm:ss')} 赞了我的博文`">
        <template #icon>
          <van-image
              round
              width="40"
              height="40"
              style="margin-right: 10px"
              :src="like.fromUser.userAvatarUrl? like.fromUser.userAvatarUrl :defaultPicture"
          />
        </template>
      </van-cell>
      <div>
        <van-space style="background: #f3f2f5;width: 100%;margin: 0" align="center"
                   @click="toBlog(like.blog.id)">
          <van-image v-if="like.blog.coverImage" :src="like.blog.coverImage" width="50" height="50"
                     style="margin-left: 10px"/>
          <van-cell :title="like.blog.author.username" :label="like.blog.title"
                    style="background: #f3f2f5;margin: 0">
          </van-cell>
        </van-space>
      </div>
      <div style="height: 2px;background-color: #706363;width: 100%;"/>

    </div>
    <div v-if="like.type===1">
      <van-cell center :border="false" :title="like.fromUser.username"
                :label="`${moment(like.createTime).format('YYYY-MM-DD HH:mm:ss')} 赞了我的评论`">
        <template #icon>
          <van-image
              round
              width="40"
              height="40"
              style="margin-right: 10px"
              :src="like.fromUser.userAvatarUrl ? like.fromUser.userAvatarUrl : defaultPicture"
          />
        </template>
      </van-cell>
      <div>
        <van-space style="background: #f3f2f5;width: 100%" @click="toBlog(like.comment?.blogId)">
          <span style="padding: 16px">{{ like.comment?.content }}</span>
        </van-space>
      </div>
      <div style="height: 2px;background-color: #706363;width: 100%;"/>

    </div>
  </van-cell-group>
  </van-list>
  <van-back-top />
</template>

<script setup lang="ts">
import {defaultPicture} from "../common/userCommon";
import {onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import request from "../service/myAxios";
import {getCurrent} from "../service/currentUser";
import {MessageType} from "../model/like";
import moment from "moment";
import {showFailToast} from "vant/lib/toast/function-call";


const currentUser = ref({})
const router = useRouter();
const currentPage = ref(1)
const userLikeList = ref<MessageType[]>([])
const loading = ref(false);
const finished = ref(false);

onMounted(async () => {
  currentUser.value = await getCurrent()
})
const toBlog = (blogId: number) => {
  router.push({
    path: "/blog",
    query: {
      id: blogId
    }
  })

}

const onLoad = async () => {
  loading.value = true;
  try {
    const res = await request.get("/message/like", {
      params: {
        currentPage: currentPage.value,
      }
    });
    if (res && res.records.length > 0){
      userLikeList.value = [...userLikeList.value, ...res.records];
      if (res.records.length < 8) {
        finished.value = true;
      }else {
        currentPage.value++; // 更新当前页码
      }
    }else {
      finished.value = true;
    }
  } catch (e) {
    showFailToast("获取点赞列表失败")
  }finally {
    loading.value = false;
  }
};

</script>

<style scoped>

</style>
