<template>
  <div>

    <div class="card" style="margin-bottom: 10px;">
      <el-input prefix-icon="Search" style="width: 300px; margin-right: 10px" placeholder="请输入用户名查询" v-model="data.username"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 10px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70"/>
        <el-table-column prop="username" label="用户名"/>
        <el-table-column prop="avatar" label="头像">
          <template v-slot="scope">
            <img v-if="scope.row.avatar" :src="scope.row.avatar" alt="" style="width: 50px; height: 50px; border-radius: 50%">
          </template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="scope">
            <el-tag type="primary" v-if="scope.row.activation === 0">{{"冻结"}}</el-tag>
            <el-tag type="success" v-if="scope.row.activation === 1">{{"激活"}}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号"/>
        <el-table-column prop="email" label="邮箱"/>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="success" :icon="Edit" circle @click="handleEdit(scope.row)" />
            <el-button type="primary" :icon="Lock" v-if="scope.row.activation === 1" circle @click="frozenAccount(scope.row)" />
            <el-button type="primary" :icon="Unlock" v-if="scope.row.activation === 0" circle @click="unfrozenAccount(scope.row)" />
            <el-button type="danger" :icon="Delete" circle @click="del(scope.row.id)" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.total">
      <el-pagination background layout="prev, pager, next" @current-change="load" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <el-dialog v-model="data.formVisible" title="信息" width="25%" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="用户名">
          <el-input v-model="data.form.username" autocomplete="off" :disabled="!!data.form.id" />
        </el-form-item>
        <el-form-item label="头像">
          <el-upload action="http://localhost:9090/files/upload" :on-success="handleFileUpload">
            <el-button type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="性别">
          <el-select
              v-model="data.form.sex"
              filterable
              placeholder="Select"
              style="width: 240px"
          >
            <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="data.form.phone" autocomplete="off" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="data.form.email" autocomplete="off" />
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
import {reactive} from "vue"
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit, Lock, Unlock} from '@element-plus/icons-vue';

const data = reactive({
  tableData: [
  ],
  total: 0,
  pageNum: 1,  // 当前的页码
  pageSize: 5,  // 每页的个数
  formVisible: false,
  form: {},
  username: '',
  sex: '',
  role: 'USER'
})

const load = () => {
  request.get('/user/selectPage', {
    params:{
      username: data.username,
      role: data.role,
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
  data.username = null;
  load();
}

const handleAdd = () =>{
  data.form = {};//初始化
  data.formVisible = true;//显示
}

//保存数据
const save = () => {
  request.request({
    method: data.form.id ? 'PUT' : 'POST',
    url: data.form.id ? '/user/update' : '/user/add',
    data: data.form
  }).then(res => {
    if(res.code === '200'){
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true;
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复,您确认要删除吗？', '确认删除', {type:'warning'}).then(res => {
    request.delete('user/delete/'+id).then(res => {
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

const frozenAccount = (row) => {
  row.activation = 0
  request.request({
    method: 'PUT',
    url: 'user/update',
    data: row
  }).then(res => {
    if(res.code === '200'){
      ElMessage.success('冻结成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const unfrozenAccount = (row) => {
  request.request({
    method: 'PUT',
    url: 'user/update',
    data: row
  }).then(res => {
    if(res.code === '200'){
      ElMessage.success('冻结成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleFileUpload = (file) => {
  data.form.avatar = file.data
}

const options = [
  {
    value: 'male',
    label: '男',
  },
  {
    value: 'female',
    label: '女',
  },
  {
    value: 'secret',
    label: '保密',
  }
]

</script>