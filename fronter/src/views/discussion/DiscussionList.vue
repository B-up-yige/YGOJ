<template>
  <div class="discussion-list">
    <div class="header">
      <h2>讨论区</h2>
      <el-button type="primary" @click="handleCreatePost" v-if="userStore.token">
        <el-icon><Plus /></el-icon>
        <span>发布新帖</span>
      </el-button>
    </div>

    <!-- 板块筛选 -->
    <div class="category-filter">
      <el-radio-group v-model="selectedCategory" @change="handleCategoryChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="GENERAL">综合讨论</el-radio-button>
        <el-radio-button label="PROBLEM_HELP">题目求助</el-radio-button>
        <el-radio-button label="ALGORITHM">算法交流</el-radio-button>
        <el-radio-button label="BUG_REPORT">Bug反馈</el-radio-button>
        <el-radio-button label="SUGGESTION">建议意见</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 帖子列表 -->
    <el-table :data="posts" style="width: 100%" v-loading="loading">
      <el-table-column label="标题" min-width="300">
        <template #default="scope">
          <div class="post-title-cell">
            <el-tag v-if="scope.row.isPinned" type="danger" size="small" class="pin-tag">置顶</el-tag>
            <el-tag 
              v-if="scope.row.category" 
              :type="getCategoryTagType(scope.row.category)" 
              size="small" 
              class="category-tag"
            >
              {{ getCategoryName(scope.row.category) }}
            </el-tag>
            <el-tag v-if="scope.row.problemId" type="info" size="small" class="problem-tag">
              题目 {{ scope.row.problemId }}
            </el-tag>
            <router-link :to="`/discussion/${scope.row.id}`" class="post-title-link">
              {{ scope.row.title }}
            </router-link>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column label="作者" width="150">
        <template #default="scope">
          <router-link :to="`/user/${scope.row.authorId}`" class="author-link">
            {{ scope.row.author?.nickname || '未知用户' }}
          </router-link>
        </template>
      </el-table-column>
      
      <el-table-column label="浏览/回复" width="120" align="center">
        <template #default="scope">
          <span class="stats-text">
            {{ scope.row.viewCount }} / {{ scope.row.commentCount }}
          </span>
        </template>
      </el-table-column>
      
      <el-table-column label="发布时间" width="180">
        <template #default="scope">
          <span class="time-text">{{ formatTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { getPostList } from '@/api/discussion'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const posts = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedCategory = ref('')

const loadPosts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    // 如果选择了板块，添加category参数
    if (selectedCategory.value) {
      params.category = selectedCategory.value
    }
    
    const res = await getPostList(params)
    if (res.data) {
      posts.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载帖子列表失败:', error)
    ElMessage.error('加载帖子列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreatePost = () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/discussion/create')
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadPosts()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadPosts()
}

const handleCategoryChange = () => {
  currentPage.value = 1
  loadPosts()
}

// 获取板块显示名称
const getCategoryName = (category) => {
  const categoryMap = {
    'GENERAL': '综合讨论',
    'PROBLEM_HELP': '题目求助',
    'ALGORITHM': '算法交流',
    'BUG_REPORT': 'Bug反馈',
    'SUGGESTION': '建议意见'
  }
  return categoryMap[category] || '未知板块'
}

// 获取板块标签类型
const getCategoryTagType = (category) => {
  const typeMap = {
    'GENERAL': '',
    'PROBLEM_HELP': 'warning',
    'ALGORITHM': 'success',
    'BUG_REPORT': 'danger',
    'SUGGESTION': 'info'
  }
  return typeMap[category] || ''
}

const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.discussion-list {
  padding: 24px;
  background: var(--color-surface);
  border-radius: 8px;
  box-shadow: var(--shadow-md);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h2 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 24px;
  font-weight: 600;
}

.category-filter {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--color-background);
  border-radius: 8px;
}

.post-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pin-tag {
  flex-shrink: 0;
}

.category-tag {
  flex-shrink: 0;
}

.problem-tag {
  flex-shrink: 0;
}

.post-title-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.post-title-link:hover {
  color: var(--color-primary-light);
  text-decoration: underline;
}

.author-link {
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: color 0.2s;
}

.author-link:hover {
  color: var(--color-primary);
}

.stats-text {
  color: var(--color-text-tertiary);
  font-size: 14px;
}

.time-text {
  color: var(--color-text-tertiary);
  font-size: 14px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>
