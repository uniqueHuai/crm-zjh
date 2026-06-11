const { get, post } = require('../../utils/request');

Page({
  data: {
    product: null,
    skus: [],
    currentSku: null,
    quantity: 1,
    loading: true
  },

  onLoad(options) {
    this.productId = options.id;
    this.loadProduct();
  },

  async loadProduct() {
    try {
      const product = await get(`/app/products/${this.productId}`);
      const skus = product.skus || [];
      this.setData({
        product,
        skus,
        currentSku: skus[0] || null,
        loading: false
      });
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  selectSku(e) {
    const currentSku = this.data.skus.find((sku) => sku.id === Number(e.currentTarget.dataset.id));
    if (currentSku) {
      this.setData({ currentSku });
    }
  },

  changeQty(e) {
    let quantity = this.data.quantity;
    if (e.currentTarget.dataset.type === 'minus' && quantity > 1) quantity -= 1;
    if (e.currentTarget.dataset.type === 'plus') quantity += 1;
    this.setData({ quantity });
  },

  async addToCart() {
    if (!this.data.currentSku) {
      wx.showToast({ title: '请选择规格', icon: 'none' });
      return;
    }

    try {
      await post('/app/cart', {
        skuId: this.data.currentSku.id,
        quantity: this.data.quantity
      });
      wx.showToast({ title: '已加入购物车', icon: 'success' });
    } catch (err) {}
  },

  buyNow() {
    if (!this.data.currentSku) {
      wx.showToast({ title: '请选择规格', icon: 'none' });
      return;
    }

    wx.navigateTo({
      url: `/pages/checkout/checkout?items=${this.data.currentSku.id}:${this.data.quantity}`
    });
  }
});
