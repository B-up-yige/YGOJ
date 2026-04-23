<template>
  <div class="discussion-detail" v-loading="loading">
    <el-button @click="goBack" class="back-btn">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回</span>
    </el-button>

    <!-- 帖子内容 -->
    <el-card class="post-card" v-if="post">
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta">
          <el-tag 
            v-if="post.category" 
            :type="getCategoryTagType(post.category)" 
            size="small"
            class="category-tag"
          >
            {{ getCategoryName(post.category) }}
          </el-tag>
          <router-link :to="`/user/${post.authorId}`" class="author-info">
            <el-avatar :size="32" class="author-avatar">
              {{ post.author?.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="author-name">{{ post.author?.nickname || '未知用户' }}</span>
          </router-link>
          <span class="meta-divider">•</span>
          <span class="time">{{ formatTime(post.createdAt) }}</span>
          <span class="meta-divider">•</span>
          <span class="views">浏览 {{ post.viewCount }}</span>
        </div>
      </div>

      <div class="post-content markdown-body" v-html="renderedContent"></div>

      <div class="post-actions" v-if="canEdit">
        <el-button type="danger" @click="handleDelete">
          <el-icon><Delete /></el-icon>
          <span>删除</span>
        </el-button>
      </div>
    </el-card>

    <!-- 评论区 -->
    <el-card class="comments-card">
      <div class="comments-header">
        <h3>评论 ({{ comments.length }})</h3>
      </div>

      <!-- 发表评论 -->
      <div class="comment-form" v-if="userStore.token">
        <el-input
          v-model="newComment"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
          maxlength="1000"
          show-word-limit
        />
        <div class="comment-actions">
          <el-button type="primary" @click="handleSubmitComment" :loading="submitting">
            发表评论
          </el-button>
        </div>
      </div>
      <div v-else class="login-tip">
        <el-alert
          title="登录后即可发表评论"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <el-button link type="primary" @click="router.push('/login')">立即登录</el-button>
          </template>
        </el-alert>
      </div>

      <!-- 评论列表 -->
      <div class="comments-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-header">
            <router-link :to="`/user/${comment.authorId}`" class="comment-author">
              <el-avatar :size="28" class="comment-avatar">
                {{ comment.author?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="comment-author-name">{{ comment.author?.nickname || '未知用户' }}</span>
            </router-link>
            <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
        </div>
        <el-empty v-if="comments.length === 0" description="暂无评论" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Delete } from '@element-plus/icons-vue'
import { getPostById, deletePost, getComments, createComment } from '@/api/discussion'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { renderMarkdown } from '@/utils/markdown'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const post = ref(null)
const comments = ref([])
const newComment = ref('')
const submitting = ref(false)

const renderedContent = computed(() => {
  return renderMarkdown(post.value?.content || '')
})

const canEdit = computed(() => {
  if (!userStore.token) return false
  // 管理员或作者可以删除
  return userStore.userInfo?.role === 'ADMIN' || 
         userStore.userInfo?.id === post.value?.authorId
})

const loadPost = async () => {
  loading.value = true
  try {
    const res = await getPostById(route.params.id)
    post.value = res.data
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  try {
    const res = await getComments(route.params.id)
    comments.value = res.data || []
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

const handleSubmitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    await createComment({
      postId: parseInt(route.params.id),
      authorId: userStore.userInfo?.id,
      content: newComment.value
    })
    ElMessage.success('评论成功')
    newComment.value = ''
    // 重新加载评论和帖子
    await Promise.all([loadComments(), loadPost()])
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deletePost(post.value.id)
    ElMessage.success('删除成功')
    router.push('/discussion')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const goBack = () => {
  router.back()
}

const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
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

onMounted(() => {
  loadPost()
  loadComments()
})
</script>

<style scoped>
.discussion-detail {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 16px;
}

.post-card {
  margin-bottom: 24px;
}

.post-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.post-title {
  margin: 0 0 16px 0;
  color: var(--color-text-primary);
  font-size: 28px;
  font-weight: 600;
  line-height: 1.4;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-secondary);
  font-size: 14px;
  flex-wrap: wrap;
}

.category-tag {
  margin-right: 4px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: inherit;
  transition: color 0.2s;
}

.author-info:hover {
  color: var(--color-primary);
}

.author-avatar {
  background: var(--color-primary);
  color: white;
  font-weight: 500;
}

.meta-divider {
  color: var(--color-text-tertiary);
}

.post-content {
  min-height: 200px;
  line-height: 1.8;
  color: var(--color-text-primary);
}

.post-actions {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: flex-end;
}

.comments-card {
  padding: 24px;
}

.comments-header {
  margin-bottom: 20px;
}

.comments-header h3 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 18px;
  font-weight: 600;
}

.comment-form {
  margin-bottom: 24px;
}

.comment-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.login-tip {
  margin-bottom: 24px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  padding: 16px;
  background: var(--color-background);
  border-radius: 8px;
  transition: background 0.2s;
}

.comment-item:hover {
  background: var(--color-surface-hover);
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: inherit;
  transition: color 0.2s;
}

.comment-author:hover {
  color: var(--color-primary);
}

.comment-avatar {
  background: var(--color-primary-light);
  color: white;
  font-size: 12px;
  font-weight: 500;
}

.comment-author-name {
  font-weight: 500;
  color: var(--color-text-primary);
}

.comment-time {
  color: var(--color-text-tertiary);
  font-size: 13px;
}

.comment-content {
  color: var(--color-text-secondary);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
