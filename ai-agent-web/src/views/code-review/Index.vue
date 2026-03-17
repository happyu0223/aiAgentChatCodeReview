<template>
  <div class="page-container">
    <el-card>
      <el-form :model="form" label-width="100px">
        <el-form-item label="项目名称">
          <el-input v-model="form.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="文件名称">
          <el-input v-model="form.fileName" placeholder="请输入文件名称" />
        </el-form-item>
        <el-form-item label="语言">
          <el-select v-model="form.language" placeholder="请选择语言">
            <el-option label="Java" value="java" />
            <el-option label="Python" value="python" />
            <el-option label="JavaScript" value="javascript" />
            <el-option label="Vue" value="vue" />
            <el-option label="Go" value="go" />
          </el-select>
        </el-form-item>
        <el-form-item label="代码内容">
          <CodeEditor v-model="form.codeContent" :language="form.language" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitReview" :loading="loading">
            提交审查
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="rawResult" class="result-card">
      <template #header>
        <span>AI原始响应</span>
      </template>
      <pre class="result-content">{{ rawResult }}</pre>
    </el-card>

    <el-card v-if="reviewResult.summary" class="result-card">
      <template #header>
        <div class="card-header">
          <span>审查结果</span>
          <div>
            <el-tag v-if="reviewResult.summary.score" type="success">评分: {{ reviewResult.summary.score }}分</el-tag>
            <el-button type="success" size="small" @click="handleOptimize" :loading="optimizing" style="margin-left: 10px;">
              一键优化
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="summary">
        <el-tag type="danger">严重: {{ reviewResult.summary.critical || 0 }}</el-tag>
        <el-tag type="warning">重要: {{ reviewResult.summary.major || 0 }}</el-tag>
        <el-tag type="info">次要: {{ reviewResult.summary.minor || 0 }}</el-tag>
      </div>

      <el-divider v-if="reviewResult.issues && reviewResult.issues.length > 0" />

      <div v-if="reviewResult.issues && reviewResult.issues.length > 0" class="issues-list">
        <h4>问题详情</h4>
        <el-collapse v-model="activeIssues">
          <el-collapse-item 
            v-for="(issue, index) in reviewResult.issues" 
            :key="index"
            :name="index"
          >
            <template #title>
              <div class="issue-title">
                <el-tag :type="getSeverityType(issue.severity)" size="small">
                  {{ getSeverityText(issue.severity) }}
                </el-tag>
                <span class="issue-line" v-if="issue.line">第{{ issue.line }}行</span>
                <span class="issue-category">[{{ issue.category }}]</span>
                <span class="issue-message">{{ issue.title || issue.message }}</span>
              </div>
            </template>
            <div class="issue-content">
              <div class="issue-detail">
                <p><strong>问题:</strong> {{ issue.message }}</p>
                <p v-if="issue.code"><strong>问题代码:</strong></p>
                <pre v-if="issue.code" class="code-block">{{ issue.code }}</pre>
                <p><strong>建议:</strong> {{ issue.suggestion }}</p>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <el-divider v-if="reviewResult.recommendations && reviewResult.recommendations.length > 0" />

      <div v-if="reviewResult.recommendations && reviewResult.recommendations.length > 0" class="recommendations">
        <h4>优化建议</h4>
        <ul>
          <li v-for="(rec, index) in reviewResult.recommendations" :key="index">
            {{ rec }}
          </li>
        </ul>
      </div>

      <div v-if="!reviewResult.issues || reviewResult.issues.length === 0" class="no-issues">
        <el-alert title="代码审查通过，未发现问题" type="success" :closable="false" />
      </div>
    </el-card>

    <el-card v-if="rawResult && !reviewResult.summary" class="result-card">
      <template #header>
        <span>审查结果(原始)</span>
      </template>
      <pre class="result-content">{{ rawResult }}</pre>
    </el-card>

    <el-card v-if="optimizedCode" class="result-card">
      <template #header>
        <div class="card-header">
          <span>优化结果</span>
          <el-button type="primary" size="small" @click="formatOptimizedCode" :loading="formatting">
            格式化代码
          </el-button>
        </div>
      </template>
      <CodeEditor v-model="optimizedCode" :language="form.language" :readonly="true" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import CodeEditor from '@/components/CodeEditor.vue'

const form = reactive({
  projectName: '',
  fileName: '',
  language: 'java',
  codeContent: '',
  reviewType: 'manual'
})

const loading = ref(false)
const optimizing = ref(false)
const formatting = ref(false)
const rawResult = ref('')
const reviewResult = ref({})
const activeIssues = ref([])
const reviewId = ref(null)
const optimizedCode = ref('')

const submitReview = async () => {
  if (!form.codeContent) {
    ElMessage.warning('请输入代码内容')
    return
  }
  
  loading.value = true
  try {
    const res = await request.post('/code-review/submit', form)
    const data = res.data
    
    reviewId.value = data.reviewId
    
    // 始终保存原始响应
    if (data.result) {
      if (typeof data.result === 'string') {
        rawResult.value = data.result
        try {
          reviewResult.value = JSON.parse(data.result)
        } catch (e) {
          reviewResult.value = {}
        }
      } else {
        rawResult.value = JSON.stringify(data.result, null, 2)
        reviewResult.value = data.result
      }
    }
    ElMessage.success('审查完成')
  } catch (error) {
    ElMessage.error('审查失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const getSeverityType = (severity) => {
  const map = { critical: 'danger', major: 'warning', minor: 'info' }
  return map[severity] || 'info'
}

const getSeverityText = (severity) => {
  const map = { critical: '严重', major: '重要', minor: '次要' }
  return map[severity] || severity
}

const handleOptimize = async () => {
  if (!reviewId.value) {
    ElMessage.warning('请先提交代码审查')
    return
  }
  
  optimizing.value = true
  try {
    const res = await request.post(`/optimize/${reviewId.value}`)
    optimizedCode.value = res.data.optimizedCode
    ElMessage.success('优化完成')
  } catch (error) {
    ElMessage.error('优化失败: ' + error.message)
  } finally {
    optimizing.value = false
  }
}

const formatOptimizedCode = async () => {
  if (!optimizedCode.value) return
  
  formatting.value = true
  try {
    const res = await request.post('/code-format/format', {
      code: optimizedCode.value,
      language: form.language
    })
    optimizedCode.value = res.data
    ElMessage.success('格式化完成')
  } catch (error) {
    ElMessage.error('格式化失败: ' + error.message)
  } finally {
    formatting.value = false
  }
}
</script>

<style scoped>
.result-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.issues-list h4,
.recommendations h4 {
  margin-bottom: 15px;
  color: #303133;
}

.issue-title {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.issue-line {
  color: #409eff;
  font-weight: bold;
}

.issue-category {
  color: #909399;
  font-size: 12px;
}

.issue-message {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.issue-content {
  padding: 10px 0;
}

.issue-detail p {
  margin: 8px 0;
  line-height: 1.6;
}

.code-block {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
}

.recommendations ul {
  padding-left: 20px;
}

.recommendations li {
  margin: 8px 0;
  line-height: 1.6;
}

.no-issues {
  margin-top: 10px;
}

.result-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  white-space: pre-wrap;
  max-height: 400px;
  overflow: auto;
}

.page-container {
  padding: 20px;
}

.code-editor{
  width: 100%;
}
</style>
