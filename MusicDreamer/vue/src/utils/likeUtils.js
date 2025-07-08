// 获取用户所有点赞映射 { musicId: likeId }
export const getUserLikesMap = () => {
    const likesMap = JSON.parse(localStorage.getItem('userLikes')) || {};
    return likesMap;
};

// 检查歌曲是否被点赞
export const isMusicLiked = (musicId) => {
    const likesMap = getUserLikesMap();
    return !!likesMap[musicId];
};

// 更新点赞状态
export const updateLikeStatus = (musicId, likeId) => {
    const likesMap = getUserLikesMap();

    if (likeId) {
        // 添加点赞
        likesMap[musicId] = likeId;
    } else {
        // 取消点赞
        delete likesMap[musicId];
    }

    localStorage.setItem('userLikes', JSON.stringify(likesMap));
};