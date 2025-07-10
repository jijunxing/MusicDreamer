<template>
  <div class="notice-container">
    <div class="notice-header">
      <h1>系统公告</h1>

      <div class="header-actions">
        <el-button type="primary" @click="goBack">
          <el-icon><ArrowLeft /></el-icon> 返回首页
        </el-button>
      </div>
    </div>

    <div class="notice-list">
      <el-card
          v-for="notice in notices"
          :key="notice.id"
          class="notice-card"
          shadow="hover"
          :body-style="{ padding: '16px' }"
      >
        <div class="notice-card-header" @click="toggleNotice(notice.id)">
          <div class="notice-title">
            <el-tag v-if="isNewNotice(notice.createTime)" type="danger" size="small">最新</el-tag>
            <span class="title-text">{{ notice.title }}</span>
          </div>

          <div class="notice-meta">
            <span class="create-time">{{ formatDate(notice.createTime) }}</span>
            <el-tag size="small">{{ notice.userName }}</el-tag>
          </div>

          <el-icon :class="{ 'rotate-icon': expandedNoticeId === notice.id }">
            <ArrowDown />
          </el-icon>
        </div>

        <div v-show="expandedNoticeId === notice.id" class="notice-content">
          {{ notice.msg }}
        </div>
      </el-card>
    </div>

    <el-pagination
        v-if="total > pageSize"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
    />

    <div v-if="notices.length === 0" class="no-notices">
      <el-empty description="暂无公告数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, User, ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 公告数据
const notices = ref([])
const expandedNoticeId = ref(null)

// 分页参数
const currentPage = ref(1)
const total = ref(0)
const pageSize = ref(10)
const searchUser = ref('')

// 获取公告列表
const fetchNotices = async () => {
  try {
    const response = await request.get('/notice/selectPage', {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        userName: searchUser.value
      }
    })

    if (response.code === '200') {
      notices.value = response.data.list || []
      total.value = response.data.total || 0
      // 默认展开第一条公告
      if (notices.value.length > 0 && expandedNoticeId.value === null) {
        expandedNoticeId.value = notices.value[0].id
      }
    } else {
      throw new Error(response.msg || '获取公告失败')
    }
  } catch (error) {
    console.error('获取公告数据失败:', error)
  }
}

// 展开/收起公告详情
const toggleNotice = (id) => {
  if (expandedNoticeId.value === id) {
    expandedNoticeId.value = null
  } else {
    expandedNoticeId.value = id
  }
}

// 处理分页变化
const handlePageChange = (newPage) => {
  currentPage.value = newPage
  fetchNotices()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 检查是否为最新公告（过去7天内）
const isNewNotice = (createTime) => {
  const noticeDate = new Date(createTime)
  const oneWeekAgo = new Date()
  oneWeekAgo.setDate(oneWeekAgo.getDate() - 7)
  return noticeDate > oneWeekAgo
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 返回首页
const goBack = () => {
  router.push('/user/home')
}

onMounted(() => {
  fetchNotices()
})
</script>

<style scoped>
.notice-container {
  max-width: 900px;
  margin: 60px auto;
  padding: 20px;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  flex-wrap: wrap;
  gap: 15px;
}

.notice-header h1 {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.notice-list {
  margin-bottom: 30px;
}

.notice-card {
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.notice-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: 5px 0;
  border-bottom: 1px solid #ebeef5;
}

.notice-title {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-text {
  font-size: 17px;
  font-weight: 500;
  max-width: 500px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 13px;
  color: #606266;
  margin: 0 15px;
}

.create-time {
  min-width: 130px;
}

.notice-content {
  padding-top: 15px;
  line-height: 1.6;
  font-size: 15px;
  color: #444;
}

.el-icon {
  transition: transform 0.3s ease;
}

.rotate-icon {
  transform: rotate(180deg);
}

.el-pagination {
  justify-content: center;
  margin-top: 20px;
}

.no-notices {
  padding: 40px 0;
  text-align: center;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .notice-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .notice-meta {
    align-self: flex-end;
    margin-top: 5px;
  }

  .el-icon {
    position: absolute;
    right: 16px;
    top: 20px;
  }

  .title-text {
    max-width: 300px;
  }
}

@media (max-width: 576px) {
  .header-actions {
    flex-direction: column;
    width: 100%;
    align-items: flex-start;
  }

  .el-input {
    width: 100%;
    margin-left: 0 !important;
    margin-top: 10px;
  }

  .title-text {
    max-width: 200px;
  }
}
</style>
