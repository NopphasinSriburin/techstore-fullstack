<template>
  <div class="admin-container">
    <div class="admin-header">
      <div class="header-text">
        <h2>⚙️ แผงควบคุมระบบหลังบ้าน (Admin Panel)</h2>
        <p>วิเคราะห์ภาพรวมยอดขาย จัดการสินค้า คำสั่งซื้อ และตั้งค่าร้านได้อย่างมีประสิทธิภาพ</p>
      </div>
      <div class="header-actions">
        <button class="btn btn-ghost" @click="$emit('gotoHome')">🔙 กลับหน้าร้านค้า</button>
        <button class="btn btn-logout" @click="$emit('switch-page', 'login')">🚪 ออกจากระบบ</button>
      </div>
    </div>

    <AdminStats :products="products" :orders="orders" />

    <!-- แท็บสลับ จัดการสินค้า / จัดการออเดอร์ / ตั้งค่าร้าน -->
    <div class="admin-tabs">
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'products' }"
        @click="activeTab = 'products'"
      >📦 จัดการสินค้า</button>
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'orders' }"
        @click="activeTab = 'orders'"
      >
        📋 จัดการคำสั่งซื้อ
        <span v-if="pendingCount > 0" class="tab-badge">{{ pendingCount }}</span>
      </button>
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'setting' }"
        @click="activeTab = 'setting'"
      >⚙️ ตั้งค่าร้าน</button>
    </div>

    <ProductManage
      v-if="activeTab === 'products'"
      :products="products"
      @save-product="handleSaveProduct"
      @delete-product="handleDeleteProduct"
    />

    <OrderManage
      v-else-if="activeTab === 'orders'"
      :orders="orders"
      @update-status="handleUpdateStatus"
      @reload="reloadData"
    />

    <!-- ─── แท็บตั้งค่าร้าน (เลขบัญชี/QR) ─── -->
    <div v-else class="shop-setting-card">
      <h3>⚙️ ตั้งค่าข้อมูลการชำระเงิน</h3>
      <p class="ss-hint">ข้อมูลนี้จะแสดงให้ลูกค้าเห็นตอนแจ้งชำระเงิน</p>

      <div class="ss-form">
        <div class="ss-left">
          <div class="ss-group">
            <label>ธนาคาร</label>
            <input v-model="setting.bankName" placeholder="เช่น ธนาคารกสิกรไทย" />
          </div>
          <div class="ss-group">
            <label>ชื่อบัญชี</label>
            <input v-model="setting.accountName" placeholder="ชื่อ-นามสกุล เจ้าของบัญชี" />
          </div>
          <div class="ss-group">
            <label>เลขที่บัญชี</label>
            <input v-model="setting.accountNumber" placeholder="xxx-x-xxxxx-x" />
          </div>
        </div>

        <div class="ss-right">
          <label>QR พร้อมเพย์</label>
          <div class="ss-qr-preview">
            <img v-if="setting.qrUrl" :src="setting.qrUrl" alt="QR" />
            <span v-else class="ss-qr-empty">ยังไม่มี QR</span>
          </div>
          <label class="ss-upload-btn">
            <span v-if="!uploadingQr">📁 อัปโหลด QR</span>
            <span v-else>⏳ กำลังอัป...</span>
            <input type="file" accept="image/*" hidden @change="uploadQr" :disabled="uploadingQr" />
          </label>
        </div>
      </div>

      <button class="ss-save" @click="saveSetting" :disabled="savingSetting">
        {{ savingSetting ? 'กำลังบันทึก...' : '💾 บันทึกข้อมูลร้าน' }}
      </button>
    </div>
  </div>
</template>

<script>
import http from '../api/http'   // ★ instance ที่แนบ JWT token
import AdminStats from '../components/admin/AdminStats.vue'
import ProductManage from '../components/admin/ProductManage.vue'
import OrderManage from '../components/admin/OrderManage.vue'

