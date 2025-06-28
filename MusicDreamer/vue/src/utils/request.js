import { ElMessage } from 'element-plus'
import router from '../router'
import axios from "axios";

const request = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL,
    timeout: 30000  // 后台接口超时时间设置
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    // 从本地存储获取JWT令牌
    const token = localStorage.getItem('jwt_token');
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }
    // 只有不是 FormData 才加 json header
    if (!(config.data instanceof FormData)) {
        config.headers['Content-Type'] = 'application/json;charset=utf-8';
    }
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        // 处理业务错误（如权限不足）
        if (res.code && res.code !== '200') {
            ElMessage.error(res.msg || '操作失败');
            return Promise.reject(res);
        }
        return res;
    },
        error => {
        console.log('err' + error)
            // 处理HTTP错误
            if (error.response) {
                const status = error.response.status;
                switch (status) {
                    case 401: // 未认证（JWT过期或无效）
                        ElMessage.error('登录已过期，请重新登录');
                        localStorage.removeItem('jwt_token');
                        router.push('/login');
                        break;
                    case 403: // 权限不足
                        ElMessage.error('权限不足，无法访问此资源');
                        break;
                    case 500: // 服务器错误
                        ElMessage.error('服务器内部错误');
                        break;
                    default:
                        ElMessage.error(error.response.data?.msg || '请求失败');
                }
            } else {
                ElMessage.error('网络错误，请稍后再试');
            }
            return Promise.reject(error);
    }
)


export default request
