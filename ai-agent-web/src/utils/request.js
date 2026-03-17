import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 120000
})

request.interceptors.request.use(
  config => {
    const userId = localStorage.getItem('userId') || 1
    config.headers['X-User-Id'] = userId
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    return Promise.reject(error)
  }
)

export default request
