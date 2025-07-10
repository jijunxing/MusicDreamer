import request from '@/utils/request'
import { ref } from 'vue'

// 全局响应式点赞状态
export const userLikesMap = ref({})
let currentUserId = null

// 初始化用户点赞数据
export const initUserLikes = async () => {
    const user = JSON.parse(localStorage.getItem('currentUser'))
    if (user) {
        currentUserId = user.id

        // 从本地存储加载
        const savedLikes = localStorage.getItem('userLikes')
        if (savedLikes) userLikesMap.value = JSON.parse(savedLikes)

        // 从服务器刷新
        await refreshUserLikes()

        // 定时同步
        setInterval(refreshUserLikes, 30 * 60 * 1000)
    }
}

// 刷新点赞数据
export const refreshUserLikes = async () => {
    if (!currentUserId) return

    try {
        const res = await request.get('/like/selectAll', {
            params: { userId: currentUserId }
        })

        if (res.data) {
            const newMap = {}
            res.data.forEach(like => {
                newMap[like.musicId] = like.id
            })
            userLikesMap.value = newMap
            localStorage.setItem('userLikes', JSON.stringify(newMap))
        }
    } catch (error) {
        console.error('刷新点赞数据失败', error)
    }
}

// 检查歌曲是否被点赞
export const isMusicLiked = (musicId) => {
    return !!userLikesMap.value[musicId]
}

// 核心点赞/取消点赞方法
export const toggleLike = async (musicId) => {
    if (!currentUserId) {
        throw new Error('用户未登录')
    }

    const isLiked = isMusicLiked(musicId)

    try {
        if (isLiked) {
            await request.delete(`/like/delete/${userLikesMap.value[musicId]}`)
            delete userLikesMap.value[musicId]
        } else {
            const res = await request.post('/like/add', {
                userId: currentUserId,
                musicId: musicId
            })
            userLikesMap.value[musicId] = res.data
        }

        // 创建新对象触发响应式更新
        userLikesMap.value = { ...userLikesMap.value }
        localStorage.setItem('userLikes', JSON.stringify(userLikesMap.value))

        return !isLiked // 返回新的点赞状态
    } catch (error) {
        console.error('点赞操作失败', error)
        throw error
    }
}