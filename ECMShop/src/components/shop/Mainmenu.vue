<template>
  <div class="mainmenu">

    <!-- Cart icon — Teleport ขึ้น slot ใน navbar ของ App.vue -->
    <Teleport to="#navbar-cart-slot">
      <div class="cart-trigger" @click="toggleCart">
        <span class="cart-icon">🛒</span>
        <span v-if="totalItemsCount > 0" class="cart-badge">{{ totalItemsCount }}</span>
      </div>
    </Teleport>

    <!-- Cart Dropdown (fixed position, ไม่ขึ้นกับ navbar) -->
    <transition name="dropdown">
      <div v-if="cartOpen" class="cart-dropdown">
        <div class="cart-dropdown-header">
          <h3>🛒 ตะกร้าสินค้า</h3>
          <button class="cart-close-btn" @click="cartOpen = false">✕</button>
        </div>

        <div class="cart-dropdown-body">
          <div v-if="cart.length === 0" class="cart-empty">
            <span class="empty-icon">🛍️</span>
            <p>ตะกร้าของคุณว่างอยู่</p>
            <span class="empty-sub">เพิ่มสินค้าที่ต้องการได้เลย</span>
          </div>

          <div v-for="item in cart" :key="item.product.id" class="cart-drop-item">
            <div class="cart-item-img">
              <img
                :src="item.product.imageUrl || 'https://placehold.co/56x56/e2e8f0/718096?text=📦'"
                :alt="item.product.name"
              />
            </div>
            <div class="cart-item-info">
              <p class="cart-item-name">{{ item.product.name }}</p>
              <p class="cart-item-price">{{ item.product.price.toLocaleString() }} ฿</p>
            </div>
            <div class="cart-item-qty-group">
              <button @click="updateQuantity(item.product.id, -1)" class="qty-mini-btn">−</button>
              <span class="qty-mini-val">{{ item.quantity }}</span>
              <button @click="updateQuantity(item.product.id, 1)" class="qty-mini-btn">+</button>
            </div>
            <div class="cart-item-subtotal">
              {{ (item.product.price * item.quantity).toLocaleString() }} ฿
            </div>
            <button @click="removeFromCart(item.product.id)" class="cart-item-remove">✕</button>
          </div>
        </div>

        <div v-if="cart.length > 0" class="cart-dropdown-footer">
          <div class="cart-summary">
            <span class="summary-label">รวม {{ totalItemsCount }} ชิ้น</span>
            <span class="summary-total">{{ totalCartPrice.toLocaleString() }} ฿</span>
          </div>
          <button @click="checkout" class="checkout-btn">💳 สั่งซื้อสินค้า</button>
        </div>
      </div>
    </transition>

    <!-- Overlay ปิด dropdown -->
    <div v-if="cartOpen" class="cart-overlay" @click="cartOpen = false"></div>

    <!-- ─── หน้าร้านค้า ─── -->
    <div v-if="viewMode === 'shop'" class="shop-page">
      <div class="hero-banner">
        <div class="hero-content">
          <span class="hero-tag">🆕 ใหม่ล่าสุด</span>
          <h1 class="hero-title">แกดเจ็ตสุดล้ำ<br>ราคาคุ้มค่า</h1>
          <p class="hero-sub">เลือกช้อปสินค้าไอทีคุณภาพดี จัดส่งรวดเร็วทั่วประเทศ</p>
        </div>
        <div class="hero-deco">⚡🎧💻📱</div>
      </div>

      <section class="products-section">
        <div class="section-title-row">
          <h2 class="section-title">✨ สินค้าทั้งหมด <span class="section-count">{{ products.length }} รายการ</span></h2>
        </div>

        <div v-if="products.length === 0" class="loading-state">
          <div class="spinner"></div>
          <p>กำลังโหลดสินค้า...</p>
        </div>

        <div class="products-grid">
          <div
            v-for="product in products"
            :key="product.id"
            class="product-card"
            :class="{ 'product-card--out': product.stockQuantity === 0 }"
          >
            <div class="card-img-wrap">
              <img
                :src="product.imageUrl || 'https://placehold.co/400x280/f0f4f8/94a3b8?text=📦'"
                :alt="product.name"
                class="card-img"
              />
              <span v-if="product.stockQuantity === 0" class="badge-sold-out">สินค้าหมด</span>
              <span v-else-if="product.stockQuantity <= 5" class="badge-low-stock">เหลือน้อย</span>
              <button
                class="quick-add-btn"
                @click="addToCart(product)"
                :disabled="product.stockQuantity === 0"
                title="เพิ่มลงตะกร้า"
              >🛒</button>
            </div>
            <div class="card-body">
              <h3 class="card-name">{{ product.name }}</h3>
              <p class="card-desc">{{ product.description || 'ไม่มีรายละเอียดสินค้า' }}</p>
              <div class="card-bottom">
                <div class="card-price-block">
                  <span class="card-price">{{ product.price.toLocaleString() }} ฿</span>
                  <span class="card-stock">คลัง: {{ product.stockQuantity }} ชิ้น</span>
                </div>
                <button
                  class="add-cart-btn"
                  @click="addToCart(product)"
                  :disabled="product.stockQuantity === 0"
                >
                  {{ product.stockQuantity === 0 ? 'สินค้าหมด' : '+ ตะกร้า' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- ─── หน้าประวัติคำสั่งซื้อ ─── -->
    <div v-if="viewMode === 'orders'" class="orders-page">
      <div class="orders-header">
        <h2>📋 ประวัติการสั่งซื้อ</h2>
        <p>รายการคำสั่งซื้อทั้งหมดของคุณ</p>
      </div>

      <div v-if="orders.length === 0" class="orders-empty">
        <span>📭</span>
        <p>ยังไม่มีประวัติการสั่งซื้อ</p>
        <button @click="$emit('update:view', 'shop')" class="go-shop-btn">ไปช้อปเลย →</button>
      </div>

      <div class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-card-head">
            <div class="order-meta">
              <span class="order-id-badge">#{{ order.id }}</span>
              <span class="order-date">{{ formatDate(order.orderDate) }}</span>
            </div>
            <div class="order-total-pill">{{ order.totalPrice.toLocaleString() }} ฿</div>
          </div>

          <div class="order-items">
            <div v-for="item in order.items" :key="item.id" class="order-line">
              <div class="order-line-img">
                <img
                  :src="(item.product && item.product.imageUrl) ? item.product.imageUrl : 'https://placehold.co/50x50/e2e8f0/718096?text=📦'"
                  :alt="item.product ? item.product.name : 'สินค้า'"
                />
              </div>
              <div class="order-line-info">
                <p class="order-line-name">{{ item.product ? item.product.name : 'สินค้าถูกลบออกแล้ว' }}</p>
                <p class="order-line-sub">{{ item.price.toLocaleString() }} ฿ × {{ item.quantity }} ชิ้น</p>
              </div>
              <div class="order-line-total">{{ (item.price * item.quantity).toLocaleString() }} ฿</div>
            </div>
          </div>

          <div class="order-card-foot">
            <span class="order-items-count">{{ order.items.length }} รายการ</span>
            <div class="order-foot-right">
              <span class="order-status-badge" :class="'ost-' + (order.status || 'PENDING').toLowerCase()">
                {{ statusText(order.status) }}
              </span>
              <button
                v-if="(order.status || 'PENDING') === 'PENDING'"
                class="pay-btn"
                @click="openPayment(order)"
              >
                {{ order.slipUrl ? '🔁 แก้สลิป' : '💳 แจ้งชำระเงิน' }}
              </button>
              <button
                v-if="(order.status || 'PENDING') === 'PENDING'"
                class="cancel-order-btn"
                @click="cancelOrder(order)"
              >🗑️ ยกเลิก</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 🆕 Modal แจ้งชำระเงิน -->
    <PaymentModal
      :order="payingOrder"
      :currentUser="currentUser"
      @close="payingOrder = null"
      @paid="fetchOrders"
    />

    <!-- Toast Notification -->
    <transition name="toast">
      <div v-if="toastMsg" class="toast-notification">{{ toastMsg }}</div>
    </transition>

  </div>
</template>

<script>
import http from '../../api/http'   // ★ instance ที่แนบ JWT token + baseURL
import PaymentModal from './PaymentModal.vue'

export default {
  name: 'Mainmenu',
  components: { PaymentModal },
  props: {
    currentUser: { type: Object, default: null },
    currentPage: { type: String, default: 'home' },
    externalView: { type: String, default: 'shop' }   // ← รับจาก App.vue
  },
  emits: ['forceLogin', 'navigate-page', 'logout', 'update:view'],
  data() {
    return {
      viewMode: 'shop',
      products: [],
      cart: [],
      orders: [],
      cartOpen: false,
      toastMsg: null,
      toastTimer: null,
      payingOrder: null   // ★ ออเดอร์ที่กำลังแจ้งชำระ
    };
  },
  watch: {
    // ซิงค์เมื่อ App.vue เปลี่ยน tab (เช่น กด "คำสั่งซื้อ" ใน navbar)
    externalView(val) {
      if (val === 'orders') {
        this.fetchOrders();
      }
      this.viewMode = val;
    },
    // ★ เก็บตะกร้าลง localStorage ทุกครั้งที่เปลี่ยน (deep = ดูถึง quantity ข้างใน)
    cart: {
      handler() {
        this.saveCart();
      },
      deep: true
    },
    // ★ ถ้าสลับ user (login/logout) ให้โหลดตะกร้าของคนนั้น
    currentUser() {
      this.loadCart();
    }
  },
  computed: {
    totalItemsCount() {
      return this.cart.reduce((sum, item) => sum + item.quantity, 0);
    },
    totalCartPrice() {
      return this.cart.reduce((sum, item) => sum + item.product.price * item.quantity, 0);
    }
  },
  mounted() {
    this.fetchProducts();
    this.viewMode = this.externalView;
    this.loadCart();     // ★ โหลดตะกร้าที่เคยเก็บไว้กลับมา
  },
  methods: {
    toggleCart() {
      this.cartOpen = !this.cartOpen;
    },
    showToast(msg) {
      this.toastMsg = msg;
      clearTimeout(this.toastTimer);
      this.toastTimer = setTimeout(() => { this.toastMsg = null; }, 2500);
    },
    async fetchProducts() {
      try {
        const response = await http.get('/api/products');
        this.products = response.data;
      } catch (error) {
        console.error('โหลดสินค้าไม่สำเร็จ:', error);
      }
    },
    async fetchOrders() {
      if (!this.currentUser?.username) { this.orders = []; return; }
      try {
        const response = await http.get('/api/orders', {
          params: { customerUsername: this.currentUser.username }
        });
        this.orders = response.data.reverse();
      } catch (error) {
        console.error('โหลดออเดอร์ไม่สำเร็จ:', error);
      }
    },
    formatDate(dateString) {
      if (!dateString) return '-';
      return new Date(dateString).toLocaleString('th-TH', {
        year: 'numeric', month: 'short', day: 'numeric',
        hour: '2-digit', minute: '2-digit'
      });
    },
    addToCart(product) {
      if (product.stockQuantity === 0) return;
      const found = this.cart.find(i => i.product.id === product.id);
      if (found) {
        if (found.quantity < product.stockQuantity) {
          found.quantity++;
          this.showToast(`✅ เพิ่ม "${product.name}" แล้ว (${found.quantity} ชิ้น)`);
        } else {
          this.showToast(`⚠️ มีในคลังแค่ ${product.stockQuantity} ชิ้น`);
        }
      } else {
        this.cart.push({ product, quantity: 1 });
        this.showToast(`🛒 เพิ่ม "${product.name}" ลงตะกร้าแล้ว`);
      }
    },
    updateQuantity(productId, delta) {
      const item = this.cart.find(i => i.product.id === productId);
      if (!item) return;
      const newQty = item.quantity + delta;
      if (newQty <= 0) this.removeFromCart(productId);
      else if (newQty > item.product.stockQuantity) this.showToast(`⚠️ มีแค่ ${item.product.stockQuantity} ชิ้น`);
      else item.quantity = newQty;
    },
    removeFromCart(productId) {
      this.cart = this.cart.filter(i => i.product.id !== productId);
    },
    async checkout() {
      if (!this.currentUser) {
        this.cartOpen = false;
        this.showToast('⚠️ กรุณาเข้าสู่ระบบก่อนสั่งซื้อ');
        this.$emit('forceLogin');
        return;
      }
      if (!this.cart.length) return;
      const payload = {
        customerUsername: this.currentUser.username,
        items: this.cart.map(item => ({
          product: { id: item.product.id },
          quantity: item.quantity
        }))
      };
      try {
        const res = await http.post('/api/orders', payload);
        this.cart = [];
        this.saveCart();              // ★ เคลียร์ตะกร้าที่เก็บไว้ด้วย
        this.cartOpen = false;
        this.showToast(`🎉 สั่งซื้อสำเร็จ! ออเดอร์ #${res.data.id}`);
        this.fetchProducts();
      } catch (err) {
        this.showToast(err.response?.data || '❌ เกิดข้อผิดพลาด ไม่สามารถสั่งซื้อได้');
      }
    },

    // ────────── 🆕 Persist Cart (เก็บตะกร้าข้ามการรีเฟรช) ──────────

    // key แยกตาม user — ตะกร้าของแต่ละคนไม่ปนกัน
    // ถ้ายังไม่ login ใช้ key กลาง 'cart_guest'
    cartKey() {
      const name = this.currentUser?.username || 'guest';
      return `cart_${name}`;
    },

    // เซฟตะกร้าลง localStorage
    saveCart() {
      try {
        localStorage.setItem(this.cartKey(), JSON.stringify(this.cart));
      } catch (e) {
        console.error('เซฟตะกร้าไม่สำเร็จ:', e);
      }
    },

    // โหลดตะกร้าจาก localStorage กลับมา
    loadCart() {
      try {
        const raw = localStorage.getItem(this.cartKey());
        this.cart = raw ? JSON.parse(raw) : [];
      } catch (e) {
        console.error('โหลดตะกร้าไม่สำเร็จ:', e);
        this.cart = [];
      }
    },

    // ────────── 🆕 แจ้งชำระเงิน ──────────
    openPayment(order) {
      this.payingOrder = order;
    },

    // 🆕 ยกเลิกออเดอร์ (เฉพาะ PENDING)
    async cancelOrder(order) {
      if (!confirm(`⚠️ ต้องการยกเลิกออเดอร์ #${order.id} ใช่หรือไม่?\nสินค้าจะถูกคืนเข้าคลัง`)) {
        return;
      }
      try {
        await http.put(`/api/orders/${order.id}/cancel`, {
          customerUsername: this.currentUser?.username
        });
        order.status = 'CANCELLED';   // อัปเดตหน้าทันที
        this.showToast(`🗑️ ยกเลิกออเดอร์ #${order.id} แล้ว`);
        this.fetchProducts();         // โหลดสต็อกใหม่ (เพราะคืนของแล้ว)
      } catch (err) {
        this.showToast(err.response?.data || '❌ ยกเลิกไม่สำเร็จ');
        this.fetchOrders();           // revert ให้ตรงกับฐานข้อมูล
      }
    },

    statusText(status) {
      const m = {
        PENDING: '⏳ รอชำระ', PAID: '💰 ชำระแล้ว', SHIPPED: '🚚 กำลังส่ง',
        COMPLETED: '✅ สำเร็จ', CANCELLED: '❌ ยกเลิก'
      };
      return m[status] || '⏳ รอชำระ';
    }
  }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Prompt:wght@400;500;600;700&display=swap');

* { box-sizing: border-box; }

.mainmenu {
  font-family: 'Prompt', sans-serif;
  background: #f1f5f9;
  min-height: calc(100vh - 64px); /* หักความสูง navbar */
}

/* ══════════════════════════
   CART TRIGGER (teleport ขึ้น navbar)
══════════════════════════ */
.cart-trigger {
  position: relative;
  cursor: pointer;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: #334155;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  user-select: none;
  flex-shrink: 0;
}
.cart-trigger:hover { background: #475569; }
.cart-icon { font-size: 1.3rem; }
.cart-badge {
  position: absolute;
  top: -6px; right: -6px;
  background: #ef4444;
  color: #fff;
  font-size: 0.68rem; font-weight: 700;
  min-width: 20px; height: 20px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  padding: 0 5px;
  border: 2px solid #1e293b;
  animation: pop 0.2s ease;
}
@keyframes pop {
  0% { transform: scale(0.5); }
  70% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

/* ══════════════════════════
   CART DROPDOWN (fixed — ไม่ผูกกับ navbar)
══════════════════════════ */
.cart-overlay { position: fixed; inset: 0; z-index: 149; }

.cart-dropdown {
  position: fixed;
  top: 72px;   /* navbar 64px + 8px gap */
  right: 24px;
  width: 410px;
  max-height: 540px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
  z-index: 150;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}
.cart-dropdown-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid #f1f5f9;
  background: #f8fafc; flex-shrink: 0;
}
.cart-dropdown-header h3 { font-size: 0.95rem; font-weight: 700; color: #1e293b; margin: 0; }
.cart-close-btn {
  background: none; border: none; color: #94a3b8; font-size: 0.9rem;
  cursor: pointer; width: 28px; height: 28px; border-radius: 6px; transition: background 0.15s;
}
.cart-close-btn:hover { background: #e2e8f0; color: #475569; }

.cart-dropdown-body { flex: 1; overflow-y: auto; padding: 4px 0; }

.cart-empty {
  display: flex; flex-direction: column; align-items: center;
  gap: 6px; padding: 40px 20px; text-align: center;
}
.empty-icon { font-size: 2.5rem; }
.cart-empty p { font-size: 0.9rem; font-weight: 600; color: #475569; margin: 0; }
.empty-sub { font-size: 0.78rem; color: #94a3b8; }

.cart-drop-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; border-bottom: 1px solid #f8fafc; transition: background 0.15s;
}
.cart-drop-item:hover { background: #fafafa; }
.cart-drop-item:last-child { border-bottom: none; }

.cart-item-img {
  width: 52px; height: 52px; border-radius: 8px; overflow: hidden;
  flex-shrink: 0; border: 1px solid #e2e8f0; background: #f8fafc;
}
.cart-item-img img { width: 100%; height: 100%; object-fit: cover; }

.cart-item-info { flex: 1; min-width: 0; }
.cart-item-name {
  font-size: 0.83rem; font-weight: 600; color: #1e293b;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin: 0 0 2px;
}
.cart-item-price { font-size: 0.73rem; color: #64748b; margin: 0; }

.cart-item-qty-group {
  display: flex; align-items: center; gap: 4px;
  background: #f1f5f9; border-radius: 8px; padding: 3px; flex-shrink: 0;
}
.qty-mini-btn {
  width: 24px; height: 24px; border: none; background: #fff; border-radius: 5px;
  cursor: pointer; font-size: 1rem; font-weight: 700; color: #334155;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08); transition: background 0.15s;
}
.qty-mini-btn:hover { background: #e2e8f0; }
.qty-mini-val {
  font-size: 0.83rem; font-weight: 700; min-width: 20px;
  text-align: center; color: #1e293b;
}

.cart-item-subtotal {
  font-size: 0.83rem; font-weight: 700; color: #0f766e;
  flex-shrink: 0; min-width: 68px; text-align: right;
}
.cart-item-remove {
  background: none; border: none; color: #fca5a5; cursor: pointer;
  font-size: 0.78rem; width: 24px; height: 24px; border-radius: 5px; transition: all 0.15s;
}
.cart-item-remove:hover { background: #fef2f2; color: #ef4444; }

.cart-dropdown-footer {
  border-top: 2px solid #f1f5f9; padding: 14px 16px; background: #fff; flex-shrink: 0;
}
.cart-summary { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.summary-label { font-size: 0.83rem; color: #64748b; }
.summary-total { font-size: 1.15rem; font-weight: 700; color: #dc2626; }

.checkout-btn {
  width: 100%;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: #fff; border: none; padding: 13px;
  border-radius: 10px; font-family: 'Prompt', sans-serif;
  font-size: 0.9rem; font-weight: 700; cursor: pointer;
  transition: all 0.2s; box-shadow: 0 4px 12px rgba(22,163,74,0.35);
}
.checkout-btn:hover {
  background: linear-gradient(135deg, #16a34a, #15803d);
  box-shadow: 0 6px 16px rgba(22,163,74,0.45); transform: translateY(-1px);
}

.dropdown-enter-active, .dropdown-leave-active { transition: all 0.2s cubic-bezier(0.4,0,0.2,1); }
.dropdown-enter-from, .dropdown-leave-to { opacity: 0; transform: translateY(-8px) scale(0.97); }

/* ══════════════════════════
   SHOP PAGE
══════════════════════════ */
.shop-page { max-width: 1280px; margin: 0 auto; padding: 0 24px 40px; }

.hero-banner {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 60%, #1e3a5f 100%);
  border-radius: 20px; margin: 24px 0; padding: 40px 48px;
  display: flex; align-items: center; justify-content: space-between; overflow: hidden;
}
.hero-content { position: relative; z-index: 1; }
.hero-tag {
  display: inline-block; background: #3b82f6; color: #fff;
  font-size: 0.73rem; font-weight: 600; padding: 4px 12px;
  border-radius: 20px; margin-bottom: 12px; letter-spacing: 0.05em;
}
.hero-title { font-size: 2rem; font-weight: 700; color: #f8fafc; line-height: 1.25; margin: 0 0 10px; }
.hero-sub { font-size: 0.88rem; color: #94a3b8; margin: 0; }
.hero-deco { font-size: 3.5rem; letter-spacing: 0.3em; filter: drop-shadow(0 4px 16px rgba(0,0,0,0.4)); flex-shrink: 0; }

.section-title-row { margin-bottom: 20px; }
.section-title { font-size: 1.2rem; font-weight: 700; color: #1e293b; margin: 0; }
.section-count { font-size: 0.85rem; font-weight: 400; color: #94a3b8; margin-left: 8px; }

.loading-state {
  display: flex; flex-direction: column; align-items: center;
  gap: 14px; padding: 60px 0; color: #94a3b8; font-size: 0.9rem;
}
.spinner {
  width: 36px; height: 36px;
  border: 3px solid #e2e8f0; border-top-color: #3b82f6;
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.product-card {
  background: #fff; border-radius: 16px; overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05), 0 4px 16px rgba(0,0,0,0.04);
  display: flex; flex-direction: column;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.product-card:hover { transform: translateY(-5px); box-shadow: 0 8px 28px rgba(0,0,0,0.12); }
.product-card--out { opacity: 0.6; }

.card-img-wrap { position: relative; height: 180px; background: #f8fafc; overflow: hidden; }
.card-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s ease; }
.product-card:hover .card-img { transform: scale(1.04); }

.badge-sold-out, .badge-low-stock {
  position: absolute; top: 10px; left: 10px;
  font-size: 0.68rem; font-weight: 700; padding: 3px 10px; border-radius: 20px;
}
.badge-sold-out { background: rgba(239,68,68,0.9); color: #fff; }
.badge-low-stock { background: rgba(245,158,11,0.9); color: #fff; }

.quick-add-btn {
  position: absolute; bottom: 10px; right: 10px;
  width: 36px; height: 36px; border-radius: 10px; border: none;
  background: rgba(255,255,255,0.95); font-size: 1rem; cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transform: translateY(6px); transition: all 0.2s;
}
.card-img-wrap:hover .quick-add-btn { opacity: 1; transform: translateY(0); }
.quick-add-btn:disabled { cursor: not-allowed; opacity: 0; }
.quick-add-btn:not(:disabled):hover { background: #3b82f6; color: #fff; }

.card-body { padding: 14px 16px 16px; flex: 1; display: flex; flex-direction: column; }
.card-name { font-size: 0.9rem; font-weight: 700; color: #1e293b; margin: 0 0 6px; line-height: 1.4; }
.card-desc {
  font-size: 0.77rem; color: #94a3b8; flex: 1; margin: 0 0 14px;
  display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; line-height: 1.5;
}
.card-bottom { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.card-price-block { display: flex; flex-direction: column; }
.card-price { font-size: 1.05rem; font-weight: 700; color: #059669; }
.card-stock { font-size: 0.7rem; color: #94a3b8; margin-top: 1px; }

.add-cart-btn {
  background: #3b82f6; color: #fff; border: none; padding: 8px 14px;
  border-radius: 8px; font-family: 'Prompt', sans-serif; font-size: 0.77rem; font-weight: 600;
  cursor: pointer; white-space: nowrap; transition: all 0.2s; flex-shrink: 0;
}
.add-cart-btn:hover:not(:disabled) { background: #2563eb; transform: translateY(-1px); }
.add-cart-btn:disabled { background: #cbd5e1; color: #94a3b8; cursor: not-allowed; }

/* ══════════════════════════
   ORDERS PAGE
══════════════════════════ */
.orders-page { max-width: 820px; margin: 0 auto; padding: 28px 24px 40px; }
.orders-header { margin-bottom: 24px; }
.orders-header h2 { font-size: 1.25rem; font-weight: 700; color: #1e293b; margin: 0 0 4px; }
.orders-header p { font-size: 0.83rem; color: #64748b; margin: 0; }

.orders-empty {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  padding: 60px 20px; background: #fff; border-radius: 16px; text-align: center;
}
.orders-empty span { font-size: 3rem; }
.orders-empty p { color: #64748b; font-size: 0.93rem; margin: 0; }
.go-shop-btn {
  margin-top: 4px; background: #3b82f6; color: #fff; border: none;
  padding: 10px 24px; border-radius: 10px; font-family: 'Prompt', sans-serif;
  font-size: 0.88rem; font-weight: 600; cursor: pointer; transition: background 0.2s;
}
.go-shop-btn:hover { background: #2563eb; }

.orders-list { display: flex; flex-direction: column; gap: 16px; }

.order-card {
  background: #fff; border-radius: 14px; border: 1px solid #e2e8f0;
  overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.order-card-head {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 20px; background: #f8fafc; border-bottom: 1px solid #f1f5f9;
  flex-wrap: wrap; gap: 8px;
}
.order-meta { display: flex; align-items: center; gap: 12px; }
.order-id-badge {
  background: #1e40af; color: #fff; font-size: 0.73rem;
  font-weight: 700; padding: 3px 10px; border-radius: 20px;
}
.order-date { font-size: 0.8rem; color: #64748b; }
.order-total-pill {
  font-size: 1rem; font-weight: 700; color: #dc2626;
  background: #fef2f2; padding: 4px 14px; border-radius: 20px;
}

.order-items { padding: 4px 0; }
.order-line {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 20px; border-bottom: 1px solid #f8fafc; transition: background 0.15s;
}
.order-line:hover { background: #fafafa; }
.order-line:last-child { border-bottom: none; }

.order-line-img {
  width: 50px; height: 50px; border-radius: 8px; overflow: hidden;
  flex-shrink: 0; border: 1px solid #e2e8f0; background: #f8fafc;
}
.order-line-img img { width: 100%; height: 100%; object-fit: cover; }

.order-line-info { flex: 1; min-width: 0; }
.order-line-name { font-size: 0.875rem; font-weight: 600; color: #1e293b; margin: 0 0 3px; }
.order-line-sub { font-size: 0.75rem; color: #64748b; margin: 0; }
.order-line-total { font-size: 0.875rem; font-weight: 700; color: #059669; flex-shrink: 0; }

.order-card-foot {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 20px; border-top: 2px solid #f1f5f9; background: #fafafa;
}
.order-items-count { font-size: 0.78rem; color: #94a3b8; }
.order-grand-total { font-size: 0.88rem; color: #475569; }
.order-grand-total strong { color: #dc2626; font-size: 1rem; }

/* 🆕 สถานะ + ปุ่มแจ้งชำระ */
.order-foot-right { display: flex; align-items: center; gap: 10px; }
.order-status-badge { font-size: 0.72rem; font-weight: 700; padding: 4px 12px; border-radius: 20px; white-space: nowrap; }
.ost-pending { background: #fef3c7; color: #92400e; }
.ost-paid { background: #dbeafe; color: #1e40af; }
.ost-shipped { background: #e0e7ff; color: #4338ca; }
.ost-completed { background: #d1fae5; color: #065f46; }
.ost-cancelled { background: #fee2e2; color: #991b1b; }
.pay-btn {
  background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; border: none;
  padding: 7px 16px; border-radius: 8px; font-family: 'Prompt', sans-serif;
  font-size: 0.8rem; font-weight: 600; cursor: pointer; transition: transform 0.15s; white-space: nowrap;
}
.pay-btn:hover { transform: translateY(-1px); }
.cancel-order-btn {
  background: #fff; color: #dc2626; border: 1.5px solid #fecaca;
  padding: 7px 14px; border-radius: 8px; font-family: 'Prompt', sans-serif;
  font-size: 0.8rem; font-weight: 600; cursor: pointer; transition: all 0.15s; white-space: nowrap;
}
.cancel-order-btn:hover { background: #fef2f2; border-color: #dc2626; }

/* ══════════════════════════
   TOAST
══════════════════════════ */
.toast-notification {
  position: fixed; bottom: 28px; left: 50%; transform: translateX(-50%);
  background: #1e293b; color: #f8fafc; font-size: 0.875rem; font-weight: 500;
  padding: 12px 24px; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.25);
  z-index: 999; white-space: nowrap; pointer-events: none;
}
.toast-enter-active, .toast-leave-active { transition: all 0.25s ease; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(12px); }

/* ══════════════════════════
   RESPONSIVE
══════════════════════════ */
@media (max-width: 640px) {
  .hero-banner { padding: 28px 20px; flex-direction: column; gap: 16px; align-items: flex-start; }
  .hero-deco { font-size: 2rem; letter-spacing: 0.1em; }
  .cart-dropdown { right: 8px; left: 8px; width: auto; }
  .shop-page, .orders-page { padding-left: 12px; padding-right: 12px; }
  .products-grid { grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; }
}
</style>