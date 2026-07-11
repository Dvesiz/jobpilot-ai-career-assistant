import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ConfigProvider locale={zhCN} theme={{ token: { colorPrimary: '#1d574c', borderRadius: 5, fontFamily: "'Source Han Sans CN', 'Microsoft YaHei', sans-serif" } }}>
      <App />
    </ConfigProvider>
  </StrictMode>,
)
