<template>
  <div class="contest-standings" v-loading="loading">
    <!-- 头部信息 -->
    <el-card class="header-card">
      <div class="header-content">
        <h1 class="title">
          <el-icon><Trophy /></el-icon>
          比赛排行榜
        </h1>
        <el-button @click="goBack">
          <el-icon><Back /></el-icon>
          返回比赛
        </el-button>
      </div>
    </el-card>

    <!-- 排行榜表格 -->
    <el-card class="standings-card">
      <div v-if="standings.length === 0" class="empty-state">
        <el-empty description="暂无参赛记录" />
      </div>
      
      <div v-else class="table-container">
        <el-table :data="standings" style="width: 100%" stripe>
          <!-- 排名 -->
          <el-table-column label="排名" width="80" fixed="left">
            <template #default="scope">
              <div class="rank-cell">
                <span v-if="scope.row.rank === 1" class="rank-badge gold">#{{ scope.row.rank }}</span>
                <span v-else-if="scope.row.rank === 2" class="rank-badge silver">#{{ scope.row.rank }}</span>
                <span v-else-if="scope.row.rank === 3" class="rank-badge bronze">#{{ scope.row.rank }}</span>
                <span v-else class="rank-normal">#{{ scope.row.rank }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- 用户信息 -->
          <el-table-column label="选手" min-width="150" fixed="left">
            <template #default="scope">
              <div class="user-cell">
                <el-avatar :size="32" class="user-avatar">
                  {{ scope.row.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <span class="user-name">{{ scope.row.nickname || '未知用户' }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- 解题数 -->
          <el-table-column label="通过" width="100" align="center">
            <template #default="scope">
              <span class="solved-count">{{ scope.row.totalSolved }}</span>
            </template>
          </el-table-column>

          <!-- 总罚时 -->
          <el-table-column label="罚时" width="100" align="center">
            <template #default="scope">
              <span class="penalty-time">{{ formatPenalty(scope.row.totalPenalty) }}</span>
            </template>
          </el-table-column>

          <!-- 各题目状态 -->
          <el-table-column 
            v-for="problemLabel in problemLabels" 
            :key="problemLabel"
            :label="problemLabel"
            width="100"
            align="center"
          >
            <template #default="scope">
              <div v-if="scope.row.problems[problemLabel]" class="problem-cell">
                <el-tooltip 
                  v-if="scope.row.problems[problemLabel].status"
                  :content="getProblemTooltip(scope.row.problems[problemLabel])"
                  placement="top"
                >
                  <div 
                    class="problem-status" 
                    :class="getStatusClass(scope.row.problems[problemLabel].status)"
                  >
                    <span v-if="scope.row.problems[problemLabel].status === 'AC'" class="ac-info">
                      <el-icon><Check /></el-icon>
                      {{ formatTime(scope.row.problems[problemLabel].firstAcTime) }}
                    </span>
                    <span v-else class="wa-info">
                      -{{ scope.row.problems[problemLabel].attempts }}
                    </span>
                  </div>
                </el-tooltip>
                <span v-else class="no-submission">-</span>
              </div>
              <span v-else class="no-submission">-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Trophy, Back, Check } from '@element-plus/icons-vue'
import { getContestStandings, getContestProblems } from '@/api/contest'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const standings = ref([])
const problemLabels = ref([])

// 加载排行榜数据
const loadStandings = async () => {
  const contestId = route.params.contestId
  if (!contestId) {
    ElMessage.error('比赛ID不能为空')
    return
  }

  loading.value = true
  try {
    // 获取比赛题目列表
    const problemsRes = await getContestProblems(contestId)
    if (problemsRes.data && problemsRes.data.length > 0) {
      // 按problemOrder排序
      const sortedProblems = problemsRes.data.sort((a, b) => a.problemOrder - b.problemOrder)
      problemLabels.value = sortedProblems.map(p => p.problemLabel)
    }

    // 获取排行榜数据
    const res = await getContestStandings(contestId)
    if (res.data) {
      standings.value = res.data
    }
  } catch (error) {
    console.error('加载排行榜失败:', error)
    ElMessage.error('加载排行榜失败')
  } finally {
    loading.value = false
  }
}

// 格式化罚时（秒转小时:分钟）
const formatPenalty = (seconds) => {
  if (!seconds || seconds === 0) return '-'
  const hours = Math.floor(seconds / 3600)
  const mins = Math.floor((seconds % 3600) / 60)
  if (hours > 0) {
    return `${hours}:${mins.toString().padStart(2, '0')}`
  }
  return `${mins}分`
}

// 格式化时间（秒转分钟，保留一位小数）
const formatTime = (seconds) => {
  if (seconds === null || seconds === undefined) return '-'
  const minutes = (seconds / 60).toFixed(1)
  return minutes
}

// 获取状态样式类
const getStatusClass = (status) => {
  return {
    'status-ac': status === 'AC',
    'status-wa': status !== 'AC' && status !== null,
  }
}

// 获取题目提示信息
const getProblemTooltip = (problemData) => {
  if (problemData.status === 'AC') {
    return `通过时间: ${formatTime(problemData.firstAcTime)} 分钟\n尝试次数: ${problemData.attempts}\n罚时: ${formatPenalty(problemData.penalty)}`
  } else {
    return `尝试次数: ${problemData.attempts}\n当前状态: ${getStatusText(problemData.status)}`
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    WA: '答案错误',
    TLE: '超时',
    MLE: '超内存',
    RE: '运行错误',
    CE: '编译错误',
    waiting: '判题中'
  }
  return texts[status] || status
}

// 返回比赛详情页
const goBack = () => {
  router.push(`/contest/${route.params.contestId}`)
}

onMounted(() => {
  loadStandings()
})
</script>

<style scoped>
.contest-standings {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* 头部卡片 */
.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: 12px;
}

.title .el-icon {
  font-size: 32px;
  color: #f59e0b;
}

[data-theme='dark'] .title {
  text-shadow: 0 0 15px rgba(245, 158, 11, 0.6);
}

/* 排行榜卡片 */
.standings-card {
  min-height: 400px;
}

.empty-state {
  padding: 60px 0;
}

.table-container {
  overflow-x: auto;
}

/* 排名单元格 */
.rank-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.rank-badge {
  font-weight: 700;
  font-size: 16px;
  padding: 4px 12px;
  border-radius: 6px;
  display: inline-block;
}

.rank-badge.gold {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  color: #000;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
}

.rank-badge.silver {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  color: #000;
  box-shadow: 0 2px 8px rgba(192, 192, 192, 0.4);
}

.rank-badge.bronze {
  background: linear-gradient(135deg, #cd7f32 0%, #daa520 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.4);
}

.rank-normal {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-secondary);
}

/* 用户单元格 */
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

.user-name {
  font-weight: 500;
  color: var(--color-text-primary);
}

/* 解题数 */
.solved-count {
  font-weight: 700;
  font-size: 18px;
  color: #67c23a;
}

/* 罚时 */
.penalty-time {
  font-weight: 600;
  color: var(--color-text-secondary);
}

/* 题目状态单元格 */
.problem-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.problem-status {
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.problem-status:hover {
  transform: scale(1.05);
}

.status-ac {
  background: rgba(103, 194, 58, 0.15);
  color: #67c23a;
  border: 1px solid rgba(103, 194, 58, 0.3);
}

[data-theme='dark'] .status-ac {
  background: rgba(103, 194, 58, 0.2);
  box-shadow: 0 0 10px rgba(103, 194, 58, 0.3);
}

.status-wa {
  background: rgba(245, 108, 108, 0.15);
  color: #f56c6c;
  border: 1px solid rgba(245, 108, 108, 0.3);
}

[data-theme='dark'] .status-wa {
  background: rgba(245, 108, 108, 0.2);
  box-shadow: 0 0 10px rgba(245, 108, 108, 0.3);
}

.ac-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.ac-info .el-icon {
  font-size: 14px;
}

.wa-info {
  font-size: 13px;
}

.no-submission {
  color: var(--color-text-tertiary);
  opacity: 0.5;
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

[data-theme='dark'] :deep(.el-card) {
  background: rgba(255, 255, 255, 0.05);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 表格样式优化 */
:deep(.el-table) {
  background: transparent;
}

:deep(.el-table th) {
  background: rgba(102, 126, 234, 0.05);
  color: var(--color-text-primary);
  font-weight: 600;
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
}

[data-theme='dark'] :deep(.el-table th) {
  background: rgba(102, 126, 234, 0.1);
  border-bottom-color: rgba(102, 126, 234, 0.3);
}

:deep(.el-table td) {
  border-bottom: 1px solid var(--color-border);
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: rgba(102, 126, 234, 0.02);
}

[data-theme='dark'] :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: rgba(102, 126, 234, 0.05);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .contest-standings {
    padding: 10px;
  }

  .header-content {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .title {
    font-size: 22px;
  }

  .title .el-icon {
    font-size: 26px;
  }
}
</style>
