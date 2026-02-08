<template>
  <div class="ranking-container">
    <div class="page-header">
      <h1>排行榜</h1>
      <div class="ranking-tabs">
        <button 
          v-for="tab in rankingTabs" 
          :key="tab.value"
          :class="['tab-button', { active: activeTab === tab.value }]"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>
    
    <div class="ranking-table">
      <table>
        <thead>
          <tr>
            <th>排名</th>
            <th>用户名</th>
            <th>通过题目数</th>
            <th>提交次数</th>
            <th>准确率</th>
          </tr>
        </thead>
        <tbody>
          <tr 
            v-for="(user, index) in rankingList" 
            :key="user.id"
            class="ranking-item"
          >
            <td>{{ index + 1 }}</td>
            <td class="username">{{ user.username }}</td>
            <td>{{ user.solved }}</td>
            <td>{{ user.submissions }}</td>
            <td>{{ user.accuracy }}%</td>
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
  name: 'RankingPage',
  data() {
    return {
      activeTab: 'weekly',
      currentPage: 1,
      pageSize: 20,
      rankingTabs: [
        { label: '周榜', value: 'weekly' },
        { label: '月榜', value: 'monthly' },
        { label: '总榜', value: 'total' }
      ],
      rankingList: Array.from({ length: 50 }, (_, i) => ({
        id: i + 1,
        username: `user${i + 1}`,
        solved: Math.floor(Math.random() * 50) + 1,
        submissions: Math.floor(Math.random() * 200) + 10,
        accuracy: Math.floor(Math.random() * 50) + 50
      }))
    }
  },
  computed: {
    totalPages() {
      return Math.ceil(this.rankingList.length / this.pageSize)
    }
  }
}
</script>

<style scoped>
.ranking-container {
  padding: 2rem 0;
}

.page-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.page-header h1 {
  color: #333;
  margin-bottom: 1rem;
}

.ranking-tabs {
  display: flex;
  gap: 1rem;
}

.tab-button {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 0.3rem;
  background-color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-button:hover {
  background-color: #f8f9fa;
}

.tab-button.active {
  background-color: #667eea;
  color: white;
  border-color: #667eea;
}

.ranking-table {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 2rem;
}

.ranking-table table {
  width: 100%;
  border-collapse: collapse;
}

.ranking-table th,
.ranking-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.ranking-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.ranking-item:hover {
  background-color: #f8f9fa;
}

.username {
  font-weight: 500;
  color: #667eea;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
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
  .ranking-container {
    padding: 1.5rem 0;
  }
}

@media (max-width: 768px) {
  .ranking-container {
    padding: 1rem 0;
  }
  
  .page-header h1 {
    font-size: 1.5rem;
  }
  
  .ranking-tabs {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .tab-button {
    padding: 0.4rem 0.8rem;
    font-size: 0.9rem;
  }
  
  .ranking-table {
    overflow-x: auto;
  }
  
  .ranking-table table {
    min-width: 600px;
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

@media (max-width: 480px) {
  .ranking-tabs {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .tab-button {
    width: 100%;
    text-align: center;
  }
  
  .ranking-table table {
    min-width: 400px;
  }
  
  .ranking-table th,
  .ranking-table td {
    padding: 0.8rem;
    font-size: 0.9rem;
  }
}
</style>