import react from '@vitejs/plugin-react'
import {defineConfig} from 'vite'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            '/api': {
                // Standard: Backend auf 8080. Für Tests überschreibbar mit BACKEND_URL.
                target: process.env.BACKEND_URL ?? "http://localhost:8080"
            }
        }
    }
})
