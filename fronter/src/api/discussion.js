import request from '@/utils/request'

// 获取帖子列表
export function getPostList(params) {
  return request({
    url: '/discuss/posts',
    method: 'get',
    params // 包含 page, pageSize, problemId, category
  })
}

// 获取帖子详情
export function getPostById(id) {
  return request({
    url: `/discuss/post/${id}`,
    method: 'get'
  })
}

// 创建帖子
export function createPost(data) {
  return request({
    url: '/discuss/post/create',
    method: 'post',
    data
  })
}

// 更新帖子
export function updatePost(data) {
  return request({
    url: '/discuss/post/update',
    method: 'put',
    data
  })
}

// 删除帖子
export function deletePost(id) {
  return request({
    url: `/discuss/post/${id}`,
    method: 'delete'
  })
}

// 获取评论列表
export function getComments(postId) {
  return request({
    url: `/discuss/comments/${postId}`,
    method: 'get'
  })
}

// 创建评论
export function createComment(data) {
  return request({
    url: '/discuss/comment/create',
    method: 'post',
    data
  })
}

// 删除评论
export function deleteComment(id) {
  return request({
    url: `/discuss/comment/${id}`,
    method: 'delete'
  })
}

// 置顶/取消置顶帖子
export function togglePinPost(id, isPinned) {
  return request({
    url: `/discuss/post/${id}/pin`,
    method: 'put',
    data: isPinned
  })
}

// ==================== 板块管理 API ====================

// 获取启用的板块列表（公开）
export function getActiveCategories() {
  return request({
    url: '/discuss/category/list',
    method: 'get'
  })
}

// 获取所有板块列表（管理员）
export function getAllCategories() {
  return request({
    url: '/discuss/category/admin/list',
    method: 'get'
  })
}

// 创建板块（管理员）
export function createCategory(data) {
  return request({
    url: '/discuss/category/admin/create',
    method: 'post',
    data
  })
}

// 更新板块（管理员）
export function updateCategory(data) {
  return request({
    url: '/discuss/category/admin/update',
    method: 'put',
    data
  })
}

// 删除板块（管理员）
export function deleteCategory(id) {
  return request({
    url: `/discuss/category/admin/${id}`,
    method: 'delete'
  })
}
