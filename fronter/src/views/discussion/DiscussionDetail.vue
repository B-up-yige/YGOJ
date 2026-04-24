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

      <div class="post-actions" v-if="canEdit || canDelete || canManageAllPosts">
        <el-button 
          :type="post.isPinned ? 'warning' : 'primary'" 
          @click="handleTogglePin"
          v-if="canManageAllPosts"
        >
          <el-icon><Top /></el-icon>
          <span>{{ post.isPinned ? '取消置顶' : '置顶' }}</span>
        </el-button>
        <el-button type="primary" @click="handleEdit" v-if="canEdit">
          <el-icon><Edit /></el-icon>
          <span>编辑</span>
        </el-button>
        <el-button type="danger" @click="handleDelete" v-if="canDelete">
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
import { ArrowLeft, Delete, Edit, Top } from '@element-plus/icons-vue'
import { getPostById, deletePost, getComments, createComment, togglePinPost } from '@/api/discussion'
import { useUserStore, PERMISSIONS } from '@/stores/user'
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
  if (!userStore.token || !post.value) return false
  const currentUserId = userStore.userInfo?.id
  
  // 管理员权限可以编辑所有帖子
  if (userStore.hasPermission(PERMISSIONS.PERM_POST_MANAGE_ALL)) {
    return true
  }
  
  // 作者本人且有管理自己帖子的权限
  if (currentUserId === post.value.authorId && 
      userStore.hasPermission(PERMISSIONS.PERM_POST_MANAGE_OWN)) {
    return true
  }
  
  return false
})

const canDelete = computed(() => {
  if (!userStore.token || !post.value) return false
  const currentUserId = userStore.userInfo?.id
  
  // 管理员权限可以删除所有帖子
  if (userStore.hasPermission(PERMISSIONS.PERM_POST_MANAGE_ALL)) {
    return true
  }
  
  // 作者本人且有管理自己帖子的权限
  if (currentUserId === post.value.authorId && 
      userStore.hasPermission(PERMISSIONS.PERM_POST_MANAGE_OWN)) {
    return true
  }
  
  return false
})

// 检查是否有管理所有帖子的权限（用于置顶功能）
const canManageAllPosts = computed(() => {
  if (!userStore.token || !post.value) return false
  return userStore.hasPermission(PERMISSIONS.PERM_POST_MANAGE_ALL)
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

const handleEdit = () => {
  router.push(`/discussion/edit/${post.value.id}`)
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

// 置顶/取消置顶帖子
const handleTogglePin = async () => {
  try {
    const action = post.value.isPinned ? '取消置顶' : '置顶'
    await ElMessageBox.confirm(`确定要${action}这个帖子吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await togglePinPost(post.value.id, !post.value.isPinned)
    ElMessage.success(`${action}成功`)
    // 重新加载帖子以更新状态
    await loadPost()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
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

/* Markdown 样式 */
.post-content :deep(h1),
.post-content :deep(h2),
.post-content :deep(h3),
.post-content :deep(h4),
.post-content :deep(h5),
.post-content :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
  color: var(--color-text-primary);
}

.post-content :deep(h1) {
  font-size: 2em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid var(--color-border);
}

.post-content :deep(h2) {
  font-size: 1.5em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid var(--color-border);
}

.post-content :deep(h3) {
  font-size: 1.25em;
}

.post-content :deep(h4) {
  font-size: 1em;
}

.post-content :deep(p) {
  margin-top: 0;
  margin-bottom: 16px;
}

.post-content :deep(ul),
.post-content :deep(ol) {
  margin-top: 0;
  margin-bottom: 16px;
  padding-left: 2em;
}

.post-content :deep(li) {
  margin: 4px 0;
}

.post-content :deep(blockquote) {
  margin: 16px 0;
  padding: 0 1em;
  color: var(--color-text-secondary);
  border-left: 0.25em solid #667eea;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 4px;
}

[data-theme='dark'] .post-content :deep(blockquote) {
  background: rgba(102, 126, 234, 0.1);
}

.post-content :deep(code) {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(102, 126, 234, 0.1);
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  color: var(--color-text-primary);
}

.post-content :deep(pre) {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: rgba(0, 0, 0, 0.03);
  border-radius: 6px;
  margin: 16px 0;
  border: 1px solid var(--color-border);
}

[data-theme='dark'] .post-content :deep(pre) {
  background-color: rgba(0, 0, 0, 0.3);
  border-color: rgba(102, 126, 234, 0.3);
}

.post-content :deep(pre code) {
  display: inline;
  max-width: 100%;
  padding: 0;
  margin: 0;
  overflow: visible;
  line-height: inherit;
  word-wrap: normal;
  background-color: transparent;
  border: 0;
}

.post-content :deep(table) {
  border-spacing: 0;
  border-collapse: collapse;
  margin: 16px 0;
  width: 100%;
}

.post-content :deep(table th),
.post-content :deep(table td) {
  padding: 6px 13px;
  border: 1px solid var(--color-border);
}

.post-content :deep(table tr) {
  background-color: transparent;
  border-top: 1px solid var(--color-border);
}

.post-content :deep(table tr:nth-child(2n)) {
  background-color: rgba(102, 126, 234, 0.03);
}

[data-theme='dark'] .post-content :deep(table tr:nth-child(2n)) {
  background-color: rgba(102, 126, 234, 0.08);
}

.post-content :deep(table th) {
  font-weight: 600;
  background-color: rgba(102, 126, 234, 0.05);
}

[data-theme='dark'] .post-content :deep(table th) {
  background-color: rgba(102, 126, 234, 0.1);
}

.post-content :deep(a) {
  color: #667eea;
  text-decoration: none;
  transition: all 0.2s ease;
}

.post-content :deep(a:hover) {
  color: #764ba2;
  text-decoration: underline;
}

.post-content :deep(img) {
  max-width: 100%;
  box-sizing: border-box;
  border-radius: 6px;
  margin: 16px 0;
}

.post-content :deep(hr) {
  height: 0.25em;
  padding: 0;
  margin: 24px 0;
  background-color: var(--color-border);
  border: 0;
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
