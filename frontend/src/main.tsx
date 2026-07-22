import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { App as AntdApp, ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ConfigProvider locale={zhCN} theme={{ token: { colorPrimary: '#173f36', colorInfo: '#173f36', colorSuccess: '#39705d', colorWarning: '#a65b35', colorText: '#202522', colorBorder: '#d7d9d3', borderRadius: 3, fontFamily: "'Noto Sans SC', 'Microsoft YaHei', sans-serif", controlHeight: 42 } }}>
      <AntdApp><App /></AntdApp>
    </ConfigProvider>
  </StrictMode>,
)
