
import http from '@/common/services/HttpClient.js';

class User {

  async getUsers(filterOpts, pageOpts) {
    let params = Object.assign({}, filterOpts || {});
    params = Object.assign(params, pageOpts || {});
    return http.get('users', params);
  }

  async getUsersCount(filterOpts) {
    return http.get('users/count', filterOpts || {});
  }

  async bulkUpdate({detail, ids}) {
    if (!ids || ids.length == 0) {
      return [];
    }

    return http.put('users/bulk-update', {detail: detail, ids: ids});
  }

  async broadcast(announcement) {
    return http.post('users/announcements', announcement);
  }
}

export default new User();
