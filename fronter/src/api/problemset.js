import request from '@/utils/request'

// 获取题集列表
export function getProblemsetList(page = 1, pageSize = 10, title = '') {
  return request({
    url: '/problemset/list',
    method: 'get',
    params: { page, pageSize, title }
  })
}

// 获取题集详情
export function getProblemsetInfo(id, userId = null) {
  return request({
    url: `/problemset/${id}`,
    method: 'get',
    params: { userId }
  })
}

// 添加题集
export function addProblemset(data) {
  return request({
    url: '/problemset/add',
    method: 'post',
    data
  })
}

// 编辑题集
export function editProblemset(data, userId = null) {
  return request({
    url: '/problemset/edit',
    method: 'put',
    params: { userId },
    data
  })
}

// 删除题集
export function delProblemset(id, userId = null) {
  return request({
    url: `/problemset/del/${id}`,
    method: 'delete',
    params: { userId }
  })
}

// 添加题集题目
export function addProblemsetProblem(data, userId = null) {
  return request({
    url: '/problemset/problem/add',
    method: 'post',
    params: { userId },
    data
  })
}

// 删除题集题目
export function delProblemsetProblem(problemsetId, problemId, userId = null) {
  return request({
    url: '/problemset/problem/del',
    method: 'delete',
    params: { problemsetId, problemId, userId }
  })
}

// 获取题集的题目列表
export function getProblemsetProblems(id) {
  return request({
    url: `/problemset/${id}/problems`,
    method: 'get'
  })
}

// 获取用户在题集中的过题情况
export function getUserProblemsetProgress(userId, problemsetId) {
  return request({
    url: '/record/problemset-progress',
    method: 'get',
    params: { userId, problemsetId }
  })
}
