import axios from "axios";
import {Message} from "@element-plus/icons-vue";

const request = axios.create({
    baseURL: "http://localhost:8080/api",
    timeout: 60000,
    withCredentials: true,
});

// 添加请求拦截器
request.interceptors.request.use(
    function (config) {
        // 在发送请求之前执行某项操作
        return config;
    },
    function (error) {
        // 对请求错误执行某些操作
        return Promise.reject(error);
    }
);

// 添加响应拦截器
request.interceptors.response.use(
    function (response) {
        // 任何在 2xx 范围内的状态代码都会导致触发此函数
        // 对响应数据执行某些操作
        const { data } = response;
        // 未登录
        if (data.code === 40100) {
            Message.error("未登录");
            window.location.href = "/user/admin/login?redirect=" + window.location.href;
        }
        return response;
    },
    function (error) {
        // 任何超出 2xx 范围的状态代码都会导致触发此函数
        // 对响应错误执行某些操作
        return Promise.reject(error);
    }
);
export default request;
