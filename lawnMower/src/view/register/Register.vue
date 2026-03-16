<template>
  <div class="page">
    <el-card class="card" shadow="always">
      <div class="title">除草机管理系统 - 注册</div>
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

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password />
        </el-form-item>

        <!-- 提示信息 -->
        <el-alert type="info" show-icon :closable="false" class="footer">
          <p class="tips">
            完成注册成功系统将自动生成您的用户账号，第一次成功注册后会自动进入管理界面
          </p>
        </el-alert>

        <!-- 去登录 -->
        <div class="login-link">
          <span>已有账号？</span>
          <el-button type="primary" link  @click="goLogin">
            去登录
          </el-button>
        </div>

        <!-- 注册按钮 -->
        <el-button 
          type="primary"
          size="large"
          style="width: 100%"
          @click="onRegister"
        >
          注册
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>


<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus' // 🌟 新增：用于弹窗提示
import axios from 'axios'
const router = useRouter()
const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})
// 判断两次输入的密码是否一致
const validateConfirmPassword = (_: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}
// 为不同的定义表单定义不同的规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}
const onRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    // 🌟 补充真实的注册请求代码
    try {
      // 向我们刚才在后端写好的 register 接口发请求
      // 注意：发给后端时只需要 username 和 password，不需要发 confirmPassword
      const res = await axios.post('http://localhost:8080/auth/register', {
        username: form.username,
        password: form.password
      })

      // 注册成功提示，并跳转到登录页让用户登录
      ElMessage.success('注册成功，请登录您的账号！')
      router.push('/login')

    } catch (error: any) {
      // 注册失败（比如用户名被占用了），捕获后端的报错并提示
      console.error('注册报错:', error)
      // 获取后端的报错信息，如果没有则显示默认提示
      const errorMsg = error.response?.data?.message || '注册失败，该用户名可能已被占用'
      ElMessage.error(errorMsg)
    }
  })
}
const goLogin = () => {
  router.push('/login')
}



</script>


<style scoped>
.page {
  min-height: 100vh;
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
  margin-top: 10px;
  color: #666;
}

.tips {
  /* margin-top: 14px; */
  justify-content: center;
}
.login-link{
    margin-top: 19px;
    margin-bottom: 14px;
    text-align: center;
}
</style>
