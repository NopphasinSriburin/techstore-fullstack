<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <div class="brand-icon">📝</div>
        <h2>สมัครสมาชิกใหม่</h2>
        <p class="subtitle">สร้างบัญชีผู้ใช้ของคุณเพื่อเริ่มช้อปปิ้งหรือจัดการระบบ</p>
      </div>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>ชื่อผู้ใช้งาน</label>
          <input v-model="form.username" type="text" placeholder="กรอกชื่อผู้ใช้งาน" required />
        </div>

        <div class="form-group">
          <label>อีเมล</label>
          <input v-model="form.email" type="email" placeholder="example@mail.com" required />
        </div>

        <div class="form-group">
          <label>รหัสผ่าน</label>
          <input v-model="form.password" type="password" placeholder="อย่างน้อย 6 ตัวอักษร" required />
        </div>

        <div class="form-group">
          <label>ประเภทบัญชี (เพื่อทดสอบสิทธิ์)</label>
          <select v-model="form.role" class="role-select">
            <option value="CUSTOMER">🛒 ลูกค้าทั่วไป (Customer)</option>
            <option value="ADMIN">⚙️ ผู้ดูแลระบบ (Admin)</option>
          </select>
        </div>

        <button type="submit" class="auth-btn">ลงทะเบียน ✨</button>
      </form>

      <div class="auth-footer">
        มีบัญชีอยู่แล้วใช่ไหม?
        <a href="#" @click.prevent="$emit('switch-page', 'login')">เข้าสู่ระบบที่นี่</a>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Register',
  emits: ['switch-page', 'register-success'],
  data() {
    return {
      form: { username: '', email: '', password: '', role: 'CUSTOMER' }
    }
  },
  methods: {
    async handleRegister() {
      try {
        await axios.post('http://localhost:8081/api/auth/register', this.form);
        alert(`🎉 สมัครสมาชิกสิทธิ์ ${this.form.role} สำเร็จแล้ว! กรุณาเข้าสู่ระบบ`);
        this.$emit('register-success');
        this.$emit('switch-page', 'login');
      } catch (error) {
        console.error(error);
        alert(error.response?.data || '❌ เกิดข้อผิดพลาดในการสมัครสมาชิก');
      }
    }
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 90vh;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-family: 'Prompt', sans-serif;
}
.auth-card {
  background: #fff;
  padding: 40px 36px;
  border-radius: 18px;
  width: 100%;
  max-width: 440px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  animation: pop 0.35s ease;
}
@keyframes pop {
  from { opacity: 0; transform: translateY(14px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.auth-brand { text-align: center; margin-bottom: 28px; }
.brand-icon {
  width: 64px; height: 64px;
  margin: 0 auto 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 30px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16px;
  box-shadow: 0 8px 18px rgba(102, 126, 234, 0.4);
}
.auth-card h2 { margin: 0 0 6px; color: #1a202c; font-size: 1.6rem; }
.subtitle { color: #718096; font-size: 0.85rem; margin: 0; }
.form-group { display: flex; flex-direction: column; margin-bottom: 18px; }
.form-group label { font-size: 0.85rem; font-weight: 600; color: #4a5568; margin-bottom: 6px; }
.form-group input, .role-select {
  padding: 12px 14px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-family: 'Prompt', sans-serif;
  font-size: 0.9rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.role-select { background-color: #fff; cursor: pointer; }
.form-group input:focus, .role-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}
.auth-btn {
  width: 100%;
  padding: 13px;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  margin-top: 8px;
  color: #fff;
  font-family: 'Prompt', sans-serif;
  background: linear-gradient(135deg, #667eea, #764ba2);
  transition: transform 0.15s, box-shadow 0.2s;
}
.auth-btn:hover { transform: translateY(-2px); box-shadow: 0 10px 22px rgba(102, 126, 234, 0.4); }
.auth-btn:active { transform: translateY(0); }
.auth-footer { margin-top: 22px; text-align: center; font-size: 0.85rem; color: #718096; }
.auth-footer a { color: #667eea; text-decoration: none; font-weight: 600; }
.auth-footer a:hover { text-decoration: underline; }
</style>