<template>
  <el-space>
    <div style="margin-bottom: 20px;">
      <el-input v-model="searchParams.username" placeholder="请输入用户名"
                style="width: 200px; margin-right: 10px;"></el-input>
      <el-input v-model="searchParams.userAccount" placeholder="请输入账号"
                style="width: 200px; margin-right: 10px;"></el-input>
      <el-input v-model="searchParams.searchText" placeholder="邮箱、联系人、描述"
                style="width: 200px; margin-right: 10px;"></el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="default" @click="resetSearch" style="margin-left: 10px;">重置</el-button>
    </div>


  </el-space>
  <el-table :data="tableData" style="width: 100%">
    <el-table-column fixed prop="id" label="id"/>
    <el-table-column prop="username" label="用户名"/>
    <el-table-column prop="userAccount" label="账号"/>
    <el-table-column prop="userAvatarUrl" label="用户头像">
      <template #default="scope">
        <el-image :src="scope.row.userAvatarUrl ? scope.row.userAvatarUrl :defaultPicture "
                  style="width: 60px; height: 60px" fit="fill"/>
      </template>
    </el-table-column>

    <el-table-column prop="gender" label="性别">
      <template #default="scope">
        {{ scope.row.gender === 1 ? "男" : "女" }}
      </template>
    </el-table-column>
    <el-table-column prop="email" label="邮箱"/>
    <el-table-column prop="contactInfo" label="联系信息"/>
    <el-table-column prop="userDesc" label="描述"/>
    <el-table-column prop="userStatus" label="状态">
      <template #default="scope">
        {{ scope.row.userStatus === 0 ? "正常" : "封禁" }}
      </template>
    </el-table-column>
    <el-table-column prop="userRole" label="角色">
      <template #default="scope">
        {{ scope.row.userRole === 1 ? "管理员" : "普通用户" }}
      </template>
    </el-table-column>
    <el-table-column prop="tags" label="标签">
      <template #default="scope">
        <el-tag v-for="tag in scope.row.tags">{{ tag }}</el-tag>
      </template>
    </el-table-column>
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
            title="确定要删除此用户吗？"
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
import {defaultPicture, jsonParseTag} from "../common/userCommon";
import { ElMessage } from 'element-plus';

const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const searchParams = ref({
  username: '',
  userAccount: '',
  searchText: '',
});

const loadData = async () => {
  const res = await request.get("/user/list/page",{
    params: {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchParams.value
    }
  });
  if (res.data.code === 0) {
    tableData.value = res.data.data.records;
    total.value = res.data.data?.total || 0;
    jsonParseTag(res.data.data.records);
  }else {
    ElMessage.error("获取数据失败");
  }
}

const handleSearch = () => {
  currentPage.value = 1; // 重置当前页为第一页
  loadData();
}
const resetSearch = () => {
  searchParams.value = {
    username: '',
    userAccount: '',
  };
  currentPage.value = 1; // 重置当前页为第一页
  loadData();
}


const handleDelete = async (id) => {
  try {
    const res = await request.post("/user/delete", {
      id: id,
    });
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
onMounted(() => {
  loadData();
});
</script>

<style scoped>

</style>