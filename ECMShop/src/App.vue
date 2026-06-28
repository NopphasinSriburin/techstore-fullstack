<template>
  <div id="app">

    <!-- ═══════════ NAVBAR (แสดงเฉพาะเมื่อ login แล้ว และไม่ได้อยู่หน้า login/register) ═══════════ -->
    <header v-if="showNavbar" class="navbar">
      <div class="navbar-inner">
        <!-- โลโก้ -->
        <div class="nav-brand" @click="goShop">
          <span class="nav-logo">⚡</span>
          <span class="nav-brand-text">TechStore</span>
        </div>

        <!-- เมนูกลาง -->
        <nav class="nav-links">
          <button
            class="nav-link"
            :class="{ active: page === 'home' && currentView === 'shop' }"
            @click="goShop"
          >🏠 ร้านค้า</button>

          <button
            v-if="!isAdmin"
            class="nav-link"
            :class="{ active: page === 'home' && currentView === 'orders' }"
            @click="goOrders"
          >📋 คำสั่งซื้อ</button>

          <button
            v-if="isAdmin"
            class="nav-link"
            :class="{ active: page === 'admin' }"
            @click="page = 'admin'"
          >⚙️ แผงควบคุม</button>
        </nav>

        <!-- ฝั่งขวา: ตะกร้า (teleport มาลงที่นี่) + ผู้ใช้ -->
        <div class="nav-right">
          <!-- ★ จุดสำคัญ: slot ให้ Mainmenu teleport ไอคอนตะกร้ามาวาง -->
          <div id="navbar-cart-slot"></div>

          <div class="nav-user">
            <span class="nav-user-avatar">{{ userInitial }}</span>
            <div class="nav-user-info">
              <span class="nav-user-name">{{ currentUser?.username }}</span>
              <span class="nav-user-role" :class="isAdmin ? 'role-admin' : 'role-customer'">
                {{ isAdmin ? 'ADMIN' : 'CUSTOMER' }}
              </span>
            </div>
          </div>

          <button class="nav-logout" @click="logout" title="ออกจากระบบ">🚪</button>
        </div>
      </div>
    </header>

    <!-- ═══════════ เนื้อหา ═══════════ -->
    <main>
      <Login
        v-if="page === 'login'"
        @switch-page="changePage"
        @login-success="onLoginSuccess"
      />

      <Register
        v-else-if="page === 'register'"
        @switch-page="changePage"
        @register-success="changePage('login')"
      />

      <AdminPanel
        v-else-if="page === 'admin'"
        :currentUser="currentUser"
        @gotoHome="goShop"
        @switch-page="changePage"
      />

      <Mainmenu
        v-else
        :currentUser="currentUser"
        :externalView="currentView"
        @update:view="currentView = $event"
        @forceLogin="changePage('login')"
        @logout="logout"
      />
    </main>
  </div>
</template>

<script>
import Login from './view/Login.vue'
import Register from './view/Register.vue'
import AdminPanel from './view/AdminPanel.vue'
import Mainmenu from './components/shop/Mainmenu.vue'

