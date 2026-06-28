<template>
  <transition name="modal-fade">
    <div v-if="order" class="pm-overlay" @click.self="$emit('close')">
      <div class="pm-box">
        <button class="pm-close" @click="$emit('close')">✕</button>

        <div class="pm-header">
          <h3>💳 แจ้งชำระเงิน</h3>
          <span class="pm-order-id">ออเดอร์ #{{ order.id }}</span>
        </div>

        <div class="pm-amount">
          <span class="pm-amount-label">ยอดที่ต้องชำระ</span>
          <span class="pm-amount-value">{{ order.totalPrice.toLocaleString() }} ฿</span>
        </div>

        <!-- ข้อมูลบัญชี/QR ที่แอดมินตั้งไว้ -->
        <div class="pm-bank">
          <div v-if="setting.qrUrl" class="pm-qr">
            <img :src="setting.qrUrl" alt="QR พร้อมเพย์" />
            <span class="pm-qr-hint">สแกนเพื่อโอน</span>
          </div>

          <div class="pm-bank-info">
            <div v-if="setting.bankName" class="pm-bank-row">
              <span class="pm-bank-label">ธนาคาร</span>
              <span class="pm-bank-val">{{ setting.bankName }}</span>
            </div>
            <div v-if="setting.accountName" class="pm-bank-row">
              <span class="pm-bank-label">ชื่อบัญชี</span>
              <span class="pm-bank-val">{{ setting.accountName }}</span>
            </div>
            <div v-if="setting.accountNumber" class="pm-bank-row">
              <span class="pm-bank-label">เลขบัญชี</span>
              <span class="pm-bank-val pm-acc-num">
                {{ setting.accountNumber }}
                <button class="pm-copy" @click="copyAcc" title="คัดลอก">📋</button>
              </span>
            </div>
            <div v-if="!setting.bankName && !setting.accountNumber && !setting.qrUrl" class="pm-no-bank">
              ⚠️ ร้านยังไม่ได้ตั้งค่าข้อมูลการชำระเงิน
            </div>
          </div>
        </div>

        <!-- อัปโหลดสลิป -->
        <div class="pm-slip">
          <label class="pm-slip-label">แนบสลิปการโอน</label>

          <div v-if="slipPreview || order.slipUrl" class="pm-slip-preview">
            <img :src="slipPreview || order.slipUrl" alt="สลิป" />
            <span v-if="order.slipUrl && !slipPreview" class="pm-slip-done">✓ แนบไว้แล้ว</span>
          </div>

          <label class="pm-slip-btn">
            <span v-if="!uploading">📁 {{ (slipPreview || order.slipUrl) ? 'เปลี่ยนรูปสลิป' : 'เลือกรูปสลิป' }}</span>
            <span v-else>⏳ กำลังอัปโหลด...</span>
            <input type="file" accept="image/*" hidden @change="onPickSlip" :disabled="uploading" />
          </label>
        </div>

        <button
          class="pm-submit"
          :disabled="!slipUrl || submitting"
          @click="submitSlip"
        >
          {{ submitting ? 'กำลังส่ง...' : '✅ ยืนยันแจ้งชำระเงิน' }}
        </button>
      </div>
    </div>
  </transition>
</template>

<script>
import http from '../../api/http'

export default {
  name: 'PaymentModal',
  props: {
    order: { type: Object, default: null },
    currentUser: { type: Object, default: null }
  },
  emits: ['close', 'paid'],
  data() {
    return {
      setting: { bankName: '', accountName: '', accountNumber: '', qrUrl: '' },
      slipUrl: '',
      slipPreview: '',
      uploading: false,
      submitting: false
    }
  },
  watch: {
    order(val) {
      if (val) {
        this.slipUrl = '';
        this.slipPreview = '';
        this.loadSetting();
      }
    }
  },
  methods: {
    async loadSetting() {
      try {
        const res = await http.get('/api/shop-setting');
        this.setting = res.data || this.setting;
      } catch (e) {
        console.error('โหลดข้อมูลบัญชีไม่สำเร็จ:', e);
      }
    },
    async onPickSlip(event) {
      const file = event.target.files[0];
      if (!file) return;
      this.slipPreview = URL.createObjectURL(file);  // โชว์ทันที
      this.uploading = true;
      try {
        const formData = new FormData();
        formData.append('files', file);
        const res = await http.post('/api/products/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        this.slipUrl = (res.data.urls && res.data.urls[0]) || '';
      } catch (e) {
        alert(e.response?.data || '❌ อัปโหลดสลิปไม่สำเร็จ');
        this.slipPreview = '';
      } finally {
        this.uploading = false;
        event.target.value = '';
      }
    },
    async submitSlip() {
      if (!this.slipUrl) return;
      this.submitting = true;
      try {
        await http.put(`/api/orders/${this.order.id}/slip`, {
          slipUrl: this.slipUrl,
          customerUsername: this.currentUser?.username
        });
        alert('✅ แจ้งชำระเงินสำเร็จ! รอแอดมินตรวจสอบ');
        this.$emit('paid');
        this.$emit('close');
      } catch (e) {
        alert(e.response?.data || '❌ แจ้งชำระเงินไม่สำเร็จ');
      } finally {
        this.submitting = false;
      }
    },
    copyAcc() {
      navigator.clipboard?.writeText(this.setting.accountNumber || '');
      alert('📋 คัดลอกเลขบัญชีแล้ว');
    }
  }
}
</script>

<style scoped>
* { box-sizing: border-box; }
.pm-overlay {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, 0.65); backdrop-filter: blur(4px);
  z-index: 320; display: flex; align-items: center; justify-content: center;
  padding: 20px; font-family: 'Prompt', sans-serif;
}
.pm-box {
  position: relative; background: #fff; border-radius: 20px;
  width: 100%; max-width: 440px; max-height: 92vh; overflow-y: auto;
  padding: 28px; box-shadow: 0 24px 60px rgba(0,0,0,0.3);
}
.pm-close {
  position: absolute; top: 14px; right: 14px;
  width: 34px; height: 34px; border-radius: 50%;
  background: rgba(15,23,42,0.08); border: none; cursor: pointer; font-size: 0.95rem;
}
.pm-close:hover { background: rgba(15,23,42,0.18); }

