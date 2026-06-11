const { get, put, del } = require('../../utils/request');

Page({
  data: {
    items: [],
    selectedIds: [],
    totalPrice: '0.00',
    editing: false,
    loading: true,
    allChecked: false
  },

  onShow() {
    this.loadCart();
  },

  async loadCart() {
    this.setData({ loading: true });
    try {
      const items = (await get('/app/cart')) || [];
      this.setData({ items: this.decorateItems(items, []) , loading: false, selectedIds: [], allChecked: false });
      this.calcTotal();
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  decorateItems(items, selectedIds) {
    return items.map((item) => ({ ...item, checked: selectedIds.includes(item.id) }));
  },

  syncSelection(selectedIds) {
    const items = this.decorateItems(this.data.items, selectedIds);
    this.setData({
      selectedIds,
      items,
      allChecked: items.length > 0 && selectedIds.length === items.length
    });
    this.calcTotal();
  },

  toggleSelect(e) {
    const id = e.currentTarget.dataset.id;
    const selectedIds = this.data.selectedIds.includes(id)
      ? this.data.selectedIds.filter((itemId) => itemId !== id)
      : this.data.selectedIds.concat(id);
    this.syncSelection(selectedIds);
  },

  selectAll() {
    const selectedIds =
      this.data.selectedIds.length === this.data.items.length
        ? []
        : this.data.items.map((item) => item.id);
    this.syncSelection(selectedIds);
  },

  async changeQty(e) {
    const id = e.currentTarget.dataset.id;
    const type = e.currentTarget.dataset.type;
    const item = this.data.items.find((cartItem) => cartItem.id === id);
    if (!item) return;

    let quantity = item.quantity;
    if (type === 'minus' && quantity > 1) quantity -= 1;
    if (type === 'plus') quantity += 1;
    if (quantity === item.quantity) return;

    try {
      await put(`/app/cart/${id}/quantity`, { quantity });
      item.quantity = quantity;
      this.setData({ items: this.decorateItems(this.data.items, this.data.selectedIds) });
      this.calcTotal();
    } catch (err) {}
  },

  async deleteItem(e) {
    const id = e.currentTarget.dataset.id;
    try {
      await del(`/app/cart/${id}`);
      const items = this.data.items.filter((item) => item.id !== id);
      const selectedIds = this.data.selectedIds.filter((itemId) => itemId !== id);
      this.setData({
        items: this.decorateItems(items, selectedIds),
        selectedIds,
        allChecked: items.length > 0 && items.length === selectedIds.length
      });
      this.calcTotal();
    } catch (err) {}
  },

  calcTotal() {
    const total = this.data.items.reduce((sum, item) => {
      if (!item.checked) return sum;
      return sum + Number(item.price || 0) * Number(item.quantity || 0);
    }, 0);
    this.setData({ totalPrice: total.toFixed(2) });
  },

  toggleEdit() {
    this.setData({ editing: !this.data.editing });
  },

  goHome() {
    wx.switchTab({ url: '/pages/index/index' });
  },

  goCheckout() {
    if (this.data.selectedIds.length === 0) {
      wx.showToast({ title: '请选择商品', icon: 'none' });
      return;
    }

    const items = this.data.items
      .filter((item) => item.checked)
      .map((item) => `${item.skuId}:${item.quantity}`)
      .join(',');
    wx.navigateTo({ url: `/pages/checkout/checkout?items=${items}` });
  }
});
