<template>
  <div class="app-layout">
    <van-sticky>
      <van-nav-bar
          :title="chatName"
          left-arrow
          @click-left="onClickLeft"
          @click-right="showPopover = true"
      >
        <template #right>
          <van-popover placement="bottom-end" v-model:show="showPopover" :actions="actions" @select="onSelect">
            <template #reference>
              <van-icon name="more-o" size="18" />
            </template>
          </van-popover>
        </template>
      </van-nav-bar>
    </van-sticky>
    <div class="main-content">
      <div class="chat-container">
        <div class="message-list" ref="messaggListRef">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="
              message.isUser ? 'message user-message' : 'message bot-message'
            "
          >
            <!-- 会话图标 -->
            <i
              :class="
                message.isUser
                  ? 'fa-solid fa-user message-icon'
                  : 'fa-solid fa-robot message-icon'
              "
            ></i>
            <!-- 会话内容 -->
            <span>
              <span v-html="message.content"></span>
              <!-- loading -->
              <span
                class="loading-dots"
                v-if="message.isThinking || message.isTyping"
              >
                <span class="dot"></span>
                <span class="dot"></span>
              </span>
            </span>
          </div>
        </div>
        <div class="input-container">
          <van-field
            v-model="inputMessage"
            placeholder="请输入消息"
          >
            <template #button>
              <van-button size="small" @click="sendMessage" :disabled="isSending" type="primary">发送</van-button>
            </template>
          </van-field>
        </div>
      </div>
    </div>
    <van-popup
        v-model:show="showPopup"
        position="right"
        :style="{ width: '50%', height: '100%' }"
    >
      <div
          v-for="(action, index) in chatMemories"
          :key="index"
          @click="selectChatMemory(action)"
          class="chat-memory-item"
      >
        {{ action.name }} | {{ moment(action.updateTime).format("YYYY-MM-DD") }}
      </div>
    </van-popup>
    <van-popup v-model:show="showNewChatPopup" >
        <div style="padding: 20px;">
          <van-field v-model="newChatName" placeholder="请输入会话名称" />
          <div style="margin-top: 20px; text-align: right;">
            <van-button size="small" @click="cancelNewChat">取消</van-button>
            <van-button size="small" type="primary" @click="createNewChat">确定</van-button>
          </div>
        </div>
    </van-popup>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import request from "../service/myAxios";
import {useRouter} from "vue-router";
import moment from "moment";

const messaggListRef = ref()
const isSending = ref(false)
const inputMessage = ref('')
const messages = ref([])
const showPopover = ref(false);
const showPopup = ref(false);
const chatMemories = ref([]);
const router = useRouter();
const showNewChatPopup = ref(false);
const newChatName = ref('');
const chatName = ref('');
const currentMemoryId = ref('');

const actions = [
  {
    text: '新会话'
  },
  {
    text: '其他对话'
  }
];
const onClickLeft = () => {
  router.back();
};
const onClickRight = () => {
  showPopover.value = true
};

const onSelect = (action) => {
  if (action.text === '新会话') {
    showNewChatPopup.value = true;
  } else if (action.text === '其他对话') {
    // 处理选择其他对话逻辑
    getChatMemories();
  }
  showPopover.value = false; // 选择后隐藏 popover
};
onMounted(() => {
  // 移除 setInterval，改用手动滚动
  watch(messages, () => scrollToBottom(), { deep: true })
  // 页面加载时获取最新对话历史记录
  getChatMemories();
})

const scrollToBottom = () => {
  if (messaggListRef.value) {
    messaggListRef.value.scrollTop = messaggListRef.value.scrollHeight
  }
}

const sendMessage = () => {
  if (inputMessage.value.trim()) {
    sendRequest(inputMessage.value.trim())
    inputMessage.value = ''
  }
}

const sendRequest = (message) => {
  if (!currentMemoryId.value) {
    console.error('未选择会话，请先选择或创建一个会话。');
    return;
  }
  isSending.value = true
  const userMsg = {
    isUser: true,
    content: message,
    isTyping: false,
    isThinking: false,
  }
  messages.value.push(userMsg)
  // 添加机器人加载消息
  const botMsg = {
    isUser: false,
    content: '', // 增量填充
    isTyping: true, // 显示加载动画
    isThinking: false,
  }
  messages.value.push(botMsg)
  const lastMsg = messages.value[messages.value.length - 1]
  scrollToBottom()
  request
    .post(
      '/xiaozhi/stream',
      { memoryId: currentMemoryId.value, message },
      {
        responseType: 'stream', // 必须为合法值 "text"
        onDownloadProgress: (e) => {
          const fullText = e.event.target.responseText // 累积的完整文本
          let newText = fullText.substring(lastMsg.content.length)
          lastMsg.content += newText //增量更新
          // console.log(lastMsg)
          scrollToBottom() // 实时滚动
        },
      }
    )
    .then(() => {
      // 流结束后隐藏加载动画
      messages.value.at(-1).isTyping = false
      isSending.value = false
    })
    .catch((error) => {
      console.error('流式错误:', error)
      messages.value.at(-1).content = '请求失败，请重试'
      messages.value.at(-1).isTyping = false
      isSending.value = false
    })
}