.pm-header { text-align: center; margin-bottom: 18px; }
.pm-header h3 { margin: 0 0 4px; font-size: 1.3rem; color: #1e293b; }
.pm-order-id { font-size: 0.82rem; color: #94a3b8; }

.pm-amount {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 14px; padding: 18px; text-align: center; margin-bottom: 20px;
}
.pm-amount-label { display: block; font-size: 0.8rem; color: rgba(255,255,255,0.85); margin-bottom: 4px; }
.pm-amount-value { font-size: 1.8rem; font-weight: 700; color: #fff; }

.pm-bank {
  background: #f8fafc; border: 1px solid #eef0f7; border-radius: 14px;
  padding: 18px; margin-bottom: 20px;
  display: flex; gap: 16px; align-items: center;
}
.pm-qr { text-align: center; flex-shrink: 0; }
.pm-qr img { width: 110px; height: 110px; object-fit: contain; border-radius: 10px; background: #fff; border: 1px solid #eef0f7; }
.pm-qr-hint { display: block; font-size: 0.7rem; color: #94a3b8; margin-top: 4px; }
.pm-bank-info { flex: 1; min-width: 0; }
.pm-bank-row { display: flex; flex-direction: column; margin-bottom: 10px; }
.pm-bank-row:last-child { margin-bottom: 0; }
.pm-bank-label { font-size: 0.72rem; color: #94a3b8; }
.pm-bank-val { font-size: 0.92rem; font-weight: 600; color: #1e293b; }
.pm-acc-num { display: flex; align-items: center; gap: 8px; letter-spacing: 0.05em; }
.pm-copy { background: none; border: none; cursor: pointer; font-size: 0.9rem; padding: 0; }
.pm-no-bank { font-size: 0.82rem; color: #d97706; }

.pm-slip { margin-bottom: 20px; }
.pm-slip-label { display: block; font-size: 0.86rem; font-weight: 600; color: #475569; margin-bottom: 10px; }
.pm-slip-preview {
  position: relative; width: 100%; max-height: 220px; overflow: hidden;
  border-radius: 12px; border: 1px solid #eef0f7; margin-bottom: 10px; background: #f8fafc;
}
.pm-slip-preview img { width: 100%; object-fit: contain; max-height: 220px; display: block; }
.pm-slip-done {
  position: absolute; top: 8px; right: 8px;
  background: #10b981; color: #fff; font-size: 0.72rem; font-weight: 600;
  padding: 3px 10px; border-radius: 20px;
}
.pm-slip-btn {
  display: flex; align-items: center; justify-content: center;
  background: #eef0ff; color: #667eea; font-weight: 600; font-size: 0.88rem;
  padding: 12px; border-radius: 10px; cursor: pointer; transition: background 0.2s;
}
.pm-slip-btn:hover { background: #667eea; color: #fff; }

.pm-submit {
  width: 100%; background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; border: none; padding: 14px; border-radius: 12px;
  font-family: 'Prompt', sans-serif; font-size: 1rem; font-weight: 700; cursor: pointer;
  transition: transform 0.15s, box-shadow 0.2s; box-shadow: 0 6px 16px rgba(102,126,234,0.35);
}
.pm-submit:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 10px 24px rgba(102,126,234,0.45); }
.pm-submit:disabled { background: #cbd5e1; cursor: not-allowed; box-shadow: none; }

.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.25s; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
</style>