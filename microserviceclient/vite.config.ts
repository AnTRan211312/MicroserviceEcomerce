import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        timeout: 10000,
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, res) => {
            console.error('\n❌ ==========================================');
            console.error('❌ Proxy Error: Không thể kết nối đến API Gateway');
            console.error('❌ ==========================================');
            console.error('📍 Lỗi:', err.message);
            console.error('\n💡 API Gateway chưa được khởi động trên port 8080');
            console.error('\n📋 HƯỚNG DẪN KHỞI ĐỘNG:');
            console.error('   1. Khởi động Eureka Server (port 8761):');
            console.error('      cd eureka-server');
            console.error('      mvn spring-boot:run');
            console.error('   ');
            console.error('   2. Khởi động API Gateway (port 8080):');
            console.error('      cd api-gateway');
            console.error('      mvn spring-boot:run');
            console.error('   ');
            console.error('   3. Hoặc chạy trong IntelliJ IDEA');
            console.error('   ');
            console.error('📄 Xem file START_SERVICES.md để biết thêm chi tiết');
            console.error('❌ ==========================================\n');
            
            if (res && typeof (res as any).writeHead === 'function' && !(res as any).headersSent) {
              (res as any).writeHead(503, {
                'Content-Type': 'application/json',
              });
              (res as any).end(
                JSON.stringify({
                  error: 'Service Unavailable',
                  message: 'API Gateway không khả dụng. Vui lòng khởi động API Gateway trên port 8080 trước.',
                  details: err.message,
                  instructions: {
                    step1: 'Khởi động Eureka Server: cd eureka-server && mvn spring-boot:run',
                    step2: 'Khởi động API Gateway: cd api-gateway && mvn spring-boot:run',
                    docs: 'Xem file START_SERVICES.md để biết thêm chi tiết'
                  }
                })
              );
            }
          });
          proxy.on('proxyReq', (_proxyReq, req) => {
            console.log(`🔄 Proxying ${req.method} ${req.url} -> http://localhost:8080${req.url}`);
          });
        },
      },
      '/prometheus': {
        target: 'http://localhost:9090',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/prometheus/, ''),
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req) => {
            console.error('❌ Prometheus proxy error:', err.message);
            console.error('💡 Đảm bảo Prometheus đang chạy trên http://localhost:9090');
          });
        },
      },
    },
  },
})
