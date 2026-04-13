import request from '@/utils/request'

// 获取题集列表
export function getProblemsetList(page = 1, pageSize = 10) {
  return request({
    url: '/problemset/list',
    method: 'get',
    params: { page, pageSize }
  })
}

// 获取题集详情
export function getProblemsetInfo(id) {
  return request({
    url: `/problemset/${id}`,
    method: 'get'
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
export function editProblemset(data) {
  return request({
    url: '/problemset/edit',
    method: 'put',
    data
  })
}

// 删除题集
export function delProblemset(id) {
  return request({
    url: `/problemset/del/${id}`,
    method: 'delete'
  })
}

// 添加题集题目
export function addProblemsetProblem(data) {
  return request({
    url: '/problemset/problem/add',
    method: 'post',
    data
  })
}

// 删除题集题目
export function delProblemsetProblem(problemsetId, problemId) {
  return request({
    url: '/problemset/problem/del',
    method: 'delete',
    params: { problemsetId, problemId }
  })
}

// 获取题集的题目列表
export function getProblemsetProblems(id) {
  return request({
    url: `/problemset/${id}/problems`,
    method: 'get'
  })
}
