const { get, post } = require('../../utils/request');

Page({
  data: {
    coupons: [],
    tab: 'available',
    loading: true
  },

  onShow() {
    this.loadCoupons();
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ tab, coupons: [], loading: true });
    this.loadCoupons(tab);
  },

  async loadCoupons(tab = this.data.tab) {
    try {
      const coupons =
        tab === 'available'
          ? await get('/app/coupons/available', { amount: 0 })
          : await get('/app/coupons/my');

      this.setData({ coupons: coupons || [], loading: false });
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  async claimCoupon(e) {
    try {
      await post(`/app/coupons/${e.currentTarget.dataset.id}/claim`);
      wx.showToast({ title: '领取成功', icon: 'success' });
      this.loadCoupons();
    } catch (err) {}
  }
});
