
<template>
  <div class="page">
    <el-card class="card" shadow="always">
      <div class="title">除草机管理系统 - 登录</div>
        <!-- 加入最下面这两个再失去焦点或者修改时可以自动做校验，之后为每一个表单加入prop属性，目的是判断给某一个表单加入相应的规则 -->
      <el-form
        label-position="top"
        :model="form"
        style="max-width: 600px"
        ref="formRef"
        :rules="rules"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>

        <!-- 去注册 -->
        <div class="login-link">
          <span>没有账号？</span>
          <el-button type="primary" link @click="goRegister">
            去注册
          </el-button>
        </div>

        <!-- 登录按钮 -->
        <el-button
          type="primary"
          size="large"
          style="width: 100%"
          @click="onLogin"
        >
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>


<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus' // 1. 引入消息提示
import axios from 'axios' // 2. 引入 axios
const router = useRouter()
const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  password: '',
})
// 为不同的定义表单定义不同的规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
}
const onLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      // 3. 发送真实请求给后端
      // 假设你的后端运行在 8080 端口，注意地址要写对
      const res = await axios.post('http://localhost:8081/auth/login', {
        username: form.username,
        password: form.password
      })

      // 4. 登录成功后的处理
      console.log('登录成功，后端返回：', res.data)
      
      // 关键步骤：把后端返回的 token 存起来（存在浏览器里）
      // 以后发请求要在 Header 里带上这个 token
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('username', form.username)
      ElMessage.success('登录成功')
      
      // 跳转到主页
      router.push('/main')

    } catch (error: any) {
      // 5. 登录失败的处理
      console.error(error)
      // 如果后端返回了错误信息，显示出来；否则显示默认错误
      const errorMsg = error.response?.data?.message || '登录失败，请检查账号密码'
      ElMessage.error(errorMsg)
    }
  })
}
const goRegister = () => {
  router.push('/register')
}

</script>


<style scoped>
.page {
  min-height: 100vh;
  align-items: center;     /* 垂直居中 */
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  /* background: #f5f7fa; */
}

.card {
  width: 500px;
  border-radius: 14px;
}

.title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 18px;
  text-align: center;
}

.footer {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 8px;
  color: #666;
}

.tips {
  margin-top: 14px;
}
.login-link{
    margin-top: 19px;
    margin-bottom: 14px;
    text-align: center;
}
</style>
