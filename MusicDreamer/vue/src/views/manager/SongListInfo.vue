<template>
  <div>
    <div class="card" style="margin-bottom: 10px; display: flex; gap: 10px; align-items: center;">
      <el-input
          prefix-icon="Search"
          style="width: 300px"
          placeholder="请输入歌单名或用户ID"
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
      <el-table :data="data.tableData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="歌单名" min-width="120" />
        <el-table-column prop="imageUrl" label="封面" width="80">
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
        <el-table-column prop="about" label="简介" min-width="200" />
        <el-table-column prop="musicCount" label="音乐数" width="80" />
        <el-table-column prop="user" label="用户ID" width="100" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" min-width="280">
          <template #default="scope">
            <el-button type="primary" @click="viewMusics(scope.row)">查看歌曲</el-button>
            <el-button type="success" :icon="VideoPlay" circle @click="playSonglist(scope.row)"></el-button>
            <el-button type="primary" :icon="Edit" circle @click="handleEdit(scope.row)" />
            <el-button type="danger" :icon="Delete" circle @click="del(scope.row.id)" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.total">
      <el-pagination background layout="prev, pager, next" @current-change="load" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <el-dialog v-model="data.formVisible" title="歌单信息" width="30%">
      <el-form :model="data.form" label-width="100px">
        <el-form-item label="歌单名">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="封面">
          <UploadFile v-model="data.form.imageUrl" type="image" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="data.form.about" autocomplete="off" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="data.form.user" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="data.musicDialogVisible" title="歌单歌曲" width="60%">
      <el-button type="primary" style="margin-bottom: 10px;" @click="openAddMusicDialog">添加歌曲</el-button>

      <el-table :data="data.currentMusics" style="width: 100%">
        <el-table-column prop="musicId" label="ID"/>
        <el-table-column prop="musicName" label="歌曲名"/>
        <el-table-column prop="singerName" label="歌手"/>
        <el-table-column prop="listenNumb" label="播放量"/>
        <el-table-column prop="activation" label="状态">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.activation === 1">激活</el-tag>
            <el-tag type="primary" v-if="scope.row.activation === 0">冻结</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="success" size="small" :icon="VideoPlay" circle @click="playMusic(scope.row)"></el-button>
            <el-button type="warning" size="small" :icon="Plus" circle @click="addToQueue(scope.row)"></el-button>
            <el-button type="danger" size="small" :icon="Delete" circle @click="removeMusic(scope.row.musicId)"></el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.musicDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="data.addMusicDialogVisible" title="添加歌曲到歌单" width="60%">
      <!-- 搜索栏 -->
      <div style="display: flex; gap: 10px; margin-bottom: 10px;">
        <el-input
            v-model="data.searchKeyword"
            placeholder="请输入歌曲名或歌手"
            clearable
            style="flex: 1"
        />
        <el-button type="primary" @click="loadSelectableMusics">搜索</el-button>
      </div>

      <!-- 可选歌曲列表 -->
      <el-table
          :data="data.selectableMusics"
          style="width: 100%"
          height="300"
          @selection-change="handleSelectChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="musicName" label="歌曲名" />
        <el-table-column prop="singerName" label="歌手" />
      </el-table>

      <template #footer>
        <el-button @click="data.addMusicDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addSelectedMusicsToSonglist">批量添加</el-button>
      </template>
    </el-dialog>


  </div>
</template>

<script setup>
import {reactive, onMounted} from "vue"
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit, Plus, VideoPlay} from '@element-plus/icons-vue';
import UploadFile from "@/components/UploadFile.vue";
import {player} from "@/utils/player";

const data = reactive({
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  formVisible: false,
  form: {},
  keyword: '',
  musicDialogVisible: false,
  currentMusics: [],
  musicDialogSonglistId : null,
  addMusicDialogVisible : false,
  selectableMusics:[],
  selectedMusicIds:[],
  searchKeyword : '',
  allMusics: [],
  selectedMusicId: null
})

onMounted(() => {
  load()
})

const load = () => {
  request.get('/songlist/selectPage', {
    params:{
      name: data.keyword,
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data.total
  })
}

const reset = () => {
  data.keyword = '';
  load();
}

const handleAdd = () =>{
  data.form = {}
  data.formVisible = true
}

const save = () => {
  console.log(data.form)
  request.request({
    method: data.form.id ? 'PUT' : 'POST',
    url: data.form.id ? '/songlist/update' : '/songlist/add',
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

const handleEdit = (row) => {
  data.form = { ...row }
  data.formVisible = true
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复,您确认要删除吗？', '确认删除', {type:'warning'}).then(res => {
    request.delete('/songlist/delete/'+id).then(res => {
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

// 移除歌曲
const removeMusic = (musicId) => {
  request.delete(`/songlist/removeMusic`, {
    params: {
      listId: data.musicDialogSonglistId,
      musicId: musicId
    }
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('移除成功')
      loadSonglistMusics(data.musicDialogSonglistId)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 打开“查看歌曲”弹窗，并记录当前歌单ID
const viewMusics = (row) => {
  data.musicDialogSonglistId = row.id
  data.currentMusics = row.musics
  data.musicDialogVisible = true
}

// 打开添加歌曲弹窗
const openAddMusicDialog = () => {
  data.searchKeyword = ''
  data.selectedMusicIds = []
  loadSelectableMusics()
  data.addMusicDialogVisible = true
}

// 加载当前歌单可添加的歌曲（排除已有的）
const loadSelectableMusics = () => {
  request.get('/songlist/selectForSonglist', {
    params: {
      keyword: data.searchKeyword,
      excludeSonglistId: data.musicDialogSonglistId
    }
  }).then(res => {
    data.selectableMusics = res.data || []
  })
}

// 表格多选监听
const handleSelectChange = (selection) => {
  data.selectedMusicIds = selection.map(item => item.musicId)
}

//批量添加歌曲
const addSelectedMusicsToSonglist = () => {
  if (!data.selectedMusicIds.length) {
    ElMessage.warning('请先选择歌曲')
    return
  }

  request.post('/songlist/addMusics',{
      listId: data.musicDialogSonglistId,
      musicIds: data.selectedMusicIds
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('添加成功')
      data.addMusicDialogVisible = false
      loadSonglistMusics(data.musicDialogSonglistId)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadSonglistMusics = (listId) => {
  if (!listId) return

  request.get(`/songlist/selectById/${listId}`).then(res => {
    if (res.code === '200') {
      data.currentMusics = res.data.musics || []
    } else {
      ElMessage.error('加载歌曲失败')
    }
  })
}

const playMusic = (row) => {
  player.play(row)
}

const addToQueue = (row) => {
  player.addToQueue(row)
}

const playSonglist = (row) => {
  if(row.musics.length === 0){
    ElMessage.warning("歌单为空")
    return
  }
  player.playList(row.musics)
  ElMessage.success('开始播放歌单全部歌曲')
}
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
</style>