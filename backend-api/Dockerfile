# ============================================================
#  Backend Dockerfile (Spring Boot / Java 25) — multi-stage
#  วางไฟล์นี้ที่: backend-api/Dockerfile
# ============================================================

# ---------- Stage 1: build jar ด้วย Maven ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# คัดลอกไฟล์ build ก่อน เพื่อให้ Docker cache layer dependency ได้
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# ดาวน์โหลด dependency ล่วงหน้า (cache ไว้ ถ้า pom ไม่เปลี่ยนจะไม่โหลดใหม่)
RUN ./mvnw dependency:go-offline -B

# คัดลอก source แล้ว build (ข้าม test เพื่อความเร็ว; เอาออกได้ถ้าอยากรันเทสต์)
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---------- Stage 2: runtime image เล็ก (เอาแค่ jdk รัน) ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# คัดลอกเฉพาะ jar ที่ build เสร็จจาก stage แรก
COPY --from=build /app/target/*.jar app.jar

# สร้างโฟลเดอร์เก็บรูปอัปโหลด (mount เป็น volume ตอน compose)
RUN mkdir -p /app/uploads

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]