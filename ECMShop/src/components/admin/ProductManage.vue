<template>
  <div class="admin-grid">
    <div class="form-card">
      <h3>{{ isEditMode ? '📝 แก้ไขข้อมูลสินค้า' : '➕ เพิ่มสินค้าใหม่เข้าคลัง' }}</h3>
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>ชื่อสินค้า <span class="required">*</span></label>
          <input v-model="form.name" type="text" placeholder="ตัวอย่าง: คีย์บอร์ดไร้สาย" required />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>ราคา (บาท) <span class="required">*</span></label>
            <input v-model.number="form.price" type="number" min="0" required />
          </div>
          <div class="form-group">
            <label>จำนวนในสต็อก <span class="required">*</span></label>
            <input v-model.number="form.stockQuantity" type="number" min="0" required />
          </div>
        </div>

        <!-- 🆕 เลือกหมวดหมู่ -->
        <div class="form-group">
          <label>หมวดหมู่สินค้า <span class="required">*</span></label>
          <div class="cat-row">
            <select v-model="form.categoryId" class="cat-select" required>
              <option value="" disabled>— เลือกหมวดหมู่ —</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
            <button type="button" class="cat-add-btn" @click="showNewCat = !showNewCat" title="เพิ่มหมวดหมู่ใหม่">
              {{ showNewCat ? '✕' : '➕' }}
            </button>
          </div>

          <!-- ฟอร์มสร้างหมวดหมู่ใหม่ในตัว -->
          <div v-if="showNewCat" class="new-cat-box">
            <input
              v-model="newCatName"
              type="text"
              placeholder="ชื่อหมวดหมู่ใหม่ เช่น คีย์บอร์ด"
              @keyup.enter="createCategory"
            />
            <button type="button" class="new-cat-save" @click="createCategory" :disabled="creatingCat">
              {{ creatingCat ? '...' : 'สร้าง' }}
            </button>
          </div>
        </div>

        <div class="form-group">
          <label>รูปหลัก (โชว์ในการ์ด/ตาราง)</label>
          <div class="main-img-row">
            <div class="main-img-preview">
              <img v-if="form.imageUrl" :src="form.imageUrl" alt="รูปหลัก" />
              <span v-else class="no-img">ยังไม่มีรูป</span>
            </div>
            <div class="main-img-actions">
              <label class="upload-mini-btn">
                📁 เลือกรูปหลัก
                <input type="file" accept="image/*" hidden @change="uploadMain" />
              </label>
              <input v-model="form.imageUrl" type="text" class="url-input" placeholder="หรือวางลิงก์ URL..." />
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>รูปเพิ่มเติม (โชว์ในหน้ารายละเอียด — เลือกได้หลายรูป)</label>
          <label class="upload-multi-btn">
            <span v-if="!uploadingExtra">➕ เพิ่มรูป (เลือกหลายรูปพร้อมกันได้)</span>
            <span v-else>⏳ กำลังอัปโหลด...</span>
            <input type="file" accept="image/*" multiple hidden @change="uploadExtra" :disabled="uploadingExtra" />
          </label>

          <div v-if="form.images.length > 0" class="extra-img-grid">
            <div v-for="(url, i) in form.images" :key="i" class="extra-img-item">
              <img :src="url" :alt="'รูป ' + (i + 1)" />
              <button type="button" class="extra-img-remove" @click="removeExtra(i)" title="ลบรูปนี้">✕</button>
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>รายละเอียดสินค้า</label>
          <textarea v-model="form.description" rows="3" placeholder="อธิบายจุดเด่นของสินค้า..."></textarea>
        </div>
        <div class="form-actions">
          <button type="submit" class="save-btn" :disabled="uploadingMain || uploadingExtra">
            {{ isEditMode ? '💾 บันทึกแก้ไข' : '🚀 เพิ่มเข้าคลัง' }}
          </button>
          <button v-if="isEditMode" type="button" class="cancel-btn" @click="resetForm">ยกเลิก</button>
        </div>
      </form>
    </div>

    <div class="table-card">
      <h3>📦 รายการสินค้าปัจจุบันในคลัง ({{ products.length }} รายการ)</h3>
      <div class="table-wrapper">
        <table class="admin-table">
          <thead>
            <tr>
              <th>รูป</th>
              <th>ชื่อสินค้า</th>
              <th style="text-align: right;">ราคา</th>
              <th style="text-align: center;">สต็อก</th>
              <th style="text-align: center;">จัดการ</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="products.length === 0">
              <td colspan="5" class="empty-row">📭 ยังไม่มีสินค้าในคลัง</td>
            </tr>
            <tr v-for="product in products" :key="product.id">
              <td>
                <div class="thumb-wrap">
                  <img :src="product.imageUrl || 'https://placehold.co/50'" class="prod-thumb" />
                  <span v-if="product.images && product.images.length" class="thumb-count">+{{ product.images.length }}</span>
                </div>
              </td>
              <td>
                <div class="prod-name">{{ product.name }}</div>
                <div class="prod-desc">{{ product.description || 'ไม่มีคำอธิบาย' }}</div>
              </td>
              <td class="price-cell">{{ product.price.toLocaleString() }} บ.</td>
              <td style="text-align: center;">
                <span :class="['stock-tag', product.stockQuantity === 0 ? 'out-of-stock' : '']">{{ product.stockQuantity }} ชิ้น</span>
              </td>
              <td style="text-align: center;">
                <div class="action-buttons">
                  <button class="edit-btn" @click="startEdit(product)" title="แก้ไข">✏️</button>
                  <button class="delete-btn" @click="$emit('delete-product', product.id, product.name)" title="ลบ">🗑️</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import http from '../../api/http'

