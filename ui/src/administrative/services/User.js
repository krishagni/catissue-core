
import http from '@/common/services/HttpClient.js';

class User {

  getUsers(filterOpts, pageOpts) {
    let params = Object.assign({}, filterOpts || {});
    params = Object.assign(params, pageOpts || {});
    return http.get('users', params);
  }

}

export default new User();