// 转换特殊字符
const convertStreamOutput = (output) => {
  return output
    .replace(/\n/g, '<br>')
    .replace(/\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;')
    .replace(/&/g, '&amp;') // 新增转义，避免 HTML 注入
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}

const cancelNewChat = () => {
  showNewChatPopup.value = false;
  newChatName.value = '';
};

const createNewChat  = () => {
  if (newChatName.value) {
    request.post('/chatMemory', {
      name: newChatName.value
    }).then((response) => {
      // console.log('创建新会话成功:', response)
      // currentMemoryId.value = response.memoryId;
      // 调用 selectChatMemory 加载新会话
      selectChatMemory({ value: response });
      showNewChatPopup.value = false;
      newChatName.value = '';
    }).catch((error) => {
      console.error('创建新会话失败:', error);
    });
  }
}
const getChatMemories = () => {
  request.get('/chatMemory').then((response) => {
    // console.log('获取会话列表成功:', response)
    chatMemories.value = response.map((memory) => ({
      name: memory.name,
      value: memory.memoryId,
      updateTime: memory.updateTime
    }));
    showPopup.value = true;


  }).catch((error) => {
    console.error('获取会话列表失败:', error)
  })
}

const selectChatMemory = (action) => {
  currentMemoryId.value = action.value;
  request.get(`/chatMemory/${action.value}`).then((response) => {
    // console.log('选择会话成功:', response.messages)
    // 这里需要根据后端返回的 ChatMemory 对象更新 messages
    // console.log(response)
    messages.value = [];
    chatName.value = response.name;

    // 解析历史消息
    if (response.messages !== null){
      const historyMessages = JSON.parse(response.messages);
      historyMessages.forEach((message) => {
        if (message.type === 'USER') {
          const userContent = message.contents ? message.contents.map(item => item.text).join('') : '';
          messages.value.push({
            isUser: true,
            content: convertStreamOutput(userContent),
            isTyping: false,
            isThinking: false
          });
        } else if (message.type === 'AI') {
          messages.value.push({
            isUser: false,
            content: convertStreamOutput(message.text),
            isTyping: false,
            isThinking: false
          });
        }
      });
    }

    showPopup.value = false;
    scrollToBottom();
  }).catch((error) => {
    console.error('选择会话失败:', error)
  })
}

</script>

<style scoped>

/* 新增会话列表项样式 */
.chat-memory-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #e0e0e0;
  transition: background-color 0.2s ease;
}

.chat-memory-item:hover {
  background-color: #f0f0f0;
}
.app-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 200px;
  background-color: #f4f4f9;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  margin-top: 10px;
}

.new-chat-button {
  width: 100%;
  margin-top: 20px;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background-color: #fff;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
}

.message {
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 4px;
  display: flex;
  /* align-items: center; */
}

.user-message {
  max-width: 70%;
  background-color: #e1f5fe;
  align-self: flex-end;
  flex-direction: row-reverse;
}

.bot-message {
  max-width: 100%;
  background-color: #f1f8e9;
  align-self: flex-start;
}

.message-icon {
  margin: 0 10px;
  font-size: 1.2em;
}

.loading-dots {
  padding-left: 5px;
}

.dot {
  display: inline-block;
  margin-left: 5px;
  width: 8px;
  height: 8px;
  background-color: #000000;
  border-radius: 50%;
  animation: pulse 1.2s infinite ease-in-out both;
}

.dot:nth-child(2) {
  animation-delay: -0.6s;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(0.6);
    opacity: 0.4;
  }

  50% {
    transform: scale(1);
    opacity: 1;
  }
}
.input-container {
  display: flex;
}

.input-container .el-input {
  flex: 1;
  margin-right: 10px;
}

/* 媒体查询，当设备宽度小于等于 768px 时应用以下样式 */
@media (max-width: 768px) {
  .main-content {
    padding: 10px 0 10px 0;
  }
  .app-layout {
    flex-direction: column;
  }

  .sidebar {
    /* display: none; */
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    padding: 10px;
  }

  .logo-section {
    flex-direction: row;
    align-items: center;
  }

  .logo-text {
    font-size: 20px;
  }

  .logo-section img {
    width: 40px;
    height: 40px;
  }

  .new-chat-button {
    margin-right: 30px;
    width: auto;
    margin-top: 5px;
  }
}

/* 媒体查询，当设备宽度大于 768px 时应用原来的样式 */
@media (min-width: 769px) {
  .main-content {
    padding: 0 0 10px 10px;
  }

  .app-layout {
    display: flex;
    height: 100vh;
  }

  .sidebar {
    width: 200px;
    background-color: #f4f4f9;
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .logo-section {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .logo-text {
    font-size: 18px;
    font-weight: bold;
    margin-top: 10px;
  }

  .new-chat-button {
    width: 100%;
    margin-top: 20px;
  }
}
</style>
