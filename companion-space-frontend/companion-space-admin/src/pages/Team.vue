<template>
  <el-table :data="tableData" style="width: 100%">
    <el-table-column fixed prop="id" label="id"/>
    <el-table-column prop="teamName" label="队伍名称"/>
    <el-table-column prop="userId" label="创建人id"/>
    <el-table-column prop="teamAvatarUrl" label="队伍头像">
      <template #default="scope">
        <el-image :src="scope.row.teamAvatarUrl ? scope.row.teamAvatarUrl :defaultPicture "
                  style="width: 60px; height: 60px" fit="fill"/>
      </template>
    </el-table-column>
    <el-table-column prop="teamDesc" label="队伍描述"/>
    <el-table-column prop="maxNum" label="最大人数"/>
    <el-table-column prop="expireTime" label="过期时间">
      <template #default="scope">
        {{ moment(scope.row.expireTime).format("YYYY-MM-DD HH:mm:ss") }}
      </template>
    </el-table-column>
    <el-table-column prop="usersId" label="队员id"/>
    <el-table-column prop="teamStatus" label="状态">
      <template #default="scope">
        {{
          scope.row.teamStatus === 0 ? "公开" : scope.row.teamStatus === 1
              ? "私有" : scope.row.teamStatus === 2 ? "加密" : "未知状态"
        }}
      </template>
    </el-table-column>
    <el-table-column prop="announce" label="公告"/>
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
import {defaultPicture} from "../common/userCommon";

const tableData = ref([]);

const loadData = async () => {
  const res = await request.get("/team/teams");
  console.log("撒顶顶顶",res)
  if (res.data.code === 0) {
    tableData.value = res.data.data.teamSet;
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