const { get, post } = require('../../utils/request');

Page({
  data: {
    orders: [],
    currentTab: 0,
    tabs: ['全部', '待付款', '待发货', '待收货', '已完成'],
    loading: false,
    hasMore: true,
    page: 1
  },

  onLoad(options) {
    if (options.tab) {
      this.setData({ currentTab: Number(options.tab) || 0 });
    }
  },

  onShow() {
    this.setData({ orders: [], page: 1, hasMore: true });
    this.loadOrders();
  },

  switchTab(e) {
    this.setData({
      currentTab: e.currentTarget.dataset.index,
      orders: [],
      page: 1,
      hasMore: true
    });
    this.loadOrders();
  },

  mapOrder(order) {
    const displayItems = order.items && order.items.length
      ? order.items
      : [{
          id: order.id,
          productName: order.productName,
          productImage: order.productImage,
          price: order.totalAmount,
          quantity: 1
        }];

    return {
      ...order,
      displayStatus: this.getStatusText(order.status),
      displayItems,
      displayItemCount: order.itemCount || displayItems.length || 1
    };
  },

  async loadOrders() {
    if (!this.data.hasMore || this.data.loading) return;
    this.setData({ loading: true });

    try {
      const statusMap = ['', 'pending', 'paid', 'shipped', 'completed'];
      const params = { page: this.data.page, size: 10 };
      const status = statusMap[this.data.currentTab];
      if (status) params.status = status;

      const result = await get('/app/orders', params);
      const records = (result.records || []).map((order) => this.mapOrder(order));

      this.setData({
        orders: this.data.orders.concat(records),
        hasMore: records.length === 10,
        loading: false,
        page: this.data.page + 1
      });
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  onReachBottom() {
    this.loadOrders();
  },

  goDetail(e) {
    wx.navigateTo({ url: `/pages/order-detail/order-detail?id=${e.currentTarget.dataset.id}` });
  },

  async payOrder(e) {
    try {
      await post(`/app/orders/${e.currentTarget.dataset.id}/pay`, { method: 'offline' });
      wx.showToast({ title: '支付成功', icon: 'success' });
      this.setData({ orders: [], page: 1, hasMore: true });
      this.loadOrders();
    } catch (err) {}
  },

  confirmOrder(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认收货',
      content: '确认已收到商品吗？',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await post(`/app/orders/${id}/confirm`);
          wx.showToast({ title: '已确认收货', icon: 'success' });
          this.setData({ orders: [], page: 1, hasMore: true });
          this.loadOrders();
        } catch (err) {}
      }
    });
  },

  getStatusText(status) {
    const map = {
      pending: '待付款',
      paid: '待发货',
      shipped: '待收货',
      completed: '已完成',
      cancelled: '已取消'
    };
    return map[status] || status;
  }
});
