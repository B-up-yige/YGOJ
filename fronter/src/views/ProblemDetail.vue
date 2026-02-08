<template>
  <div class="problem-detail-container">
    <div class="problem-header">
      <h1>{{ problem.title }}</h1>
      <div class="problem-meta">
        <span class="problem-id">编号: {{ problem.id }}</span>
        <span :class="['difficulty-badge', `difficulty-${problem.difficulty}`]">
          {{ problem.difficulty }}
        </span>
        <span class="acceptance-rate">通过率: {{ problem.acceptanceRate }}%</span>
      </div>
    </div>
    
    <div class="problem-content">
      <div class="problem-description">
        <h2>题目描述</h2>
        <div class="description-content">
          {{ problem.description }}
        </div>
      </div>
      
      <div class="problem-examples">
        <h2>输入输出示例</h2>
        <div class="example-item" v-for="(example, index) in problem.examples" :key="index">
          <div class="example-input">
            <h3>输入 {{ index + 1 }}:</h3>
            <pre>{{ example.input }}</pre>
          </div>
          <div class="example-output">
            <h3>输出 {{ index + 1 }}:</h3>
            <pre>{{ example.output }}</pre>
          </div>
        </div>
      </div>
      
      <div class="problem-constraints">
        <h2>约束条件</h2>
        <ul>
          <li v-for="(constraint, index) in problem.constraints" :key="index">
            {{ constraint }}
          </li>
        </ul>
      </div>
    </div>
    
    <div class="code-editor-section">
      <div class="editor-header">
        <h2>编写代码</h2>
        <div class="language-selector">
          <label>语言:</label>
          <select v-model="selectedLanguage">
            <option value="cpp">C++</option>
            <option value="java">Java</option>
            <option value="python">Python</option>
            <option value="javascript">JavaScript</option>
          </select>
        </div>
      </div>
      
      <div class="editor-container">
        <CodeEditor v-model="code" :language="selectedLanguage" />
      </div>
      
      <div class="submit-section">
        <button class="btn-submit" @click="submitCode">提交代码</button>
        <div class="submit-hint">
          <span>提示: 提交代码后将跳转到结果页面</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import CodeEditor from '../components/CodeEditor.vue'

export default {
  name: 'ProblemDetail',
  components: {
    CodeEditor
  },
  data() {
    return {
      problem: {
        id: 1001,
        title: '两数之和',
        difficulty: '简单',
        acceptanceRate: 45.6,
        description: '给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出 和为目标值 target  的那 两个 整数，并返回它们的数组下标。\n\n你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。\n\n你可以按任意顺序返回答案。',
        examples: [
          {
            input: 'nums = [2,7,11,15], target = 9',
            output: '[0,1]'
          },
          {
            input: 'nums = [3,2,4], target = 6',
            output: '[1,2]'
          },
          {
            input: 'nums = [3,3], target = 6',
            output: '[0,1]'
          }
        ],
        constraints: [
          '2 <= nums.length <= 10^4',
          '-10^9 <= nums[i] <= 10^9',
          '-10^9 <= target <= 10^9',
          '只会存在一个有效答案'
        ]
      },
      selectedLanguage: 'cpp',
      code: ''
    }
  },
  mounted() {
    // 模拟从API获取题目数据
    this.loadProblemData()
  },
  methods: {
    loadProblemData() {
      // 这里应该是从API获取数据的逻辑
      // 现在使用模拟数据
      console.log('Loading problem data for ID:', this.$route.params.id)
    },
    submitCode() {
      // 模拟提交代码
      console.log('Submitting code:', this.code)
      console.log('Language:', this.selectedLanguage)
      // 跳转到结果页面
      this.$router.push(`/submit/${Math.floor(Math.random() * 10000)}`)
    }
  }
}
</script>

<style scoped>
.problem-detail-container {
  padding: 2rem 0;
}

.problem-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.problem-header h1 {
  color: #333;
  margin-bottom: 1rem;
}

.problem-meta {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.problem-id {
  font-weight: 500;
}

.difficulty-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 1rem;
  font-size: 0.9rem;
  font-weight: 500;
}

.difficulty-简单 {
  background-color: #e8f5e8;
  color: #4caf50;
}

.difficulty-中等 {
  background-color: #fff3e0;
  color: #ff9800;
}

.difficulty-困难 {
  background-color: #ffebee;
  color: #f44336;
}

.acceptance-rate {
  font-weight: 500;
}

.problem-content {
  margin-bottom: 3rem;
}

.problem-description,
.problem-examples,
.problem-constraints {
  margin-bottom: 2rem;
}

.problem-description h2,
.problem-examples h2,
.problem-constraints h2 {
  color: #333;
  margin-bottom: 1rem;
  font-size: 1.3rem;
}

.description-content {
  line-height: 1.6;
  white-space: pre-line;
}

.example-item {
  margin-bottom: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 0.5rem;
  padding: 1rem;
}

.example-input h3,
.example-output h3 {
  font-size: 1rem;
  margin-bottom: 0.5rem;
  color: #555;
}

.example-input pre,
.example-output pre {
  background-color: #f1f1f1;
  padding: 1rem;
  border-radius: 0.3rem;
  font-family: 'Courier New', Courier, monospace;
  white-space: pre-wrap;
}

.problem-constraints ul {
  list-style-type: disc;
  padding-left: 2rem;
}

.problem-constraints li {
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.code-editor-section {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.editor-header h2 {
  color: #333;
  font-size: 1.3rem;
}

.language-selector {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.language-selector select {
  padding: 0.4rem;
  border: 1px solid #ddd;
  border-radius: 0.3rem;
}

.editor-container {
  margin-bottom: 1.5rem;
}

.code-textarea {
  width: 100%;
  height: 400px;
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 0.5rem;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  line-height: 1.5;
  resize: vertical;
}

.submit-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-submit {
  padding: 0.8rem 2rem;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 0.3rem;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-submit:hover {
  background-color: #45a049;
}

.submit-hint {
  color: #666;
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .problem-detail-container {
    padding: 1.5rem 0;
  }
}

@media (max-width: 768px) {
  .problem-detail-container {
    padding: 1rem 0;
  }
  
  .problem-header h1 {
    font-size: 1.5rem;
  }
  
  .problem-meta {
    flex-wrap: wrap;
    gap: 1rem;
  }
  
  .problem-description h2,
  .problem-examples h2,
  .problem-constraints h2,
  .editor-header h2 {
    font-size: 1.1rem;
  }
  
  .editor-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .language-selector {
    width: 100%;
  }
  
  .language-selector select {
    flex: 1;
    width: 100%;
  }
  
  .submit-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .btn-submit {
    width: 100%;
  }
  
  .example-input pre,
  .example-output pre {
    font-size: 0.9rem;
    padding: 0.8rem;
  }
}

@media (max-width: 480px) {
  .problem-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .difficulty-badge {
    padding: 0.2rem 0.6rem;
    font-size: 0.8rem;
  }
  
  .code-editor-section {
    padding: 1rem;
  }
  
  .btn-submit {
    padding: 0.6rem 1.5rem;
    font-size: 0.9rem;
  }
}
</style>