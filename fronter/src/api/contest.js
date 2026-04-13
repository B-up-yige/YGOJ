import request from '@/utils/request'

// 获取比赛列表
export function getContestList(page = 1, pageSize = 10) {
  return request({
    url: '/contest/list',
    method: 'get',
    params: { page, pageSize }
  })
}

// 获取比赛详情
export function getContestInfo(id) {
  return request({
    url: `/contest/${id}`,
    method: 'get'
  })
}

// 添加比赛
export function addContest(data) {
  return request({
    url: '/contest/add',
    method: 'post',
    data
  })
}

// 编辑比赛
export function editContest(data) {
  return request({
    url: '/contest/edit',
    method: 'put',
    data
  })
}

// 删除比赛
export function delContest(id) {
  return request({
    url: `/contest/del/${id}`,
    method: 'delete'
  })
}

// 添加比赛题目
export function addContestProblem(data) {
  return request({
    url: '/contest/problem/add',
    method: 'post',
    data
  })
}

// 删除比赛题目
export function delContestProblem(contestId, problemId) {
  return request({
    url: '/contest/problem/del',
    method: 'delete',
    params: { contestId, problemId }
  })
}

// 获取比赛的题目列表
export function getContestProblems(id) {
  return request({
    url: `/contest/${id}/problems`,
    method: 'get'
  })
}
