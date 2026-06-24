// i18n 国际化配置 Internationalization setup
import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS,
}

const savedLocale = localStorage.getItem('locale') || 'zh-CN'

const i18n = createI18n({
  legacy: false,         // Composition API 模式 Composition API mode
  locale: savedLocale,
  fallbackLocale: 'en-US',
  messages,
})

export default i18n
