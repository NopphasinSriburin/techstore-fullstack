<template>
  <transition name="modal-fade">
    <div v-if="product" class="pdm-overlay" @click.self="$emit('close')">
      <div class="pdm-box">
        <button class="pdm-close" @click="$emit('close')">✕</button>

        <div class="pdm-content">
          <!-- แกลเลอรีรูป -->
          <div class="pdm-gallery">
            <div class="pdm-main-img">
              <img :src="activeImage || 'https://placehold.co/500x500/f0f4f8/94a3b8?text=📦'" :alt="product.name" />
            </div>
            <div v-if="allImages.length > 1" class="pdm-thumbs">
              <button
                v-for="(img, i) in allImages"
                :key="i"
                class="pdm-thumb"
                :class="{ active: img === activeImage }"
                @click="activeImage = img"
              >
                <img :src="img" :alt="'รูป ' + (i + 1)" />
              </button>
            </div>
          </div>

          <!-- ข้อมูลสินค้า -->
          <div class="pdm-info">
            <h2 class="pdm-name">{{ product.name }}</h2>
            <div class="pdm-price">{{ product.price.toLocaleString() }} ฿</div>

            <div class="pdm-stock" :class="{ out: product.stockQuantity === 0 }">
              {{ product.stockQuantity === 0 ? '❌ สินค้าหมด' : '✅ มีสินค้า ' + product.stockQuantity + ' ชิ้น' }}
            </div>

            <div class="pdm-desc-block">
              <h4>รายละเอียดสินค้า</h4>
              <p class="pdm-desc">{{ product.description || 'ไม่มีรายละเอียดสินค้า' }}</p>
            </div>

            <button
              class="pdm-add-btn"
              :disabled="product.stockQuantity === 0"
              @click="$emit('add-to-cart', product)"
            >
              {{ product.stockQuantity === 0 ? 'สินค้าหมด' : '🛒 เพิ่มลงตะกร้า' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'ProductDetailModal',
  props: {
    product: { type: Object, default: null }
  },
  emits: ['close', 'add-to-cart'],
  data() {
    return { activeImage: null }
  },
  computed: {
    // รวมรูปหลัก + รูปเสริม (ไม่ซ้ำ)
    allImages() {
      if (!this.product) return [];
      const list = [];
      if (this.product.imageUrl) list.push(this.product.imageUrl);
      if (Array.isArray(this.product.images)) {
        this.product.images.forEach(img => {
          if (img && !list.includes(img)) list.push(img);
        });
      }
      return list;
    }
  },
  watch: {
    product(newVal) {
      // เปิด modal ใหม่ → รีเซ็ตรูปที่โชว์เป็นรูปแรก
      if (newVal) {
        this.activeImage = this.allImages[0] || null;
      }
    }
  }
}
</script>

<style scoped>
* { box-sizing: border-box; }
.pdm-overlay {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, 0.65);
  backdrop-filter: blur(4px);
  z-index: 300;
  display: flex; align-items: center; justify-content: center;
  padding: 20px;
  font-family: 'Prompt', sans-serif;
}
.pdm-box {
  position: relative;
  background: #fff;
  border-radius: 20px;
  width: 100%;
  max-width: 820px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.3);
}
.pdm-close {
  position: absolute; top: 14px; right: 14px;
  width: 36px; height: 36px; border-radius: 50%;
  background: rgba(15, 23, 42, 0.08); border: none;
  font-size: 1rem; cursor: pointer; z-index: 2;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.2s;
}
.pdm-close:hover { background: rgba(15, 23, 42, 0.18); }

.pdm-content { display: grid; grid-template-columns: 1fr 1fr; gap: 28px; padding: 28px; }

.pdm-gallery { display: flex; flex-direction: column; gap: 12px; }
.pdm-main-img {
  width: 100%; aspect-ratio: 1;
  border-radius: 16px; overflow: hidden;
  background: #f8fafc; border: 1px solid #eef0f7;
}
.pdm-main-img img { width: 100%; height: 100%; object-fit: cover; }
.pdm-thumbs { display: flex; gap: 8px; flex-wrap: wrap; }
.pdm-thumb {
  width: 60px; height: 60px;
  border-radius: 10px; overflow: hidden;
  border: 2px solid transparent; padding: 0;
  cursor: pointer; background: #f8fafc;
  transition: border-color 0.2s;
}
.pdm-thumb.active { border-color: #667eea; }
.pdm-thumb img { width: 100%; height: 100%; object-fit: cover; }

.pdm-info { display: flex; flex-direction: column; }
.pdm-name { font-size: 1.4rem; font-weight: 700; color: #1e293b; margin: 8px 0 12px; }
.pdm-price { font-size: 1.6rem; font-weight: 700; color: #0d9488; margin-bottom: 12px; }
.pdm-stock {
  display: inline-block; width: fit-content;
  font-size: 0.85rem; font-weight: 600;
  padding: 5px 14px; border-radius: 20px;
  background: #ecfdf5; color: #0d9488; margin-bottom: 20px;
}
.pdm-stock.out { background: #fff1f2; color: #e11d48; }

.pdm-desc-block { flex: 1; margin-bottom: 20px; }
.pdm-desc-block h4 { font-size: 0.95rem; font-weight: 700; color: #475569; margin: 0 0 8px; }
.pdm-desc { font-size: 0.9rem; color: #64748b; line-height: 1.7; margin: 0; white-space: pre-wrap; }

.pdm-add-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff; border: none; padding: 15px;
  border-radius: 12px; font-family: 'Prompt', sans-serif;
  font-size: 1rem; font-weight: 700; cursor: pointer;
  transition: transform 0.15s, box-shadow 0.2s;
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.35);
}
.pdm-add-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 10px 24px rgba(102, 126, 234, 0.45); }
.pdm-add-btn:disabled { background: #cbd5e1; cursor: not-allowed; box-shadow: none; }

.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.25s; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }

@media (max-width: 700px) {
  .pdm-content { grid-template-columns: 1fr; padding: 20px; gap: 18px; }
  .pdm-box { max-height: 94vh; }
}
</style>