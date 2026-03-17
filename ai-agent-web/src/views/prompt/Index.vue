<template>
  <div class="page-container">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增提示词</el-button>
        <el-button @click="loadData">刷新</el-button>
      </div>
      
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="promptKey" label="Key" width="150" />
        <el-table-column prop="promptName" label="名称" width="150" />
        <el-table-column prop="promptType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeText(row.promptType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isActive === 1 ? 'success' : 'danger'">
              {{ row.isActive === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              :type="row.isActive === 1 ? 'warning' : 'success'" 
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.isActive === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="70%"
      destroy-on-close
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="Key">
          <el-input v-model="form.promptKey" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.promptName" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.promptType">
            <el-option label="代码审查" value="code_review" />
            <el-option label="代码优化" value="optimize" />
            <el-option label="知识问答" value="learn" />
            <el-option label="智能聊天" value="chat" />
            <el-option label="审批流程" value="approval" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="系统提示词">
          <el-input v-model="form.systemPrompt" type="textarea" :rows="10" />
        </el-form-item>
        <el-form-item label="用户模板">
          <el-input v-model="form.userPromptTemplate" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.isActive" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const form = reactive({
  id: null,
  promptKey: '',
  promptName: '',
  promptType: '',
  systemPrompt: '',
  userPromptTemplate: '',
  description: '',
  isActive: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/prompt/list')
    list.value = res.data || []
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    promptKey: '',
    promptName: '',
    promptType: 'code_review',
    systemPrompt: '',
    userPromptTemplate: '',
    description: '',
    isActive: 1
  })
  dialogTitle.value = '新增提示词'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑提示词'
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (form.id) {
      await request.put('/prompt', form)
    } else {
      await request.post('/prompt', form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const toggleStatus = async (row) => {
  row.isActive = row.isActive === 1 ? 0 : 1
  try {
    await request.put('/prompt', row)
    ElMessage.success(row.isActive === 1 ? '已启用' : '已禁用')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该提示词?', '提示', { type: 'warning' })
    await request.delete(`/prompt/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getTypeText = (type) => {
  const map = {
    code_review: '代码审查',
    optimize: '代码优化',
    learn: '知识问答',
    chat: '智能聊天',
    approval: '审批流程'
  }
  return map[type] || type
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 15px;
}
</style>
