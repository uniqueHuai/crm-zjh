const { get } = require('../../utils/request');
const app = getApp();

// 默认页面配置（无模板时的降级方案）
const DEFAULT_PAGE_CONFIG = [
  { type: 'search', visible: true, props: { placeholder: '搜索商品' } },
  { type: 'banner', visible: true, props: {} },
  { type: 'category_grid', visible: true, props: { title: '分类导航' } },
  { type: 'product_list', visible: true, props: { title: '热门推荐', count: 6 } },
  { type: 'activity_list', visible: true, props: { title: '限时活动' } },
];

Page({
  data: {
    modules: [],
    banners: [
      { id: 1, image: '/images/banner1.png' },
      { id: 2, image: '/images/banner2.png' }
    ],
    notice: null,
    categories: [],
    hotProducts: [],
    activities: [],
    loading: true
  },

  onShow() {
    this.checkLogin();
    this.loadPageTemplate();
  },

  checkLogin() {
    if (!app.globalData.token) {
      wx.navigateTo({ url: '/pages/login/login' });
    }
  },

  // 获取页面模板配置
  async loadPageTemplate() {
    this.setData({ loading: true });
    try {
      const res = await get('/app/page-templates/homepage');
      const rawConfig = res && res.pageConfig ? res.pageConfig : null;
      const modules = this.parseModules(rawConfig);
      this.setData({ modules }, () => this.loadModuleData(modules));
    } catch (err) {
      // 模板不存在则使用默认配置
      this.setData({ modules: DEFAULT_PAGE_CONFIG }, () => this.loadModuleData(DEFAULT_PAGE_CONFIG));
    }
  },

  // 解析后端 pageConfig JSON
  parseModules(rawConfig) {
    if (!rawConfig) return DEFAULT_PAGE_CONFIG;
    try {
      const parsed = typeof rawConfig === 'string' ? JSON.parse(rawConfig) : rawConfig;
      return Array.isArray(parsed) ? parsed : DEFAULT_PAGE_CONFIG;
    } catch {
      return DEFAULT_PAGE_CONFIG;
    }
  },

  // 根据模板模块类型加载对应数据
  async loadModuleData(modules) {
    // 找出需要后端数据的模块类型
    const needCategories = modules.some(m => m.type === 'category_grid' && m.visible !== false);
    const needProducts = modules.some(m => m.type === 'product_list' && m.visible !== false);
    const needActivities = modules.some(m => m.type === 'activity_list' && m.visible !== false);

    // 从模板中提取 banner 和 notice 配置
    const bannerModule = modules.find(m => m.type === 'banner' && m.visible !== false);
    const noticeModule = modules.find(m => m.type === 'notice' && m.visible !== false);

    try {
      const promises = [];
      if (bannerModule && bannerModule.props?.images) {
        this.setData({ banners: bannerModule.props.images });
      }
      if (noticeModule) {
        this.setData({ notice: noticeModule.props });
      }
      if (needCategories) promises.push(get('/app/products/categories').catch(() => []));
      if (needProducts) {
        const productModule = modules.find(m => m.type === 'product_list');
        const size = productModule?.props?.count || 6;
        promises.push(get('/app/products', { page: 1, size }).catch(() => ({ records: [] })));
      }
      if (needActivities) promises.push(get('/app/activities').catch(() => []));

      const results = await Promise.all(promises);
      let idx = 0;
      if (needCategories) {
        const categories = results[idx++] || [];
        this.setData({ categories: Array.isArray(categories) ? categories : [] });
      }
      if (needProducts) {
        const products = results[idx++] || { records: [] };
        this.setData({ hotProducts: products.records || [] });
      }
      if (needActivities) {
        this.setData({ activities: results[idx] || [] });
      }
    } catch (err) {
      // 局部数据加载失败不影响页面渲染
    } finally {
      this.setData({ loading: false });
    }
  },

  goCategory() {
    wx.switchTab({ url: '/pages/category/category' });
  },

  goProduct(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/product/product?id=${id}` });
  },

  goActivity(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/activity/activity?id=${id}` });
  },

  goSearch() {
    wx.navigateTo({ url: '/pages/category/category?search=1' });
  },

  goNotice() {
    const notice = this.data.notice;
    if (notice?.link) {
      wx.navigateTo({ url: notice.link });
    }
  },

  goAd(e) {
    const link = e.currentTarget.dataset.link;
    if (link) {
      wx.navigateTo({ url: link });
    }
  }
});
