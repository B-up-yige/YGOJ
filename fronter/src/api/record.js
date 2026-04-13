import request from '@/utils/request'

// 获取记录列表
export function getRecordList(page = 1, pageSize = 10) {
  return request({
    url: '/record/list',
    method: 'get',
    params: { page, pageSize }
  })
}

// 获取提交记录信息
export function getRecordInfo(id) {
  return request({
    url: `/record/recordinfo/${id}`,
    method: 'get'
  })
}

// 提交代码
export function submitCode(data) {
  return request({
    url: '/record/submit',
    method: 'post',
    data
  })
}

// 获取提交记录的测试点详情
export function getRecordDetails(id) {
  return request({
    url: `/record/recordinfo/${id}/details`,
    method: 'get'
  })
}

// 获取用户统计数据
export function getUserStatistics(userId) {
  return request({
    url: `/record/statistics/${userId}`,
    method: 'get'
  })
}

// 获取用户学习曲线数据
export function getUserLearningCurve(userId, days = 30) {
  return request({
    url: `/record/learning-curve/${userId}`,
    method: 'get',
    params: { days }
  })
}

// 获取用户按标签统计的数据
export function getUserStatsByTag(userId) {
  return request({
    url: `/record/statistics/${userId}/by-tag`,
    method: 'get'
  })
}

// 获取比赛的提交记录列表
export function getContestRecordList(contestId, page = 1, pageSize = 10) {
  return request({
    url: '/record/list',
    method: 'get',
    params: { contestId, page, pageSize }
  })
}
