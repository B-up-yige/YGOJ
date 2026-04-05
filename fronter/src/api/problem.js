import request from '@/utils/request'

// 获取题目列表
export function getProblemList(page = 1, pageSize = 10) {
  return request({
    url: '/problem/list',
    method: 'get',
    params: { page, pageSize }
  })
}

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

// 获取测试用例列表
export function getTestCaseList(problemId) {
  return request({
    url: '/problem/getTestCase',
    method: 'get',
    params: { problemId }
  })
}

// 添加测试用例
export function addTestCase(formData) {
  return request({
    url: '/problem/addTestCase',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除测试用例
export function delTestCase(data) {
  return request({
    url: '/problem/delTestCase',
    method: 'delete',
    data
  })
}

// 获取题目标签
export function getProblemTags(id) {
  return request({
    url: `/problem/probleminfo/${id}/tag`,
    method: 'get'
  })
}

// 添加题目标签
export function addProblemTag(id, tagName) {
  return request({
    url: `/problem/probleminfo/${id}/addTag`,
    method: 'post',
    data: tagName,
    headers: {
      'Content-Type': 'text/plain'
    }
  })
}

// 删除题目标签
export function delProblemTag(id, tagName) {
  return request({
    url: `/problem/probleminfo/${id}/delTag`,
    method: 'delete',
    data: tagName,
    headers: {
      'Content-Type': 'text/plain'
    }
  })
}
