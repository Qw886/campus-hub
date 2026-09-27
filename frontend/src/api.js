const API_PREFIX = '/api'

export async function request(path, options = {}) {
  const token = localStorage.getItem('campus_token')
  const headers = { ...(options.headers || {}) }
  if (options.body !== undefined && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json'
  }
  if (token) headers.Authorization = `Bearer ${token}`

  const response = await fetch(`${API_PREFIX}${path}`, { ...options, headers })
  const body = await response.json().catch(() => ({}))
  if (!response.ok || body.code !== 200) {
    const error = new Error(body.message || `请求失败（${response.status}）`)
    error.code = body.code || response.status
    throw error
  }
  return body.data
}

export const get = (path) => request(path)
export const post = (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) })
export const put = (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) })
export const remove = (path) => request(path, { method: 'DELETE' })

export async function uploadImage(file) {
  const token = localStorage.getItem('campus_token')
  const form = new FormData()
  form.append('file', file)
  const response = await fetch(`${API_PREFIX}/uploads/images`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: form
  })
  const body = await response.json().catch(() => ({}))
  if (!response.ok || body.code !== 200) throw new Error(body.message || '图片上传失败')
  return body.data.url
}
