function request(url, method = 'GET', data = {}) {
  return new Promise((resolve, reject) => {
    const app = getApp();
    const token = app.globalData.token;
    const header = { 'Content-Type': 'application/json' };

    if (token) {
      header.Authorization = 'Bearer ' + token;
    }

    wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data,
      header,
      success(res) {
        const { code, message, data: result } = res.data || {};
        if (code === 200) {
          resolve(result);
          return;
        }

        if (code === 401) {
          wx.removeStorageSync('mp_token');
          wx.removeStorageSync('customer_info');
          app.globalData.token = null;
          app.globalData.customerInfo = null;
          wx.showToast({ title: '登录已过期', icon: 'none' });
          setTimeout(() => {
            wx.reLaunch({ url: '/pages/login/login' });
          }, 1000);
          reject(new Error(message || '未授权'));
          return;
        }

        wx.showToast({ title: message || '请求失败', icon: 'none' });
        reject(new Error(message || '请求失败'));
      },
      fail(err) {
        wx.showToast({ title: '网络异常', icon: 'none' });
        reject(err);
      }
    });
  });
}

module.exports = {
  get: (url, params) => request(url, 'GET', params),
  post: (url, data) => request(url, 'POST', data),
  put: (url, data) => request(url, 'PUT', data),
  del: (url) => request(url, 'DELETE')
};
