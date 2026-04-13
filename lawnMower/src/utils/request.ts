import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8081', // ✅ 改成你后端地址
  timeout: 10000,
})

// 你要是有 token 再加拦截器，这里先不写

export default request
