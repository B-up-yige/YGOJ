<template>
  <div class="problem-edit">
    <el-card>
      <template #header>
        <h2>{{ isEdit ? '编辑题目' : '创建题目' }}</h2>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="题目标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入题目标题" />
        </el-form-item>

        <el-form-item label="题目描述" prop="description">
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
                  v-model="form.description"
                  type="textarea"
                  :rows="15"
                  placeholder="请输入题目描述（支持 Markdown 语法）"
                  class="markdown-textarea"
                />
              </div>
              <div v-show="editorMode === 'preview' || editorMode === 'split'" class="preview-pane">
                <div class="markdown-preview markdown-body" v-html="renderedDescription"></div>
              </div>
            </div>
            <div class="markdown-hint">
              <el-icon><InfoFilled /></el-icon>
              <span>支持 Markdown 语法：标题、粗体、列表、代码块、表格等</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="时间限制" prop="timeLimit">
          <el-input-number v-model="form.timeLimit" :min="100" :step="100" style="width: 100%" />
          <span style="margin-left: 10px; color: var(--color-text-tertiary);">(毫秒)</span>
        </el-form-item>

        <el-form-item label="内存限制" prop="memoryLimit">
          <el-input-number v-model="form.memoryLimit" :min="64" :step="64" style="width: 100%" />
          <span style="margin-left: 10px; color: var(--color-text-tertiary);">(MB)</span>
        </el-form-item>

        <!-- 标签管理 -->
        <el-form-item label="标签" v-if="isEdit">
          <div class="tag-management">
            <div class="tags-display" v-if="tags.length > 0">
              <el-tag
                v-for="tag in tags"
                :key="tag"
                closable
                @close="handleRemoveTag(tag)"
                style="margin-right: 8px; margin-bottom: 8px;"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="add-tag">
              <el-input
                v-model="newTag"
                placeholder="输入标签名称"
                style="width: 200px; margin-right: 8px;"
                @keyup.enter="handleAddTag"
              />
              <el-button type="primary" @click="handleAddTag" :disabled="!newTag.trim()">
                添加标签
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            提交
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>

      <!-- 测试用例管理入口 -->
      <div v-if="isEdit" style="margin-top: 30px; padding-top: 20px; border-top: 1px solid var(--color-border);">
        <h3>测试用例管理</h3>
        <p style="color: var(--color-text-secondary); margin-bottom: 15px;">题目信息保存后，可以添加或编辑测试用例</p>
        <el-button type="warning" @click="goToTestCaseEdit">
          <el-icon><Setting /></el-icon>
          管理测试用例
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Setting, InfoFilled } from '@element-plus/icons-vue'
import { getProblemInfo, addProblem, editProblem, getProblemTags, addProblemTag, delProblemTag } from '@/api/problem'
import { useUserStore } from '@/stores/user'
import { renderMarkdown } from '@/utils/markdown'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  description: '',
  timeLimit: 1000,
  memoryLimit: 256
})

const tags = ref([])
const newTag = ref('')
const editorMode = ref('split') // 'edit' | 'preview' | 'split'

// 渲染 Markdown 描述
const renderedDescription = computed(() => {
  return renderMarkdown(form.description || '')
})

const rules = {
  title: [
    { required: true, message: '请输入题目标题', trigger: 'blur' },
    { min: 3, max: 100, message: '标题长度在 3-100 个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入题目描述', trigger: 'blur' }
  ],
  timeLimit: [
    { required: true, message: '请设置时间限制', trigger: 'blur' }
  ],
  memoryLimit: [
    { required: true, message: '请设置内存限制', trigger: 'blur' }
  ]
}

const loadProblem = async () => {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    const res = await getProblemInfo(route.params.id)
    const data = res.data
    form.title = data.title
    form.description = data.description
    form.timeLimit = data.timeLimit
    form.memoryLimit = data.memoryLimit
    
    // 加载标签
    await loadTags()
  } catch (error) {
    console.error('加载题目失败:', error)
  } finally {
    loading.value = false
  }
}

const loadTags = async () => {
  try {
    const res = await getProblemTags(route.params.id)
    if (res.data) {
      // 后端返回的是 Tag 对象数组，需要提取 tag 字段
      tags.value = res.data.map(item => item.tag)
    }
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const handleAddTag = async () => {
  if (!newTag.value.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  
  if (newTag.value.length > 20) {
    ElMessage.warning('标签长度不能超过20个字符')
    return
  }
  
  try {
    await addProblemTag(route.params.id, newTag.value.trim())
    ElMessage.success('添加标签成功')
    newTag.value = ''
    // 重新加载标签
    await loadTags()
  } catch (error) {
    console.error('添加标签失败:', error)
    ElMessage.error('添加标签失败')
  }
}

const handleRemoveTag = async (tag) => {
  try {
    await delProblemTag(route.params.id, tag)
    ElMessage.success('删除标签成功')
    // 重新加载标签
    await loadTags()
  } catch (error) {
    console.error('删除标签失败:', error)
    ElMessage.error('删除标签失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 获取当前用户 ID
        const userStore = useUserStore()
        const authorId = userStore.userInfo?.id || localStorage.getItem('userId')
        
        if (isEdit.value) {
          // 编辑模式，添加 ID
          const submitData = { ...form, id: route.params.id }
          await editProblem(submitData)
          ElMessage.success('更新成功')
        } else {
          // 创建模式，添加作者 ID
          const submitData = { ...form, authorId: authorId }
          await addProblem(submitData)
          ElMessage.success('创建成功')
        }
        router.push('/problems')
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const goBack = () => {
  router.back()
}

const goToTestCaseEdit = () => {
  if (route.params.id) {
    router.push(`/problem/testcase/${route.params.id}`)
  }
}

onMounted(() => {
  loadProblem()
})
</script>

<style scoped>
.problem-edit {
  padding: 20px;
}

.problem-edit h3 {
  margin: 0 0 15px 0;
  color: var(--color-text-primary);
  font-size: 16px;
}

.tag-management {
  width: 100%;
}

.tags-display {
  margin-bottom: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.add-tag {
  display: flex;
  align-items: center;
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
