const { get, post } = require('../../utils/request');

Page({
  data: {
    profile: null,
    team: [],
    commission: [],
    tab: 'team',
    loading: true
  },

  onShow() {
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const profile = await get('/app/distribution/my');
      this.setData({ profile: profile || null, loading: false });
      this.loadTeam();
      this.loadCommission();
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  async loadTeam() {
    try {
      const team = await get('/app/distribution/team');
      this.setData({ team: team || [] });
    } catch (err) {}
  },

  async loadCommission() {
    try {
      const commission = ((await get('/app/distribution/commission')) || []).map((item) => ({
        ...item,
        statusText: this.getCommissionStatus(item.status)
      }));
      this.setData({ commission });
    } catch (err) {}
  },

  switchTab(e) {
    this.setData({ tab: e.currentTarget.dataset.tab });
  },

  bindReferral() {
    wx.showModal({
      title: '绑定推荐人',
      content: '请输入推荐人的ID',
      editable: true,
      success: async (res) => {
        if (!res.confirm || !res.content) return;
        try {
          await post('/app/distribution/bind', { referrerId: Number(res.content) });
          wx.showToast({ title: '绑定成功', icon: 'success' });
          this.loadData();
        } catch (err) {}
      }
    });
  },

  getCommissionStatus(status) {
    const map = { pending: '待结算', settled: '已结算', cancelled: '已失效' };
    return map[status] || status;
  }
});
