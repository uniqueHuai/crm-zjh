const app = getApp();
const { post } = require('../../utils/request');

Page({
  data: {
    canLogin: true
  },

  onLoad(options) {
    if (options.referrerId) {
      this.referrerId = options.referrerId;
    }
  },

  handleLogin() {
    if (!this.data.canLogin) return;
    this.setData({ canLogin: false });

    wx.login({
      success: (res) => {
        if (res.code) {
          this.doLogin(res.code);
          return;
        }
        wx.showToast({ title: '登录失败', icon: 'none' });
        this.setData({ canLogin: true });
      },
      fail: () => {
        this.setData({ canLogin: true });
      }
    });
  },

  async doLogin(code) {
    try {
      const payload = { code };
      if (this.referrerId) payload.referrerId = this.referrerId;

      const result = await post('/app/auth/login', payload);
      const { token, customer } = result;

      app.globalData.token = token;
      app.globalData.customerInfo = customer;
      wx.setStorageSync('mp_token', token);
      wx.setStorageSync('customer_info', customer);

      wx.showToast({ title: '登录成功', icon: 'success' });
      wx.switchTab({ url: '/pages/index/index' });
    } catch (err) {
      this.setData({ canLogin: true });
    }
  },

  handlePhoneNumber(e) {
    if (e.detail.errMsg === 'getPhoneNumber:ok' && e.detail.code) {
      post('/app/auth/phone', { code: e.detail.code });
    }
  }
});
