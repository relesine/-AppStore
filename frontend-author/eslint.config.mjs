// 轻量应用商店 — ESLint 9 Flat Config（作者端）
// 运行：npm run lint（检查并自动修复） / npm run lint:check（只检查）
import pluginVue from 'eslint-plugin-vue'
import eslintPluginPrettierRecommended from 'eslint-plugin-prettier/recommended'

export default [
  // 忽略构建产物与自动生成文件
  {
    ignores: ['dist/**', 'node_modules/**', 'auto-imports.d.ts', 'components.d.ts', '*.local'],
  },
  // Vue 3 官方推荐规则集
  ...pluginVue.configs['flat/recommended'],
  // 项目风格调整
  {
    files: ['**/*.{js,vue}'],
    languageOptions: {
      sourceType: 'module',
    },
    rules: {
      // 允许单词命名的页面组件（Home.vue 等）
      'vue/multi-word-component-names': 'off',
      // 模板格式交给 Prettier 统一
      'vue/max-attributes-per-line': 'off',
      'vue/singleline-html-element-content-newline': 'off',
      'vue/html-self-closing': 'off',
      'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
      'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
      'no-unused-vars': ['warn', { args: 'none' }],
    },
  },
  // Prettier 集成：格式即规则，冲突区域可自动修复
  eslintPluginPrettierRecommended,
  {
    rules: {
      'prettier/prettier': [
        'warn',
        {
          semi: false,
          singleQuote: true,
          printWidth: 100,
          endOfLine: 'lf',
        },
      ],
    },
  },
]