export default {
  name: 'AdminPanel',
  components: { AdminStats, ProductManage, OrderManage },
  props: {
    currentUser: { type: Object, default: null }
  },
  emits: ['gotoHome', 'switch-page'],
  data() {
    return {
      activeTab: 'products',
      products: [],
      orders: [],
      // ตั้งค่าร้าน
      setting: { bankName: '', accountName: '', accountNumber: '', qrUrl: '' },
      uploadingQr: false,
      savingSetting: false
    }
  },
  computed: {
    pendingCount() {
      return this.orders.filter(o => (o.status || 'PENDING') === 'PENDING').length;
    }
  },
  mounted() {
    this.reloadData();
    this.loadSetting();
  },
  methods: {
    async reloadData() {
      try {
        const [prodRes, orderRes] = await Promise.all([
          http.get('/api/products'),
          http.get('/api/admin/orders')   // ★ ดึงออเดอร์ทั้งหมด (เฉพาะแอดมิน)
        ]);
        this.products = prodRes.data;
        this.orders = orderRes.data;
      } catch (error) {
        console.error('โหลดข้อมูลแผงควบคุมล้มเหลว:', error);
      }
    },
    async handleSaveProduct({ isEditMode, id, formData }) {
      try {
        if (isEditMode) {
          await http.put(`/api/products/${id}`, formData);
          alert('💾 แก้ไขข้อมูลสินค้าสำเร็จ!');
        } else {
          await http.post('/api/products', formData);
          alert('🚀 เพิ่มสินค้าใหม่สำเร็จ!');
        }
        this.reloadData();
      } catch (error) {
        alert('❌ ไม่สามารถบันทึกข้อมูลสินค้าได้');
      }
    },
    async handleDeleteProduct(id, name) {
      if (confirm(`⚠️ ต้องการลบสินค้า "${name}" ใช่หรือไม่?`)) {
        try {
          await http.delete(`/api/products/${id}`);
          alert('🗑️ ลบสินค้าเรียบร้อย');
          this.reloadData();
        } catch (error) {
          alert('❌ ลบสินค้าไม่สำเร็จ');
        }
      }
    },
    async handleUpdateStatus({ id, status }) {
      try {
        await http.put(`/api/admin/orders/${id}/status`, { status });
        const order = this.orders.find(o => o.id === id);
        if (order) order.status = status;
        if (status === 'CANCELLED') this.reloadData();
      } catch (error) {
        alert(error.response?.data || '❌ เปลี่ยนสถานะไม่สำเร็จ');
        this.reloadData();
      }
    },

    // ─── ตั้งค่าร้าน ───
    async loadSetting() {
      try {
        const res = await http.get('/api/shop-setting');
        if (res.data) this.setting = res.data;
      } catch (e) {
        console.error('โหลดข้อมูลร้านไม่สำเร็จ:', e);
      }
    },
    async uploadQr(event) {
      const file = event.target.files[0];
      if (!file) return;
      this.uploadingQr = true;
      try {
        const fd = new FormData();
        fd.append('files', file);
        const res = await http.post('/api/products/upload', fd, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        this.setting.qrUrl = (res.data.urls && res.data.urls[0]) || '';
      } catch (e) {
        alert('❌ อัป QR ไม่สำเร็จ');
      } finally {
        this.uploadingQr = false;
        event.target.value = '';
      }
    },
    async saveSetting() {
      this.savingSetting = true;
      try {
        await http.put('/api/admin/shop-setting', this.setting);
        alert('💾 บันทึกข้อมูลร้านสำเร็จ');
      } catch (e) {
        alert(e.response?.data || '❌ บันทึกไม่สำเร็จ');
      } finally {
        this.savingSetting = false;
      }
    }
  }
}
</script>

<style scoped>
.admin-container { max-width: 1350px; margin: 0 auto; padding: 24px; font-family: 'Prompt', sans-serif; }
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 22px 26px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 12px 30px rgba(102, 126, 234, 0.3);
}
.header-text h2 { margin: 0 0 4px; color: #fff; font-size: 1.5rem; }
.header-text p { margin: 0; color: rgba(255, 255, 255, 0.85); font-size: 0.9rem; }
.header-actions { display: flex; gap: 10px; flex-shrink: 0; }
.btn {
  border: none;
  padding: 10px 18px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  font-family: 'Prompt', sans-serif;
  transition: transform 0.15s, background 0.2s;
}
.btn:hover { transform: translateY(-2px); }
.btn-ghost { background: rgba(255, 255, 255, 0.2); color: #fff; }
.btn-ghost:hover { background: rgba(255, 255, 255, 0.32); }
.btn-logout { background: #fff; color: #764ba2; }
.btn-logout:hover { background: #f7f7ff; }

/* แท็บ */
.admin-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 22px;
  border-bottom: 2px solid #eef0f7;
}
.tab-btn {
  position: relative;
  background: transparent;
  border: none;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
  padding: 12px 22px;
  font-family: 'Prompt', sans-serif;
  font-size: 0.95rem;
  font-weight: 600;
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s;
}
.tab-btn:hover { color: #667eea; }
.tab-btn.active { color: #667eea; border-bottom-color: #667eea; }
.tab-badge {
  background: #ef4444; color: #fff;
  font-size: 0.68rem; font-weight: 700;
  padding: 1px 7px; border-radius: 20px; margin-left: 5px;
}

/* ตั้งค่าร้าน */
.shop-setting-card { background: #fff; border-radius: 16px; padding: 26px; border: 1px solid #edf0f7; box-shadow: 0 4px 18px rgba(0,0,0,0.05); }
.shop-setting-card h3 { margin: 0 0 4px; font-size: 1.15rem; color: #1e293b; font-weight: 700; }
.ss-hint { font-size: 0.84rem; color: #94a3b8; margin: 0 0 22px; }
.ss-form { display: grid; grid-template-columns: 1fr 200px; gap: 28px; margin-bottom: 22px; }
.ss-group { display: flex; flex-direction: column; margin-bottom: 16px; }
.ss-group label, .ss-right > label { font-size: 0.85rem; font-weight: 600; color: #475569; margin-bottom: 7px; }
.ss-group input { padding: 12px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px; font-family: 'Prompt', sans-serif; font-size: 0.92rem; box-sizing: border-box; }
.ss-group input:focus { outline: none; border-color: #667eea; box-shadow: 0 0 0 3px rgba(102,126,234,0.15); }
.ss-right { display: flex; flex-direction: column; }
.ss-qr-preview { width: 100%; aspect-ratio: 1; border: 1.5px dashed #cbd5e0; border-radius: 12px; background: #f8fafc; display: flex; align-items: center; justify-content: center; overflow: hidden; margin-bottom: 10px; }
.ss-qr-preview img { width: 100%; height: 100%; object-fit: contain; }
.ss-qr-empty { font-size: 0.8rem; color: #94a3b8; }
.ss-upload-btn { display: flex; align-items: center; justify-content: center; background: #eef0ff; color: #667eea; font-weight: 600; font-size: 0.85rem; padding: 10px; border-radius: 10px; cursor: pointer; transition: background 0.2s; }
.ss-upload-btn:hover { background: #667eea; color: #fff; }
.ss-save { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; border: none; padding: 13px 28px; border-radius: 10px; font-family: 'Prompt', sans-serif; font-weight: 700; font-size: 0.92rem; cursor: pointer; box-shadow: 0 4px 12px rgba(102,126,234,0.32); transition: transform 0.15s; }
.ss-save:hover:not(:disabled) { transform: translateY(-2px); }
.ss-save:disabled { opacity: 0.6; cursor: not-allowed; }

@media (max-width: 640px) {
  .admin-header { flex-direction: column; align-items: flex-start; }
  .tab-btn { padding: 10px 14px; font-size: 0.86rem; }
  .ss-form { grid-template-columns: 1fr; }
}
</style>