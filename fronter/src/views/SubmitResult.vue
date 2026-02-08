<template>
  <div class="submit-result-container">
    <div class="page-header">
      <h1>提交结果</h1>
      <div class="result-id">
        提交ID: {{ submitId }}
      </div>
    </div>
    
    <div class="result-status">
      <div :class="['status-card', `status-${result.status}`]">
        <div class="status-icon">
          <span v-if="result.status === 'AC'">✓</span>
          <span v-else-if="result.status === 'WA'">✗</span>
          <span v-else-if="result.status === 'TLE'">⏰</span>
          <span v-else-if="result.status === 'MLE'">💾</span>
          <span v-else-if="result.status === 'RE'">💥</span>
          <span v-else-if="result.status === 'CE'">🔧</span>
          <span v-else>🔄</span>
        </div>
        <div class="status-content">
          <h2>{{ result.statusText }}</h2>
          <div class="status-meta">
            <span class="execution-time">执行时间: {{ result.executionTime }}ms</span>
            <span class="memory-usage">内存使用: {{ result.memoryUsage }}MB</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="result-details">
      <div class="test-cases">
        <h2>测试用例</h2>
        <div class="test-case-list">
          <div 
            v-for="(testCase, index) in result.testCases" 
            :key="index"
            :class="['test-case-item', `test-case-${testCase.status}`]"
          >
            <span class="test-case-index">测试用例 {{ index + 1 }}:</span>
            <span class="test-case-status">{{ testCase.statusText }}</span>
            <span v-if="testCase.time" class="test-case-time">{{ testCase.time }}ms</span>
          </div>
        </div>
      </div>
      
      <div v-if="result.errorMessage" class="error-message">
        <h2>错误信息</h2>
        <pre>{{ result.errorMessage }}</pre>
      </div>
      
      <div class="submitted-code">
        <h2>提交的代码</h2>
        <div class="code-container">
          <pre>{{ result.code }}</pre>
        </div>
      </div>
    </div>
    
    <div class="action-buttons">
      <router-link to="/problems" class="btn-back">返回题目列表</router-link>
      <router-link :to="`/problem/${result.problemId}`" class="btn-retry">重新提交</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SubmitResult',
  data() {
    return {
      submitId: '',
      result: {
        status: 'AC',
        statusText: 'Accepted',
        executionTime: 4,
        memoryUsage: 7.8,
        problemId: 1001,
        testCases: [
          { status: 'AC', statusText: '通过', time: 2 },
          { status: 'AC', statusText: '通过', time: 1 },
          { status: 'AC', statusText: '通过', time: 1 }
        ],
        errorMessage: '',
        code: '#include <iostream>\n#include <vector>\n#include <unordered_map>\nusing namespace std;\n\nvector<int> twoSum(vector<int>& nums, int target) {\n    unordered_map<int, int> map;\n    for (int i = 0; i < nums.size(); i++) {\n        int complement = target - nums[i];\n        if (map.find(complement) != map.end()) {\n            return {map[complement], i};\n        }\n        map[nums[i]] = i;\n    }\n    return {};\n}\n\nint main() {\n    vector<int> nums = {2, 7, 11, 15};\n    int target = 9;\n    vector<int> result = twoSum(nums, target);\n    cout << "[" << result[0] << ", " << result[1] << "]" << endl;\n    return 0;\n}'
      }
    }
  },
  mounted() {
    // 获取提交ID
    this.submitId = this.$route.params.id
    // 模拟从API获取结果数据
    this.loadResultData()
  },
  methods: {
    loadResultData() {
      // 这里应该是从API获取数据的逻辑
      // 现在使用模拟数据
      console.log('Loading result data for submit ID:', this.submitId)
      
      // 模拟不同的结果状态
      const statuses = ['AC', 'WA', 'TLE', 'MLE', 'RE', 'CE']
      const statusTexts = {
        'AC': 'Accepted',
        'WA': 'Wrong Answer',
        'TLE': 'Time Limit Exceeded',
        'MLE': 'Memory Limit Exceeded',
        'RE': 'Runtime Error',
        'CE': 'Compilation Error'
      }
      
      // 随机选择一个状态（大部分时间为AC）
      const randomStatus = Math.random() > 0.2 ? 'AC' : statuses[Math.floor(Math.random() * statuses.length)]
      
      // 更新结果数据
      this.result = {
        ...this.result,
        status: randomStatus,
        statusText: statusTexts[randomStatus],
        executionTime: Math.floor(Math.random() * 100) + 1,
        memoryUsage: Math.floor(Math.random() * 20) + 1,
        testCases: Array.from({ length: 3 }, (_, i) => ({
          status: randomStatus === 'AC' ? 'AC' : Math.random() > 0.5 ? 'AC' : randomStatus,
          statusText: randomStatus === 'AC' ? '通过' : Math.random() > 0.5 ? '通过' : statusTexts[randomStatus],
          time: Math.floor(Math.random() * 50) + 1
        })),
        errorMessage: randomStatus !== 'AC' ? '示例错误信息：未通过测试用例 2' : '',
        code: this.result.code
      }
    }
  }
}
</script>

