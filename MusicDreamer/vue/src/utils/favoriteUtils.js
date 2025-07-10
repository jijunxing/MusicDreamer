import request from '@/utils/request'
import { ref } from 'vue'

// 全局响应式收藏状态
export const userFavoritesMap = ref({})
let currentUserId = null

// 初始化用户收藏数据
export const initUserFavorites = async () => {
    const user = JSON.parse(localStorage.getItem('currentUser'))
    if (user) {
        currentUserId = user.id

        // 从本地存储加载
        const savedFavorites = localStorage.getItem('userFavorites')
        if (savedFavorites) userFavoritesMap.value = JSON.parse(savedFavorites)

        // 从服务器刷新
        await refreshUserFavorites()

        // 定时同步
        setInterval(refreshUserFavorites, 30 * 60 * 1000)
    }
}

// 刷新收藏数据
export const refreshUserFavorites = async () => {
    if (!currentUserId) return

    try {
        const res = await request.get('/favorite/selectAll', {
            params: { userId: currentUserId }
        })

        if (res.data) {
            const newMap = {}
            res.data.forEach(favorite => {
                newMap[favorite.songlistId] = favorite.id
            })
            userFavoritesMap.value = newMap
            localStorage.setItem('userFavorites', JSON.stringify(newMap))
        }
    } catch (error) {
        console.error('刷新收藏数据失败', error)
    }
}

// 检查歌单是否被收藏
export const isSonglistFavorited = (songlistId) => {
    return !!userFavoritesMap.value[songlistId]
}

// 核心收藏/取消收藏方法
export const toggleFavorite = async (songlistId) => {
    if (!currentUserId) {
        throw new Error('用户未登录')
    }

    const isFavorited = isSonglistFavorited(songlistId)

    try {
        if (isFavorited) {
            await request.delete(`/favorite/delete/${userFavoritesMap.value[songlistId]}`)
            delete userFavoritesMap.value[songlistId]
        } else {
            const res = await request.post('/favorite/add', {
                userId: currentUserId,
                songlistId: songlistId
            })
            userFavoritesMap.value[songlistId] = res.data
        }

        // 创建新对象触发响应式更新
        userFavoritesMap.value = { ...userFavoritesMap.value }
        localStorage.setItem('userFavorites', JSON.stringify(userFavoritesMap.value))

        return !isFavorited // 返回新的收藏状态
    } catch (error) {
        console.error('收藏操作失败', error)
        throw error
    }
}