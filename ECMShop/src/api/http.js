// src/api/http.js
// ════════════════════════════════════════════════════════
// axios instance กลาง — แนบ JWT ทุก request และเด้งออกเฉพาะเมื่อ token หมดอายุจริง
// ════════════════════════════════════════════════════════
import axios from 'axios'

const http = axios.create({
  baseURL: 'http://localhost:8081'
})

// ── ขาออก: แนบ token จาก localStorage ทุกครั้ง ──
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ── ขาเข้า: จัดการ error ──
http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status

    // 401 = token หมดอายุ / ไม่ถูกต้อง / ไม่มี token → ออกจากระบบจริง
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.dispatchEvent(new CustomEvent('auth-expired'))
    }

    // 403 = ล็อกอินอยู่ แต่ "ไม่มีสิทธิ์" เข้าทรัพยากรนี้
    // ❌ ไม่ใช่ token หมดอายุ — ไม่ต้อง logout แค่ปล่อยให้ตัวเรียกจัดการ error เอง
    // (เช่น customer พยายามเข้า endpoint admin)

    return Promise.reject(error)
  }
)

export default http