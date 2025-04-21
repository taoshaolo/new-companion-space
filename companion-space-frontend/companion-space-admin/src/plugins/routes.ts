import Login from "../pages/Login.vue";
import Home from "../pages/Home.vue";
import User from "../pages/User.vue";
import Blog from "../pages/Blog.vue";
import Team from "../pages/Team.vue";

const routes = [
    { path: "/", redirect: "/admin/home" },
    {
        path: "/admin/home",
        name: "首页",
        component: Home,
    },
    {
        path: "/admin/login",
        name:"登录",
        component: Login,
        meta: {
            hideInMenu: true,
        },
    },
    {
        path: "/manage",
        name: "管理页面",
        children: [
            {
                path: "/admin/user",
                name: "用户管理",
                component: User,
            },
            {
                path: "/admin/blog",
                name: "博客管理",
                component: Blog,
            },
            {
                path: "/admin/team",
                name: "队伍管理",
                component: Team,
            },
            {
                path: "/admin/tag",
                name: "标签管理",
                component: () => import("../pages/Tag.vue"),
            },
        ],
    },
];
export default routes;
