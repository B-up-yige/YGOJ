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
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="10"
            placeholder="请输入题目描述"
          />
        </el-form-item>

        <el-form-item label="时间限制" prop="timeLimit">
          <el-input-number v-model="form.timeLimit" :min="100" :step="100" style="width: 100%" />
          <span style="margin-left: 10px; color: #999;">(毫秒)</span>
        </el-form-item>

        <el-form-item label="内存限制" prop="memoryLimit">
          <el-input-number v-model="form.memoryLimit" :min="64" :step="64" style="width: 100%" />
          <span style="margin-left: 10px; color: #999;">(MB)</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            提交
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProblemInfo, addProblem, editProblem } from '@/api/problem'

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
  } catch (error) {
    console.error('加载题目失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (isEdit.value) {
          // 编辑模式，添加 ID
          const submitData = { ...form, id: route.params.id }
          await editProblem(submitData)
          ElMessage.success('更新成功')
        } else {
          // 创建模式
          await addProblem(form)
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

onMounted(() => {
  loadProblem()
})
</script>

<style scoped>
.problem-edit {
  padding: 20px;
}
</style>
