<template>
  <div class="page-container">
    <el-card>
      <div class="search-box">
        <el-input
          v-model="question"
          placeholder="请输入问题..."
          @keyup.enter="search"
        >
          <template #append>
            <el-button @click="search" :loading="loading">搜索</el-button>
          </template>
        </el-input>
      </div>

      <div v-if="answer" class="answer-box">
        <h3>回答:</h3>
        <p>{{ answer }}</p>
        <div v-if="sources.length" class="sources">
          <h4>参考来源:</h4>
          <el-tag v-for="s in sources" :key="s.id" style="margin-right: 8px;">
            {{ s.title }}
          </el-tag>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const question = ref('')
const answer = ref('')
const sources = ref([])
const loading = ref(false)

const search = async () => {
  if (!question.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }
  
  loading.value = true
  try {
    const res = await request.post('/knowledge/search', { question: question.value })
    answer.value = res.data.answer
    sources.value = res.data.sources || []
  } catch (error) {
    ElMessage.error('搜索失败: ' + error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.search-box {
  margin-bottom: 20px;
}

.answer-box {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.answer-box h3 {
  margin-bottom: 10px;
  color: #409eff;
}

.answer-box p {
  line-height: 1.8;
  margin-bottom: 15px;
}

.sources h4 {
  margin-bottom: 10px;
}

.page-container {
  padding: 20px;
}
</style>
