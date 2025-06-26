<template>
  <div class="songlist-info">
    <h2>歌单管理</h2>
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="歌单名">
        <el-input v-model="searchForm.name" placeholder="输入歌单名" />
      </el-form-item>
      <el-form-item label="用户ID">
        <el-input v-model="searchForm.user" placeholder="输入用户ID" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchSongLists">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="openAddDialog">新增歌单</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="songLists" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="歌单名" width="150" />
      <el-table-column prop="user" label="用户ID" width="100" />
      <el-table-column prop="tags" label="标签" width="120" />
      <el-table-column prop="createDate" label="创建时间" width="150" />
      <el-table-column prop="image" label="封面" width="120">
        <template #default="scope">
          <img :src="scope.row.image" alt="封面" style="width: 60px; height: 60px; object-fit: cover;" />
        </template>
      </el-table-column>
      <el-table-column prop="message" label="简介" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="mini" @click="editSongList(scope.row)">编辑</el-button>
          <el-button size="mini" type="danger" @click="deleteSongList(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :visible.sync="dialogVisible" :title="dialogTitle">
      <el-form :model="editForm">
        <el-form-item label="歌单名">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="editForm.tags" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="editForm.message" />
        </el-form-item>
        <el-form-item label="封面URL">
          <UploadFile v-model="editForm.image" type="image" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="editForm.user" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveSongList">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import UploadFile from '@/components/UploadFile.vue'

const songLists = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const editForm = reactive({})
const searchForm = reactive({ name: '', user: '' })

const fetchSongLists = async () => {
  loading.value = true
  const { data } = await request.get('/songlist/selectAll', { params: searchForm })
  songLists.value = data.data || []
  loading.value = false
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.user = ''
  fetchSongLists()
}

const openAddDialog = () => {
  Object.assign(editForm, { id: null, name: '', tags: '', message: '', image: '', user: '' })
  dialogTitle.value = '新增歌单'
  dialogVisible.value = true
}

const editSongList = (row) => {
  Object.assign(editForm, row)
  dialogTitle.value = '编辑歌单'
  dialogVisible.value = true
}

const saveSongList = async () => {
  if (editForm.id) {
    await request.put('/songlist/update', editForm)
    ElMessage.success('更新成功')
  } else {
    await request.post('/songlist/add', editForm)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchSongLists()
}

const deleteSongList = async (row) => {
  await request.delete(`/songlist/delete/${row.id}`)
  ElMessage.success('删除成功')
  fetchSongLists()
}

onMounted(fetchSongLists)
</script>

<style scoped>
.songlist-info {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style> 