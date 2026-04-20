<template>
  <div v-if="notFound" class="not-found-container">
    <NotFound />
  </div>
  <div v-else class="record-detail" v-loading="loading">
    <!-- 提交头部信息 -->
    <el-card class="record-header-card">
      <div class="record-header">
        <div class="record-title-section">
          <h1 class="record-title">提交记录 #{{ record.id }}</h1>
          <div class="record-meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              <span>用户 {{ record.userId }}</span>
            </span>
            <span class="meta-separator">•</span>
            <span class="meta-item">
              <el-icon><Document /></el-icon>
              <span>题目 {{ record.problemId }}</span>
            </span>
            <span class="meta-separator">•</span>
            <span class="meta-item">
              <el-tag :type="getStatusType(record.status)" size="small">
                {{ getStatusText(record.status) }}
              </el-tag>
            </span>
          </div>
          <div class="record-info-line">
            <span class="info-item" v-if="record.language">
              <el-icon><Files /></el-icon>
              <span>{{ record.language }}</span>
            </span>
            <span class="info-separator" v-if="record.language && record.submitTime">•</span>
            <span class="info-item" v-if="record.submitTime">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(record.submitTime) }}</span>
            </span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 主体内容区域 -->
    <div class="main-content">
      <!-- 提交内容 -->
      <el-card class="record-content-card">
        <!-- 编译错误信息 -->
        <div class="compile-section" v-if="record.compileStderr">
          <h3 class="section-title error-title">
            <el-icon color="#f56c6c"><CircleClose /></el-icon>
            编译错误
          </h3>
          <pre class="compile-error"><code>{{ record.compileStderr }}</code></pre>
        </div>

        <!-- 编译警告信息 -->
        <div class="compile-section" v-else-if="record.compileStdout">
          <h3 class="section-title warning-title">
            <el-icon color="#e6a23c"><Warning /></el-icon>
            编译警告
          </h3>
          <pre class="compile-warning"><code>{{ record.compileStdout }}</code></pre>
        </div>

        <!-- 提交的代码 -->
        <div class="code-section" v-if="record.code">
          <h2 class="section-title">提交的代码</h2>
          <pre class="code-block"><code>{{ record.code }}</code></pre>
        </div>

        <!-- 测试点详情 -->
        <div class="testcase-section" v-if="details.length > 0">
          <h2 class="section-title">测试点详情</h2>
          <el-table :data="details" style="width: 100%" border>
            <el-table-column prop="recordId" label="记录ID" width="100" />
            <el-table-column label="状态" width="150">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="耗时(ms)" width="120" />
            <el-table-column label="内存(MB)" width="120">
              <template #default="scope">
                {{ formatMemory(scope.row.memory) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <!-- 侧边操作栏 -->
      <div class="sidebar">
        <el-button type="primary" size="large" @click="viewProblem" class="sidebar-btn">
          <el-icon><View /></el-icon>
          <span>查看题目</span>
        </el-button>
        <el-button 
          type="warning" 
          size="large" 
          @click="handleRejudge" 
          v-permission="PERMISSIONS.PERM_SYSTEM_CONFIG"
          class="sidebar-btn"
        >
          <el-icon><Refresh /></el-icon>
          <span>重测</span>
        </el-button>
        <el-button size="large" @click="goBack" class="sidebar-btn">
          <el-icon><Back /></el-icon>
          <span>返回</span>
        </el-button>
      </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleClose, Warning, User, Document, Files, Clock, View, Refresh, Back } from '@element-plus/icons-vue'
import { getRecordInfo, getRecordDetails, rejudge } from '@/api/record'
import { PERMISSIONS } from '@/stores/user'
import NotFound from '@/views/NotFound.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const notFound = ref(false)
const record = ref({
  id: route.params.id,
  userId: '',
  problemId: '',
  status: '',
  language: '',
  code: '',
  submitTime: ''
})
const details = ref([])

const loadRecord = async () => {
  loading.value = true
  try {
    const res = await getRecordInfo(route.params.id)
    record.value = res.data
    
    // 加载测试点详情
    const detailRes = await getRecordDetails(route.params.id)
    details.value = detailRes.data || []
  } catch (error) {
    console.error('加载记录详情失败:', error)
    // 检查是否是资源不存在的错误
    const errorMsg = error.message || ''
    if (errorMsg.includes('不存在') || errorMsg.includes('404') || 
        (error.response && (error.response.status === 404 || error.response.status === 403))) {
      notFound.value = true
    }
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const statusMap = {
    'AC': 'success',
    'WA': 'danger',
    'TLE': 'warning',
    'MLE': 'warning',
    'RE': 'danger',
    'CE': 'info',
    'waiting': '',
    'Accepted': 'success',
    'Wrong Answer': 'danger',
    'Time Limit Exceeded': 'warning',
    'Memory Limit Exceeded': 'warning',
    'Runtime Error': 'danger',
    'Compilation Error': 'info'
  }
  return statusMap[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  // 处理 LocalDateTime 格式: 2024-01-01T12:00:00
  const dateStr = time.replace('T', ' ')
  return dateStr.substring(0, 19)
}

const getStatusText = (status) => {
  const textMap = {
    'AC': '通过',
    'WA': '答案错误',
    'TLE': '超时',
    'MLE': '超内存',
    'RE': '运行错误',
    'CE': '编译错误',
    'waiting': '等待中',
    'Accepted': '通过',
    'Wrong Answer': '答案错误',
    'Time Limit Exceeded': '超时',
    'Memory Limit Exceeded': '超内存',
    'Runtime Error': '运行错误',
    'Compilation Error': '编译错误'
  }
  return textMap[status] || status
}

// 将字节转换为MB（保留两位小数）
const formatMemory = (bytes) => {
  if (bytes === null || bytes === undefined || bytes === 0) {
    return '0.00'
  }
  const mb = bytes / (1024 * 1024)
  return mb.toFixed(2)
}

const goBack = () => {
  router.back()
}

const viewProblem = () => {
  if (record.value.problemId) {
    router.push(`/problem/${record.value.problemId}`)
  }
}

const handleRejudge = async () => {
  try {
    await ElMessageBox.confirm('确定要重新判题吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await rejudge(record.value.id)
    ElMessage.success('重测任务已提交')
    // 重新加载记录详情
    loadRecord()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重测失败:', error)
      ElMessage.error('重测失败')
    }
  }
}

onMounted(() => {
  loadRecord()
})
</script>

<style scoped>
.not-found-container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.record-detail {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* 提交头部卡片 */
.record-header-card {
  margin-bottom: 20px;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.record-title-section {
  flex: 1;
}

.record-title {
  margin: 0 0 12px 0;
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.3;
}

[data-theme='dark'] .record-title {
  text-shadow: 0 0 15px rgba(102, 126, 234, 0.6);
}

.record-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.meta-item .el-icon {
  font-size: 16px;
  color: #667eea;
}

.meta-separator {
  color: var(--color-text-secondary);
  opacity: 0.5;
}

.record-info-line {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
  opacity: 0.7;
}

.info-item .el-icon {
  font-size: 14px;
  color: #667eea;
}

.info-separator {
  color: var(--color-text-secondary);
  opacity: 0.5;
}

/* 主体内容区域 */
.main-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* 提交内容卡片 */
.record-content-card {
  flex: 1;
  min-width: 0;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.3);
  display: flex;
  align-items: center;
  gap: 8px;
}

.error-title,
.warning-title {
  font-size: 18px;
}

[data-theme='dark'] .section-title {
  text-shadow: 0 0 10px rgba(102, 126, 234, 0.5);
}

.code-section,
.compile-section,
.testcase-section {
  margin: 24px 0;
}

.code-block,
.compile-error,
.compile-warning {
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
}

.code-block {
  background-color: var(--color-bg);
}

[data-theme='dark'] .code-block {
  background-color: rgba(0, 0, 0, 0.3);
}

.code-block code {
  color: var(--color-text-primary);
}

[data-theme='dark'] .code-block code {
  color: #e0e0e0;
}

.compile-error {
  background-color: rgba(245, 108, 108, 0.1);
  border-left: 4px solid #f56c6c;
}

[data-theme='dark'] .compile-error {
  background-color: rgba(245, 108, 108, 0.15);
}

.compile-error code {
  color: #f56c6c;
}

[data-theme='dark'] .compile-error code {
  color: #f87171;
}

.compile-warning {
  background-color: rgba(230, 162, 60, 0.1);
  border-left: 4px solid #e6a23c;
}

[data-theme='dark'] .compile-warning {
  background-color: rgba(230, 162, 60, 0.15);
}

.compile-warning code {
  color: #e6a23c;
}

[data-theme='dark'] .compile-warning code {
  color: #fbbf24;
}

/* 侧边操作栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 160px;
  position: sticky;
  top: 20px;
}

.sidebar-btn {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: auto;
  padding: 16px 12px;
  margin: 0 !important;
  border: 1px solid var(--color-border) !important;
}

/* 统一所有按钮的边框和背景 */
.sidebar :deep(.el-button) {
  background: var(--color-surface) !important;
  border: 1px solid var(--color-border) !important;
  color: var(--color-text-primary) !important;
}

.sidebar :deep(.el-button--primary) {
  background: var(--color-surface) !important;
  border: 1px solid var(--color-border) !important;
  color: var(--color-text-primary) !important;
}

.sidebar :deep(.el-button--warning) {
  background: var(--color-surface) !important;
  border: 1px solid var(--color-border) !important;
  color: var(--color-text-primary) !important;
}

.sidebar :deep(.el-button:hover) {
  background: rgba(102, 126, 234, 0.1) !important;
  border-color: #667eea !important;
}

.sidebar-btn .el-icon {
  font-size: 24px;
}

.sidebar-btn span {
  font-size: 13px;
  text-align: center;
}

/* Element Plus 卡片科技风格 */
:deep(.el-card) {
  background: var(--color-surface);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--color-border);
  transition: all 0.3s ease;
}

:deep(.el-card:hover) {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

[data-theme='dark'] :deep(.el-card) {
  background: rgba(255, 255, 255, 0.05);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

[data-theme='dark'] :deep(.el-card:hover) {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.5);
  border-color: rgba(102, 126, 234, 0.3);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }

  .record-content-card {
    order: 1;
  }

  .sidebar {
    order: 2;
    flex-direction: row;
    flex-wrap: wrap;
    width: 100%;
    position: static;
  }

  .sidebar-btn {
    flex: 1;
    min-width: calc(33.333% - 8px);
    padding: 12px 8px;
  }

  .sidebar-btn .el-icon {
    font-size: 20px;
  }

  .sidebar-btn span {
    font-size: 12px;
  }
}
</style>
