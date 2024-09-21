<template>
  <van-sticky>
    <van-nav-bar
        title="正文"
        left-arrow
        @click-left="onClickLeft"
        @click-right="showBottom=true"
    >
      <template #right>
        <van-icon name="ellipsis"/>
      </template>
    </van-nav-bar>
  </van-sticky>
  <div v-if="images.length>0">
    <div style="width: 100%;"/>
    <van-swipe :autoplay="3000" lazy-render style="height: 200px">
      <van-swipe-item v-for="image in images" :key="image">
        <img :src="image" style="height: 200px;width: 100%" alt=""/>
      </van-swipe-item>
    </van-swipe>
    <!--    <div style="width: 100%;background-color: black"/>-->
  </div>
  <van-cell-group inset>
    <van-cell :title="blog.title" title-style="font-size:18px; font-weight: bold;"/>
    <van-cell center style="padding-right: 6px">
      <template #title>
        <span @click="toAuthor(author.id)">{{ author.username }}</span>
      </template>
      <template #icon>
        <van-image :src="author.userAvatarUrl ? author.userAvatarUrl :defaultPicture " width="40" height="40" round
                   style="margin-right: 10px"
                   @click="toAuthor(author.id)"/>
      </template>
    </van-cell>
    <van-cell :title="blog.content"/>
  </van-cell-group>

  <van-divider/>
  <van-cell-group inset>
    <!--        todo 排序-->
    <van-cell :title="`评论 ${blog.commentsNum}`"/>
  </van-cell-group>
  <div class="line"></div>

  <div style="padding-bottom: 80px">
    <comment-list :comment-list="commentList" @refresh="refresh"/>
  </div>


  <van-cell-group :border="false">
    <van-field v-model="comment" :autosize="{minHeight: 32}" type="textarea" rows="1" placeholder="评论"
               :border="false"
               @keyup.enter="addComment"
               style="position: fixed;bottom: 0;padding-left: 16px;border-top: 1px solid #C1C1C1;padding-right: 10px">
      <template #right-icon>
        <van-icon name="upgrade" size="30" color="#4387f6" @click="addComment"/>
      </template>
      <template #button>
        <van-icon name="envelop-o" size="15" style="margin-right: 5px">
          <span style="margin-left: 2px">{{ blog.commentsNum }}</span>
        </van-icon>
        <van-icon name="good-job-o" v-if="!blog.isLike" size="15" @click="likeBlog(blog)">
          <span style="margin-left: 2px">{{ blog.likedNum }}</span>
        </van-icon>
        <van-icon name="good-job-o" v-else color="red" size="15" @click="likeBlog(blog)"
                  style="margin-right: 2px">
          <span style="margin-left: 2px">{{ blog.likedNum }}</span>
        </van-icon>
      </template>
    </van-field>
  </van-cell-group>
  <van-popup
      v-if="blog.userId===currentUser.id || currentUser.userRole===1"
      v-model:show="showBottom"
      round
      position="bottom"
  >
    <van-grid :border="false">
      <van-grid-item @click="copyUrl">
        <template #icon>
          <van-icon name="link-o"/>
        </template>
        <template #text>
          <span class="grid-font">复制链接</span>
        </template>
      </van-grid-item>
      <van-grid-item @click="updateBlog">
        <template #icon>
          <van-icon name="records-o"/>
        </template>
        <template #text>
          <span class="grid-font">编辑</span>
        </template>
      </van-grid-item>
      <van-grid-item @click="deleteBlog">
        <template #icon>
          <van-icon name="delete-o"/>
        </template>
        <template #text>
          <span class="grid-font">删除</span>
        </template>
      </van-grid-item>
      <van-grid-item @click="report">
        <template #icon>
          <van-icon name="warn-o"/>
        </template>
        <template #text>
          <span class="grid-font">举报</span>
        </template>
      </van-grid-item>
    </van-grid>
  </van-popup>

  <van-popup
      v-else
      v-model:show="showBottom"
      round
      position="bottom"
  >
    <van-grid :border="false" :column-num="2">
      <van-grid-item @click="copyUrl">
        <template #icon>
          <van-icon name="share-o"/>
        </template>
        <template #text>
          <span class="grid-font">复制链接</span>
        </template>
      </van-grid-item>
      <van-grid-item @click="report">
        <template #icon>
          <van-icon name="warn-o"/>
        </template>
        <template #text>
          <span class="grid-font">举报</span>
        </template>
      </van-grid-item>
    </van-grid>
  </van-popup>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import request from "../service/myAxios";
