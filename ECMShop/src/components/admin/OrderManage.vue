<template>
  <div class="order-manage">
    <div class="om-head">
      <h3>📋 จัดการคำสั่งซื้อทั้งหมด ({{ orders.length }} ออเดอร์)</h3>
      <button class="om-refresh" @click="$emit('reload')">🔄 รีเฟรช</button>
    </div>

    <div v-if="orders.length === 0" class="om-empty">
      📭 ยังไม่มีคำสั่งซื้อในระบบ
    </div>

    <div v-else class="om-list">
      <div v-for="order in orders" :key="order.id" class="om-card">
        <!-- หัวออเดอร์ -->
        <div class="om-card-top">
          <div class="om-meta">
            <span class="om-id">#{{ order.id }}</span>
            <span class="om-customer">👤 {{ order.customerUsername || 'ไม่ระบุ' }}</span>
            <span class="om-date">{{ formatDate(order.orderDate) }}</span>
          </div>
          <div class="om-top-right">
            <span class="om-total">{{ formatPrice(order.totalPrice) }} ฿</span>
            <span class="om-status-tag" :class="'st-' + (order.status || 'PENDING').toLowerCase()">
              {{ statusLabel(order.status) }}
            </span>
          </div>
        </div>

        <!-- รายการสินค้าในออเดอร์ -->
        <div class="om-items">
          <div v-for="item in order.items" :key="item.id" class="om-item">
            <div class="om-item-img">
              <img
                :src="(item.product && item.product.imageUrl) ? item.product.imageUrl : 'https://placehold.co/40x40/e2e8f0/718096?text=📦'"
                :alt="item.product ? item.product.name : 'สินค้า'"
              />
            </div>
            <span class="om-item-name">{{ item.product ? item.product.name : 'สินค้าถูกลบแล้ว' }}</span>
            <span class="om-item-qty">× {{ item.quantity }}</span>
            <span class="om-item-price">{{ formatPrice(numeric(item.price) * item.quantity) }} ฿</span>
          </div>
        </div>

        <!-- 🆕 สลิปการโอนเงิน -->
        <div v-if="order.slipUrl" class="om-slip">
          <span class="om-slip-label">📎 สลิปการโอน:</span>
          <img :src="order.slipUrl" class="om-slip-thumb" @click="viewSlip(order.slipUrl)" />
          <button
            v-if="(order.status || 'PENDING') === 'PENDING'"
            class="om-approve"
            @click="$emit('update-status', { id: order.id, status: 'PAID' })"
          >✅ อนุมัติการชำระ</button>
        </div>
        <div v-else-if="(order.status || 'PENDING') === 'PENDING'" class="om-no-slip">
          ⏳ ลูกค้ายังไม่แนบสลิป
        </div>

        <!-- เปลี่ยนสถานะ -->
        <div class="om-card-foot">
          <label class="om-status-label">เปลี่ยนสถานะ:</label>
          <select
            class="om-status-select"
            :value="order.status || 'PENDING'"
            @change="onChangeStatus(order, $event.target.value)"
          >
            <option value="PENDING">⏳ รอชำระเงิน</option>
            <option value="PAID">💰 ชำระแล้ว</option>
            <option value="SHIPPED">🚚 กำลังจัดส่ง</option>
            <option value="COMPLETED">✅ สำเร็จ</option>
            <option value="CANCELLED">❌ ยกเลิก</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 🆕 ดูสลิปเต็มจอ -->
    <div v-if="slipViewer" class="om-lightbox" @click="slipViewer = null">
      <img :src="slipViewer" alt="สลิป" />
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderManage',
  props: {
    orders: { type: Array, default: () => [] }
  },
  emits: ['update-status', 'reload'],
  data() {
    return { slipViewer: null }
  },
  methods: {
    viewSlip(url) {
      this.slipViewer = url;
    },
    numeric(v) {
      return typeof v === 'number' ? v : parseFloat(v || 0);
    },
    formatPrice(v) {
      return this.numeric(v).toLocaleString();
    },
    formatDate(dateString) {
      if (!dateString) return '-';
      return new Date(dateString).toLocaleString('th-TH', {
        year: 'numeric', month: 'short', day: 'numeric',
        hour: '2-digit', minute: '2-digit'
      });
    },
    statusLabel(status) {
      const map = {
        PENDING: '⏳ รอชำระเงิน',
        PAID: '💰 ชำระแล้ว',
        SHIPPED: '🚚 กำลังจัดส่ง',
        COMPLETED: '✅ สำเร็จ',
        CANCELLED: '❌ ยกเลิก'
      };
      return map[status] || '⏳ รอชำระเงิน';
    },
    onChangeStatus(order, newStatus) {
      if (newStatus === 'CANCELLED') {
        if (!confirm(`⚠️ ยืนยันยกเลิกออเดอร์ #${order.id}? ระบบจะคืนสต็อกสินค้ากลับ`)) {
          return; // ผู้ใช้ยกเลิก — select จะ revert เพราะ value ผูกกับ prop
        }
      }
      this.$emit('update-status', { id: order.id, status: newStatus });
    }
  }
}
</script>