export default {
  name: 'ProductManage',
  props: { products: { type: Array, default: () => [] } },
  emits: ['save-product', 'delete-product'],
  data() {
    return {
      isEditMode: false,
      currentProductId: null,
      uploadingMain: false,
      uploadingExtra: false,
      categories: [],
      showNewCat: false,
      newCatName: '',
      creatingCat: false,
      form: { name: '', price: '', stockQuantity: '', categoryId: '', imageUrl: '', description: '', images: [] }
    }
  },
  mounted() {
    this.loadCategories();
  },
  methods: {
    async uploadMain(event) {
      const file = event.target.files[0];
      if (!file) return;
      this.uploadingMain = true;
      try {
        const urls = await this.doUpload([file]);
        if (urls.length) this.form.imageUrl = urls[0];
      } catch (e) {
        alert(e.response?.data || '❌ อัปโหลดรูปหลักไม่สำเร็จ');
      } finally {
        this.uploadingMain = false;
        event.target.value = '';
      }
    },
    async uploadExtra(event) {
      const files = Array.from(event.target.files);
      if (!files.length) return;
      this.uploadingExtra = true;
      try {
        const urls = await this.doUpload(files);
        this.form.images.push(...urls);
      } catch (e) {
        alert(e.response?.data || '❌ อัปโหลดรูปไม่สำเร็จ');
      } finally {
        this.uploadingExtra = false;
        event.target.value = '';
      }
    },
    async doUpload(files) {
      const formData = new FormData();
      files.forEach(f => formData.append('files', f));
      const res = await http.post('/api/products/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      return res.data.urls || [];
    },
    removeExtra(index) {
      this.form.images.splice(index, 1);
    },

    // 🆕 โหลดหมวดหมู่ทั้งหมด
    async loadCategories() {
      try {
        const res = await http.get('/api/categories');
        this.categories = res.data;
      } catch (e) {
        console.error('โหลดหมวดหมู่ไม่สำเร็จ:', e);
      }
    },

    // 🆕 สร้างหมวดหมู่ใหม่
    async createCategory() {
      const name = this.newCatName.trim();
      if (!name) return;
      this.creatingCat = true;
      try {
        const res = await http.post('/api/categories', { name });
        this.categories.push(res.data);
        this.form.categoryId = res.data.id;   // เลือกอันที่เพิ่งสร้างเลย
        this.newCatName = '';
        this.showNewCat = false;
      } catch (e) {
        alert(e.response?.data || '❌ สร้างหมวดหมู่ไม่สำเร็จ');
      } finally {
        this.creatingCat = false;
      }
    },

    handleSubmit() {
      if (!this.form.categoryId) {
        alert('⚠️ กรุณาเลือกหมวดหมู่สินค้า');
        return;
      }
      // แปลง categoryId → category object ให้ backend
      const payload = {
        ...this.form,
        images: [...this.form.images],
        category: { id: this.form.categoryId }
      };
      delete payload.categoryId;

      this.$emit('save-product', {
        isEditMode: this.isEditMode,
        id: this.currentProductId,
        formData: payload
      });
      this.resetForm();
    },
    startEdit(product) {
      this.isEditMode = true;
      this.currentProductId = product.id;
      this.form = {
        name: product.name,
        price: product.price,
        stockQuantity: product.stockQuantity,
        categoryId: product.category ? product.category.id : '',
        imageUrl: product.imageUrl || '',
        description: product.description || '',
        images: Array.isArray(product.images) ? [...product.images] : []
      };
    },
    resetForm() {
      this.isEditMode = false;
      this.currentProductId = null;
      this.showNewCat = false;
      this.newCatName = '';
      this.form = { name: '', price: '', stockQuantity: '', categoryId: '', imageUrl: '', description: '', images: [] };
    }
  }
}
</script>

<style scoped>
* { box-sizing: border-box; }
.admin-grid { display: grid; grid-template-columns: 400px 1fr; gap: 24px; align-items: start; font-family: 'Prompt', sans-serif; }
.form-card, .table-card { background: #fff; border-radius: 16px; padding: 26px; box-shadow: 0 4px 18px rgba(0,0,0,0.05); border: 1px solid #edf0f7; }
.form-card h3, .table-card h3 { margin: 0 0 20px; font-size: 1.15rem; color: #1e293b; font-weight: 700; border-bottom: 2px solid #f1f5f9; padding-bottom: 14px; }
.form-group { display: flex; flex-direction: column; margin-bottom: 16px; min-width: 0; }
.form-group label { font-size: 0.86rem; font-weight: 600; color: #475569; margin-bottom: 7px; }
.required { color: #e11d48; font-weight: 700; }
.form-group input, .form-group textarea { width: 100%; max-width: 100%; box-sizing: border-box; font-family: 'Prompt', sans-serif; padding: 12px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px; font-size: 0.92rem; transition: border-color 0.2s, box-shadow 0.2s; background: #fff; resize: vertical; }
.form-group input:focus, .form-group textarea:focus { outline: none; border-color: #667eea; box-shadow: 0 0 0 3px rgba(102,126,234,0.15); }
.form-group input:hover, .form-group textarea:hover { border-color: #cbd5e0; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; min-width: 0; }

/* หมวดหมู่ */
.cat-row { display: flex; gap: 8px; }
.cat-select {
  flex: 1; min-width: 0;
  padding: 12px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px;
  font-family: 'Prompt', sans-serif; font-size: 0.92rem; background: #fff; cursor: pointer;
}
.cat-select:focus { outline: none; border-color: #667eea; box-shadow: 0 0 0 3px rgba(102,126,234,0.15); }
.cat-add-btn {
  flex-shrink: 0; width: 46px; border: 1.5px solid #e2e8f0; border-radius: 10px;
  background: #eef0ff; color: #667eea; font-size: 1rem; cursor: pointer; transition: background 0.2s;
}
.cat-add-btn:hover { background: #667eea; color: #fff; }
.new-cat-box { display: flex; gap: 8px; margin-top: 8px; }
.new-cat-box input {
  flex: 1; min-width: 0; padding: 10px 12px; border: 1.5px solid #c7d2fe;
  border-radius: 10px; font-family: 'Prompt', sans-serif; font-size: 0.88rem; box-sizing: border-box;
}
.new-cat-box input:focus { outline: none; border-color: #667eea; }
.new-cat-save {
  flex-shrink: 0; background: #667eea; color: #fff; border: none; padding: 0 18px;
  border-radius: 10px; font-family: 'Prompt', sans-serif; font-weight: 600; font-size: 0.85rem; cursor: pointer;
}
.new-cat-save:hover:not(:disabled) { background: #5568d3; }
.new-cat-save:disabled { opacity: 0.6; cursor: not-allowed; }

.main-img-row { display: flex; gap: 12px; align-items: stretch; }
.main-img-preview { width: 88px; height: 88px; flex-shrink: 0; border-radius: 12px; border: 1.5px dashed #cbd5e0; background: #f8fafc; overflow: hidden; display: flex; align-items: center; justify-content: center; }
.main-img-preview img { width: 100%; height: 100%; object-fit: cover; }
.no-img { font-size: 0.72rem; color: #94a3b8; text-align: center; }
.main-img-actions { flex: 1; display: flex; flex-direction: column; gap: 8px; min-width: 0; }
.url-input { font-size: 0.82rem !important; padding: 9px 12px !important; }

.upload-mini-btn, .upload-multi-btn { display: inline-flex; align-items: center; justify-content: center; background: #eef0ff; color: #667eea; font-family: 'Prompt', sans-serif; font-weight: 600; font-size: 0.85rem; padding: 10px 14px; border-radius: 10px; cursor: pointer; transition: background 0.2s; border: 1.5px solid transparent; }
.upload-mini-btn:hover, .upload-multi-btn:hover { background: #667eea; color: #fff; }
.upload-multi-btn { width: 100%; }

.extra-img-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(72px, 1fr)); gap: 8px; margin-top: 10px; }
.extra-img-item { position: relative; aspect-ratio: 1; border-radius: 10px; overflow: hidden; border: 1px solid #eef0f7; }
.extra-img-item img { width: 100%; height: 100%; object-fit: cover; }
.extra-img-remove { position: absolute; top: 3px; right: 3px; width: 20px; height: 20px; border-radius: 50%; background: rgba(15,23,42,0.7); color: #fff; border: none; cursor: pointer; font-size: 0.7rem; display: flex; align-items: center; justify-content: center; }
.extra-img-remove:hover { background: #e11d48; }

.form-actions { display: flex; gap: 12px; margin-top: 24px; }
.save-btn { flex: 1; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; border: none; padding: 13px; border-radius: 10px; font-family: 'Prompt', sans-serif; font-weight: 700; font-size: 0.92rem; cursor: pointer; transition: transform 0.15s, box-shadow 0.2s; box-shadow: 0 4px 12px rgba(102,126,234,0.32); }
.save-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(102,126,234,0.42); }
.save-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.cancel-btn { background: #f8fafc; color: #64748b; border: 1.5px solid #e2e8f0; padding: 13px 20px; border-radius: 10px; font-family: 'Prompt', sans-serif; font-weight: 600; font-size: 0.92rem; cursor: pointer; transition: background 0.2s, border-color 0.2s; }
.cancel-btn:hover { background: #f1f5f9; border-color: #cbd5e0; }

.table-wrapper { overflow-x: auto; }
.admin-table { width: 100%; border-collapse: separate; border-spacing: 0; font-size: 0.9rem; }
.admin-table th { background: #f8fafc; padding: 13px 12px; color: #475569; font-weight: 700; font-size: 0.82rem; border-bottom: 2px solid #eef0f7; text-align: left; white-space: nowrap; }
.admin-table tbody tr { transition: background 0.15s; border-bottom: 1px solid #f1f5f9; }
.admin-table tbody tr:hover { background: #f8faff; }
.admin-table td { padding: 14px 12px; vertical-align: middle; }
.empty-row { text-align: center; color: #94a3b8; padding: 40px 0; font-size: 0.95rem; }

.thumb-wrap { position: relative; width: 50px; }
.prod-thumb { width: 50px; height: 50px; object-fit: cover; border-radius: 10px; border: 1px solid #eef0f7; transition: transform 0.2s; }
.admin-table tbody tr:hover .prod-thumb { transform: scale(1.05); }
.thumb-count { position: absolute; bottom: -4px; right: -4px; background: #667eea; color: #fff; font-size: 0.6rem; font-weight: 700; padding: 1px 5px; border-radius: 10px; border: 1.5px solid #fff; }
.prod-name { font-weight: 700; color: #1e293b; font-size: 0.92rem; }
.prod-desc { font-size: 0.78rem; color: #94a3b8; max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 2px; }
.price-cell { text-align: right; font-weight: 700; color: #0d9488; white-space: nowrap; }

.stock-tag { background: #ecfdf5; color: #0d9488; padding: 5px 12px; border-radius: 16px; font-size: 0.78rem; font-weight: 700; border: 1px solid #a7f3d0; display: inline-block; white-space: nowrap; }
.stock-tag.out-of-stock { background: #fff1f2; color: #e11d48; border-color: #fecdd3; }

.action-buttons { display: flex; gap: 8px; justify-content: center; }
.edit-btn, .delete-btn { border: none; width: 36px; height: 36px; border-radius: 9px; cursor: pointer; font-size: 0.95rem; display: flex; align-items: center; justify-content: center; transition: transform 0.15s, background 0.2s; }
.edit-btn { background: #eef0ff; }
.edit-btn:hover { background: #667eea; transform: translateY(-2px); }
.delete-btn { background: #fff1f2; }
.delete-btn:hover { background: #fecdd3; transform: translateY(-2px); }

@media (max-width: 900px) { .admin-grid { grid-template-columns: 1fr; } }
</style>