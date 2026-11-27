import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// 11_02: 개발할 때만 API를 8080 앱으로 넘긴다. 배포에서는 Traefik(11_02)·CloudFront(11_04)가 같은 주소로 묶는다
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: { '/product': 'http://localhost:8080' },
  },
})
