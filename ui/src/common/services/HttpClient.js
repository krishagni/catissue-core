
import axios from 'axios';

class HttpClient {
  protocol = '';

  host = '';

  port = '';

  path = '';

  headers = {};

  constructor() {
  }

  async get(url, params) {
    const res = await axios.get(
      this.getUrl(url),
      {
        headers: this.headers,
        params: params
      },
    );

    return res.data;
  }

  async post(url, params, data) {
    const res = await axios.post(
      this.getUrl(url),
      data,
      {
        headers: this.headers,
        params: params
      },
    );

    return res.data;
  }


  async put(url, params, data) {
    const res = await axios.put(
      this.getUrl(url),
      data,
      {
        headers: this.headers,
        params: params
      },
    );

    return res.data;
  }

  getUrl(url) {
    let result = '';
    if (this.host) {
      result = this.protocol ? (this.protocol + '://') : 'https://';
      result += this.host;
      if (this.port) {
        result += ':' + this.port;
      }
    }

    if (this.path) {
      result += this.path;
    }

    if (result && !result.endsWith('/')) {
      result += '/';
    }

    result += url;
    return result;
  }

  downloadFile(url) {
    let clickEvent;
    if (typeof Event == 'function') {
      clickEvent = new MouseEvent('click', { view: window, bubbles: true, cancelable: false });
    } else {
      clickEvent = document.createEvent('Event');
      clickEvent.initEvent('click', true, false);
    }

    let link = document.createElement('a');
    link.href = url;
    link.target = '_blank';
    link.dispatchEvent(clickEvent);
  }
}

export default new HttpClient();
