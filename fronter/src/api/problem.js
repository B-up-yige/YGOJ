import request from '@/utils/request'

// 获取题目信息
export function getProblemInfo(id) {
  return request({
    url: `/problem/probleminfo/${id}`,
    method: 'get'
  })
}

// 添加题目
export function addProblem(data) {
  return request({
    url: '/problem/add',
    method: 'post',
    data
  })
}

// 编辑题目
export function editProblem(data) {
  return request({
    url: '/problem/edit',
    method: 'put',
    data
  })
}

// 删除题目
export function delProblem(id) {
  return request({
    url: `/problem/del/${id}`,
    method: 'delete'
  })
}
