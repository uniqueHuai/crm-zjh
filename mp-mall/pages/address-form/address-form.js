const { get, post, put } = require('../../utils/request');

Page({
  data: {
    mode: 'create',
    addressId: null,
    form: {
      name: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      address: '',
      isDefault: false
    },
    saving: false
  },

  onLoad(options) {
    if (!options.id) return;
    this.setData({ mode: 'edit', addressId: Number(options.id) });
    wx.setNavigationBarTitle({ title: '编辑地址' });
    this.loadAddress();
  },

  async loadAddress() {
    try {
      const addresses = await get('/app/addresses');
      const address = (addresses || []).find((item) => item.id === this.data.addressId);
      if (!address) return;
      this.setData({
        form: {
          name: address.name || '',
          phone: address.phone || '',
          province: address.province || '',
          city: address.city || '',
          district: address.district || '',
          address: address.address || '',
          isDefault: !!address.isDefault
        }
      });
    } catch (err) {
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  onFieldChange(e) {
    this.setData({
      [`form.${e.currentTarget.dataset.field}`]: e.detail.value
    });
  },

  onDefaultChange(e) {
    this.setData({ 'form.isDefault': e.detail.value });
  },

  async handleSave() {
    const { form } = this.data;

    if (!form.name.trim()) {
      wx.showToast({ title: '请填写收件人姓名', icon: 'none' });
      return;
    }
    if (!/^1\d{10}$/.test(form.phone.trim())) {
      wx.showToast({ title: '请填写正确的手机号', icon: 'none' });
      return;
    }
    if (!form.province.trim()) {
      wx.showToast({ title: '请填写省份', icon: 'none' });
      return;
    }
    if (!form.city.trim()) {
      wx.showToast({ title: '请填写城市', icon: 'none' });
      return;
    }
    if (!form.address.trim()) {
      wx.showToast({ title: '请填写详细地址', icon: 'none' });
      return;
    }

    this.setData({ saving: true });
    try {
      if (this.data.mode === 'edit') {
        await put(`/app/addresses/${this.data.addressId}`, form);
        wx.showToast({ title: '修改成功', icon: 'success' });
      } else {
        await post('/app/addresses', form);
        wx.showToast({ title: '添加成功', icon: 'success' });
      }

      setTimeout(() => {
        wx.navigateBack();
      }, 800);
    } catch (err) {
      this.setData({ saving: false });
    }
  }
});
