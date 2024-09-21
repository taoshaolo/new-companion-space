import * as VueRouter from 'vue-router'

const routes = [
    {
        path: '/',
        meta: {title: "首页"},
        component: () => import("../pages/IndexPage.vue")
    },
    {
        path: '/team',
        meta: {title: "队伍",},
        component: () => import("../pages/TeamPage.vue")
    },
    {
        path: '/team/show',
        meta: {title: "查看队伍",},
        name: 'teamShow',
        component: () => import("../pages/TeamShow.vue")
    },
    {
        path: '/team/create',
        meta: {title: "创建队伍",},
        name: 'teamCreate',
        component: () => import("../pages/TeamCreatePage.vue")
    },
    {
        path: '/team/edit',
        meta: {title: "修改队伍"},
        name: 'teamEdit',
        component: () => import("../pages/TeamEditPage.vue")
    },
    {
        path: '/user',
        meta: {title: "我的"},
        component: () => import("../pages/UserPage.vue")
    },
    {
        path: '/user/more',
        meta: {title: "详细信息"},
        component: () => import("../pages/UserPageMore.vue")
    },
    {
        path: '/user/more/password',
        meta: {title: "修改密码"},
        component: () => import("../pages/PasswordEdit.vue")
    },
    {
        path: '/user/login',
        meta: {title: "登录",},
        component: () => import("../pages/LoginUser.vue")
    },
    {
        path: '/user/register',
        meta: {title: "注册",},
        component: () => import("../pages/RegisterUser.vue")
    },
    {
        path: '/user/show/:userId',
        meta: {title: "查看用户",},
        name: 'userShow',
        component: () => import("../pages/UserShow.vue")
    },
    {
        path: '/user/team',
        meta: {title: "加入的队伍",},
        name: "userTeamPage",
        component: () => import("../pages/UserTeamPage.vue")
    },
    {
        path: '/user/edit',
        meta: {title: "信息修改"},
        component: () => import("../pages/UserEdit.vue")
    },
    {
        path: '/friends',
        meta: {title: "我的好友",},
        component: () => import("../pages/FriendsPage.vue")
    },
    {
        path: '/apply',
        meta: {title: "新的朋友",},
        component: () => import("../pages/ApplicationRecord.vue")
    },
    {
        path: '/chat',
        meta: {title: "聊天",},
        component: () => import("../pages/ChatPage.vue")
    },
    {
        path: '/public_chat',
        meta: {title: "聊天室",},
        component: () => import("../components/Chat.vue")
    },
    {
        path: '/search',
        meta: {title: "搜索",},
        component: () => import("../pages/UserSearch.vue")
    },
    {
        path: "/blog",
        meta:{
            title: "博文",
            noLayout: true, // 不需要使用 BasicLayout 的页面
        },
        component: () => import("../pages/BlogPage.vue"),
    },
    {
        path: "/blog/edit",
        component: () => import("../pages/BlogEditPage.vue"),
        meta: {
            title: "博文编辑",
            noLayout: true, // 不需要使用 BasicLayout 的页面
        }
    },
    {
        path: "/user/comment",
        component: () => import("../pages/UserBlogCommentPage.vue"),
        meta: {
            title: "所有评论",
            noLayout: true, // 不需要使用 BasicLayout 的页面
        },
    },
    {
        path: "/user/like",
        meta: {
            title: "赞",
            noLayout: true, // 不需要使用 BasicLayout 的页面
        },
        component: () => import("../pages/UserLikePage.vue"),
    },
    {
        path: "/user/blog",
        meta: {
            title: "我的博文",
        },
        component: () => import("../pages/UserBlogPage.vue")
    },
    {
        path: "/ai",
        component: () => import("../pages/AiPage.vue"),
        meta: {
            title: "AI 助手",
            noLayout: true, // 不需要使用 BasicLayout 的页面
        }
    },



]

const routers = VueRouter.createRouter({
    history: VueRouter.createWebHashHistory(),
    // history: VueRouter.createWebHistory(),
    routes
})

routers.beforeEach((to, from, next) => {
    to.meta.lastRoutePath = from.path;
    next();
});

export default routers