<style scoped>
* { box-sizing: border-box; }
.order-manage { font-family: 'Prompt', sans-serif; }

.om-head {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 18px; flex-wrap: wrap; gap: 12px;
}
.om-head h3 { margin: 0; font-size: 1.15rem; font-weight: 700; color: #1e293b; }
.om-refresh {
  background: #eef0ff; color: #667eea; border: none;
  padding: 9px 16px; border-radius: 10px; font-family: 'Prompt', sans-serif;
  font-weight: 600; font-size: 0.85rem; cursor: pointer; transition: background 0.2s;
}
.om-refresh:hover { background: #667eea; color: #fff; }

.om-empty {
  text-align: center; padding: 50px 20px; color: #94a3b8;
  background: #fff; border-radius: 16px; border: 1px solid #edf0f7; font-size: 0.95rem;
}

.om-list { display: flex; flex-direction: column; gap: 16px; }

.om-card {
  background: #fff; border-radius: 16px; border: 1px solid #edf0f7;
  overflow: hidden; box-shadow: 0 4px 14px rgba(0, 0, 0, 0.04);
}
.om-card-top {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; background: #f8faff; border-bottom: 1px solid #f1f5f9;
  flex-wrap: wrap; gap: 10px;
}
.om-meta { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
.om-id {
  background: linear-gradient(135deg, #667eea, #764ba2); color: #fff;
  font-size: 0.78rem; font-weight: 700; padding: 3px 11px; border-radius: 20px;
}
.om-customer { font-size: 0.86rem; font-weight: 600; color: #475569; }
.om-date { font-size: 0.8rem; color: #94a3b8; }
.om-top-right { display: flex; align-items: center; gap: 12px; }
.om-total { font-size: 1.05rem; font-weight: 700; color: #0d9488; }

.om-status-tag {
  font-size: 0.74rem; font-weight: 700; padding: 4px 12px; border-radius: 20px; white-space: nowrap;
}
.st-pending   { background: #fef3c7; color: #92400e; }
.st-paid      { background: #dbeafe; color: #1e40af; }
.st-shipped   { background: #e0e7ff; color: #4338ca; }
.st-completed { background: #d1fae5; color: #065f46; }
.st-cancelled { background: #fee2e2; color: #991b1b; }

.om-items { padding: 8px 20px; }
.om-item {
  display: flex; align-items: center; gap: 12px;
  padding: 8px 0; border-bottom: 1px dashed #f1f5f9;
}
.om-item:last-child { border-bottom: none; }
.om-item-img {
  width: 40px; height: 40px; border-radius: 8px; overflow: hidden;
  flex-shrink: 0; border: 1px solid #eef0f7; background: #f8fafc;
}
.om-item-img img { width: 100%; height: 100%; object-fit: cover; }
.om-item-name { flex: 1; font-size: 0.86rem; color: #1e293b; font-weight: 500; min-width: 0; }
.om-item-qty { font-size: 0.82rem; color: #64748b; flex-shrink: 0; }
.om-item-price { font-size: 0.86rem; font-weight: 700; color: #475569; flex-shrink: 0; min-width: 80px; text-align: right; }

.om-card-foot {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 20px; background: #fafbff; border-top: 1px solid #f1f5f9;
}
.om-status-label { font-size: 0.84rem; font-weight: 600; color: #475569; }
.om-status-select {
  font-family: 'Prompt', sans-serif; font-size: 0.86rem; font-weight: 600;
  padding: 8px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px;
  cursor: pointer; color: #1e293b; background: #fff;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.om-status-select:focus {
  outline: none; border-color: #667eea; box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

/* 🆕 สลิป */
.om-slip {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 20px; background: #f0fdf4; border-top: 1px solid #dcfce7;
}
.om-slip-label { font-size: 0.82rem; font-weight: 600; color: #15803d; }
.om-slip-thumb {
  width: 48px; height: 48px; object-fit: cover; border-radius: 8px;
  border: 1px solid #bbf7d0; cursor: pointer; transition: transform 0.15s;
}
.om-slip-thumb:hover { transform: scale(1.05); }
.om-approve {
  margin-left: auto; background: #16a34a; color: #fff; border: none;
  padding: 8px 16px; border-radius: 8px; font-family: 'Prompt', sans-serif;
  font-weight: 600; font-size: 0.82rem; cursor: pointer; transition: background 0.2s;
}
.om-approve:hover { background: #15803d; }
.om-no-slip {
  padding: 10px 20px; font-size: 0.82rem; color: #d97706;
  background: #fffbeb; border-top: 1px solid #fef3c7;
}
.om-lightbox {
  position: fixed; inset: 0; background: rgba(0,0,0,0.85);
  z-index: 400; display: flex; align-items: center; justify-content: center;
  padding: 30px; cursor: zoom-out;
}
.om-lightbox img { max-width: 90%; max-height: 90%; border-radius: 10px; }

@media (max-width: 640px) {
  .om-card-top { flex-direction: column; align-items: flex-start; }
  .om-top-right { width: 100%; justify-content: space-between; }
}
</style>