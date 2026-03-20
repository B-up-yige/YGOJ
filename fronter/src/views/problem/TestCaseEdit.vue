<template>
  <div class="testcase-edit">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>测试用例管理 - 题目 #{{ problemId }}</h2>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <!-- 添加测试用例表单 -->
      <el-form :model="testCaseForm" :rules="rules" ref="formRef" label-width="120px" style="margin-bottom: 30px;">
        <el-form-item label="添加方式" prop="addMode">
          <el-radio-group v-model="testCaseForm.addMode">
            <el-radio label="text">文本输入</el-radio>
            <el-radio label="file">文件上传</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 文本输入模式 -->
        <template v-if="testCaseForm.addMode === 'text'">
          <el-form-item label="输入数据" prop="inputData">
            <el-input
              v-model="testCaseForm.inputData"
              type="textarea"
              :rows="4"
              placeholder="请输入测试输入数据"
            />
          </el-form-item>

          <el-form-item label="期望输出" prop="expectedOutput">
            <el-input
              v-model="testCaseForm.expectedOutput"
              type="textarea"
              :rows="4"
              placeholder="请输入期望输出结果"
            />
          </el-form-item>
        </template>

        <!-- 文件上传模式 -->
        <template v-if="testCaseForm.addMode === 'file'">
          <el-form-item label="输入文件" prop="inputFile">
            <el-upload
              ref="inputUploadRef"
              :auto-upload="false"
              :limit="1"
              :on-change="handleInputFileChange"
              :on-remove="handleInputFileRemove"
            >
              <el-button type="primary">选择输入文件</el-button>
              <template #tip>
                <div class="el-upload__tip">只能上传一个文件</div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item label="输出文件" prop="outputFile">
            <el-upload
              ref="outputUploadRef"
              :auto-upload="false"
              :limit="1"
              :on-change="handleOutputFileChange"
              :on-remove="handleOutputFileRemove"
            >
              <el-button type="primary">选择输出文件</el-button>
              <template #tip>
                <div class="el-upload__tip">只能上传一个文件</div>
              </template>
            </el-upload>
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" @click="handleAddTestCase" :loading="adding">
            添加测试用例
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 测试用例列表 -->
      <el-divider>测试用例列表</el-divider>

      <el-table :data="testCases" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="inputFileName" label="输入文件" width="300">
          <template #default="scope">
            <el-tag type="info" effect="plain">{{ scope.row.inputFileName || '无' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="outputFileName" label="输出文件" width="300">
          <template #default="scope">
            <el-tag type="success" effect="plain">{{ scope.row.outputFileName || '无' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button link type="danger" @click="handleDeleteTestCase(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTestCaseList, addTestCase, delTestCase } from '@/api/problem'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const adding = ref(false)

const problemId = ref(route.params.id)

// 测试用例列表
const testCases = ref([])

// 添加测试用例表单
const testCaseForm = reactive({
  addMode: 'text', // 'text' 或 'file'
  inputData: '',
  expectedOutput: '',
  inputFile: null,
  outputFile: null
})

// 文件上传引用
const inputUploadRef = ref(null)
const outputUploadRef = ref(null)

const rules = {
  inputData: [
    { required: true, message: '请输入测试输入数据', trigger: 'blur' }
  ],
  expectedOutput: [
    { required: true, message: '请输入期望输出结果', trigger: 'blur' }
  ],
  inputFile: [
    { required: true, message: '请选择输入文件', trigger: 'change' }
  ],
  outputFile: [
    { required: true, message: '请选择输出文件', trigger: 'change' }
  ]
}

// 处理输入文件变化
const handleInputFileChange = (file) => {
  testCaseForm.inputFile = file.raw
}

// 处理输入文件移除
const handleInputFileRemove = () => {
  testCaseForm.inputFile = null
}

// 处理输出文件变化
const handleOutputFileChange = (file) => {
  testCaseForm.outputFile = file.raw
}

// 处理输出文件移除
const handleOutputFileRemove = () => {
  testCaseForm.outputFile = null
}

// 加载测试用例列表
const loadTestCases = async () => {
  loading.value = true
  try {
    const res = await getTestCaseList(problemId.value)
    if (res.data) {
      testCases.value = res.data
    }
  } catch (error) {
    console.error('加载测试用例失败:', error)
    ElMessage.error('加载测试用例失败')
  } finally {
    loading.value = false
  }
}

// 添加测试用例
const handleAddTestCase = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      adding.value = true
      try {
        // 根据添加方式使用不同的提交方法
        if (testCaseForm.addMode === 'text') {
          // 文本模式：将文本保存为临时文件再上传
          const inputDataBlob = new Blob([testCaseForm.inputData], { type: 'text/plain' })
          const outputDataBlob = new Blob([testCaseForm.expectedOutput], { type: 'text/plain' })
          
          const inputFile = new File([inputDataBlob], 'input.txt', { type: 'text/plain' })
          const outputFile = new File([outputDataBlob], 'output.txt', { type: 'text/plain' })
          
          await submitTestcaseWithFiles(inputFile, outputFile)
        } else {
          // 文件模式：直接上传文件
          await submitTestcaseWithFiles(testCaseForm.inputFile, testCaseForm.outputFile)
        }
        
        ElMessage.success('添加成功')
        
        // 清空表单
        resetForm()
        
        // 刷新列表
        loadTestCases()
      } catch (error) {
        console.error('添加失败:', error)
        ElMessage.error('添加失败，请稍后重试')
      } finally {
        adding.value = false
      }
    }
  })
}

// 提交测试用例（使用文件）
const submitTestcaseWithFiles = async (inputFile, outputFile) => {
  const formData = new FormData()
  
  // 添加 problemId
  formData.append('problemId', problemId.value)
  
  // 添加文件
  formData.append('input', inputFile)
  formData.append('output', outputFile)
  
  await addTestCase(formData)
}

// 重置表单
const resetForm = () => {
  testCaseForm.addMode = 'text'
  testCaseForm.inputData = ''
  testCaseForm.expectedOutput = ''
  testCaseForm.inputFile = null
  testCaseForm.outputFile = null
  
  // 清空上传组件
  if (inputUploadRef.value) {
    inputUploadRef.value.clearFiles()
  }
  if (outputUploadRef.value) {
    outputUploadRef.value.clearFiles()
  }
  
  // 重置表单验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 删除测试用例
const handleDeleteTestCase = async (testCase) => {
  try {
    await ElMessageBox.confirm('确定要删除这个测试用例吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await delTestCase(testCase)
    ElMessage.success('删除成功')
    
    // 刷新列表
    loadTestCases()
  } catch {
    // 取消删除
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadTestCases()
})
</script>

<style scoped>
.testcase-edit {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
}
</style>
