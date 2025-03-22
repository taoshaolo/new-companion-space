<template>
  <van-sticky>
    <van-nav-bar
        title="AI 助手"
        left-arrow
        @click-left="onClickLeft"
    >
    </van-nav-bar>
  </van-sticky>
  <div class="chat-container">
    <div class="content" ref="chatRoom" v-html="stats.content"></div>
    <van-cell-group inset style="position: fixed;bottom: 0;width: 100%">
      <van-field
          v-model="stats.text"
          center
          clearable
          placeholder="聊点什么吧...."
      >
        <template #button>
          <van-button size="small" type="primary" @click="send" style="margin-right: 16px">发送</van-button>
        </template>
      </van-field>
    </van-cell-group>
    <van-loading v-if="stats.loading" size="24px" vertical>加载中...</van-loading>
  </div>
</template>
<script setup>
import {nextTick, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {showFailToast} from "vant";

import getCurrent from "../service/currentUser";
import request from "../service/myAxios";

const defaultMessage = "你好,我是伙伴空间的智能 AI 助手,欢迎向我提问。";
const route = useRoute();
const router = useRouter();
const chatRoom = ref(null);
const onClickLeft = () => {
  router.back();
};
const stats = ref({
  user: {
    id: 0,
    username: "",
    userAvatarUrl: "",
  },
  ai: {
    username: "AI",
    avatarUrl: "https://p1.itc.cn/q_70/images03/20230908/8bb29620b4db40368ca362bd440b8412.png",
  },
  text: "",
  content: "",
  loading: false, // 添加 loading 状态
});
onMounted(async () => {
  stats.value.user = await getCurrent();
  createContent(stats.value.ai, null, defaultMessage);
});

const send = async () => {
  if (!stats.value.text.trim()) {
    showFailToast("请输入内容");
  } else {
    stats.value.loading = true; // 设置 loading 状态
    createContent(null, stats.value.user, stats.value.text);
    let res = await request.post("/chat/aiChat", {
      message: stats.value.text,
    });
    stats.value.text = "";
    if (res) {
      createContent(stats.value.ai, null, res);
    }
    stats.value.loading = false; // 清除 loading 状态
    await nextTick(() => {
      const lastElement = chatRoom.value.lastElementChild;
      lastElement.scrollIntoView();
    });
  }
};

/**
 * 这个方法是用来将 json的聊天消息数据转换成 html的。
 */
const createContent = (remoteUser, nowUser, text) => {
  // 当前用户消息
  let html;
  if (nowUser) {
    // nowUser 表示是否显示当前用户发送的聊天消息，绿色气泡
    html = `
    <div class="message self">
      <div class="myInfo">
        <span class="username">${nowUser.username}</span>
        <p class="text">${text}</p>
      </div>
    </div>
`;
  } else if (remoteUser) {
    // remoteUser表示远程用户聊天消息，灰色的气泡
    html = `
     <div class="message other">
      <img :alt="${remoteUser.username}" class="avatar" src="${remoteUser.avatarUrl}">
      <div class="info">
        <span class="username">${remoteUser.username.length < 10 ? remoteUser.username : remoteUser.username}&nbsp;&nbsp;&nbsp;</span>
        <p class="text" >${text}</p>
      </div>
    </div>
`;
  }
  stats.value.content += html;
};

</script>
<style>
.chat-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow-y: auto;
}

.message {
  display: flex;
  align-items: center;
  margin: 10px 10px;
}

.content {
  padding-top: 22px;
  padding-bottom: 57px;
  display: flex;
  flex-direction: column
}

.self {
  align-self: flex-end;
}

.avatar {
  align-self: flex-start;
  width: 35px;
  height: 35px;
  border-radius: 50%;
  margin-right: 10px;
  margin-left: 10px;
}

.username {
  align-self: flex-start;
  text-align: center;
  max-width: 200px;
  font-size: 12px;
  color: #999;
  padding-bottom: 1px;
  white-space: nowrap;
  overflow: visible;
  /*background-color: #fff;*/
}

.info {
  display: flex;
  flex-direction: column;
  order: 2;
}

.myInfo {
  text-align: end;
  align-self: flex-start;
}

.text {
  padding: 10px;
  border-radius: 10px;
  background-color: #eee;
  word-wrap: break-word;
  word-break: break-all;
}

.other .text {
  align-self: flex-start;
}

.self .text {
  background-color: #2bd277;
  color: #fff;
}
</style>
