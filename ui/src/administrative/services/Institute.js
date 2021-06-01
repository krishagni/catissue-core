
import http from '@/common/services/HttpClient.js';

class Institute {

  getInstitutes(filterOpts, pageOpts) {
    let params = Object.assign({}, filterOpts || {});
    params = Object.assign(params, pageOpts || {});
    return http.get('institutes', params);
  }

}

export default new Institute();
