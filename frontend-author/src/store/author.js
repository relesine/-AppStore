import { defineStore } from 'pinia'

export const useAuthorStore = defineStore('author', {
  state: () => ({
    token: localStorage.getItem('author_token') || '',
    userInfo: JSON.parse(localStorage.getItem('author_info') || 'null'),
  }),
  getters: { isLogin: (state) => !!state.token },
  actions: {
    setLogin(data) {
      this.token = data.token
      this.userInfo = data
      localStorage.setItem('author_token', data.token)
      localStorage.setItem('author_info', JSON.stringify(data))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('author_token')
      localStorage.removeItem('author_info')
    },
  },
})
