<template>
  <div class="discussion-create">
    <el-card>
      <template #header>
        <h2>{{ isEdit ? '编辑帖子' : '发布新帖' }}</h2>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="帖子标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="板块分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择板块分类" style="width: 100%" v-loading="loadingCategories">
            <el-option 
              v-for="cat in categories" 
              :key="cat.code" 
              :label="cat.name" 
              :value="cat.code" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="帖子内容" prop="content">
          <div class="markdown-editor-container">
            <div class="editor-tabs">
              <el-radio-group v-model="editorMode" size="small">
                <el-radio-button label="edit">编辑</el-radio-button>
                <el-radio-button label="preview">预览</el-radio-button>
                <el-radio-button label="split">分屏</el-radio-button>
              </el-radio-group>
            </div>
            <div class="editor-content" :class="'mode-' + editorMode">
              <div v-show="editorMode === 'edit' || editorMode === 'split'" class="editor-pane">
                <el-input
                  v-model="form.content"
                  type="textarea"
                  :rows="15"
                  placeholder="请输入帖子内容（支持 Markdown 语法）"
                  maxlength="5000"
                  show-word-limit
                  class="markdown-textarea"
                />
              </div>
              <div v-show="editorMode === 'preview' || editorMode === 'split'" class="preview-pane">
                <div class="markdown-preview markdown-body" v-html="renderedContent"></div>
              </div>
            </div>
            <div class="markdown-hint">
              <el-icon><InfoFilled /></el-icon>
              <span>支持 Markdown 语法：标题、粗体、列表、代码块、表格等</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存' : '发布' }}
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { InfoFilled } from '@element-plus/icons-vue'
import { createPost, updatePost, getPostById, getActiveCategories } from '@/api/discussion'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const submitting = ref(false)
const loadingCategories = ref(false)
const categories = ref([])
const editorMode = ref('split') // 'edit' | 'preview' | 'split'

const isEdit = computed(() => !!route.params.id)

// Markdown 实时预览
const renderedContent = computed(() => {
  return renderMarkdown(form.value.content || '')
})

const form = ref({
  title: '',
  content: '',
  category: '', // 默认从后端获取
  authorId: userStore.userInfo?.id
})

const rules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择板块分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 10, message: '内容至少 10 个字符', trigger: 'blur' }
  ]
}

const loadPost = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await getPostById(route.params.id)
    const post = res.data
    form.value = {
      title: post.title,
      content: post.content,
      category: post.category || '',
      authorId: post.authorId
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载帖子失败')
  }
}

// 加载板块列表
const loadCategories = async () => {
  loadingCategories.value = true
  try {
    const res = await getActiveCategories()
    categories.value = res.data || []
    // 设置默认选中第一个板块
    if (categories.value.length > 0 && !form.value.category) {
      form.value.category = categories.value[0].code
    }
  } catch (error) {
    console.error('加载板块列表失败:', error)
  } finally {
    loadingCategories.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await updatePost({
          id: parseInt(route.params.id),
          ...form.value
        })
        ElMessage.success('更新成功')
      } else {
        await createPost(form.value)
        ElMessage.success('发布成功')
      }
      router.push('/discussion')
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(isEdit.value ? '更新失败' : '发布失败')
    } finally {
      submitting.value = false
    }
  })
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loadCategories()
  loadPost()
})
</script>

<style scoped>
.discussion-create {
  padding: 20px;
}

/* Markdown 编辑器容器 */
.markdown-editor-container {
  width: 100%;
}

.editor-tabs {
  margin-bottom: 12px;
  display: flex;
  justify-content: flex-end;
}

.editor-content {
  display: flex;
  gap: 12px;
  min-height: 400px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  overflow: hidden;
}

.editor-content.mode-edit .editor-pane,
.editor-content.mode-preview .preview-pane {
  width: 100%;
}

.editor-content.mode-split .editor-pane,
.editor-content.mode-split .preview-pane {
  width: 50%;
}

.editor-pane,
.preview-pane {
  height: 100%;
  overflow: auto;
}

.markdown-textarea :deep(.el-textarea__inner) {
  height: 400px !important;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  border: none;
  border-radius: 0;
}

.markdown-preview {
  padding: 16px;
  min-height: 400px;
  background-color: var(--color-bg);
  border-left: 1px solid var(--color-border);
}

.markdown-hint {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

[data-theme='dark'] .markdown-hint {
  background: rgba(102, 126, 234, 0.1);
}

.markdown-hint .el-icon {
  color: #667eea;
  font-size: 16px;
}

/* Markdown 预览样式 */
.markdown-body {
  color: var(--color-text-primary);
  font-size: 15px;
  line-height: 1.8;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
  color: var(--color-text-primary);
}

.markdown-body :deep(h1) {
  font-size: 2em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid var(--color-border);
}

.markdown-body :deep(h2) {
  font-size: 1.5em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid var(--color-border);
}

.markdown-body :deep(h3) {
  font-size: 1.25em;
}

.markdown-body :deep(h4) {
  font-size: 1em;
}

.markdown-body :deep(p) {
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin-top: 0;
  margin-bottom: 16px;
  padding-left: 2em;
}

.markdown-body :deep(li) {
  margin: 4px 0;
}

.markdown-body :deep(blockquote) {
  margin: 16px 0;
  padding: 0 1em;
  color: var(--color-text-secondary);
  border-left: 0.25em solid #667eea;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 4px;
}

[data-theme='dark'] .markdown-body :deep(blockquote) {
  background: rgba(102, 126, 234, 0.1);
}

.markdown-body :deep(code) {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(102, 126, 234, 0.1);
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  color: var(--color-text-primary);
}

.markdown-body :deep(pre) {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: rgba(0, 0, 0, 0.03);
  border-radius: 6px;
  margin: 16px 0;
  border: 1px solid var(--color-border);
}

[data-theme='dark'] .markdown-body :deep(pre) {
  background-color: rgba(0, 0, 0, 0.3);
  border-color: rgba(102, 126, 234, 0.3);
}

.markdown-body :deep(pre code) {
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

.markdown-body :deep(table) {
  border-spacing: 0;
  border-collapse: collapse;
  margin: 16px 0;
  width: 100%;
}

.markdown-body :deep(table th),
.markdown-body :deep(table td) {
  padding: 6px 13px;
  border: 1px solid var(--color-border);
}

.markdown-body :deep(table tr) {
  background-color: transparent;
  border-top: 1px solid var(--color-border);
}

.markdown-body :deep(table tr:nth-child(2n)) {
  background-color: rgba(102, 126, 234, 0.03);
}

[data-theme='dark'] .markdown-body :deep(table tr:nth-child(2n)) {
  background-color: rgba(102, 126, 234, 0.08);
}

.markdown-body :deep(table th) {
  font-weight: 600;
  background-color: rgba(102, 126, 234, 0.05);
}

[data-theme='dark'] .markdown-body :deep(table th) {
  background-color: rgba(102, 126, 234, 0.1);
}

.markdown-body :deep(a) {
  color: #667eea;
  text-decoration: none;
  transition: all 0.2s ease;
}

.markdown-body :deep(a:hover) {
  color: #764ba2;
  text-decoration: underline;
}

.markdown-body :deep(img) {
  max-width: 100%;
  box-sizing: border-box;
  border-radius: 6px;
  margin: 16px 0;
}

.markdown-body :deep(hr) {
  height: 0.25em;
  padding: 0;
  margin: 24px 0;
  background-color: var(--color-border);
  border: 0;
}
</style>