<style scoped>
.submit-result-container {
  padding: 2rem 0;
}

.page-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.page-header h1 {
  color: #333;
  margin-bottom: 0.5rem;
}

.result-id {
  color: #666;
  font-size: 0.9rem;
}

.result-status {
  margin-bottom: 2rem;
}

.status-card {
  display: flex;
  align-items: center;
  gap: 2rem;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.status-AC {
  background-color: #e8f5e8;
  border-left: 4px solid #4caf50;
}

.status-WA {
  background-color: #ffebee;
  border-left: 4px solid #f44336;
}

.status-TLE {
  background-color: #fff3e0;
  border-left: 4px solid #ff9800;
}

.status-MLE {
  background-color: #e3f2fd;
  border-left: 4px solid #2196f3;
}

.status-RE {
  background-color: #f3e5f5;
  border-left: 4px solid #9c27b0;
}

.status-CE {
  background-color: #eeeeee;
  border-left: 4px solid #616161;
}

.status-PENDING {
  background-color: #fff9c4;
  border-left: 4px solid #ffeb3b;
}

.status-icon {
  font-size: 3rem;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.8);
}

.status-content h2 {
  margin-bottom: 0.5rem;
  font-size: 1.5rem;
}

.status-meta {
  display: flex;
  gap: 2rem;
  font-size: 0.9rem;
  color: #666;
}

.result-details {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  margin-bottom: 2rem;
}

.test-cases {
  margin-bottom: 2rem;
}

.test-cases h2,
.error-message h2,
.submitted-code h2 {
  color: #333;
  margin-bottom: 1rem;
  font-size: 1.3rem;
}

.test-case-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.test-case-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.8rem;
  border-radius: 0.3rem;
  background-color: #f8f9fa;
}

.test-case-AC {
  border-left: 3px solid #4caf50;
}

.test-case-WA {
  border-left: 3px solid #f44336;
}

.test-case-TLE {
  border-left: 3px solid #ff9800;
}

.test-case-index {
  font-weight: 500;
  min-width: 100px;
}

.test-case-status {
  flex: 1;
}

.test-case-time {
  font-size: 0.8rem;
  color: #666;
}

.error-message {
  margin-bottom: 2rem;
}

.error-message pre {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 0.3rem;
  font-family: 'Courier New', Courier, monospace;
  white-space: pre-wrap;
  border-left: 3px solid #f44336;
}

.submitted-code {
  margin-bottom: 2rem;
}

.code-container {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 0.3rem;
  overflow-x: auto;
}

.code-container pre {
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-back,
.btn-retry {
  padding: 0.8rem 2rem;
  border: none;
  border-radius: 0.3rem;
  font-size: 1rem;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-back {
  background-color: #667eea;
  color: white;
}

.btn-back:hover {
  background-color: #5a6fd8;
}

.btn-retry {
  background-color: #4caf50;
  color: white;
}

.btn-retry:hover {
  background-color: #45a049;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .submit-result-container {
    padding: 1.5rem 0;
  }
}

@media (max-width: 768px) {
  .submit-result-container {
    padding: 1rem 0;
  }
  
  .page-header h1 {
    font-size: 1.5rem;
  }
  
  .status-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
    padding: 1.5rem;
  }
  
  .status-content h2 {
    font-size: 1.3rem;
  }
  
  .status-meta {
    flex-wrap: wrap;
    gap: 1rem;
  }
  
  .result-details {
    padding: 1.5rem;
  }
  
  .test-cases h2,
  .error-message h2,
  .submitted-code h2 {
    font-size: 1.1rem;
  }
  
  .test-case-item {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .test-case-index {
    min-width: auto;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .btn-back,
  .btn-retry {
    width: 100%;
    text-align: center;
  }
  
  .code-container pre {
    font-size: 0.9rem;
  }
}

@media (max-width: 480px) {
  .status-card {
    padding: 1rem;
  }
  
  .status-icon {
    font-size: 2rem;
    width: 40px;
    height: 40px;
  }
  
  .result-details {
    padding: 1rem;
  }
  
  .btn-back,
  .btn-retry {
    padding: 0.6rem 1.5rem;
    font-size: 0.9rem;
  }
}
</style>