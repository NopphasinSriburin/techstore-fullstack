# ============================================================
#  Frontend Dockerfile (Vue 3 / Vite) — multi-stage
#  วางไฟล์นี้ที่: frontend/Dockerfile
# ============================================================

# ---------- Stage 1: build static files ----------
FROM node:20-alpine AS build
WORKDIR /app

# คัดลอก package ก่อน เพื่อ cache npm install
COPY package*.json ./
RUN npm install

# คัดลอก source แล้ว build เป็นไฟล์ static (โฟลเดอร์ dist)
COPY . .
RUN npm run build

# ---------- Stage 2: serve ด้วย nginx ----------
FROM nginx:alpine

# คัดลอกไฟล์ static ที่ build เสร็จไปให้ nginx เสิร์ฟ
COPY --from=build /app/dist /usr/share/nginx/html

# คัดลอก nginx config (รองรับ Vue routing แบบ SPA)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]