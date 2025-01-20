<template>
  <div id="container" style="height: 30vh; margin-outside: 20px"></div>
  <van-cell-group>
    <van-cell title="经度" :value="longitude"/>
    <van-cell title="纬度" :value="latitude"/>
    <van-cell title="地址" :value="address" @click="showAddressDialog = true"/>
  </van-cell-group>
  <van-button style="margin-top: 20px" type="primary" @click="doSearchNearby(longitude,latitude)">确定</van-button>
  <van-dialog v-model:show="showAddressDialog" title="输入地址" show-cancel-button @confirm="searchByAddress">
    <van-field v-model="address" placeholder="请输入地址"/>
  </van-dialog>
</template>

<script setup>
import {onMounted, onUnmounted, ref} from 'vue';
import AMapLoader from '@amap/amap-jsapi-loader';
import {showFailToast} from "vant";
import {useRouter} from "vue-router";
import request from "../service/myAxios";

const address = ref();
const longitude = ref();
const latitude = ref();
const searchAddress = ref('');
const showAddressDialog = ref(false);
let map = null;
let marker = null;

window._AMapSecurityConfig = {
  securityJsCode: "222f5060b342214b2b3638ab47ff84df",
};

onMounted(() => {
  AMapLoader.load({
    key: "a5f9bea09085e2416809811e4888cb97", // 申请好的Web端开发者Key，首次调用 load 时必填
    version: "2.0", // 指定要加载的 JSAPI 的版本，缺省时默认为 1.4.15
    plugins: ["AMap.Geolocation", "AMap.Geocoder", "AMap.Scale"], // 需要使用的的插件列表，如比例尺'AMap.Scale'等
  })
      .then((AMap) => {
        map = new AMap.Map("container", {
          // 设置地图容器id
          viewMode: "2D", // 是否为2D地图模式
          zoom: 15, // 初始化地图级别
          resizeEnable: true,
        });
        const geolocation = new AMap.Geolocation({
          enableHighAccuracy: true, // 是否使用高精度定位，默认:true
          timeout: 10000, // 超过10秒后停止定位，默认：5s
          buttonPosition: 'RB', // 定位按钮停靠位置
          buttonOffset: new AMap.Pixel(10, 20), // 定位按钮与设置的停靠位置的偏移量，默认：Pixel(10, 20)

        });
        // 添加定位控件
        map.addControl(geolocation);
        geolocation.getCurrentPosition(function (status, result) {
          if (status === 'complete') {
            onComolete(result);
          } else {
            onError(result);
          }
        });

        function onComolete(result) {
          longitude.value = result.position.lng;
          latitude.value = result.position.lat;
          addMarker(result.position);
          getAddress(result.position);
        }

        function onError(result) {
          showFailToast('定位失败')
        }

        // 监听地图点击事件
        map.on('click', function (e) {
          const newLatLng = e.lnglat;
          longitude.value = newLatLng.getLng();
          latitude.value = newLatLng.getLat();
          addMarker(newLatLng);
          getAddress(newLatLng);
        });

      })
      .catch((e) => {
        console.error(e);
      });
});

const router = useRouter();
const doSearchNearby = async (longitude, latitude) => {
  try {
    // 保存经纬度到用户信息中
    const res = await request.post("/user/saveGeo", {
      longitude: longitude,
      latitude: latitude
    });
    if (!res){
      showFailToast('保存经纬度失败');
    }
    // 跳转回 IndexPage 并传递经纬度参数
    router.push({
      path: '/',
      query: {
        longitude,
        latitude
      }
    });
  } catch (error) {
    showFailToast('保存经纬度失败');
  }
}

function getAddress(latLng) {
  AMap.plugin('AMap.Geocoder', function () {
    const geocoder = new AMap.Geocoder({
      city: "", // 空字符串表示全国范围内的地理编码，默认值为"全国"
    });
    geocoder.getAddress(latLng, function (status, result) {
      if (status === 'complete' && result.info === 'OK') {
        address.value = result.regeocode.formattedAddress;
      } else {
        showFailToast('查询失败');
      }
    });
  });
}

function addMarker(latLng) {
  if (marker) {
    marker.setPosition(latLng);
  } else {
    marker = new AMap.Marker({
      position: latLng,
      map: map,
      draggable: true, // 允许拖动标记
    });

    // 监听标记拖动事件
    marker.on('dragend', function (e) {
      const newLatLng = e.target.getPosition();
      longitude.value = newLatLng.getLng();
      latitude.value = newLatLng.getLat();
      getAddress(newLatLng);
    });
  }
}

function searchByAddress() {
  if (!searchAddress.value) {
    showFailToast('请输入地址');
    return;
  }
  AMap.plugin('AMap.Geocoder', function () {
    const geocoder = new AMap.Geocoder({
      city: "", // 空字符串表示全国范围内的地理编码，默认值为"全国"
    });
    geocoder.getLocation(searchAddress.value, function (status, result) {
      if (status === 'complete' && result.info === 'OK') {
        const position = result.geocodes[0].location;
        longitude.value = position.lng;
        latitude.value = position.lat;
        address.value = searchAddress.value;
        map.setCenter(position);
        addMarker(position);
      } else {
        showFailToast('地址搜索失败');
      }
    });
  });
}


onUnmounted(() => {
  map?.destroy();
});

</script>

<style>
html, body, #container {
  height: 100%;
  width: 100%;
}
</style>
