<template>
  <div class="contest-edit">
    <el-card>
      <template #header>
        <h2>{{ isEdit ? '编辑比赛' : '创建比赛' }}</h2>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="比赛标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入比赛标题" />
        </el-form-item>

        <el-form-item label="比赛描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请输入比赛描述"
          />
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
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
import { getContestInfo, addContest, editContest } from '@/api/contest'
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
  startTime: '',
  endTime: '',
  authorId: null
})

const rules = {
  title: [
    { required: true, message: '请输入比赛标题', trigger: 'blur' },
    { min: 3, max: 100, message: '标题长度在 3-100 个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入比赛描述', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

const loadContest = async () => {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    const res = await getContestInfo(route.params.id)
    const data = res.data
    form.title = data.title
    form.description = data.description
    form.startTime = data.startTime
    form.endTime = data.endTime
  } catch (error) {
    console.error('加载比赛失败:', error)
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
      // 设置作者ID
      form.authorId = userStore.userInfo?.id || 1
      
      if (isEdit.value) {
        form.id = route.params.id
        await editContest(form)
        ElMessage.success('更新成功')
      } else {
        await addContest(form)
        ElMessage.success('创建成功')
      }
      router.push('/contests')
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
  loadContest()
})
</script>

<style scoped>
.contest-edit {
  padding: 20px;
}
</style>
