<template>
  <div>
    <!-- 查询区域 -->
    <div class="card" style="margin-bottom: 10px; display: flex; gap: 10px; align-items: center;">
      <el-input
          prefix-icon="Search"
          style="width: 300px"
          placeholder="请输入公告标题关键词"
          v-model="data.title"
          clearable
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>

    <!-- 新增按钮 -->
    <div class="card" style="margin-bottom: 10px">
      <el-button type="primary" @click="handleAdd">新增公告</el-button>
    </div>

    <!-- 公告列表表格 -->
    <el-table :data="data.tableData" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="公告标题" />
      <el-table-column label="公告内容">
        <template #default="scope">
          <span>{{ truncateText(scope.row.msg, 30) }}</span>
          <el-button
              type="text"
              size="small"
              @click="showDetail(scope.row.msg)"
              style="margin-left: 6px; padding: 0"
          >
            查看详情
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column prop="userName" label="创建人" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button type="primary" icon="Edit" circle @click="handleEdit(scope.row)" />
          <el-button type="danger" icon="Delete" circle @click="del(scope.row.id)" />
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="card" v-if="data.total">
      <el-pagination
          background
          layout="prev, pager, next"
          @current-change="handlePageChange"
          :page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="data.formVisible" title="公告信息" width="40%" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="公告标题" :rules="[{ required: true, message: '请输入公告标题', trigger: 'blur' }]">
          <el-input v-model="data.form.title" autocomplete="off" />
        </el-form-item>
        <el-form-item label="公告内容" :rules="[{ required: true, message: '请输入公告内容', trigger: 'blur' }]">
          <el-input type="textarea" v-model="data.form.msg" :rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="data.detailVisible" title="公告详情" width="40%">
      <div style="white-space: pre-wrap; word-break: break-word;">
        {{ data.detailContent }}
      </div>
      <template #footer>
        <el-button type="primary" @click="data.detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, onMounted} from "vue";
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";

const data = reactive({
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  formVisible: false,
  form: {},
  title: "",
  detailVisible: false,
  detailContent: ""
});

// 截断文字工具函数
const truncateText = (text, length) => {
  if (!text) return "";
  return text.length > length ? text.slice(0, length) + "..." : text;
};

const showDetail = (content) => {
  data.detailContent = content || "";
  data.detailVisible = true;
};

const load = () => {
  request
      .get("/notice/selectPage", {
        params: {
          title: data.title,
          pageNum: data.pageNum,
          pageSize: data.pageSize
        }
      })
      .then((res) => {
        data.tableData = res.data?.list || [];
        data.total = res.data.total;
      });
};

const reset = () => {
  data.title = "";
  data.pageNum = 1;
  load();
};

const handleAdd = () => {
  data.form = {};
  data.formVisible = true;
};

const handleEdit = (row) => {
  data.form = {...row};
  data.formVisible = true;
};

const save = () => {
  const url = data.form.id ? "/notice/update" : "/notice/add";
  data.form.userId = JSON.parse(localStorage.getItem('currentUser')).id
  request
      .request({
        method: data.form.id ? "PUT" : "POST",
        url,
        data: data.form
      })
      .then((res) => {
        if (res.code === "200") {
          ElMessage.success("保存成功");
          data.formVisible = false;
          load();
        } else {
          ElMessage.error(res.msg);
        }
      });
};

const del = (id) => {
  ElMessageBox.confirm("删除后数据无法恢复，您确认要删除吗？", "确认删除", {
    type: "warning"
  })
      .then(() => {
        request.delete(`/notice/delete/${id}`).then((res) => {
          if (res.code === "200") {
            ElMessage.success("删除成功");
            load();
          } else {
            ElMessage.error(res.msg);
          }
        });
      })
      .catch(() => {
      });
};

const handlePageChange = (page) => {
  data.pageNum = page;
  load();
};

onMounted(() => {
  load();
});
</script>
