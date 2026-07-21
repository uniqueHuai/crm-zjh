/**
 * SSE 流式消费工具 — 支持 POST 方式发送请求体
 */
export class SseClient {
  private abortController: AbortController | null = null

  async connect(
    url: string,
    body: Record<string, unknown>,
    options: {
      onMessage: (data: string) => void
      onDone?: () => void
      onError?: (err: Error) => void
      onConvId?: (id: string) => void
      token?: string
    },
  ) {
    this.abortController = new AbortController()

    try {
      const headers: Record<string, string> = { 'Content-Type': 'application/json' }
      if (options.token) {
        headers['Authorization'] = `Bearer ${options.token}`
      }

      const response = await fetch(url, {
        method: 'POST',
        headers,
        body: JSON.stringify(body),
        signal: this.abortController.signal,
      })

      if (!response.ok) {
        options.onError?.(new Error(`HTTP ${response.status}`))
        return
      }

      const reader = response.body?.getReader()
      if (!reader) {
        options.onError?.(new Error('No response body'))
        return
      }

      const decoder = new TextDecoder()
      let buffer = ''

      let lastEvent = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          const trimmed = line.trim()
          if (!trimmed) continue

          // Track event type
          if (trimmed.startsWith('event:')) {
            lastEvent = trimmed.slice(6).trim()
            continue
          }

          // Handle data: 开头的行
          const DATA_PREFIX = 'data:'
          if (trimmed.startsWith(DATA_PREFIX)) {
            const data = trimmed.slice(DATA_PREFIX.length).trim()

            if (data === '[DONE]') {
              options.onDone?.()
              return
            }
            if (!data) continue

            // event:error → onError callback
            if (lastEvent === 'error') {
              lastEvent = ''
              options.onError?.(new Error(data))
              return
            }

            try {
              const parsed = JSON.parse(data)
              if (parsed.conv_id) {
                options.onConvId?.(parsed.conv_id)
              } else {
                options.onMessage(data)
              }
            } catch {
              options.onMessage(data)
            }
            lastEvent = ''
          }
        }
      }
      options.onDone?.()
    } catch (err: unknown) {
      if (err instanceof Error && err.name !== 'AbortError') {
        options.onError?.(err)
      }
    }
  }

  abort() {
    this.abortController?.abort()
  }
}
