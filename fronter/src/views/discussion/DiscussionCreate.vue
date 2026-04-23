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
          <el-select v-model="form.category" placeholder="请选择板块分类" style="width: 100%">
            <el-option label="综合讨论" value="GENERAL" />
            <el-option label="题目求助" value="PROBLEM_HELP" />
            <el-option label="算法交流" value="ALGORITHM" />
            <el-option label="Bug反馈" value="BUG_REPORT" />
            <el-option label="建议意见" value="SUGGESTION" />
          </el-select>
        </el-form-item>

        <el-form-item label="关联题目" prop="problemId">
          <el-input-number 
            v-model="form.problemId" 
            :min="1" 
            placeholder="可选，输入题目ID"
            style="width: 100%"
          />
          <div class="form-tip">如果不关联题目，请留空</div>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="15"
            placeholder="请输入帖子内容（支持Markdown格式）"
            maxlength="5000"
            show-word-limit
          />
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
import { createPost, updatePost, getPostById } from '@/api/discussion'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const submitting = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = ref({
  title: '',
  content: '',
  category: 'GENERAL', // 默认板块
  problemId: null,
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
      category: post.category || 'GENERAL',
      problemId: post.problemId,
      authorId: post.authorId
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载帖子失败')
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
</style>
