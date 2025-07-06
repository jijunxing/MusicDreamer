<template>
  <div>
    <div class="card" style="margin-bottom: 10px; display: flex; gap: 10px; align-items: center;">
      <el-input
          prefix-icon="Search"
          style="width: 300px"
          placeholder="请输入歌名或歌手名"
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
      <el-table :data="data.tableData" style="width: 100%" v-loading="data.loading">
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
              {{ tag.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="singerName" label="歌手用户名"/>
        <el-table-column prop="timelength" label="时长(秒)"/>
        <el-table-column prop="listenNumb" label="播放量"/>
        <el-table-column label="状态">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.activation === 1">激活</el-tag>
            <el-tag type="primary" v-if="scope.row.activation === 0">冻结</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340">
          <template #default="scope">
            <el-button type="primary" :icon="VideoPlay" circle @click="playNow(scope.row)" />
            <el-button type="warning" icon="Plus" circle @click="addToQueue(scope.row)" />
            <el-button type="primary" :icon="Edit" circle @click="handleEdit(scope.row)" />
            <el-button type="danger" :icon="Delete" circle @click="del(scope.row.musicId)" />
            <el-button v-if="scope.row.activation === 1" type="primary" @click="freezeMusic(scope.row)">冻结</el-button>
            <el-button v-if="scope.row.activation === 0" type="success" @click="unfreezeMusic(scope.row)">解冻</el-button>
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
            <el-form-item label="音频(.mp3)">
              <UploadFile v-model="data.form.musicUrl" type="music" />
            </el-form-item>
            <el-form-item label="歌词(.lrc)">
              <UploadFile v-model="data.form.lyricsUrl" type="lrc" />
            </el-form-item>
          </div>

          <!-- 右列 -->
          <div style="flex: 1;">
            <el-form-item label="歌手ID">
              <el-input v-model="data.form.fromSinger" autocomplete="off" />
            </el-form-item>

            <template v-for="(options, type) in groupedTags" :key="type">
              <el-form-item :label="type">
                <el-select v-model="data.selectedTags[type]" placeholder="请选择" clearable>
                  <el-option
                      v-for="tag in options"
                      :key="tag.id"
                      :label="tag.name"
                      :value="tag.id"
                  />
                </el-select>
              </el-form-item>
            </template>
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
import {reactive, onMounted, computed} from "vue"
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit, VideoPlay, Plus} from '@element-plus/icons-vue';
import { player } from '@/utils/player'

const playNow = (row) => {
  if (row.activation === 0) {
    ElMessage.warning('该歌曲已被冻结，无法播放！')
    return
  }
  player.play(row)
}

const addToQueue = (row) => {
  player.addToQueue(row)
  ElMessage.success("成功加入播放列表")
}

const data = reactive({
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 6,
  formVisible: false,
  form: {},
  keyword: '',
  tagList: [],
  selectedTags: {},
  loading: true
})

const groupedTags = computed(() => {
  const groups = {}
  data.tagList.forEach(tag => {
    if (!groups[tag.type]) groups[tag.type] = []
    groups[tag.type].push(tag)
  })
  return groups
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
      musicName: data.keyword,
      singerName: data.keyword,
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

const handleAdd = () =>{
  data.form = {}
  data.selectedTags = {}
  data.formVisible = true
}

const save = () => {
  const tagIds = Object.values(data.selectedTags).filter(Boolean)
  const payload = {
    ...data.form,
    tags: tagIds.map(id => ({ id }))
  }
  request.request({
    method: payload.musicId ? 'PUT' : 'POST',
    url: payload.musicId ? '/music/update' : '/music/addWithTags',
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
  data.form = { ...row }
  data.selectedTags = {}
  row.tags?.forEach(tag => {
    data.selectedTags[tag.type] = tag.id
  })
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

const freezeMusic = (row) => {
  request.put(`/music/freeze/${row.musicId}`).then(res => {
    if (res.code === '200') {
      ElMessage.success('冻结成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const unfreezeMusic = (row) => {
  request.put(`/music/unfreeze/${row.musicId}`).then(res => {
    if (res.code === '200') {
      ElMessage.success('解冻成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}
</script>