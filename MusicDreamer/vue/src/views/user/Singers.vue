<template>
  <div class="singer-page">
    <!-- 顶部筛选区域 -->
    <div class="singer-header">
      <h1 class="page-title">歌手库</h1>
      <div class="filter-area">
        <el-input
            v-model="searchKeyword"
            placeholder="搜索歌手..."
            clearable
            class="search-input"
            @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 歌手卡片网格 -->
    <div class="singer-grid" v-loading="loading">
      <div
          v-for="singer in filteredSingers"
          :key="singer.id"
          class="singer-card"
          @click="viewSingerDetail(singer)"
      >
        <div class="card-cover">
          <el-avatar
              :src="singer.avatar || defaultAvatar"
              :size="120"
              class="singer-avatar"
          />
        </div>
        <div class="card-info">
          <h3 class="singer-name">{{ singer.username }}</h3>
          <div class="singer-meta">
            <span class="song-count">
              {{ singer.songCount }} 首单曲
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 无数据提示 -->
    <div v-if="!loading && filteredSingers.length === 0" class="no-data">
      <el-empty description="未找到匹配的歌手" />
    </div>

    <!-- 分页控件 -->
    <div class="pagination" v-if="pagination.total > pagination.pageSize">
      <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[24, 48, 96]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
// 默认头像
const defaultAvatar = ref('/src/assets/imgs/default_singer_avatar.png')

// 搜索关键字
const searchKeyword = ref('')

// 歌手数据
const singers = ref([])

// 分页参数
const pagination = ref({
  pageNum: 1,
  pageSize: 36,
  total: 0
})

// 加载状态
const loading = ref(false)

// 过滤后的歌手列表
const filteredSingers = computed(() => {
  let result = singers.value

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(singer =>
        singer.username.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 获取歌手数据
const fetchSingers = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/selectAll', {
      params: {
        role: 'SINGER',
        pageNum: pagination.value.pageNum,
        pageSize: pagination.value.pageSize
        // 原注释保留，未修改首字母相关参数部分
      }
    })

    if (res.code === '200') {
      const singerList = res.data.list || res.data

      // 并行获取每位歌手的单曲数
      const enrichedSingers = await Promise.all(
          singerList.map(async (singer) => {
            try {
              const musicRes = await request.get('/music/selectAll', {
                params: { singerName: singer.username }
              })
              const songCount = musicRes.code === '200' ? musicRes.data.length : 0
              return { ...singer, songCount }
            } catch (err) {
              console.error('获取歌曲失败:', err)
              return { ...singer, songCount: 0 }
            }
          })
      )

      singers.value = enrichedSingers
      pagination.value.total = res.data.total || singerList.length
    } else {
      ElMessage.error('获取歌手列表失败: ' + res.msg)
    }
  } catch (err) {
    console.error(err)
    ElMessage.error('请求错误: ' + err.message)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  // 原注释保留，未修改首字母相关逻辑
  pagination.value.pageNum = 1
  fetchSingers()
}

// 分页大小变化
const handleSizeChange = (newSize) => {
  pagination.value.pageSize = newSize
  fetchSingers()
}

// 页码变化
const handlePageChange = (newPage) => {
  pagination.value.pageNum = newPage
  fetchSingers()
}

// 查看歌手详情
const viewSingerDetail = (singer) => {
  router.push(`/user/singers/${singer.id}`)
}

onMounted(() => {
  fetchSingers()
})
</script>

<style scoped lang="scss">
/* 样式部分保留原注释，仅删除首字母筛选相关样式 */
.singer-page {
  max-width: 1200px;
  margin: 60px auto;
  padding: 20px;
}

.singer-header {
  margin-bottom: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #ff9a3c, #ff6b6b);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 20px;
}

.filter-area {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
}

.search-input {
  width: 300px;

  :deep(.el-input__inner) {
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.1);
    border: none;
    transition: all 0.3s ease;

    &:focus {
      background: rgba(255, 255, 255, 0.15);
      box-shadow: 0 0 15px rgba(255, 154, 60, 0.2);
    }
  }
}

.singer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 25px;
  margin-bottom: 30px;
}

.singer-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
    background: rgba(255, 255, 255, 0.08);

    .hover-overlay {
      opacity: 1;
    }

    .singer-avatar {
      transform: scale(1.05);
    }
  }
}

.card-cover {
  position: relative;
  padding: 25px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, rgba(22, 34, 49, 0.7), rgba(14, 22, 33, 0.9));
}

.singer-avatar {
  border: 3px solid rgba(255, 255, 255, 0.1);
  transition: transform 0.3s ease;
}

.hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.card-info {
  padding: 20px;
  text-align: center;
}

.singer-name {
  font-size: 18px;
  font-weight: 600;
  color: #39393A;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.singer-meta {
  font-size: 14px;
  color: #aaa;
}

.song-count {
  display: inline-block;
  padding: 5px 12px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 15px;
  color: #409eff;
}

.no-data {
  padding: 40px 0;
  text-align: center;
  color: #888;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-area {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }

  .singer-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
  }

  .card-cover {
    padding: 15px;
  }

  .singer-avatar {
    width: 80px;
    height: 80px;
  }
}
</style>