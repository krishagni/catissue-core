
import http from '@/common/services/HttpClient.js';

class UserGroup {

  getUserGroups(filterOpts, pageOpts) {
    let params = Object.assign({}, filterOpts || {});
    params = Object.assign(params, pageOpts || {});
    return http.get('user-groups', params);
  }

}

export default new UserGroup();
