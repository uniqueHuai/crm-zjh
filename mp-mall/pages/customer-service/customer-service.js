const app = getApp();
const { get, post } = require('../../utils/request');

Page({
  data: {
    messages: [],
    inputMessage: '',
    hasInput: false,
    loading: false,
    conversationId: null,
    conversationStatus: 'ai',     // 'ai' | 'handoff' | 'human'
    showQuickQuestions: true,
    quickQuestions: [
      '怎么查看订单物流？',
      '如何申请退款？',
      '发货后多久能到？',
      '可以修改地址吗？',
      '转人工客服',
    ],
    scrollToId: '',
    pollingTimer: null,
  },

  onLoad() {
    // 从全局获取客户信息
    const info = app.globalData.customerInfo || wx.getStorageSync('customer_info');
    this.customerId = (info && (info.id || info.customerId)) || null;

    // 检查是否有未完成的对话（从本地缓存恢复）
    this.restoreConversation();
  },

  onUnload() {
    // 页面离开时停止轮询
    this.stopPolling();
    // 保存对话状态到本地
    this.saveConversation();
  },

  /* ── 对话持久化 ── */

  saveConversation() {
    if (this.data.conversationId) {
      wx.setStorageSync('cs_conversation', {
        id: this.data.conversationId,
        status: this.data.conversationStatus,
        timestamp: Date.now(),
      });
    }
  },

  async restoreConversation() {
    const saved = wx.getStorageSync('cs_conversation');
    if (!saved || !saved.id) return;

    // 超过30分钟的旧对话不恢复
    if (Date.now() - (saved.timestamp || 0) > 30 * 60 * 1000) {
      wx.removeStorageSync('cs_conversation');
      return;
    }

    try {
      const msgs = await get(`/ai/customer_service/conversations/${saved.id}/messages`);
      if (msgs && msgs.length > 0) {
        const messages = msgs.map((m) => ({
          role: m.role,
          content: m.content,
          staffReply: m.staffReply || false,
        }));

        const status = saved.status === 'handoff' || saved.status === 'human' ? 'human' : 'ai';
        this.setData({
          messages,
          conversationId: saved.id,
          conversationStatus: status,
        });

        // 如果已经是人工状态，启动轮询
        if (status === 'human') {
          this.startPolling();
        }

        this.scrollToBottom();
      }
    } catch (err) {
      // 恢复失败，忽略
    }
  },

  /* ── 输入处理 ── */

  onInputChange(e) {
    const value = e.detail.value;
    this.setData({
      inputMessage: value,
      hasInput: value.trim().length > 0,
      showQuickQuestions: value.trim().length === 0 && this.data.messages.length === 0,
    });
  },

  sendQuickQuestion(e) {
    const question = e.currentTarget.dataset.question;
    this.setData({ inputMessage: question, hasInput: true, showQuickQuestions: false });
    this.sendMessage();
  },

  /* ── 发送消息 ── */

  async sendMessage() {
    const content = this.data.inputMessage.trim();
    if (!content || this.data.loading) return;

    // 清空输入
    this.setData({
      inputMessage: '',
      hasInput: false,
      showQuickQuestions: false,
    });

    // 添加用户消息到界面
    const userMessage = { role: 'user', content };
    const nextMessages = [...this.data.messages, userMessage];
    this.setData({
      messages: nextMessages,
      loading: true,
      scrollToId: `msg-${nextMessages.length - 1}`,
    });

    try {
      const token = app.globalData.token || wx.getStorageSync('mp_token');
      if (!token) {
        this.setData({ loading: false });
        wx.navigateTo({ url: '/pages/login/login' });
        return;
      }

      const response = await this._doRequest(content, token);
      await this._processResponse(response, nextMessages);
    } catch (err) {
      wx.showToast({ title: '网络异常', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  _doRequest(content, token) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${app.globalData.baseUrl}/ai/app/customer-service/chat`,
        method: 'POST',
        data: {
          conversationId: this.data.conversationId,
          customerId: this.customerId,
          message: content,
        },
        header: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
          Accept: 'text/event-stream',
        },
        responseType: 'text',
        success: resolve,
        fail: reject,
      });
    });
  },

  async _processResponse(response, currentMessages) {
    let responseText = '';
    if (response.data instanceof ArrayBuffer) {
      responseText = new TextDecoder('utf-8').decode(response.data);
    } else {
      responseText = String(response.data || '');
    }

    let reply = '';
    let conversationId = this.data.conversationId;
    let handoff = false;

    const lines = responseText.replace(/\r\n/g, '\n').split('\n');
    for (const line of lines) {
      const current = line.trim();
      if (!current.startsWith('data: ')) continue;

      const payload = current.slice(6);
      if (payload === '[DONE]') continue;

      try {
        const parsed = JSON.parse(payload);

        // 新会话 ID
        if (parsed.conv_id) {
          conversationId = parsed.conv_id;
        }
        // 转人工
        else if (parsed.handoff) {
          handoff = true;
          reply = parsed.message || '正在为您转接人工客服，请稍候...';
        }
        // 普通内容
        else if (parsed.content) {
          reply += parsed.content;
        }
        // 错误
        else if (parsed.error) {
          reply = '抱歉，我暂时无法回答这个问题。输入"转人工"可联系人工客服。';
        }
      } catch (err) {
        // 非 JSON 数据行（纯文本）
        reply += payload;
      }
    }

    if (!reply) {
      reply = '抱歉，暂时无法处理您的请求，请稍后再试。';
    }

    // 更新对话状态
    const newStatus = handoff ? 'human' : 'ai';
    const assistantMessage = { role: 'assistant', content: reply, handoff };
    const messages = [...currentMessages, assistantMessage];

    this.setData({
      messages,
      conversationId,
      conversationStatus: newStatus,
      loading: false,
      scrollToId: `msg-${messages.length - 1}`,
    });

    // 保存对话状态
    this.saveConversation();

    // 如果转人工，启动轮询接收客服回复
    if (handoff) {
      this.startPolling();
    }
  },

  /* ── 轮询接收人工客服回复 ── */

  startPolling() {
    this.stopPolling();

    const timer = setInterval(async () => {
      if (!this.data.conversationId) return;

      try {
        const msgs = await get(`/ai/customer_service/conversations/${this.data.conversationId}/messages`);
        if (!msgs || msgs.length === 0) return;

        // 只取比当前消息列表更新的 staff_reply 消息
        const currentLen = this.data.messages.length;
        const newMsgs = msgs.slice(currentLen).filter((m) => m.role === 'assistant' && m.staffReply);

        if (newMsgs.length === 0) return;

        const newItems = newMsgs.map((m) => ({
          role: 'assistant',
          content: `[人工客服] ${m.content}`,
          staffReply: true,
        }));

        this.setData({
          messages: [...this.data.messages, ...newItems],
          scrollToId: `msg-${this.data.messages.length + newItems.length - 1}`,
        });
      } catch (err) {
        // 轮询失败，静默
      }
    }, 3000); // 每 3 秒轮询一次

    this.data.pollingTimer = timer;
  },

  stopPolling() {
    if (this.data.pollingTimer) {
      clearInterval(this.data.pollingTimer);
      this.data.pollingTimer = null;
    }
  },

  scrollToBottom() {
    const msgs = this.data.messages;
    if (msgs.length > 0) {
      this.setData({ scrollToId: `msg-${msgs.length - 1}` });
    }
  },
});
