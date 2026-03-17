<template>
  <div class="page-container">
    <el-card>
      <el-table :data="list" stripe>
        <el-table-column prop="flowNo" label="流程编号" width="200" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="row.priority === 3 ? 'danger' : row.priority === 2 ? 'warning' : 'info'">
              {{ row.priority === 3 ? '高' : row.priority === 2 ? '中' : '低' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending'" type="success" size="small" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button v-if="row.status === 'pending'" type="danger" size="small" @click="handleReject(row)">
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const list = ref([])

const loadData = async () => {
  try {
    const res = await request.get('/approval/list')
    list.value = res.data || []
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.prompt('请输入审批意见', '审批通过', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.post(`/approval/${row.id}/approve`)
    ElMessage.success('审批通过')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = async (row) => {
  try {
    await ElMessageBox.prompt('请输入驳回理由', '审批驳回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.post(`/approval/${row.id}/reject`)
    ElMessage.success('已驳回')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const getStatusType = (status) => {
  const map = { pending: 'warning', approved: 'success', rejected: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { pending: '待审批', approved: '已通过', rejected: '已驳回' }
  return map[status] || status
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
