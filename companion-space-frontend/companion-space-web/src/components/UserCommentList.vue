<template>
  <van-cell-group v-for="comment in props.commentList" :border="false">
    <van-cell center :border="false" :title="comment.commentUser.username"
              :label="`${moment(comment.createTime).format('YYYY-MM-DD HH:mm:ss')} 评论帖子`">
      <template #icon>
        <van-image
            round
            width="40"
            height="40"
            style="margin-right: 10px"
            :src="comment.commentUser.userAvatarUrl ?? defaultPicture"
        />
      </template>
      <template #value>
        <van-icon v-if="!comment.isLiked" name="good-job-o" size="15" @click="likeComment(comment)">
          {{ comment.likedNum }}
        </van-icon>
        <van-icon v-else name="good-job-o" color="red" size="15" @click="likeComment(comment)">
          {{ comment.likedNum }}
        </van-icon>
        <van-icon v-if="String(currentUser.id)===comment.commentUser.id || currentUser.role===1" name="delete-o"
                  size="15" style="margin-left: 10px" @click="deleteComment(comment.id)"/>
      </template>
    </van-cell>

    <div style="display: block;width: 80%;word-wrap: break-word;margin-left: 10px">
      {{ comment.content }}
    </div>
    <div>
      <van-space style="background: #f3f2f5;width: 100%;margin: 0" align="center"
                 @click="toBlog(comment.blog.id)">
        <van-image v-if="comment.blog.coverImage" :src="comment.blog.coverImage" width="50" height="50"
                   style="margin-left: 10px"/>
        <van-cell :title="comment.blog.author.username" :label="comment.blog.title"
                  style="background: #f3f2f5;margin: 0">
        </van-cell>
      </van-space>
    </div>
    <div style="height: 2px;background-color: #706363;width: 100%;"/>
  </van-cell-group>

</template>

<script setup lang="ts">
import {defaultPicture} from "../common/userCommon";
import {CommentType} from "../model/comment";
import request from "../service/myAxios";
import {onMounted, ref} from "vue";
import {getCurrent} from "../service/currentUser";
import moment from "moment";
import {showConfirmDialog, showFailToast} from "vant";
import {useRouter} from "vue-router";

let emits = defineEmits(['refresh']);

interface BlogCommentsProps {
  commentList: CommentType[]
}

const currentUser = ref()
let props = defineProps<BlogCommentsProps>();

onMounted(async () => {
  currentUser.value = await getCurrent()
})
const likeComment = async (comment:any) => {
  let res = await request.put("/comments/like/" + comment.id);
  if (res) {
    let res_ = await request.get("/comments/" + comment.id);
    if (res_) {
      comment.likedNum = res_.likedNum
      comment.isLiked = res_.isLiked
    }
  }
}
const deleteComment = async (id:number) => {
  showConfirmDialog({
    title: '确定要删除评论吗',
    message:
        '此操作无法撤回',
  })
      .then(async () => {
        let res = await request.delete("/comments/" + id);
        if (res) {
          emits("refresh")
        } else {
          showFailToast("删除失败")
        }
      })
      .catch(() => {
      });
}
let router = useRouter();
const toBlog = (blogId:number) => {
  router.push({
    path: "/blog",
    query: {
      id: blogId
    }
  })
}
</script>

<style scoped>

</style>
