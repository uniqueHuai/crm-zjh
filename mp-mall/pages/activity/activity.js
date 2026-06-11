const { get, post } = require('../../utils/request');

Page({
  data: {
    activities: [],
    selectedActivity: null,
    products: [],
    loading: true,
    activeTab: 0
  },

  onLoad(options) {
    this.loadActivities(options.id);
  },

  async loadActivities(selectId) {
    this.setData({ loading: true });
    try {
      const activities = await get('/app/activities');
      const index = selectId ? activities.findIndex((item) => item.id === Number(selectId)) : 0;
      const activeTab = index >= 0 ? index : 0;

      this.setData({
        activities: activities || [],
        activeTab,
        loading: false
      });

      if (activities && activities.length > 0) {
        this.selectActivity({ currentTarget: { dataset: { index: activeTab } } });
      }
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  selectActivity(e) {
    const activity = this.data.activities[e.currentTarget.dataset.index];
    if (!activity) return;

    this.setData({
      selectedActivity: activity,
      activeTab: e.currentTarget.dataset.index,
      loading: true
    });
    this.loadProducts(activity.id);
  },

  async loadProducts(activityId) {
    try {
      const products = await get(`/app/activities/${activityId}/products`);
      this.setData({ products: products || [], loading: false });
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  goProduct(e) {
    wx.navigateTo({ url: `/pages/product/product?id=${e.currentTarget.dataset.id}` });
  },

  async seckillBuy(e) {
    const skuId = e.currentTarget.dataset.skuId;
    if (!skuId) {
      wx.showToast({ title: '请选择规格', icon: 'none' });
      return;
    }

    try {
      const order = await post('/app/activities/seckill', {
        activityId: this.data.selectedActivity.id,
        skuId,
        quantity: 1
      });
      wx.showToast({ title: '下单成功', icon: 'success' });
      wx.navigateTo({ url: `/pages/order-detail/order-detail?id=${order.id}` });
    } catch (err) {}
  },

  async groupBuy(e) {
    const skuId = e.currentTarget.dataset.skuId;
    if (!skuId) {
      wx.showToast({ title: '请选择规格', icon: 'none' });
      return;
    }

    try {
      const order = await post('/app/activities/group', {
        activityId: this.data.selectedActivity.id,
        skuId,
        quantity: 1
      });
      wx.showToast({ title: '参团成功', icon: 'success' });
      wx.navigateTo({ url: `/pages/order-detail/order-detail?id=${order.id}` });
    } catch (err) {}
  }
});
