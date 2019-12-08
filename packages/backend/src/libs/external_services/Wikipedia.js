const request = require('request-promise-native');
const config = require('../../config/getConfig');

class Wikipedia {
    constructor(options) {
        this.options = options;
        this.PAGE_ENDPOINT = 'https://en.wikipedia.org/api/rest_v1';
        this.options = {
            headers: {
                'User-Agent': config.apiIntegration.userAgent,
                Accept: 'application/json',
            },
            json: true,
        };
    }

    _makePageRequest(endpoint) {
        this.options.uri = `${this.PAGE_ENDPOINT}${endpoint.replace(/\s+/, '_')}`;
        return request(this.options);
    }

    /**
     * Get latest HTML for a title.
     * @param {string} title page's title
     * @see https://en.wikipedia.org/api/rest_v1/#!/Page_content/get_page
     */
    getPageByTitle(title) {
        return this._makePageRequest(`/page/html/${title}`);
    }

    /**
     * Get a text extract & thumb summary of a page.
     * @param {string} title page's title
     * @see https://en.wikipedia.org/api/rest_v1/#!/Page_content/get_page_summary_title
     */
    getSummaryByTitle(title) {
        return this._makePageRequest(`/page/summary/${title}`);
    }
}

module.exports = Wikipedia;
