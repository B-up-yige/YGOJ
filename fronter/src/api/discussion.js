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