export default {
  name: 'App',
  components: { Login, Register, AdminPanel, Mainmenu },
  data() {
    return {
      page: 'login',          // 'login' | 'register' | 'home' | 'admin'
      currentView: 'shop',    // ส่งให้ Mainmenu: 'shop' | 'orders'
      currentUser: null
    }
  },
  computed: {
    isAdmin() {
      return this.currentUser?.role === 'ADMIN';
    },
    showNavbar() {
      return this.currentUser && this.page !== 'login' && this.page !== 'register';
    },
    userInitial() {
      return this.currentUser?.username?.charAt(0).toUpperCase() || '?';
    }
  },
  watch: {
    // ★ Guard: กันลูกค้าทั่วไปดันเข้าหน้าแอดมิน (พิมพ์ URL / แก้ state)
    page(target) {
      if (target === 'admin' && !this.isAdmin) {
        this.page = this.currentUser ? 'home' : 'login';
      }
    }
  },
  mounted() {
    // ★ กู้ session ตอนรีเฟรชหน้าเว็บ — อ่าน user ที่เก็บไว้กลับมา
    const token = localStorage.getItem('token');
    const userRaw = localStorage.getItem('user');
    if (token && userRaw) {
      try {
        this.currentUser = JSON.parse(userRaw);
        this.page = this.currentUser.role === 'ADMIN' ? 'admin' : 'home';
      } catch (e) {
        this.forceLogout();
      }
    }

    // ★ ฟัง event ตอน token หมดอายุ (ส่งมาจาก http.js interceptor)
    window.addEventListener('auth-expired', this.onAuthExpired);
  },
  beforeUnmount() {
    window.removeEventListener('auth-expired', this.onAuthExpired);
  },
  methods: {
    changePage(target) {
      this.page = target;
    },
    onLoginSuccess(userData) {
      this.currentUser = userData;
      this.currentView = 'shop';
      this.page = userData.role === 'ADMIN' ? 'admin' : 'home';
    },
    goShop() {
      this.page = 'home';
      this.currentView = 'shop';
    },
    goOrders() {
      this.page = 'home';
      this.currentView = 'orders';
    },
    logout() {
      this.forceLogout();
    },
    forceLogout() {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      this.currentUser = null;
      this.currentView = 'shop';
      this.page = 'login';
    },
    onAuthExpired() {
      this.forceLogout();
      alert('⚠️ เซสชันหมดอายุ กรุณาเข้าสู่ระบบใหม่');
    }
  }
}
</script>

<style>
* { box-sizing: border-box; }
body { margin: 0; }

/* ═══════════ NAVBAR ═══════════ */
.navbar {
  position: sticky;
  top: 0;
  z-index: 200;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 16px rgba(102, 126, 234, 0.3);
  font-family: 'Prompt', sans-serif;
}
.navbar-inner {
  max-width: 1350px;
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

/* โลโก้ */
.nav-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}
.nav-logo { font-size: 1.4rem; }
.nav-brand-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.02em;
}

/* เมนูกลาง */
.nav-links {
  display: flex;
  align-items: center;
  gap: 6px;
}
.nav-link {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.82);
  font-family: 'Prompt', sans-serif;
  font-size: 0.88rem;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.nav-link:hover { background: rgba(255, 255, 255, 0.15); color: #fff; }
.nav-link.active { background: rgba(255, 255, 255, 0.22); color: #fff; }

/* ฝั่งขวา */
.nav-right {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}
#navbar-cart-slot { display: flex; align-items: center; }

.nav-user {
  display: flex;
  align-items: center;
  gap: 9px;
}
.nav-user-avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
  font-weight: 700;
  font-size: 0.95rem;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.nav-user-info { display: flex; flex-direction: column; line-height: 1.2; }
.nav-user-name { font-size: 0.83rem; font-weight: 600; color: #fff; }
.nav-user-role {
  font-size: 0.6rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  padding: 1px 6px;
  border-radius: 6px;
  width: fit-content;
  margin-top: 1px;
}
.role-admin { background: #fbbf24; color: #78350f; }
.role-customer { background: rgba(255, 255, 255, 0.25); color: #fff; }

.nav-logout {
  background: rgba(255, 255, 255, 0.15);
  border: none;
  width: 36px; height: 36px;
  border-radius: 9px;
  font-size: 1.05rem;
  cursor: pointer;
  transition: background 0.2s;
  flex-shrink: 0;
}
.nav-logout:hover { background: rgba(255, 255, 255, 0.3); }

/* responsive */
@media (max-width: 640px) {
  .navbar-inner { padding: 0 12px; gap: 8px; }
  .nav-brand-text { display: none; }
  .nav-link { padding: 8px 10px; font-size: 0.8rem; }
  .nav-user-info { display: none; }
}
</style>