<template>
  <div class="problemset-edit">
    <el-card>
      <template #header>
        <h2>{{ isEdit ? '编辑题集' : '创建题集' }}</h2>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="题集标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入题集标题" />
        </el-form-item>

        <el-form-item label="题集描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请输入题集描述"
          />
        </el-form-item>

        <el-form-item label="是否公开" prop="isPublic">
          <el-switch v-model="form.isPublic" active-text="公开" inactive-text="私有" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '更新' : '创建' }}
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
import { getProblemsetInfo, addProblemset, editProblemset } from '@/api/problemset'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  description: '',
  isPublic: true,
  authorId: null
})

const rules = {
  title: [
    { required: true, message: '请输入题集标题', trigger: 'blur' },
    { min: 3, max: 100, message: '标题长度在 3-100 个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入题集描述', trigger: 'blur' }
  ]
}

const loadProblemset = async () => {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    const res = await getProblemsetInfo(route.params.id)
    const data = res.data
    form.title = data.title
    form.description = data.description
    form.isPublic = data.isPublic
  } catch (error) {
    console.error('加载题集失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      form.authorId = userStore.userInfo?.id || 1
      
      if (isEdit.value) {
        form.id = route.params.id
        await editProblemset(form)
        ElMessage.success('更新成功')
      } else {
        await addProblemset(form)
        ElMessage.success('创建成功')
      }
      router.push('/problemsets')
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadProblemset()
})
</script>

<style scoped>
.problemset-edit {
  padding: 20px;
}
</style>
