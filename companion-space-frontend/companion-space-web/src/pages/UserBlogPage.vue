<template>
  <van-pull-refresh
      v-model="refreshLoading"
      success-text="刷新成功"
      @refresh="onRefresh"
  >
    <van-list
        style="margin: 15px"
        v-model:loading="listLoading"
        :finished="listFinished"
        offset="0"
        finished-text="没有更多了"
        @load="onLoad"
    >
      <template #loading>
        <van-skeleton>
          <template #template>
            <div style="display: flex;width: 100%;margin: 15px">
              <div :style="{ flex: 1, marginLeft: '16px' }">
                <van-skeleton-paragraph row-width="60%"/>
                <van-skeleton-paragraph row-width="60%"/>
              </div>
              <van-skeleton-image/>
            </div>
          </template>
        </van-skeleton>
      </template>
      <blog-list :blog-list="blogList"/>
    </van-list>
  </van-pull-refresh>
  <van-empty v-if="blogList?.length<1 && !listLoading" description="数据为空"/>
</template>

<script setup>
import {ref} from "vue";
import {useRouter} from "vue-router";
import BlogList from "../components/BlogList.vue";
import request from "../service/myAxios";
import {showFailToast} from "vant";

let router = useRouter();
const refreshLoading = ref(false)
const listLoading = ref(false)
const listFinished = ref(false)
const currentPage = ref(0)
const blogList = ref([])

const onLoad = () => {
  currentPage.value++
  listBlogs(currentPage.value)
}

async function listBlogs(currentPage) {
  listLoading.value = true
  const res = await request.get("/blog/list/my/blog?currentPage=" + currentPage)
  if (res) {
    if (res.records.length === 0) {
      listFinished.value = true
      return
    } else {
      res.records.forEach(blog => blogList.value.push(blog))
    }
  } else {
    showFailToast("博文加载失败，请稍后重试")
  }
  listLoading.value = false
}
const onRefresh = async () => {
  blogList.value = []
  listFinished.value = false
  currentPage.value = 1
  await listBlogs(currentPage.value)
  refreshLoading.value = false
}
</script>

<style scoped>

</style>
