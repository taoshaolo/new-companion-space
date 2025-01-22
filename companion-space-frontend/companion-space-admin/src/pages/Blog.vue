<template>
  <div style="margin-bottom: 20px; display: flex; justify-content: center;">
    <el-input v-model="searchParams.title" placeholder="请输入标题"
              style="width: 200px; margin-right: 10px;"></el-input>
    <el-button type="primary" @click="handleSearch">搜索</el-button>
    <el-button type="default" @click="resetSearch" style="margin-left: 10px;">重置</el-button>
  </div>

  <el-table :data="tableData" style="width: 100%">
    <el-table-column fixed prop="id" label="id"/>
    <el-table-column prop="userId" label="作者id"/>
    <el-table-column prop="title" label="标题"/>
    <el-table-column prop="coverImage" label="封面图片">
      <template #default="scope">
        <el-image :src="scope.row.coverImage ? scope.row.coverImage : defaultPicture"
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
      <template #default="scope">
        <el-popconfirm
            confirm-button-text="确认"
            cancel-button-text="取消"
            icon-color="#6200ea"
            title="确定要删除吗？"
            @confirm="handleDelete(scope.row.id)"
        >
          <template #reference>
            <el-button link type="primary" size="small">删除</el-button>
          </template>
        </el-popconfirm>
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
import {onMounted, ref} from "vue";
import request from "../plugins/request";
import moment from "moment";
import {defaultPicture} from "../common/userCommon";
import {ElMessage} from 'element-plus';

const tableData = ref([]);
const total = ref(0);
const currentPage = ref(1)
const pageSize = ref(10)
const searchParams = ref({
  title: '',
});

const loadData = async () => {
  const res = await request.get("/blog/list", {
    params: {
      currentPage: currentPage.value,
      pageSize: pageSize.value,
      ...searchParams.value
    }
  });
  if (res.data.code === 0) {
    tableData.value = res.data.data.records || [];
    total.value = res.data.data?.total || 0;
  } else {
    ElMessage.error("获取数据失败");
  }
}

const handleSearch = () => {
  currentPage.value = 1; // 重置当前页为第一页
  loadData();
}
const resetSearch = () => {
  searchParams.value = {
    title: '',
  };
  currentPage.value = 1; // 重置当前页为第一页
  loadData();
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  loadData();
};

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage;
  loadData();
};

const handleDelete = async (id) => {
  try {
    const res = await request.delete(`/blog/${id}`);
    if (res.data.code === 0) {
      ElMessage.success("删除成功");
      await loadData();
    } else {
      ElMessage.error("删除失败");
    }
  } catch (error) {
    ElMessage.error("请求失败");
  }
}

/**
 * 监听 searchParams 变量，改变时触发数据的重新加载
 */
onMounted(() => {
  loadData();
});
</script>

<style scoped>
</style>