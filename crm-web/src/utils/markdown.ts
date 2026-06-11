const BASE_URL = '/api/v1'

/**
 * 简单的 Markdown 渲染 — 将 Markdown 文本转为 HTML
 * 按块级元素拆分，避免 <p> 嵌套块级标签导致浏览器解析异常
 */
export function renderMarkdown(text: string): string {
  if (!text) return ''

  // 1. 转义 HTML 特殊字符（全局防 XSS）
  let html = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 2. 代码块 (```) — 优先处理，保护不被后续规则破坏
  html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, (_, lang, code) => {
    const langClass = lang ? ` class="lang-${lang}"` : ''
    return `<pre><code${langClass}>${code.trim()}</code></pre>`
  })

  // 3. 行内样式：加粗、斜体、行内代码、链接
  html = html
    // 行内代码 (`)
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    // 加粗 (**text**)
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    // 斜体 (*text*)
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    // 链接 [text](url)
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, (_, t, u) => `<a href="${resolveUrl(u)}" target="_blank" rel="noopener noreferrer">${t}</a>`)
    // 自动识别链接
    .replace(/(^|\s)(https?:\/\/\S+)(?=[\s,;.!?]|$)/g, (_, p, u) => `${p}<a href="${u}" target="_blank" rel="noopener noreferrer">${u}</a>`)

  // 4. 按空行切分段落，逐块处理
  const blocks = html.split(/\n{2,}/).map(block => {
    block = block.trim()
    if (!block) return ''

    // 已经是块级标签的逐行转换
    const blockLevelTags = /^<(pre|ul|ol|h[1-3]|hr|li|table|blockquote)/
    if (blockLevelTags.test(block)) return block

    // 尝试按行处理块内元素
    const lines = block.split('\n')
    const out: string[] = []
    let inList = false

    for (const line of lines) {
      const trimmed = line.trim()

      // 水平线
      if (/^---$/.test(trimmed)) { out.push('<hr>'); continue }

      // 标题
      const hMatch = trimmed.match(/^(#{1,3})\s+(.+)$/)
      if (hMatch) {
        const level = hMatch[1].length
        out.push(`<h${level}>${hMatch[2]}</h${level}>`)
        continue
      }

      // 无序列表
      if (/^[-*+]\s+(.+)$/.test(trimmed)) {
        if (!inList) { out.push('<ul>'); inList = true }
        out.push(`<li>${trimmed.replace(/^[-*+]\s+/, '')}</li>`)
        continue
      }

      // 有序列表
      if (/^\d+\.\s+(.+)$/.test(trimmed)) {
        if (!inList) { out.push('<ol>'); inList = true }
        out.push(`<li>${trimmed.replace(/^\d+\.\s+/, '')}</li>`)
        continue
      }

      // 从列表中退出
      if (inList) { out.push(inList === 'ul' ? '</ul>' : '</ol>'); inList = false }

      // 普通段落行
      out.push(line)
    }

    // 关闭未闭合的列表
    if (inList) out.push('</ul>')

    const joined = out.join('\n')
    // 如果结果已经是块级标签，直接返回
    if (blockLevelTags.test(joined)) return joined
    // 否则包在 <p> 里，行内换行转 <br>
    return `<p>${joined.replace(/\n/g, '<br>')}</p>`
  }).filter(Boolean)

  return blocks.join('\n') || '<p></p>'
}

function resolveUrl(url: string): string {
  if (url.startsWith('/') && !url.startsWith('/api/') && !url.startsWith('http')) {
    return BASE_URL + url
  }
  return url
}
