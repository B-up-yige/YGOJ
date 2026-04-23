import MarkdownIt from 'markdown-it'

// 创建 markdown-it 实例
const md = new MarkdownIt({
  html: true,        // 允许 HTML 标签
  linkify: true,     // 自动识别链接
  typographer: true, // 启用智能标点和其他替换
  breaks: true       // 将换行符转换为 <br>
})

// 自定义配置：为代码块添加默认语言类
const defaultRender = md.renderer.rules.fence || function(tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}

md.renderer.rules.fence = (tokens, idx, options, env, self) => {
  const token = tokens[idx]
  const info = token.info ? md.utils.unescapeAll(token.info).trim() : ''
  
  // 如果没有指定语言，添加一个默认的 language-plain 类
  if (!info) {
    token.info = 'plain'
  }
  
  return defaultRender(tokens, idx, options, env, self)
}

/**
 * 渲染 Markdown 内容为 HTML
 * @param {string} content - Markdown 内容
 * @returns {string} - 渲染后的 HTML
 */
export function renderMarkdown(content) {
  if (!content) return ''
  return md.render(content)
}

/**
 * 渲染行内 Markdown 内容为 HTML
 * @param {string} content - Markdown 内容
 * @returns {string} - 渲染后的 HTML
 */
export function renderMarkdownInline(content) {
  if (!content) return ''
  return md.renderInline(content)
}

export default md
