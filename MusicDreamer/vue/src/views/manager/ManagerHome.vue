<template>
  <div class="manager-home">

    <!-- 数据概览卡片 -->
    <div class="data-cards">
      <div class="data-card">
        <div class="card-title">用户总数</div>
        <div class="card-value">{{ userStats.total }}</div>
        <div class="card-footer">
          <span>普通用户: {{ userStats.normal }} | 歌手: {{ userStats.singer }} | 管理员: {{ userStats.admin }}</span>
        </div>
      </div>

      <div class="data-card">
        <div class="card-title">歌曲总数</div>
        <div class="card-value">{{ musicStats.total }}</div>
        <div class="card-footer">
          <span>总播放量: {{ musicStats.totalPlays }}</span>
        </div>
      </div>

      <div class="data-card">
        <div class="card-title">歌单数量</div>
        <div class="card-value">{{ songlistStats.total }}</div>
        <div class="card-footer">
          <span>总收藏数: {{ songlistStats.totalFavorites }}</span>
        </div>
      </div>


    </div>

    <!-- 图表区域 -->
    <div class="chart-container">
      <div class="chart-panel">
        <div class="chart-title">用户类型分布</div>
        <div class="chart" ref="userChart"></div>
      </div>

      <div class="chart-panel">
        <div class="chart-title">歌曲语言分布</div>
        <div class="chart" ref="languageChart"></div>
      </div>

      <div class="chart-panel">
        <div class="chart-title">歌曲风格分布</div>
        <div class="chart" ref="genreChart"></div>
      </div>

      <div class="chart-panel">
        <div class="chart-title">用户增长趋势</div>
        <div class="chart" ref="growthChart"></div>
      </div>

    </div>

    <!-- 热门歌曲排行榜 -->
    <div class="top-list">
      <div class="list-header">
        <div class="chart-title">热门歌曲TOP10</div>
        <el-button type="primary" size="small">查看全部</el-button>
      </div>
      <el-table :data="topSongs" style="width: 100%">
        <el-table-column label="歌曲">
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <img :src="scope.row.image_url" class="music-cover">
              <div style="margin-left: 15px;">
                <div>{{ scope.row.music_name }}</div>
                <div style="font-size: 12px; color: #909399;">{{ scope.row.singer }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="listen_numb" label="播放量" />
        <el-table-column prop="like_count" label="点赞量"  />
        <el-table-column prop="language" label="语言">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.language }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="genre" label="风格">
          <template #default="scope">
            <el-tag type="success" size="small">{{ scope.row.genre }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时长">
          <template #default="scope">
            {{ formatDuration(scope.row.timelength) }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 热门歌单排行榜 -->
    <div class="top-list">
      <div class="list-header">
        <div class="chart-title">热门歌单TOP10</div>
        <el-button type="primary" size="small">查看全部</el-button>
      </div>
      <el-table :data="topSonglists" style="width: 100%">
        <el-table-column label="歌单信息">
          <template #default="scope">
            <div class="songlist-info">
              <img :src="scope.row.imageUrl" class="songlist-cover">
              <div>
                <div class="songlist-name">{{ scope.row.name }}</div>
                <div class="songlist-about">{{ scope.row.about }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="播放量" width="120">
          <template #default="scope">
            {{ scope.row.totalPlay }}
          </template>
        </el-table-column>
        <el-table-column prop="favoriteCount" label="收藏量" width="120" />
        <el-table-column prop="musicCount" label="歌曲数" width="120" />
        <el-table-column prop="userName" label="创建者" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import request from "@/utils/request";

// 用户统计数据
const userStats = ref({
  total: 42,
  normal: 35,
  singer: 5,
  admin: 2
})
// 歌曲统计数据
const musicStats = ref({
  total: 0,
  totalPlays: 0,
  languages: {},
  genres: {}
})
// 歌单统计数据
const songlistStats = ref({
  total: 0,
  totalFavorites: 0
})

// 热门歌曲数据
const topSongs = ref([])
// 热门歌单数据
const songlists = ref([])
const topSonglists = ref([])

// 图表实例引用
const userChart = ref(null)
const languageChart = ref(null)
const genreChart = ref(null)
const growthChart = ref(null)

// 格式化时长
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs < 10 ? '0' + secs : secs}`
}

// 获取用户数据并分类统计
const fetchUserData = async () => {
  try {
    const response = await request.get('/user/selectAll')
    const users = response.data // 假设返回的是用户数组

    // 初始化统计值
    const stats = {
      total: users.length,
      normal: 0,
      singer: 0,
      admin: 0,
      userGrowthData: {}
    }

    // 根据role分类统计
    users.forEach(user => {
      switch(user.role) {
        case 'USER':
          stats.normal++
          break
        case 'SINGER':
          stats.singer++
          break
        case 'ADMIN':
          stats.admin++
          break
      }
    })

    stats.userGrowthData = processUserGrowth(users)

    userStats.value = stats
    return stats
  } catch (error) {
    console.error('获取用户数据失败:', error)
    return null
  }
}

// 新增用户增长数据处理函数
const processUserGrowth = (users) => {
  // 1. 过滤并排序有效用户
  const validUsers = users
      .filter(user => user.createTime)
      .sort((a, b) => new Date(a.createTime) - new Date(b.createTime))

  // 2. 提取日期范围
  const dates = []
  const dailyCount = {}
  let currentDate = new Date(validUsers[0].createTime)
  const lastDate = new Date(validUsers[validUsers.length - 1].createTime)

  // 生成日期序列
  while (currentDate <= lastDate) {
    const dateStr = currentDate.toISOString().split('T')[0]
    dates.push(dateStr)
    dailyCount[dateStr] = 0
    currentDate.setDate(currentDate.getDate() + 1)
  }

  // 3. 统计每日新增
  validUsers.forEach(user => {
    const date = user.createTime.split(' ')[0]
    dailyCount[date] = (dailyCount[date] || 0) + 1
  })

  // 4. 计算累计用户
  const newUsers = dates.map(date => dailyCount[date] || 0)
  const totalUsers = []
  let total = 0

  newUsers.forEach(count => {
    total += count
    totalUsers.push(total)
  })

  // 5. 格式化日期显示 (MM/DD)
  const formattedDates = dates.map(date => {
    const [year, month, day] = date.split('-')
    return `${month}/${day}`
  })

  return {
    dates: formattedDates,
    newUsers,
    totalUsers
  }
}

// 获取音乐数据并处理统计
const fetchMusicData = async () => {
  try {
    const response = await request.get('/music/selectAll')
    const songs = response.data

    const stats = {
      total: songs.length,
      totalPlays: 0,
      languages: {},
      genres: {}
    }

    songs.forEach(song => {
      // 使用新字段名 listenNumb（原 listen_numb）
      stats.totalPlays += song.listenNumb || 0

      // 从 tags 中提取语言和风格
      if (song.tags) {
        song.tags.forEach(tag => {
          if (tag.type === '语言') {
            const lang = tag.name || '未知'
            stats.languages[lang] = (stats.languages[lang] || 0) + 1
          } else if (tag.type === '风格') {
            const genre = tag.name || '未知'
            stats.genres[genre] = (stats.genres[genre] || 0) + 1
          }
        })
      }
    })

    musicStats.value = stats
    return stats
  } catch (error) {
    console.error('获取音乐数据失败:', error)
    return null
  }
}

// 获取热门歌曲数据
const fetchTopSongs = async () => {
  try {
    const response = await request.get('/music/selectTop?limit=10')
    topSongs.value = response.data.map(song => ({
      music_id: song.musicId,
      music_name: song.musicName,
      singer: song.singerName, // 使用 singerName 字段
      image_url: song.imageUrl,
      listen_numb: song.listenNumb, // 使用新字段名
      like_count: song.likeCount,
      language: song.tags?.find(tag => tag.type === '语言')?.name || '未知',
      genre: song.tags?.find(tag => tag.type === '风格')?.name || '未知',
      timelength: song.timelength
    }))
  } catch (error) {
    console.error('获取热门歌曲失败:', error)
  }
}

// 获取歌单数据并计算播放量
const fetchSonglists = async () => {
  try {
    const response = await request.get('/songlist/selectAll')
    const rawSonglists = response.data
    let totalFavorites = 0 // 初始化总收藏数

    const processed = rawSonglists.map(songlist => {
      let totalPlay = 0
      // 累加每个歌单的收藏量
      totalFavorites += songlist.favoriteCount || 0

      if (songlist.musics) {
        totalPlay = songlist.musics.reduce(
            (sum, music) => sum + (music.listenNumb || 0), 0
        )
      }
      return {
        ...songlist,
        totalPlay,
        musicCount: songlist.musics?.length || 0
      }
    })

    // 更新歌单统计数据
    songlistStats.value = {
      total: processed.length,
      totalFavorites // 设置总收藏数
    }

    processed.sort((a, b) => b.totalPlay - a.totalPlay)
    songlists.value = processed
    topSonglists.value = processed.slice(0, 10)
    return processed
  } catch (error) {
    console.error('获取歌单数据失败:', error)
    return []
  }
}

// 初始化图表
const initCharts = async () => {
  // 获取统计数据
  const [userStats, musicStatsData,songlistsData ] = await Promise.all([
    fetchUserData(),
    fetchMusicData(),
    fetchSonglists()
  ]);
  const userGrowthData = userStats.userGrowthData
  await fetchTopSongs();
  // 用户类型分布图
  const userChartInstance = echarts.init(userChart.value)
  userChartInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 10,
      data: ['普通用户', '歌手', '管理员']
    },
    series: [{
      name: '用户类型',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '18',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        { value: userStats.normal, name: '普通用户' },
        { value: userStats.singer, name: '歌手' },
        { value: userStats.admin, name: '管理员' }
      ]
    }]
  })

  // 歌曲语言分布图
  const languageChartInstance = echarts.init(languageChart.value)
  languageChartInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 10,
      data: Object.keys(musicStatsData.languages)
    },
    color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'],
    series: [{
      name: '歌曲语言',
      type: 'pie',
      radius: '60%',
      data: Object.entries(musicStatsData.languages).map(([name, value]) => ({
        value,
        name
      }))
    }]
  })

  // 歌曲风格分布图
  const genreChartInstance = echarts.init(genreChart.value)
  genreChartInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 10,
      data: Object.keys(musicStatsData.genres)
    },
    color: ['#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
    series: [{
      name: '歌曲风格',
      type: 'pie',
      radius: '60%',
      data: Object.entries(musicStatsData.genres).map(([name, value]) => ({
        value,
        name
      }))
    }]
  })

  // ===== 用户增长趋势图 =====
  const growthChartInstance = echarts.init(growthChart.value)
  growthChartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        let result = params[0].name + '<br/>'
        params.forEach(item => {
          result += `${item.marker} ${item.seriesName}: ${item.value}<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['总用户数', '新增用户'],
      bottom: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: userGrowthData.dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '总用户数',
        type: 'line',
        smooth: true,
        data: userGrowthData.totalUsers,
        lineStyle: { width: 3 },
        symbolSize: 8
      },
      {
        name: '新增用户',
        type: 'bar',
        data: userGrowthData.newUsers,
        itemStyle: { color: '#91cc75' }
      }
    ]
  })

  // 响应式调整
  const handleResize = () => {
    userChartInstance.resize()
    languageChartInstance.resize()
    genreChartInstance.resize()
    growthChartInstance.resize()
  }

  window.addEventListener('resize', handleResize)

  // 返回清理函数
  return () => {
    window.removeEventListener('resize', handleResize)
    userChartInstance.dispose()
    languageChartInstance.dispose()
    genreChartInstance.dispose()
    growthChartInstance.dispose()
  }
}

onMounted(() => {
  initCharts()
})
</script>

<style scoped>
.manager-home {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.data-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 25px;
}

.data-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.data-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.15);
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 10px;
}

.card-footer {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #67c23a;
}

.chart-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 25px;
}

.chart-panel {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
  color: #303133;
}

.chart {
  height: 300px;
  width: 100%;
}

.top-list {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 25px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.music-cover {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  object-fit: cover;
}

.songlist-info {
  display: flex;
  align-items: center;
}

.songlist-cover {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  margin-right: 15px;
  object-fit: cover;
}

.songlist-name {
  font-weight: 500;
}

.songlist-about {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.stats-highlight {
  display: inline-block;
  background: #ecf5ff;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  margin-left: 10px;
}

.up-trend::before {
  content: "↑";
  margin-right: 3px;
}

.down-trend::before {
  content: "↓";
  margin-right: 3px;
}
</style>