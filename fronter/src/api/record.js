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
