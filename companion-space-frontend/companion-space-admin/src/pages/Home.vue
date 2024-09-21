<template>
  <el-row :gutter="20">
    <el-col :span="12">
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <span>热门标签统计（Top 10）</span>
          </div>
        </template>
        <v-chart :option="userTagOptions" style="height: 300px"/>
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <span>统计用户活跃度（Top 10）</span>
          </div>
        </template>
        <v-chart :option="userActivityOptions" style="height: 300px"/>
      </el-card>
    </el-col>
  </el-row>
  <div style="margin-top: 20px"/>
  <el-row :gutter="20">
    <el-col :span="12">
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <span>热门博客统计（Top 10）</span>
          </div>
        </template>
        <v-chart :option="blogLikeOptions" style="height: 300px"/>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import VChart from "vue-echarts";
import {computed, ref, watchEffect} from "vue";
import request from "../plugins/request";
import {Message} from "@element-plus/icons-vue";

const userTagList = ref([]);
const userActivityList = ref([]);
const blogLikeList = ref([]);
// 获取数据
const loadUserTagData = async () => {
  const res = await request.get("/statistic/userTag");
  if (res.data.code === 0) {
    userTagList.value = res.data.data || [];
  } else {
    Message.error("获取数据失败");
  }
}
const loadUserActivityData = async () => {
  const res = await request.get("/statistic/userActivity");
  if (res.data.code === 0) {
    userActivityList.value = res.data.data || [];
  } else {
    Message.error("获取数据失败");
  }
}
const loadBlogLikeData = async () => {
  const res = await request.get("/statistic/blogLike");
  if (res.data.code === 0) {
    blogLikeList.value = res.data.data || [];
  } else {
    Message.error("获取数据失败");
  }
}

// 统计最热门标签 Top 10（环形图)
const userTagOptions = computed(() => {
  return {
    legend: {
      top: 'top'
    },
    tooltip: {
      // 触发类型，默认数据项触发，可选为：'item' | 'axis'
      trigger: 'item',
      // 格式化提示内容
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    series: [
      {
        name: '热门标签分布',
        type: 'pie',
        radius: [10, 100],
        center: ['50%', '60%'],
        roseType: 'area',
        itemStyle: {
          borderRadius: 8
        },
        data: userTagList.value.map((item) => {
          return {
            value: item.tagCount,
            name: item.tag
          };
        }),
      }
    ]
  };
})
// 统计热门博客 Top 10（柱状图）
const blogLikeOptions = computed(() => {
  return {
    xAxis: {
      type: "category",
      data: blogLikeList.value.map((item) => item.id),
      name: "博客 id",
    },
    yAxis: {
      type: "value",
      name: "点赞数",
    },
    series: [
      {
        data: blogLikeList.value.map((item) => item.likedNum),
        type: "bar",
        label: {
          show: true, // 显示标签
          position: "top", // 标签的位置
          // 可以进一步定义样式
          textStyle: {
            color: "black", // 标签的颜色
            fontSize: 12, // 标签的字体大小
          },
        },
      },
    ],
  };
});
// 统计用户活跃度 Top 10（饼图）
const userActivityOptions = computed(() => {
  return {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '用户活跃度统计',
        type: 'pie',
        radius: '50%',
        data: userActivityList.value.map((item) => {
          return {
            value: item.activity,
            name: item.fromId
          }
        }),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
});
watchEffect(() => {
  loadUserTagData();
});
watchEffect(() => {
  loadUserActivityData();
});
watchEffect(() => {
  loadBlogLikeData();
});
</script>

<style scoped>
.box-card {
  width: 600px;
  margin: 0 auto;
}

</style>
