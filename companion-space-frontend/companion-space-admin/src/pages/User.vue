<template>
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
      <template #default>
        <el-button link type="primary" size="small" @click="handleClick">
          Detail
        </el-button>
        <el-button link type="primary" size="small">Edit</el-button>
        <el-button link type="primary" size="small">delete</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import {ref, watchEffect} from "vue";
import request from "../plugins/request";
import moment from "moment";
import {defaultPicture, jsonParseTag} from "../common/userCommon";
import {Message} from "@element-plus/icons-vue";

const tableData = ref([]);

const loadData = async () => {
  const res = await request.get("/user/search");
  if (res.data.code === 0) {
    tableData.value = res.data.data;
    jsonParseTag(res.data.data);
  }else {
    Message.error("获取数据失败");
  }
}

const handleClick = () => {
  console.log('click')
}

/**
 * 监听 searchParams 变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  loadData();
});
</script>

<style scoped>

</style>