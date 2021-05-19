
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
}

export default new HttpClient();
