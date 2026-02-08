<template>
  <div class="home-container">
    <div class="hero-section">
      <h1>YGOJ - 在线评测系统</h1>
      <p>挑战算法，提升编程能力</p>
      <div class="buttons">
        <router-link to="/problems" class="btn-primary">浏览题目</router-link>
      </div>
    </div>
    
    <div class="stats-section">
      <div class="stat-card">
        <h3>题目数量</h3>
        <p>100+</p>
      </div>
      <div class="stat-card">
        <h3>用户数量</h3>
        <p>1000+</p>
      </div>
      <div class="stat-card">
        <h3>提交次数</h3>
        <p>10000+</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  padding: 2rem;
}

.hero-section {
  text-align: center;
  padding: 4rem 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 1rem;
  margin-bottom: 2rem;
}

.hero-section h1 {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.hero-section p {
  font-size: 1.2rem;
  margin-bottom: 2rem;
}

.buttons {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.btn-primary {
  background-color: #4CAF50;
  color: white;
  padding: 0.8rem 1.6rem;
  border-radius: 0.5rem;
  text-decoration: none;
  font-weight: bold;
  transition: background-color 0.3s;
}

.btn-primary:hover {
  background-color: #45a049;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-top: 2rem;
}

.stat-card {
  background: white;
  padding: 2rem;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-card h3 {
  color: #333;
  margin-bottom: 0.5rem;
}

.stat-card p {
  font-size: 2rem;
  font-weight: bold;
  color: #667eea;
}
</style>