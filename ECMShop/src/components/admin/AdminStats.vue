<template>
  <div class="stats-grid">
    <div class="stat-card revenue">
      <div class="stat-icon">💰</div>
      <div class="stat-info">
        <span class="stat-label">รายได้รวมทั้งหมด</span>
        <span class="stat-value">{{ totalRevenue.toLocaleString() }} บาท</span>
      </div>
    </div>

    <div class="stat-card orders">
      <div class="stat-icon">📦</div>
      <div class="stat-info">
        <span class="stat-label">จำนวนคำสั่งซื้อ</span>
        <span class="stat-value">{{ totalOrdersCount.toLocaleString() }} ออเดอร์</span>
      </div>
    </div>

    <div class="stat-card products">
      <div class="stat-icon">📱</div>
      <div class="stat-info">
        <span class="stat-label">สินค้าทั้งหมดในระบบ</span>
        <span class="stat-value">{{ totalProductsCount }} รายการ</span>
      </div>
    </div>

    <div class="stat-card out-of-stock">
      <div class="stat-icon">⚠️</div>
      <div class="stat-info">
        <span class="stat-label">สินค้าที่หมดคลัง</span>
        <span class="stat-value" :class="{ 'text-danger': outOfStockCount > 0 }">
          {{ outOfStockCount }} รายการ
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminStats',
  props: {
    products: { type: Array, default: () => [] },
    orders: { type: Array, default: () => [] }
  },
  computed: {
    totalRevenue() {
      return this.orders.reduce((sum, order) => sum + (order.totalPrice || 0), 0);
    },
    totalOrdersCount() {
      return this.orders.length;
    },
    totalProductsCount() {
      return this.products.length;
    },
    outOfStockCount() {
      return this.products.filter(p => p.stockQuantity === 0).length;
    }
  }
}
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 18px;
  margin-bottom: 28px;
  font-family: 'Prompt', sans-serif;
}
.stat-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  padding: 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  border: 1px solid #edf0f7;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.stat-card::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 5px;
  border-radius: 16px 0 0 16px;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 26px rgba(102, 126, 234, 0.16);
}
.stat-icon {
  font-size: 1.7rem;
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info { display: flex; flex-direction: column; }
.stat-label { font-size: 0.83rem; color: #94a3b8; font-weight: 500; }
.stat-value { font-size: 1.35rem; font-weight: 700; color: #1e293b; margin-top: 3px; }

/* รายได้ — ม่วงหลัก */
.stat-card.revenue::before { background: linear-gradient(180deg, #667eea, #764ba2); }
.stat-card.revenue .stat-icon { background: #eef0ff; }
/* ออเดอร์ — น้ำเงิน */
.stat-card.orders::before { background: linear-gradient(180deg, #4f9cf9, #2563eb); }
.stat-card.orders .stat-icon { background: #eaf2ff; }
/* สินค้า — เขียวอมฟ้า */
.stat-card.products::before { background: linear-gradient(180deg, #34d399, #0d9488); }
.stat-card.products .stat-icon { background: #e6fff6; }
/* หมดคลัง — แดง */
.stat-card.out-of-stock::before { background: linear-gradient(180deg, #fb7185, #e11d48); }
.stat-card.out-of-stock .stat-icon { background: #fff1f3; }

.text-danger { color: #e11d48 !important; }

@media (max-width: 640px) {
  .stats-grid { grid-template-columns: 1fr 1fr; gap: 12px; }
  .stat-card { padding: 16px; gap: 12px; }
  .stat-icon { width: 46px; height: 46px; font-size: 1.4rem; }
  .stat-value { font-size: 1.1rem; }
}
</style>  