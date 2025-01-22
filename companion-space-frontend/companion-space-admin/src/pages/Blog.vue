<template>
  <el-table :data="tableData" style="width: 100%">
    <el-table-column fixed prop="id" label="id"/>
    <el-table-column prop="userId" label="作者id"/>
    <el-table-column prop="title" label="标题"/>
    <el-table-column prop="images" label="图片">
      <template #default="scope">
        <el-image :src="scope.row.images ? scope.row.images : defaultPicture"
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
  <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 30, 40]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
  />
</template>

<script setup>
import {ref, watchEffect} from "vue";
import request from "../plugins/request";
import moment from "moment";
import {defaultPicture} from "../common/userCommon";
import { ElMessage } from 'element-plus';

const tableData = ref([]);
const total = ref(0);
const currentPage = ref(1)
const pageSize = ref(10)

const loadData = async () => {
  const res = await request.get("/blog/list", {
    params: {
      currentPage: currentPage.value,
      pageSize: pageSize.value,
    }
  });
  if (res.data.code === 0) {
    tableData.value = res.data.data.records || [];
    total.value = res.data.data?.total || 0;
  } else {
    ElMessage.error("获取数据失败");
  }
}

const handleClick = () => {
  console.log('click')
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  loadData();
};

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage;
  loadData();
};


/**
 * 监听 searchParams 变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  loadData();
});
</script>

<style scoped>
</style>