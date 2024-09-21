<template>
  <el-table :data="tableData" style="width: 100%">
    <el-table-column fixed prop="id" label="id"/>
    <el-table-column prop="userId" label="作者id"/>
    <el-table-column prop="title" label="标题"/>
    <el-table-column prop="images" label="图片">
      <template #default="scope">
        <el-image  :src="scope.row.images ? scope.row.images : defaultPicture"
                  style="width: 60px; height: 60px" fit="fill"/>
      </template>
    </el-table-column>

    <el-table-column prop="content" label="文章"/>
    <el-table-column prop="likedNum" label="点赞数"/>
    <el-table-column prop="commentsNum" label="评论数"/>
    <el-table-column prop="createTime" label="创建时间">
      <template #default="scope">
        {{ moment(scope.row.createTime).format("YYYY-MM-DD HH:mm:ss") }}

      </template>
    </el-table-column>
    <el-table-column fixed="right" prop="operation" label="操作">
      <template #default>
        <el-button link type="primary" size="small" @click="handleClick">
          Detail
        </el-button>
        <el-button link type="primary" size="small">Edit</el-button>
        <el-button link type="primary" size="small">delete</el-button>
      </template>
    </el-table-column>
  </el-table>
    <el-pagination class="pagination-container"
        v-model:currentPage="currentPage"
        :total="total"
        @current-change="handlePageChange"
    />
</template>

<script setup>
import {ref, watchEffect} from "vue";
import request from "../plugins/request";
import moment from "moment";
import {defaultPicture, jsonParseTag} from "../common/userCommon";
import {Message} from "@element-plus/icons-vue";

const tableData = ref([]);
const total = ref(0);
const currentPage = ref(1)
const pageSize = ref(10)

const loadData = async () => {
  const res = await request.get("/blog/list",{
    params: {
      currentPage: currentPage.value,
      pageSize: pageSize.value,
    }
  });
  if (res.data.code === 0) {
    tableData.value = res.data.data.records || [];
    total.value = res.data.data?.total || 0;
  }else {
    Message.error("获取数据失败");
  }
}

const handleClick = () => {
  console.log('click')
}
const handlePageChange = (newPage) => {
  currentPage.value = newPage;
  loadData(); // 当页面改变时，重新获取数据
};

/**
 * 监听 searchParams 变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  loadData();
});
</script>

<style scoped>
.pagination-container {
  position: fixed;
  bottom: 0;
  width: 100%;
  background-color: #fff; /* 根据需要调整背景颜色 */
  padding: 10px 0;
  box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.1); /* 可选的阴影效果 */
}
</style>