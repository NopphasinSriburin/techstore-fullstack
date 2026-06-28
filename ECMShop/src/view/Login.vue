<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <div class="brand-icon">🔑</div>
        <h2>เข้าสู่ระบบ</h2>
        <p class="subtitle">ยินดีต้อนรับกลับมา! กรุณาเข้าสู่ระบบเพื่อใช้งาน</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>ชื่อผู้ใช้งานหรืออีเมล</label>
          <input v-model="form.username" type="text" placeholder="กรอกชื่อผู้ใช้งานหรืออีเมลของคุณ" required />
        </div>

        <div class="form-group">
          <label>รหัสผ่าน</label>
          <input v-model="form.password" type="password" placeholder="กรอกรหัสผ่าน" required />
        </div>

        <button type="submit" class="auth-btn">เข้าสู่ระบบ 🚀</button>
      </form>

      <div class="auth-footer">
        ยังไม่มีบัญชีสมาชิก?
        <a href="#" @click.prevent="$emit('switch-page', 'register')">สมัครสมาชิกที่นี่</a>
      </div>
    </div>
  </div>
</template>

<script>
import http from '../api/http'   // ★ ใช้ instance ที่แนบ token ให้อัตโนมัติ

export default {
  name: 'Login',
  emits: ['switch-page', 'login-success'],
  data() {
    return {
      form: { username: '', password: '' }
    }
  },
  methods: {
    async handleLogin() {
      try {
        const payload = {
          username: this.form.username.trim(),
          password: this.form.password.trim()
        }
        const response = await http.post('/api/auth/login', payload);
        const data = response.data;   // { token, id, username, email, role }

        // ★ เก็บ token + ข้อมูล user ไว้ใน localStorage
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify({
          id: data.id,
          username: data.username,
          email: data.email,
          role: data.role
        }));

        alert(`👋 ยินดีต้อนรับคุณ ${data.username} (เข้าสู่ระบบสำเร็จ)`);
        this.$emit('login-success', data);
      } catch (error) {
        console.error(error);
        alert(error.response?.data || '❌ ไม่สามารถเข้าสู่ระบบได้');
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
.form-group input {
  padding: 12px 14px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-family: 'Prompt', sans-serif;
  font-size: 0.9rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.form-group input:focus {
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