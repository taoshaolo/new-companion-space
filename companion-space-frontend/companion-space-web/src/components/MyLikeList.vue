<template>
  <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
  >
    <van-cell-group v-for="like in myLikeList" :border="false" style="margin: 15px;">
      <div v-if="like.type===0">
        <van-cell center :border="false"
                  :title="`${moment(like.createTime).format('YYYY-MM-DD HH:mm:ss')} 我赞了 ${like.toUser.username} 的博文`">
          <template #icon>
            <van-image
                round
                width="40"
                height="40"
                style="margin-right: 10px"
                :src="like.toUser.userAvatarUrl? like.toUser.userAvatarUrl :defaultPicture"
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
      </div>
      <div v-if="like.type===1">
        <van-cell center :border="false"
                  :title="`${moment(like.createTime).format('YYYY-MM-DD HH:mm:ss')} 我赞了 ${like.toUser.username} 的评论`"
        >
          <template #icon>
            <van-image
                round
                width="40"
                height="40"
                style="margin-right: 10px"
                :src="like.toUser.userAvatarUrl ? like.toUser.userAvatarUrl : defaultPicture"
            />
          </template>
        </van-cell>
        <div>
          <van-space style="background: #f3f2f5;width: 100%" @click="toBlog(like.comment?.blogId)">
            <span style="padding: 16px">{{ like.comment?.content }}</span>
          </van-space>
        </div>
      </div>
    </van-cell-group>
  </van-list>
  <van-back-top />
</template>

<script setup lang="ts">
import {defaultPicture} from "../common/userCommon";
import {onMounted, ref, watch, watchEffect} from "vue";
import {useRouter} from "vue-router";
import request from "../service/myAxios";
import {getCurrent} from "../service/currentUser";
import {MessageType} from "../model/like";
import moment from "moment";
import {showFailToast} from "vant/lib/toast/function-call";

const currentUser = ref({})
const router = useRouter();
const myLikeList = ref<MessageType[]>([])
const loading = ref(false);
const finished = ref(false);
const currentPage = ref(1);

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
    const res = await request.get("/message/like/my", {
      params: {
        currentPage: currentPage.value,
      }
    });
    if (res && res.records.length > 0){
        myLikeList.value = [...myLikeList.value, ...res.records];
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
