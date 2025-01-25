<template>
  <div style="margin-bottom: 20px; display: flex; justify-content: center;">
    <el-input v-model="searchParams.category" placeholder="请输入分类"
              style="width: 200px; margin-right: 10px;"></el-input>
    <el-input v-model="searchParams.tagName" placeholder="请输入标签名称"
              style="width: 200px; margin-right: 10px;"></el-input>
    <el-button type="primary" @click="handleSearch">搜索</el-button>
    <el-button type="default" @click="resetSearch" style="margin-left: 10px;">重置</el-button>
    <el-button type="primary" @click="dialogVisible = true">+ 添加标签</el-button>
  </div>

  <el-dialog title="添加标签" v-model="dialogVisible" width="30%">
    <el-form :model="form" label-width="100px">
      <el-form-item label="标签名称">
        <el-input v-model="form.tagName"></el-input>
      </el-form-item>
      <el-form-item label="分类">
        <el-input v-model="form.category"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddTag">确认</el-button>
      </span>
    </template>
  </el-dialog>
  <el-table :data="tableData" style="width: 100%">
    <el-table-column fixed prop="id" label="id"/>
    <el-table-column prop="category" label="分类"/>
    <el-table-column prop="tagName" label="标签名称">
      <template #default="scope">
        <el-tag>{{ scope.row.tagName }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="userId" label="创建用户 id"/>
    <el-table-column prop="createTime" label="创建时间">
      <template #default="scope">
        {{ moment(scope.row.createTime).format("YYYY-MM-DD HH:mm:ss") }}
      </template>
    </el-table-column>
    <el-table-column prop="updateTime" label="更新时间">
      <template #default="scope">
        {{ moment(scope.row.updateTime).format("YYYY-MM-DD HH:mm:ss") }}
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
import {ref, onMounted} from "vue";
import request from "../plugins/request";
import moment from "moment";
import {ElMessage} from 'element-plus';

const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const dialogVisible = ref(false);
const form = ref({
  tagName: '',
  category: ''
});
const searchParams = ref({
  category: '',
  tagName: ''
});

const loadData = async () => {
  const res = await request.get("/tag/list/page", {
    params: {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchParams.value
    }
  });
  if (res.data.code === 0) {
    tableData.value = res.data.data.records;
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
    category: '',
    tagName: '',
  };
  currentPage.value = 1; // 重置当前页为第一页
  loadData();
}

const handleDelete = async (id) => {
  try {
    const res = await request.post("/tag/delete", {
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

const handleAddTag = async () => {
  try {
    const res = await request.post("/tag/add", form.value);
    if (res.data.code === 0) {
      ElMessage.success("添加成功");
      dialogVisible.value = false;
      await loadData();
    } else {
      ElMessage.error("添加失败");
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