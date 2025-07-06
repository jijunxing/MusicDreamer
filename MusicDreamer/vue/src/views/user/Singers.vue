<template>
  <div class="singer-container">
    <!-- 字母索引 -->
    <div class="index-bar">
      <span
          v-for="letter in indexLetters"
          :key="letter"
          class="index-letter"
          :class="{ active: currentIndex === letter }"
          @click="scrollToLetter(letter)"
      >
        {{ letter }}
      </span>
    </div>

    <!-- 歌手列表 -->
    <div class="singer-list" ref="listRef">
      <!-- 热门歌手 -->
      <div class="section" v-if="hotSingers.length > 0">
        <h2 class="section-title">热门歌手</h2>
        <div class="singer-grid">
          <div
              v-for="singer in hotSingers"
              :key="singer.id"
              class="singer-card"
              @click="viewSingerDetail(singer)"
          >
            <div class="avatar-container">
              <el-avatar :size="90" :src="singer.avatar" />
            </div>
            <div class="singer-name">{{ singer.name }}</div>
          </div>
        </div>
      </div>

      <!-- 按字母分组 -->
      <div
          v-for="group in singerGroups"
          :key="group.title"
          class="section"
          :data-index="group.title"
      >
        <h2 class="section-title">{{ group.title }}</h2>
        <ul class="singer-items">
          <li
              v-for="singer in group.items"
              :key="singer.id"
              class="singer-item"
              @click="viewSingerDetail(singer)"
          >
            <el-avatar :size="60" :src="singer.avatar" />
            <span class="name">{{ singer.name }}</span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const listRef = ref(null)
const currentIndex = ref('热')
const singers = ref([])

// 生成A-Z字母数组
const indexLetters = computed(() => {
  const letters = []
  for (let i = 65; i <= 90; i++) {
    letters.push(String.fromCharCode(i))
  }
  return ['热', ...letters]
})

// 获取歌手数据
const fetchSingers = async () => {
  try {
    const res = await request.get('/singer/selectAll')
    if (res.code === '200') {
      singers.value = res.data
    } else {
      console.error('获取歌手数据失败:', res.msg)
    }
  } catch (error) {
    console.error('请求异常:', error)
  }
}

// 热门歌手（前10位）
const hotSingers = computed(() => {
  return singers.value.slice(0, 10)
})

// 按字母分组的歌手
const singerGroups = computed(() => {
  const groups = {}

  // 初始化A-Z分组
  for (let i = 65; i <= 90; i++) {
    const letter = String.fromCharCode(i)
    groups[letter] = {
      title: letter,
      items: []
    }
  }

  // 按首字母分组
  singers.value.forEach(singer => {
    const firstLetter = singer.name.charAt(0).toUpperCase()
    if (groups[firstLetter]) {
      groups[firstLetter].items.push(singer)
    } else {
      // 非A-Z字符归到#
      if (!groups['#']) groups['#'] = { title: '#', items: [] }
      groups['#'].items.push(singer)
    }
  })

  // 过滤空分组并按字母排序
  return Object.values(groups)
      .filter(group => group.items.length > 0)
      .sort((a, b) => a.title.localeCompare(b.title))
})

// 滚动到指定字母
const scrollToLetter = (letter) => {
  currentIndex.value = letter

  if (letter === '热') {
    listRef.value.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }

  const targetElement = listRef.value.querySelector(`[data-index="${letter}"]`)
  if (targetElement) {
    targetElement.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 监听滚动更新当前索引
const handleScroll = () => {
  const sections = listRef.value.querySelectorAll('.section')
  const scrollTop = listRef.value.scrollTop

  sections.forEach(section => {
    const offsetTop = section.offsetTop
    const height = section.offsetHeight

    if (scrollTop >= offsetTop - 50 && scrollTop < offsetTop + height - 50) {
      currentIndex.value = section.dataset.index
    }
  })
}

// 查看歌手详情
const viewSingerDetail = (singer) => {
  router.push({
    name: 'SingerDetail',
    params: { id: singer.id },
    query: { name: singer.name }
  })
}

onMounted(() => {
  fetchSingers()
  if (listRef.value) {
    listRef.value.addEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
.singer-container {
  display: flex;
  max-width: 1200px;
  margin: 30px auto;
  height: calc(100vh - 180px);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.index-bar {
  width: 50px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px 0;
  background: #f8f9fa;
  overflow-y: auto;
}

.index-letter {
  padding: 5px 10px;
  margin: 4px 0;
  font-size: 14px;
  cursor: pointer;
  border-radius: 15px;
  transition: all 0.3s;
}

.index-letter:hover {
  background: #e9ecef;
}

.index-letter.active {
  background: #409eff;
  color: white;
}

.singer-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  position: relative;
}

.section {
  margin-bottom: 30px;
}

.section-title {
  padding: 8px 15px;
  background: linear-gradient(to right, #409eff, #e6f7ff);
  border-left: 4px solid #1890ff;
  font-size: 18px;
  color: #1a1a1a;
  margin-bottom: 15px;
  border-radius: 0 8px 8px 0;
}

.singer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 25px;
  margin-bottom: 20px;
}

.singer-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s;
}

.singer-card:hover {
  transform: translateY(-5px);
}

.avatar-container {
  margin-bottom: 12px;
}

.singer-name {
  font-size: 14px;
  text-align: center;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100px;
}

.singer-items {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.singer-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
}

.singer-item:hover {
  background: #f5f7fa;
}

.singer-item .name {
  margin-left: 15px;
  font-size: 15px;
  color: #333;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .singer-container {
    flex-direction: column;
    height: auto;
  }

  .index-bar {
    width: 100%;
    flex-direction: row;
    padding: 10px 5px;
    overflow-x: auto;
  }

  .index-letter {
    margin: 0 3px;
  }

  .singer-grid {
    grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
    gap: 15px;
  }
}
</style>