import CommentList from "../components/CommentList.vue";
import {showConfirmDialog, showFailToast, showSuccessToast} from "vant";
import {getCurrent} from "../service/currentUser.ts";
import {defaultPicture} from "../common/userCommon";

const showBottom = ref(false)
let router = useRouter();
const comment = ref("")
const toAuthor = (id) => {
  router.push({
    name: "userShow",
    params: {
      userId: id
    }
  })
}
const onClickLeft = () => {
  router.back()
};
const images = ref([])
let route = useRoute();
const blog = ref({});
const author = ref({})
const commentList = ref([])
const currentUser = ref({})
const listComments = async () => {
  let id = route.query.id;
  let commentRes = await request.get("/comments?blogId=" + id);
  if (commentRes) {
    commentList.value = commentRes
  }
}
onMounted(async () => {
  currentUser.value = await getCurrent();
  let id = route.query.id;
  let res = await request.get("/blog/" + id);
  if (res) {
    console.log(res)
    blog.value = res
    author.value = res.author
    if (res.images) {
      let imgStrs = res.images.split(",");
      imgStrs.forEach((imgstr) => {
        images.value.push(imgstr)
      })
    }
    await listComments()
  } else {
    showFailToast("加载失败")
  }

})
const likeBlog = async (blog) => {
  let res = await request.put("/blog/like/" + blog.id);
  console.log(res)
  if (res) {
    let res_ = await request.get("/blog/" + blog.id);
    if (res_) {
      blog.likedNum = res_.likedNum
      blog.isLike = res_.isLike
    }
  }
}
const addComment = async () => {
  if (comment.value === "") {
    showFailToast("请输入评论内容")
  } else {
    let res = await request.post("/comments/add", {
      blogId: blog.value.id,
      content: comment.value
    });
    if (res) {
      showSuccessToast("添加成功")
    } else {
      showFailToast("添加失败")
    }
    await listComments()
    comment.value = ""

    let id = route.query.id;
    let newBlogRes = await request.get("/blog/" + id);
    if (newBlogRes) {
      console.log(newBlogRes)
      blog.value.commentsNum = newBlogRes.commentsNum
    }
  }
}
const copyUrl = async () => {
  try {
    await navigator.clipboard.writeText(document.location.href);
    showSuccessToast({
      message: "已复制到剪切板",
      className: "copyToast"
    });
  } catch (err) {
    console.error('Failed to copy URL: ', err);
  }
  showBottom.value = false;
}
// const copyUrl = () => {
//   var textArea = document.createElement('textarea')
//   document.body.appendChild(textArea)
//   textArea.readOnly = 'readonly'
//   textArea.style.opacity = '0'
//   textArea.value = document.location.href
//   textArea.select()
//   if (textArea.setSelectionRange)
//     textArea.setSelectionRange(0, textArea.value.length)
//   else
//     textArea.select()
//   document.execCommand("copy")
//   document.body.removeChild(textArea)
//   //指定toast样式，使文字在一行显示
//   showSuccessToast({
//     message: "已复制到剪切板",
//     className: "copyToast"
//   })
//   showBottom.value = false
// }
const report = () => {
  showFailToast("举报成功")
  showBottom.value = false
}
const deleteBlog = async () => {
  showConfirmDialog({
    title: '确定要删除博文吗',
    message:
        '此操作无法撤回',
  })
      .then(async () => {
        let res = await request.delete("/blog/" + blog.value.id);
        if (res) {
          await router.replace("/")
          showSuccessToast("删除成功")
        } else {
          showFailToast("删除失败")
        }
      })
      .catch(() => {
        showBottom.value = false
      });
}
const updateBlog = () => {
  router.push({
    path: "/blog/edit",
    query: {
      id: blog.value.id,
      images: images.value,
      title: blog.value.title,
      content: blog.value.content
    }
  })
}
const refresh = () => {
  location.reload();
}
</script>

<style>
:deep(.van-field__value) {
  margin-right: 15px;
}

.line {
  width: 100%;
  height: 1px;
  background: #ededed;
  position: relative;
}

.line::after {
  position: absolute;
  top: 0;
  left: 0;
  content: " ";
  display: block;
  width: 10%;
  height: 100%;
  background-color: #39a9ed;
}

.grid-font {
  font-size: 13px;
  color: #949494;
  margin-top: 5px
}

.copyToast {
  width: 100px;
}
</style>
