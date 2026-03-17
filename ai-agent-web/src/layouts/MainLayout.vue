<template>
  <div class="layout">
    <el-container>
      <el-aside width="200px" class="sidebar">
        <div class="logo">
          <h3>AI 智能体</h3>
        </div>
        <el-menu
          :default-active="$route.path"
          router
          class="menu"
        >
          <el-menu-item index="/code-review">
            <span>代码审查</span>
          </el-menu-item>
          <el-menu-item index="/chat">
            <span>智能聊天</span>
          </el-menu-item>
          <el-menu-item index="/approval">
            <span>审批流程</span>
          </el-menu-item>
          <el-menu-item index="/knowledge">
            <span>知识库</span>
          </el-menu-item>
          <el-menu-item index="/prompt">
            <span>提示词管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style scoped>
.layout {
  height: 100vh;
}

.sidebar {
  background: #304156;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2b3a4a;
}

.logo h3 {
  color: #fff;
  margin: 0;
  font-size: 18px;
}

.menu {
  border-right: none;
  background: #304156;
}

.menu :deep(.el-menu-item) {
  color: #bfcbd9;
}

.menu :deep(.el-menu-item:hover),
.menu :deep(.el-menu-item.is-active) {
  background: #263445;
  color: #409eff;
}

.main-content {
  padding: 0;
  background: #f5f7fa;
}
</style>
