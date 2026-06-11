const { get, post } = require('../../utils/request');

Page({
  data: {
    order: null,
    orderItems: [],
    orderStatusText: '',
    orderStatusType: 'gray',
    formattedAmount: '0.00',
    formattedDiscount: '0.00',
    formattedShipping: '0.00',
    formattedPayAmount: '0.00',
    loading: true
  },

  onLoad(options) {
    if (options.id) {
      this.orderId = options.id;
      this.loadOrder();
    }
  },

  onShow() {
    if (this.orderId) {
      this.loadOrder();
    }
  },

  async loadOrder() {
    this.setData({ loading: true });
    try {
      const order = await get(`/app/orders/${this.orderId}`);
      this.setData({
        order,
        orderItems: order.items && order.items.length ? order.items : [order],
        orderStatusText: this.getStatusText(order.status),
        orderStatusType: this.getStatusType(order.status),
        formattedAmount: Number(order.totalAmount || 0).toFixed(2),
        formattedDiscount: Number(order.discountAmount || 0).toFixed(2),
        formattedShipping: Number(order.shippingFee || 0).toFixed(2),
        formattedPayAmount: Number(order.payAmount || order.totalAmount || 0).toFixed(2),
        loading: false
      });
    } catch (err) {
      this.setData({ loading: false });
      wx.showToast({ title: '订单加载失败', icon: 'none' });
    }
  },

  async payOrder() {
    try {
      await post(`/app/orders/${this.orderId}/pay`, { method: 'offline' });
      wx.showToast({ title: '支付成功', icon: 'success' });
      this.loadOrder();
    } catch (err) {}
  },

  confirmOrder() {
    wx.showModal({
      title: '确认收货',
      content: '确认已收到商品吗？',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await post(`/app/orders/${this.orderId}/confirm`);
          wx.showToast({ title: '已确认收货', icon: 'success' });
          this.loadOrder();
        } catch (err) {}
      }
    });
  },

  cancelOrder() {
    wx.showModal({
      title: '取消订单',
      content: '确定要取消这个订单吗？',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await post(`/app/orders/${this.orderId}/cancel`);
          wx.showToast({ title: '已取消', icon: 'success' });
          this.loadOrder();
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
  },

  getStatusType(status) {
    const map = {
      pending: 'orange',
      paid: 'blue',
      shipped: 'blue',
      completed: 'green',
      cancelled: 'gray'
    };
    return map[status] || 'gray';
  },

  copyText(e) {
    wx.setClipboardData({ data: String(e.currentTarget.dataset.text) });
  }
});
