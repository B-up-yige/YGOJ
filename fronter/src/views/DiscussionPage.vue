<template>
  <div class="discussion-container">
    <div class="page-header">
      <h1>讨论</h1>
      <div class="search-box">
        <input type="text" v-model="searchKeyword" placeholder="搜索讨论..." />
        <button class="btn-search">搜索</button>
      </div>
    </div>
    
    <div class="discussion-tabs">
      <button 
        v-for="tab in discussionTabs" 
        :key="tab.value"
        :class="['tab-button', { active: activeTab === tab.value }]"
        @click="activeTab = tab.value"
      >
        {{ tab.label }}
      </button>
    </div>
    
    <div class="discussion-list">
      <div 
        v-for="discussion in filteredDiscussions" 
        :key="discussion.id"
        class="discussion-item"
      >
        <div class="discussion-header">
          <h3 class="discussion-title">{{ discussion.title }}</h3>
          <span class="discussion-meta">
            <span class="author">{{ discussion.author }}</span>
            <span class="time">{{ discussion.time }}</span>
          </span>
        </div>
        <div class="discussion-content">
          {{ discussion.content }}
        </div>
        <div class="discussion-footer">
          <span class="views">{{ discussion.views }} 浏览</span>
          <span class="replies">{{ discussion.replies }} 回复</span>
          <span :class="['tag', `tag-${discussion.tag}`]">{{ discussion.tag }}</span>
        </div>
      </div>
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
  name: 'DiscussionPage',
  data() {
    return {
      activeTab: 'all',
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10,
      discussionTabs: [
        { label: '全部', value: 'all' },
        { label: '题目讨论', value: 'problem' },
        { label: '系统公告', value: 'announcement' },
        { label: '经验分享', value: 'experience' }
      ],
      discussions: [
        {
          id: 1,
          title: '关于两数之和的最优解法',
          author: 'user1',
          time: '2026-02-01 10:30',
          content: '大家好，我想和大家讨论一下两数之和的最优解法，除了哈希表还有其他更高效的方法吗？',
          views: 120,
          replies: 15,
          tag: 'problem'
        },
        {
          id: 2,
          title: '系统更新公告',
          author: 'admin',
          time: '2026-02-01 09:00',
          content: '系统将于今晚进行维护更新，预计 downtime 为 2 小时，请大家提前做好准备。',
          views: 350,
          replies: 5,
          tag: 'announcement'
        },
        {
          id: 3,
          title: '分享一下我的刷题经验',
          author: 'user2',
          time: '2026-01-31 16:45',
          content: '最近刷了 100 道算法题，总结了一些经验，希望对大家有帮助...',
          views: 200,
          replies: 25,
          tag: 'experience'
        },
        {
          id: 4,
          title: '求助：最长回文子串的动态规划解法',
          author: 'user3',
          time: '2026-01-31 14:20',
          content: '我在理解最长回文子串的动态规划解法时遇到了困难，有没有大佬能详细解释一下？',
          views: 180,
          replies: 20,
          tag: 'problem'
        },
        {
          id: 5,
          title: '推荐一些优质的算法学习资源',
          author: 'user4',
          time: '2026-01-30 11:15',
          content: '想给大家推荐一些我认为不错的算法学习资源，包括书籍、视频和网站...',
          views: 250,
          replies: 30,
          tag: 'experience'
        }
      ]
    }
  },
  computed: {
    filteredDiscussions() {
      let filtered = this.discussions
      
      // 按标签筛选
      if (this.activeTab !== 'all') {
        filtered = filtered.filter(discussion => discussion.tag === this.activeTab)
      }
      
      // 按关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(discussion => 
          discussion.title.toLowerCase().includes(keyword) || 
          discussion.content.toLowerCase().includes(keyword)
        )
      }
      
      return filtered
    },
    totalPages() {
      return Math.ceil(this.filteredDiscussions.length / this.pageSize)
    }
  }
}
</script>

<style scoped>
.discussion-container {
  padding: 2rem 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
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

.discussion-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
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

.discussion-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.discussion-item {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  transition: transform 0.3s, box-shadow 0.3s;
}

.discussion-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.discussion-header {
  margin-bottom: 1rem;
}

.discussion-title {
  color: #333;
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
}

.discussion-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.9rem;
  color: #666;
}

.discussion-content {
  margin-bottom: 1rem;
  line-height: 1.6;
  color: #555;
}

.discussion-footer {
  display: flex;
  gap: 1.5rem;
  font-size: 0.9rem;
  color: #666;
  align-items: center;
}

.tag {
  padding: 0.2rem 0.6rem;
  border-radius: 1rem;
  font-size: 0.8rem;
  font-weight: 500;
}

.tag-problem {
  background-color: #e3f2fd;
  color: #2196f3;
}

.tag-announcement {
  background-color: #fff3e0;
  color: #ff9800;
}

.tag-experience {
  background-color: #e8f5e8;
  color: #4caf50;
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
  .discussion-container {
    padding: 1.5rem 0;
  }
  
  .search-box input {
    width: 250px;
  }
}

@media (max-width: 768px) {
  .discussion-container {
    padding: 1rem 0;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .page-header h1 {
    font-size: 1.5rem;
  }
  
  .search-box {
    width: 100%;
  }
  
  .search-box input {
    flex: 1;
    width: 100%;
  }
  
  .discussion-tabs {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .tab-button {
    padding: 0.4rem 0.8rem;
    font-size: 0.9rem;
  }
  
  .discussion-item {
    padding: 1.2rem;
  }
  
  .discussion-title {
    font-size: 1rem;
  }
  
  .discussion-meta {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .discussion-footer {
    flex-wrap: wrap;
    gap: 1rem;
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
  .discussion-tabs {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .tab-button {
    width: 100%;
    text-align: center;
  }
  
  .discussion-item {
    padding: 1rem;
  }
  
  .discussion-content {
    font-size: 0.9rem;
  }
  
  .tag {
    padding: 0.1rem 0.4rem;
    font-size: 0.7rem;
  }
}
</style>