<template>
  <div>
    <!-- 查询区 -->
    <div class="card" style="margin-bottom: 10px;">
      <el-input
          prefix-icon="Search"
          style="width: 300px; margin-right: 10px"
          placeholder="请输入公告标题"
          v-model="data.title"
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <!-- 表格区 -->
    <div class="card" style="margin-bottom: 10px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增公告</el-button>
      </div>
      <el-table :data="data.tableData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="msg" label="内容" />
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column prop="userId" label="发布人ID" width="120" />
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button type="primary" :icon="Edit" circle @click="handleEdit(scope.row)" />
            <el-button type="danger" :icon="Delete" circle @click="del(scope.row.id)" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="card" v-if="data.total">
      <el-pagination
          background
          layout="prev, pager, next"
          @current-change="load"
          :page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
      />
    </div>

    <!-- 弹窗：新增 / 编辑 -->
    <el-dialog v-model="data.formVisible" title="公告信息" width="40%" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 40px">
        <el-form-item label="标题">
          <el-input v-model="data.form.title" autocomplete="off" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
              type="textarea"
              v-model="data.form.msg"
              :rows="6"
              placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="发布人ID">
          <el-input v-model="data.form.userId" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit } from '@element-plus/icons-vue'

const data = reactive({
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 5,
  formVisible: false,
  form: {},
  title: ''
})

// 查询公告分页列表
const load = () => {
  request.get('/message/selectPage', {
    params: {
      title: data.title,
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data.total
  })
}

load()

const reset = () => {
  data.title = ''
  load()
}

const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const save = () => {
  request.request({
    method: data.form.id ? 'PUT' : 'POST',
    url: data.form.id ? '/message/update' : '/message/add',
    data: data.form
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，是否确认删除？', '确认删除', { type: 'warning' }).then(() => {
    request.delete(`/message/delete/${id}`).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  })
}
</script>
