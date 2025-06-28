<template>
  <div>
    <div class="card" style="margin-bottom: 10px;">
      <el-input prefix-icon="Search" style="width: 300px; margin-right: 10px" placeholder="请输入歌曲名查询" v-model="data.musicName"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 10px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" style="width: 100%">
        <el-table-column prop="musicId" label="ID" width="70"/>
        <el-table-column prop="musicName" label="歌曲名"/>
        <el-table-column prop="imageUrl" label="封面">
          <template v-slot="scope">
            <el-image
                v-if="scope.row.imageUrl"
                :src="scope.row.imageUrl"
                :preview-src-list="[scope.row.imageUrl]"
                :preview-teleported="true"
                append-to-body
                style="width: 50px; height: 50px; border-radius: 10%; cursor: pointer"
                fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签">
          <template v-slot="scope">
            <el-tag
                v-for="tag in scope.row.tags"
                :key="tag.id"
                style="margin: 2px"
                type="info"
            >
              {{ tag.name }}（{{ tag.type }}）
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="singerName" label="歌手用户名"/>
        <el-table-column prop="listenNumb" label="播放量"/>
        <el-table-column label="状态">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.activation === 1">激活</el-tag>
            <el-tag type="primary" v-if="scope.row.activation === 0">冻结</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button type="primary" :icon="VideoPlay" circle @click="playNow(scope.row)" />
            <el-button type="warning" icon="Plus" circle @click="addToQueue(scope.row)" />
            <el-button type="primary" :icon="Edit" circle @click="handleEdit(scope.row)" />
            <el-button type="danger" :icon="Delete" circle @click="del(scope.row.musicId)" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.total">
      <el-pagination background layout="prev, pager, next" @current-change="load" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <el-dialog v-model="data.formVisible" title="音乐信息" width="50%">
      <el-form :model="data.form" label-width="100px">
        <div style="display: flex; gap: 20px;">
          <!-- 左列 -->
          <div style="flex: 1;">
            <el-form-item label="歌曲名">
              <el-input v-model="data.form.musicName" autocomplete="off" />
            </el-form-item>
            <el-form-item label="封面">
              <UploadFile v-model="data.form.imageUrl" type="image" />
            </el-form-item>
            <el-form-item label="音乐文件">
              <UploadFile v-model="data.form.musicUrl" type="music" />
            </el-form-item>
            <el-form-item label="歌手ID">
              <el-input v-model="data.form.fromSinger" autocomplete="off" />
            </el-form-item>
            <el-form-item label="时长(秒)">
              <el-input v-model="data.form.timelength" autocomplete="off" type="number" />
            </el-form-item>
            <el-form-item label="标签">
              <el-select
                  v-model="data.form.tagIds"
                  multiple
                  placeholder="请选择标签"
                  filterable
                  style="width: 100%"
              >
                <el-option
                    v-for="tag in data.tagList"
                    :key="tag.id"
                    :label="tag.name + '（' + tag.type + '）'"
                    :value="tag.id"
                />
              </el-select>
            </el-form-item>
          </div>

          <!-- 右列 -->
          <div style="flex: 1;">
            <el-form-item label="歌词">
              <el-input
                  type="textarea"
                  v-model="data.form.lyric"
                  :rows="10"
                  placeholder="请输入歌词内容或路径"
              />
            </el-form-item>
          </div>
        </div>
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
import {Delete, Edit, VideoPlay, Plus} from '@element-plus/icons-vue';
import UploadFile from '@/components/UploadFile.vue'
import { player } from '@/utils/player'

const playNow = (row) => {
  player.play(row)
}

const addToQueue = (row) => {
  player.addToQueue(row)
}

const data = reactive({
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 5,
  formVisible: false,
  form: {
    tagIds: []
  },
  musicName: '',
  tagList: []
})

onMounted(() => {
  load()
  loadTags()
})

const loadTags = () => {
  request.get('/tag/list').then(res => {
    data.tagList = res.data
  })
}

const load = () => {
  request.get('/music/selectPage', {
    params:{
      musicName: data.musicName,
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data.total
  })
}

const reset = () => {
  data.musicName = null;
  load();
}

const handleAdd = () =>{
  data.form = { tagIds: [] };
  data.formVisible = true;
}

const save = () => {
  const payload = {
    ...data.form,
    tags: data.form.tagIds.map(id => ({ id }))
  }
  request.request({
    method: payload.musicId ? 'PUT' : 'POST',
    url: payload.musicId ? '/music/update' : '/music/add',
    data: payload
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

const handleEdit = (row) => {
  data.form = {
    ...row,
    tagIds: row.tags?.map(t => t.id) || []
  }
  data.formVisible = true
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复,您确认要删除吗？', '确认删除', {type:'warning'}).then(res => {
    request.delete('/music/delete/'+id).then(res => {
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
