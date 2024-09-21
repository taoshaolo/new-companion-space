// export const defaultPicture = "https://img.qimuu.icu/typory/teamImg1.jpg"
export const defaultPicture = "https://img2.baidu.com/it/u=60856268,584589370&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=800"

// @ts-ignore
export const jsonParseTag = (usersList) => {
    // @ts-ignore
    usersList.forEach(user => {
        if (user.tags) {
            user.tags = user.tags ? JSON.parse(user.tags) : '该用户暂未设置';
        }
    })
}