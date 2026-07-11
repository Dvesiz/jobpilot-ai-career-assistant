import axios from 'axios'

export const http = axios.create({
  baseURL: '/api',
  timeout: 30_000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('resumeor_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message ?? '请求失败，请稍后重试'
    if (error.response?.status === 401 && localStorage.getItem('resumeor_token')) {
      localStorage.removeItem('resumeor_token')
      localStorage.removeItem('resumeor_resume_id')
      window.location.assign('/login')
    }
    return Promise.reject(new Error(message))
  },
)
