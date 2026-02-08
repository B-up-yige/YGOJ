<template>
  <div class="app-container">
    <header class="navbar">
      <div class="navbar-container">
        <router-link to="/" class="logo">YGOJ</router-link>
        <nav class="nav-links">
          <router-link to="/">首页</router-link>
          <router-link to="/problems">题目</router-link>
          <router-link to="/ranking">排行榜</router-link>
          <router-link to="/discussion">讨论</router-link>
        </nav>
        <div class="user-section">
          <button class="btn-login">登录</button>
          <button class="btn-register">注册</button>
        </div>
      </div>
    </header>
    
    <main class="main-content">
      <router-view></router-view>
    </main>
    
    <footer class="footer">
      <div class="footer-container">
        <p>© 2026 YGOJ - 在线评测系统</p>
      </div>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  line-height: 1.6;
  color: #333;
  background-color: #f5f5f5;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  background-color: #f8f9fa;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #667eea;
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: 2rem;
}

.nav-links a {
  text-decoration: none;
  color: #333;
  font-weight: 500;
  transition: color 0.3s;
}

.nav-links a:hover {
  color: #667eea;
}

.user-section {
  display: flex;
  gap: 1rem;
}

.btn-login, .btn-register {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.3rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-login {
  background-color: transparent;
  color: #667eea;
  border: 1px solid #667eea;
}

.btn-login:hover {
  background-color: #667eea;
  color: white;
}

.btn-register {
  background-color: #667eea;
  color: white;
}

.btn-register:hover {
  background-color: #5a6fd8;
}

.main-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 2rem;
}

.footer {
  background-color: #333;
  color: white;
  padding: 2rem 0;
  margin-top: 2rem;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .navbar-container,
  .main-content,
  .footer-container {
    padding: 0 1.5rem;
  }
  
  .nav-links {
    gap: 1.5rem;
  }
}

@media (max-width: 768px) {
  .navbar-container {
    flex-wrap: wrap;
    height: auto;
    padding: 1rem 1rem;
  }
  
  .nav-links {
    width: 100%;
    justify-content: center;
    margin: 1rem 0;
    gap: 1rem;
  }
  
  .user-section {
    width: 100%;
    justify-content: center;
  }
  
  .main-content {
    padding: 1.5rem 1rem;
  }
  
  .page-header h1 {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .logo {
    font-size: 1.2rem;
  }
  
  .nav-links {
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
  }
  
  .user-section {
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
  }
  
  .btn-login,
  .btn-register {
    width: 100%;
    max-width: 200px;
    text-align: center;
  }
}
</style>