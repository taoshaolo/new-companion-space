import Login from "../pages/Login.vue";
import Home from "../pages/Home.vue";
import User from "../pages/User.vue";
import Blog from "../pages/Blog.vue";
import Team from "../pages/Team.vue";

const routes = [
    { path: "/", redirect: "/home" },
    {
        path: "/home",
        name: "首页",
        component: Home,
    },
    {
        path: "/login",
        name:"登录",
        component: Login,
        meta: {
            hideInMenu: true,
        },
    },
    {
        path: "/admin",
        name: "管理页面",
        children: [
            {
                path: "/user",
                name: "用户管理",
                component: User,
            },
            {
                path: "/blog",
                name: "博客管理",
                component: Blog,
            },
            {
                path: "/team",
                name: "队伍管理",
                component: Team,
            }
        ],
    },
];
export default routes;
