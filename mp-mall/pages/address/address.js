const { get, put, del } = require('../../utils/request');

Page({
  data: {
    addresses: [],
    loading: true
  },

  onShow() {
    this.loadAddresses();
  },

  async loadAddresses() {
    this.setData({ loading: true });
    try {
      const addresses = await get('/app/addresses');
      this.setData({ addresses: addresses || [], loading: false });
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  async setDefault(e) {
    try {
      await put(`/app/addresses/${e.currentTarget.dataset.id}/default`);
      wx.showToast({ title: '已设为默认', icon: 'success' });
      this.loadAddresses();
    } catch (err) {}
  },

  deleteAddress(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个地址吗？',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await del(`/app/addresses/${id}`);
          this.loadAddresses();
        } catch (err) {}
      }
    });
  },

  editAddress(e) {
    wx.navigateTo({ url: `/pages/address-form/address-form?id=${e.currentTarget.dataset.id}` });
  },

  addAddress() {
    wx.navigateTo({ url: '/pages/address-form/address-form' });
  }
});
