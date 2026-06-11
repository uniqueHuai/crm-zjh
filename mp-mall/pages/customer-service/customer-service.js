const app = getApp();

Page({
  data: {
    messages: [],
    inputMessage: '',
    hasInput: false,
    loading: false,
    showQuickQuestions: true,
    scrollToId: '',
    conversationId: null,
    customerId: '',
    quickQuestions: [
      '你们有哪些商品？',
      '如何下单购买？',
      '支持哪些支付方式？',
      '多久可以发货？',
      '如何申请退换货？',
      '售后服务怎么联系？'
    ]
  },

  onLoad() {
    const customerInfo = app.globalData.customerInfo || wx.getStorageSync('customer_info');
    if (customerInfo && customerInfo.id) {
      this.setData({ customerId: customerInfo.id });
    }

    this.setData({
      messages: [{ role: 'assistant', content: '您好，我是智能客服小C，请问有什么可以帮您？' }]
    });
  },

  onInputChange(e) {
    const inputMessage = e.detail.value;
    this.setData({ inputMessage, hasInput: inputMessage.trim().length > 0 });
  },

  sendQuickQuestion(e) {
    this.setData({
      inputMessage: e.currentTarget.dataset.question,
      hasInput: true,
      showQuickQuestions: false
    });
    this.sendMessage();
  },

  async sendMessage() {
    const content = this.data.inputMessage.trim();
    if (!content || this.data.loading) return;

    const userMessage = { role: 'user', content };
    const nextMessages = this.data.messages.concat(userMessage);

    this.setData({
      messages: nextMessages,
      inputMessage: '',
      hasInput: false,
      loading: true,
      showQuickQuestions: false,
      scrollToId: `msg-${nextMessages.length - 1}`
    });

    try {
      const token = app.globalData.token || wx.getStorageSync('mp_token');
      if (!token) {
        this.setData({ loading: false });
        wx.navigateTo({ url: '/pages/login/login' });
        return;
      }

      const response = await new Promise((resolve, reject) => {
        wx.request({
          url: `${app.globalData.baseUrl}/ai/app/customer-service/chat`,
          method: 'POST',
          data: {
            conversationId: this.data.conversationId,
            customerId: this.data.customerId,
            message: content
          },
          header: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
            Accept: 'text/event-stream'
          },
          responseType: 'text',
          success: resolve,
          fail: reject
        });
      });

      let responseText = '';
      if (response.data instanceof ArrayBuffer) {
        responseText = new TextDecoder('utf-8').decode(response.data);
      } else {
        responseText = String(response.data || '');
      }

      let reply = '';
      let conversationId = this.data.conversationId;
      const lines = responseText.replace(/\r\n/g, '\n').split('\n');

      lines.forEach((line) => {
        const current = line.trim();
        if (!current.startsWith('data: ')) return;

        const payload = current.slice(6);
        if (payload === '[DONE]') return;

        try {
          const parsed = JSON.parse(payload);
          if (parsed.conv_id) {
            conversationId = parsed.conv_id;
          } else if (parsed.error) {
            reply = '抱歉，我暂时无法回答这个问题，已为您转接人工客服。';
          } else if (parsed.content) {
            reply += parsed.content;
          }
        } catch (err) {}
      });

      if (!reply) {
        reply = '抱歉，暂时无法处理您的请求，请稍后再试。';
      }

      const messages = this.data.messages.concat({ role: 'assistant', content: reply });
      this.setData({
        messages,
        conversationId,
        loading: false,
        scrollToId: `msg-${messages.length - 1}`
      });
    } catch (err) {
      wx.showToast({ title: '网络异常', icon: 'none' });
      this.setData({ loading: false });
    }
  }
});
