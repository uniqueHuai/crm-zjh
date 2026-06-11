const { get } = require('../../utils/request');
const app = getApp();

Page({
  data: {
    customerInfo: null,
    orderCounts: { pending: 0, paid: 0, shipped: 0 }
  },

  onShow() {
    if (!this.checkLogin()) return;
    this.loadData();
  },

  checkLogin() {
    if (!app.globalData.token) {
      wx.navigateTo({ url: '/pages/login/login' });
      return false;
    }

    const info = app.globalData.customerInfo || wx.getStorageSync('customer_info');
    if (info) {
      this.setData({ customerInfo: typeof info === 'string' ? JSON.parse(info) : info });
    }
    return true;
  },

  async loadData() {
    try {
      const [profile, orders] = await Promise.all([
        get('/app/auth/info').catch(() => null),
        get('/app/orders', { page: 1, size: 100 }).catch(() => ({ records: [] }))
      ]);

      if (profile) {
        this.setData({ customerInfo: profile });
        app.globalData.customerInfo = profile;
      }

      const records = orders.records || [];
      this.setData({
        orderCounts: {
          pending: records.filter((item) => item.status === 'pending').length,
          paid: records.filter((item) => item.status === 'paid').length,
          shipped: records.filter((item) => item.status === 'shipped').length
        }
      });
    } catch (err) {}
  },

  goOrders(e) {
    wx.navigateTo({ url: `/pages/order/order?tab=${e.currentTarget.dataset.tab || 0}` });
  },

  goAddresses() {
    wx.navigateTo({ url: '/pages/address/address' });
  },

  goCoupons() {
    wx.navigateTo({ url: '/pages/coupons/coupons' });
  },

  goDistribution() {
    wx.navigateTo({ url: '/pages/distribution/distribution' });
  },

  goActivities() {
    wx.navigateTo({ url: '/pages/activity/activity' });
  },

  goCustomerService() {
    wx.navigateTo({ url: '/pages/customer-service/customer-service' });
  }
});
