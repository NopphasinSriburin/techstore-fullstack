package com.gadgetshop.backend_api;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Component;

import java.io.IOException;

//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE) // 👑 สั่งให้ทำงานเป็นด่านแรกสุดของเซิร์ฟเวอร์ ก่อนระบบความปลอดภัยใดๆ
public class GlobalCorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // บังคับยัดหัว Header อนุญาตให้หน้าบ้านพอร์ต 5173 เข้าถึงได้ชัวร์ๆ
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // ถ้าน่าบ้านส่งสัญญาณทักทาย (Preflight OPTIONS) มา ให้ตอบกลับสถานะผ่านทันที 200 OK
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res); // ปล่อยให้คำสั่งวิ่งไปที่ Controller ตามปกติ
        }
    }
}