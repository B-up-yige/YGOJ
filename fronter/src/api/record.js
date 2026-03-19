import request from '@/utils/request'

// 获取提交记录信息
export function getRecordInfo(id) {
  return request({
    url: `/record/recordinfo/${id}`,
    method: 'get'
  })
}

// 添加提交记录
export function addRecord(data) {
  return request({
    url: '/record/add',
    method: 'post',
    data
  })
}

// 编辑提交记录状态
export function editRecordStatus(id, status) {
  return request({
    url: '/record/editStatus',
    method: 'put',
    params: { id, status }
  })
}
