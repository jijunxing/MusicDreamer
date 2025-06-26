<template>
  <div class="log-info">
    <h2>日志管理</h2>
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="用户名">
        <el-input v-model="searchForm.userName" placeholder="输入用户名" />
      </el-form-item>
      <el-form-item label="歌曲名">
        <el-input v-model="searchForm.musicName" placeholder="输入歌曲名" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchLogs">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="logs" style="width: 100%" v-loading="loading">
      <el-table-column prop="createDate" label="创建时间" width="180" />
      <el-table-column prop="userName" label="用户名" width="120" />
      <el-table-column prop="musicName" label="歌曲名" width="120" />
      <el-table-column prop="doSome" label="操作记录" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="mini" @click="editLog(scope.row)">编辑</el-button>
          <el-button size="mini" type="danger" @click="deleteLog(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :visible.sync="dialogVisible" title="编辑日志">
      <el-form :model="editForm">
        <el-form-item label="操作记录">
          <el-input v-model="editForm.doSome" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="updateLog">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const logs = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editForm = reactive({})
const searchForm = reactive({ userName: '', musicName: '' })

const fetchLogs = async () => {
  loading.value = true
  const { data } = await request.get('/log/selectAll', { params: searchForm })
  logs.value = data.data || []
  loading.value = false
}

const resetSearch = () => {
  searchForm.userName = ''
  searchForm.musicName = ''
  fetchLogs()
}

const editLog = (row) => {
  Object.assign(editForm, row)
  dialogVisible.value = true
}

const updateLog = async () => {
  await request.put('/log/update', editForm)
  ElMessage.success('更新成功')
  dialogVisible.value = false
  fetchLogs()
}

const deleteLog = async (row) => {
  await request.delete('/log/delete', { params: {
    createDate: row.createDate,
    userName: row.userName,
    musicName: row.musicName
  }})
  ElMessage.success('删除成功')
  fetchLogs()
}

onMounted(fetchLogs)
</script>

<style scoped>
.log-info {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style> 