<template>
  <div>
    <div class="card" style="margin-bottom: 10px; display: flex; gap: 10px; align-items: center;">
      <el-input
          prefix-icon="Search"
          style="width: 300px"
          placeholder="请输入标题"
          v-model="data.keyword"
          clearable
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 10px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" style="width: 99%" v-loading="data.loading">
        <el-table-column prop="id" label="ID" width="60"/>
        <el-table-column prop="title" label="标题"  width="180"/>
        <el-table-column prop="imageUrl" label="轮播图">
          <template v-slot="scope">
            <el-image
                v-if="scope.row.imageUrl"
                :src="scope.row.imageUrl"
                :preview-src-list="[scope.row.imageUrl]"
                :preview-teleported="true"
                append-to-body
                style="width: 100px; height: 50px; border-radius: 10%; cursor: pointer"
                fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="relatedType" label="关联类型" />
        <el-table-column prop="relatedId" label="关联项ID" />
        <el-table-column prop="userName" label="创建人" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" :icon="Edit" circle @click="handleEdit(scope.row)" />
            <el-button type="danger" :icon="Delete" circle @click="del(scope.row.id)" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="data.formVisible" title="轮播图信息" width="30%">
      <el-form :model="data.form" label-width="120px" class="form-container">
        <el-form-item label="标题">
          <el-input v-model="data.form.title" autocomplete="off" placeholder="请输入轮播图标题" style="width: 250px" />
        </el-form-item>
        <el-form-item label="轮播图">
          <UploadFile v-model="data.form.imageUrl" type="image" placeholder="请上传轮播图" style="width: 250px" />
        </el-form-item>
        <el-form-item label="关联类型">
          <el-select v-model="data.form.relatedType" placeholder="请选择关联类型" style="width: 250px">
            <el-option label="歌手" value="SINGER"></el-option>
            <el-option label="歌单" value="SONGLIST"></el-option>
            <el-option label="歌曲" value="SONG"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关联项ID">
          <el-input v-model="data.form.relatedId" autocomplete="off" placeholder="请输入关联项ID" style="width: 250px" />
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

import {reactive, onMounted} from "vue"
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";

const data = reactive({
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 6,
  formVisible: false,
  form: {},
  keyword: '',
  loading: true
})

onMounted(() => {
  load()
})

const load = () => {
  request.get('/carousel/selectPage', {
    params:{
      title: data.keyword,
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data.total
    setTimeout(() => { data.loading = false }, 250);
  })
}

const reset = () => {
  data.keyword = null;
  load();
}

const save = () => {
  data.form.userId = JSON.parse(localStorage.getItem('currentUser')).id
  request.request({
    method: data.form.id ? 'PUT' : 'POST',
    url: data.form.id ? '/carousel/update' : '/carousel/add',
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

const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = { ...row }
  data.formVisible = true
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复,您确认要删除吗？', '确认删除', {type:'warning'}).then(res => {
    request.delete('/carousel/delete/'+id).then(res => {
      if(res.code === '200'){
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.log(err)
  })
}

</script>

<style scoped>
.form-container {
  padding: 15px;
}

.el-form-item {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 15px;
  border-top: 1px solid #ebeef5;
}
</style> 