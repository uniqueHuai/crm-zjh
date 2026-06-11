const { get } = require('../../utils/request');

Page({
  data: {
    categories: [],
    currentCategory: null,
    products: [],
    page: 1,
    hasMore: true,
    loading: false,
    keywords: ''
  },

  onLoad() {
    this.loadCategories();
  },

  async loadCategories() {
    try {
      const categories = await get('/app/products/categories');
      this.setData({ categories: categories || [] });
      if (categories && categories.length > 0) {
        this.applyCategory(categories[0]);
      }
    } catch (err) {}
  },

  selectCategory(e) {
    const category = e.currentTarget.dataset.item;
    this.applyCategory(category);
  },

  applyCategory(category) {
    this.setData({
      currentCategory: category || null,
      products: [],
      page: 1,
      hasMore: true
    });
    this.loadProducts();
  },

  async loadProducts() {
    if (!this.data.hasMore || this.data.loading) return;
    this.setData({ loading: true });

    try {
      const params = { page: this.data.page, size: 10 };
      if (this.data.currentCategory) params.categoryId = this.data.currentCategory.id;
      if (this.data.keywords) params.keywords = this.data.keywords;

      const result = await get('/app/products', params);
      const records = result.records || [];

      this.setData({
        products: this.data.products.concat(records),
        hasMore: records.length === 10,
        page: this.data.page + 1,
        loading: false
      });
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  onSearchInput(e) {
    this.setData({ keywords: e.detail.value });
  },

  onSearch() {
    this.setData({ products: [], page: 1, hasMore: true });
    this.loadProducts();
  },

  goProduct(e) {
    wx.navigateTo({ url: `/pages/product/product?id=${e.currentTarget.dataset.id}` });
  },

  onReachBottom() {
    this.loadProducts();
  }
});
