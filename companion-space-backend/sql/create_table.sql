create table team
(
    id            bigint auto_increment comment 'id'
        primary key,
    teamName      varchar(256)                       null comment '队伍名',
    teamAvatarUrl varchar(1024)                      null comment '队伍头像',
    teamPassword  varchar(512)                       null comment '队伍加密密码',
    teamDesc      varchar(1024)                      null comment '队伍描述',
    maxNum        int      default 1                 not null comment '最大人数',
    expireTime    datetime                           null comment '过期时间',
    userId        bigint                             null comment '创建人id',
    usersId       varchar(1024)                      null comment '加入队伍的用户id',
    createTime    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    teamStatus    int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 加密',
    isDelete      tinyint  default 0                 not null comment '是否删除',
    announce      varchar(512)                       null comment '队伍公告',
    updateTime    datetime default CURRENT_TIMESTAMP null
)
    comment '队伍' charset = utf8;

create table user
(
    id            bigint auto_increment comment '用户id'
        primary key,
    username      varchar(256)                       null comment '用户昵称',
    userAccount   varchar(256)                       not null comment '账号',
    userAvatarUrl varchar(1024)                      null comment '用户头像',
    gender        tinyint                            null comment '性别 1 - 男  2-女',
    userPassword  varchar(512)                       null comment '密码',
    contactInfo   varchar(512)                       null comment '联系方式',
    userDesc      varchar(512)                       null comment '个人简介',
    userStatus    int      default 0                 not null comment '状态 0 - 正常',
    userRole      int      default 0                 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    tags          varchar(1024)                      null comment '标签 json 列表',
    teamIds       varchar(512)                       null comment '队伍id列表',
    userIds       varchar(512)                       null comment '添加的好友',
    createTime    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime    datetime default CURRENT_TIMESTAMP null,
    isDelete      tinyint  default 0                 not null comment '是否删除',
    email         varchar(128)                       null comment '邮箱'
)
    comment '用户表' charset = utf8;

create table friends
(
    id         bigint auto_increment comment '好友申请id'
        primary key,
    fromId     bigint                             not null comment '发送申请的用户id',
    receiveId  bigint                             null comment '接收申请的用户id ',
    isRead     tinyint  default 0                 not null comment '是否已读(0-未读 1-已读)',
    status     tinyint  default 0                 not null comment '申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-已撤销）',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null,
    isDelete   tinyint  default 0                 not null comment '是否删除',
    remark     varchar(214)                       null comment '好友申请备注信息'
)
    comment '好友申请管理表' charset = utf8mb4;

create table chat
(
    id         bigint auto_increment comment '聊天记录id'
        primary key,
    fromId     bigint                                  not null comment '发送消息id',
    toId       bigint                                  null comment '接收消息id',
    text       varchar(512) collate utf8mb4_unicode_ci null,
    chatType   tinyint                                 not null comment '聊天类型 1-私聊 2-群聊',
    createTime datetime default CURRENT_TIMESTAMP      null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP      null,
    teamId     bigint                                  null
)
    comment '聊天消息表' charset = utf8mb4;


create table blog
(
    id          bigint auto_increment comment '主键'
        primary key,
    userId      bigint                                   not null comment '用户id',
    title       varchar(255) collate utf8mb4_unicode_ci  not null comment '标题',
    images      varchar(2048)                            null comment '图片，最多9张，多张以","隔开',
    content     varchar(2048) collate utf8mb4_unicode_ci not null comment '文章',
    likedNum    int unsigned default '0'                 null comment '点赞数量',
    commentsNum int unsigned default '0'                 null comment '评论数量',
    createTime  timestamp    default CURRENT_TIMESTAMP   not null comment '创建时间',
    updateTime  timestamp    default CURRENT_TIMESTAMP   not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint      default 0                   null comment '逻辑删除'
)
    comment '博客表' charset = utf8mb4;

create table blog_comments
(
    id         bigint auto_increment comment '主键'
        primary key,
    userId     bigint                                 not null comment '用户id',
    blogId     bigint                                 not null comment '博文id',
    parentId   bigint unsigned                        null comment '关联的1级评论id，如果是一级评论，则值为0',
    answerId   bigint unsigned                        null comment '回复的评论id',
    content    varchar(255)                           not null comment '回复的内容',
    likedNum   int unsigned default '0'               null comment '点赞数',
    status     tinyint unsigned                       null comment '状态，0：正常，1：被举报，2：禁止查看',
    createTime timestamp    default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint      default 0                 null comment '逻辑删除'
)
    comment '博客评论表' charset = utf8mb4;

create table blog_like
(
    id         bigint auto_increment comment '主键'
        primary key,
    blogId     bigint                             not null comment '博文id',
    userId     bigint                             not null comment '用户id',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime                           null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 null comment '逻辑删除'
)
    comment '博客点赞表' charset = utf8mb4;

create table comment_like
(
    id         bigint auto_increment comment '主键'
        primary key,
    commentId  bigint                             not null comment '评论id',
    userId     bigint                             not null comment '用户id',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 null comment '逻辑删除'
)
    comment '评论点赞表' charset = utf8mb4;

create table message
(
    id         bigint auto_increment comment '主键'
        primary key,
    type       tinyint                            null comment '类型 -0 博文点赞 -1 评论点赞',
    fromId     bigint                             null comment '消息发送的用户id',
    toId       bigint                             null comment '消息接收的用户id',
    data       varchar(255)                       null comment '消息内容',
    isRead     tinyint  default 0                 null comment '已读-0 未读 ,1 已读',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 null comment '逻辑删除'
)
    comment '点赞、评论提示表' charset = utf8mb4;