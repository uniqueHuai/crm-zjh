App({
  globalData: {
    token: null,
    customerInfo: null,
    baseUrl: 'http://localhost:8080/api/v1'
  },

  onLaunch() {
    const token = wx.getStorageSync('mp_token');
    const info = wx.getStorageSync('customer_info');
    if (token) {
      this.globalData.token = token;
      this.globalData.customerInfo = info;
    }
  }
});
