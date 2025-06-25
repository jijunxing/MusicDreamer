<template>
  <div class="home-container">
    <!-- 欢迎语 -->
    <div class="welcome-card">
      <div>欢迎您，<span class="user-name">{{ user.username }}</span> 祝您今天过得开心！</div>
    </div>
  </div>
</template>

<script setup>
  const user = JSON.parse(localStorage.getItem('currentUser') || '{}')
</script>

<style scoped>

.home-container {
padding: 20px;
max-width: 1200px;
margin: 0 auto;
}

.welcome-card {
background: #fff;
padding: 20px;
border-radius: 8px;
box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
margin-bottom: 20px;
}

.user-name {
color: #409EFF;
font-weight: bold;
}

</style>