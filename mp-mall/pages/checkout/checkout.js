const { get, post } = require('../../utils/request');

Page({
  data: {
    items: [],
    defaultAddress: null,
    addresses: [],
    couponList: [],
    selectedCoupon: null,
    remark: '',
    totalAmount: '0.00',
    discountAmount: '0.00',
    payAmount: '0.00',
    loading: true,
    submitting: false
  },

  onLoad(options) {
    this.itemsStr = options.items || '';
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const [addresses, cartItems] = await Promise.all([
        get('/app/addresses').catch(() => []),
        get('/app/cart').catch(() => [])
      ]);

      const items = this.parseItems(cartItems || []);
      const defaultAddress = (addresses || []).find((item) => item.isDefault) || (addresses || [])[0] || null;

      this.setData({
        addresses: addresses || [],
        defaultAddress,
        items,
        loading: false
      });
      this.calcAmount();
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  parseItems(cartItems) {
    if (!this.itemsStr) {
      return cartItems.map((item) => ({
        skuId: item.skuId,
        productName: item.productName,
        productImage: item.productImage,
        skuSpecs: item.skuSpecs,
        price: item.price,
        quantity: item.quantity,
        stock: item.stock
      }));
    }

    return this.itemsStr.split(',').map((pair) => {
      const [skuIdText, qtyText] = pair.split(':');
      const skuId = Number(skuIdText);
      const quantity = Number(qtyText || 1);
      const matched = cartItems.find((item) => item.skuId === skuId);

      return matched
        ? { ...matched, quantity }
        : { skuId, quantity, productName: '商品', price: 0, productImage: '' };
    });
  },

  async loadCoupons() {
    try {
      const coupons = await get('/app/coupons/available', { amount: this.data.payAmount });
      this.setData({ couponList: coupons || [] });
      return coupons || [];
    } catch (err) {
      return [];
    }
  },

  calcAmount() {
    const total = this.data.items.reduce((sum, item) => {
      return sum + Number(item.price || 0) * Number(item.quantity || 0);
    }, 0);
    const discount = Number(this.data.selectedCoupon ? this.data.selectedCoupon.discount || 0 : 0);
    const payAmount = Math.max(0, total - discount);

    this.setData({
      totalAmount: total.toFixed(2),
      discountAmount: discount.toFixed(2),
      payAmount: payAmount.toFixed(2)
    });
  },

  selectAddress() {
    const addressList = this.data.addresses;
    if (addressList.length === 0) {
      wx.navigateTo({ url: '/pages/address/address' });
      return;
    }

    wx.showActionSheet({
      itemList: addressList.map((item) => `${item.name} ${item.phone} ${item.province || ''}${item.city || ''}${item.district || ''}${item.address || ''}`),
      success: (res) => {
        this.setData({ defaultAddress: addressList[res.tapIndex] });
      }
    });
  },

  async selectCoupon() {
    const couponList = await this.loadCoupons();
    if (couponList.length === 0) {
      wx.showToast({ title: '暂无可用优惠券', icon: 'none' });
      return;
    }

    wx.showActionSheet({
      itemList: couponList.map((item) => `${item.name} - ¥${item.discount || 0}`),
      success: (res) => {
        this.setData({ selectedCoupon: couponList[res.tapIndex] });
        this.calcAmount();
      }
    });
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
  },

  async submitOrder() {
    if (!this.data.defaultAddress) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' });
      return;
    }

    if (this.data.submitting) return;
    this.setData({ submitting: true });

    try {
      const order = await post('/app/orders', {
        items: this.data.items.map((item) => ({ skuId: item.skuId, quantity: item.quantity })),
        addressId: this.data.defaultAddress.id,
        couponId: this.data.selectedCoupon ? this.data.selectedCoupon.id : null,
        remark: this.data.remark
      });

      wx.redirectTo({ url: `/pages/order-detail/order-detail?id=${order.id}` });
    } catch (err) {
      this.setData({ submitting: false });
    }
  }
});
