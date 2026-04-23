<template>
  <div class="discussion-create">
    <el-button @click="goBack" class="back-btn">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回</span>
    </el-button>

    <el-card class="form-card">
      <h2>{{ isEdit ? '编辑帖子' : '发布新帖' }}</h2>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标题" prop="title">
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

        <el-form-item label="内容" prop="content">
          <el-tabs v-model="activeTab" type="border-card">
            <el-tab-pane label="编辑" name="edit">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="15"
                placeholder="请输入帖子内容（支持Markdown格式）"
                maxlength="5000"
                show-word-limit
              />
            </el-tab-pane>
            <el-tab-pane label="预览" name="preview">
              <div class="markdown-preview markdown-body" v-html="renderedContent"></div>
            </el-tab-pane>
          </el-tabs>
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
import { ArrowLeft } from '@element-plus/icons-vue'
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
const activeTab = ref('edit')

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
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 16px;
}

.form-card {
  padding: 32px;
}

.form-card h2 {
  margin: 0 0 24px 0;
  color: var(--color-text-primary);
  font-size: 24px;
  font-weight: 600;
}

.form-tip {
  margin-top: 4px;
  color: var(--color-text-tertiary);
  font-size: 12px;
}

.markdown-preview {
  min-height: 300px;
  padding: 16px;
  background: var(--el-fill-color-blank);
  border-radius: 4px;
}

/* Markdown 样式 */
.markdown-body {
  line-height: 1.6;
  color: var(--color-text-primary);
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
  border-bottom: 1px solid var(--el-border-color);
  padding-bottom: 0.3em;
}

.markdown-body :deep(h2) {
  font-size: 1.5em;
  border-bottom: 1px solid var(--el-border-color);
  padding-bottom: 0.3em;
}

.markdown-body :deep(h3) {
  font-size: 1.25em;
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
  margin-top: 0.25em;
}

.markdown-body :deep(blockquote) {
  margin: 0;
  padding: 0 1em;
  color: var(--color-text-secondary);
  border-left: 0.25em solid var(--el-border-color);
}

.markdown-body :deep(code) {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: var(--el-fill-color);
  border-radius: 6px;
  font-family: 'Courier New', Courier, monospace;
}

.markdown-body :deep(pre) {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: var(--el-fill-color);
  border-radius: 6px;
}

.markdown-body :deep(pre code) {
  padding: 0;
  margin: 0;
  font-size: 100%;
  background-color: transparent;
  border: 0;
}

.markdown-body :deep(table) {
  border-spacing: 0;
  border-collapse: collapse;
  margin-bottom: 16px;
  width: 100%;
}

.markdown-body :deep(table th),
.markdown-body :deep(table td) {
  padding: 6px 13px;
  border: 1px solid var(--el-border-color);
}

.markdown-body :deep(table tr) {
  background-color: transparent;
  border-top: 1px solid var(--el-border-color);
}

.markdown-body :deep(table tr:nth-child(2n)) {
  background-color: var(--el-fill-color-light);
}

.markdown-body :deep(img) {
  max-width: 100%;
  box-sizing: border-box;
}

.markdown-body :deep(a) {
  color: var(--color-primary);
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}
</style>
