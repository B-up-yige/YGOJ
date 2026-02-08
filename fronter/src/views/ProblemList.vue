<template>
  <div class="problem-list-container">
    <div class="page-header">
      <h1>题目列表</h1>
      <div class="search-box">
        <input type="text" v-model="searchKeyword" placeholder="搜索题目..." />
        <button class="btn-search">搜索</button>
      </div>
    </div>
    
    <div class="filter-section">
      <div class="difficulty-filter">
        <span>难度:</span>
        <button 
          v-for="level in difficultyLevels" 
          :key="level.value"
          :class="['btn-difficulty', { active: selectedDifficulty === level.value }]"
          @click="selectedDifficulty = level.value"
        >
          {{ level.label }}
        </button>
      </div>
    </div>
    
    <div class="problem-table">
      <table>
        <thead>
          <tr>
            <th>编号</th>
            <th>标题</th>
            <th>难度</th>
            <th>通过率</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr 
            v-for="problem in filteredProblems" 
            :key="problem.id"
            class="problem-item"
          >
            <td>{{ problem.id }}</td>
            <td>
              <router-link :to="`/problem/${problem.id}`" class="problem-title">{{ problem.title }}</router-link>
            </td>
            <td>
              <span :class="['difficulty-badge', `difficulty-${problem.difficulty}`]">
                {{ problem.difficulty }}
              </span>
            </td>
            <td>{{ problem.acceptanceRate }}%</td>
            <td>
              <router-link :to="`/problem/${problem.id}`" class="btn-view">查看</router-link>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <div class="pagination">
      <button 
        class="btn-page" 
        :disabled="currentPage === 1"
        @click="currentPage--"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页
      </span>
      <button 
        class="btn-page" 
        :disabled="currentPage === totalPages"
        @click="currentPage++"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProblemList',
  data() {
    return {
      problems: [
        { id: 1001, title: '两数之和', difficulty: '简单', acceptanceRate: 45.6 },
        { id: 1002, title: '两数相加', difficulty: '中等', acceptanceRate: 35.2 },
        { id: 1003, title: '无重复字符的最长子串', difficulty: '中等', acceptanceRate: 30.5 },
        { id: 1004, title: '寻找两个正序数组的中位数', difficulty: '困难', acceptanceRate: 28.9 },
        { id: 1005, title: '最长回文子串', difficulty: '中等', acceptanceRate: 25.8 },
        { id: 1006, title: 'Z字形变换', difficulty: '中等', acceptanceRate: 40.1 },
        { id: 1007, title: '整数反转', difficulty: '简单', acceptanceRate: 32.1 },
        { id: 1008, title: '字符串转换整数 (atoi)', difficulty: '中等', acceptanceRate: 15.9 },
        { id: 1009, title: '回文数', difficulty: '简单', acceptanceRate: 50.1 },
        { id: 1010, title: '正则表达式匹配', difficulty: '困难', acceptanceRate: 20.3 }
      ],
      searchKeyword: '',
      selectedDifficulty: '全部',
      currentPage: 1,
      pageSize: 10,
      difficultyLevels: [
        { label: '全部', value: '全部' },
        { label: '简单', value: '简单' },
        { label: '中等', value: '中等' },
        { label: '困难', value: '困难' }
      ]
    }
  },
  computed: {
    filteredProblems() {
      let filtered = this.problems
      
      // 按难度筛选
      if (this.selectedDifficulty !== '全部') {
        filtered = filtered.filter(problem => problem.difficulty === this.selectedDifficulty)
      }
      
      // 按关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(problem => 
          problem.title.toLowerCase().includes(keyword) || 
          problem.id.toString().includes(keyword)
        )
      }
      
      return filtered
    },
    totalPages() {
      return Math.ceil(this.filteredProblems.length / this.pageSize)
    }
  }
}
</script>

<style scoped>
.problem-list-container {
  padding: 2rem 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  color: #333;
}

.search-box {
  display: flex;
  gap: 0.5rem;
}

.search-box input {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 0.3rem;
  width: 300px;
}

.btn-search {
  padding: 0.5rem 1rem;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 0.3rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-search:hover {
  background-color: #5a6fd8;
}

.filter-section {
  margin-bottom: 1.5rem;
}

.difficulty-filter {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.difficulty-filter span {
  font-weight: 500;
}

.btn-difficulty {
  padding: 0.3rem 0.8rem;
  border: 1px solid #ddd;
  border-radius: 1rem;
  background-color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-difficulty.active {
  background-color: #667eea;
  color: white;
  border-color: #667eea;
}

.problem-table {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.problem-table table {
  width: 100%;
  border-collapse: collapse;
}

.problem-table th,
.problem-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.problem-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.problem-item:hover {
  background-color: #f8f9fa;
}

.problem-title {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.problem-title:hover {
  color: #5a6fd8;
  text-decoration: underline;
}

.difficulty-badge {
  padding: 0.2rem 0.6rem;
  border-radius: 1rem;
  font-size: 0.8rem;
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

.btn-view {
  padding: 0.4rem 0.8rem;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 0.3rem;
  text-decoration: none;
  font-size: 0.8rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-view:hover {
  background-color: #45a049;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.btn-page {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 0.3rem;
  background-color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-page:hover:not(:disabled) {
  background-color: #f8f9fa;
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .problem-list-container {
    padding: 1.5rem 0;
  }
  
  .search-box input {
    width: 250px;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .search-box {
    width: 100%;
  }
  
  .search-box input {
    flex: 1;
    width: 100%;
  }
  
  .difficulty-filter {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .difficulty-filter span {
    width: 100%;
  }
  
  .problem-table {
    overflow-x: auto;
  }
  
  .problem-table table {
    min-width: 600px;
  }
}

@media (max-width: 480px) {
  .problem-list-container {
    padding: 1rem 0;
  }
  
  .btn-difficulty {
    padding: 0.3rem 0.6rem;
    font-size: 0.8rem;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .btn-page {
    padding: 0.4rem 0.8rem;
    font-size: 0.8rem;
  }
}
</style>