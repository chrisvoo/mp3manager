const MB = require('nodebrainz');
const config = require('../../config/getConfig');

/* https://musicbrainz.org/doc/Terminology
 - Artist: An artist is generally a musician (or musician persona), group of musicians, or other
           music professional (like a producer or engineer). https://musicbrainz.org/doc/Artist
 - Artist Credits: Artist credits indicate who is the main credited artist (or artists) for releases,
          release groups, tracks and recordings, and how they are credited. https://musicbrainz.org/doc/Artist_Credits
 - Release Group: used to group several different releases into a single logical entity. Every
          release belongs to one, and only one release group. https://musicbrainz.org/doc/Release_Group
 - Medium: physical, separate things you would get when you buy something in a record store.
          They are the individual CDs, vinyls, etc. https://musicbrainz.org/doc/Medium
 - Release / Date: The release date is the date in which a release was made available through some
          sort of distribution mechanism. https://musicbrainz.org/doc/Release/Date
 - Recording: an entity which can be linked to tracks on releases. Each track must always be
          associated with a single recording, but a recording can be linked to any number
          of tracks. https://musicbrainz.org/doc/Recording
 - Track: is the way a recording is represented on a particular release https://musicbrainz.org/doc/Track

 Rate limiting: 300 requests/sec (on average), and decline (http 503) the rest.
 */

class MBClient {
    constructor(options = {}) {
        this.useragent = options.useragent || config.apiIntegration.userAgent;
        this.retryOn = options.retryOn || true;
        this.retryDelay = options.retryDelay || 3000;
        this.retryCount = options.retryCount || 3;
        this.optionsInit = {
            userAgent: this.useragent,
            retryOn: this.retryOn,
            retryDelay: this.retryDelay,
            retryCount: this.retryCount,
        };
        this.client = new MB(this.optionsInit);
    }

    search(entity, options) {
        return new Promise((resolve, reject) => {
            // eslint-disable-next-line
            // https://lucene.apache.org/core/4_3_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package_description
            const method = options.hasOwnProperty('query') ? 'luceneSearch' : 'search';

            this.client[method](entity, options, (err, response) => {
                if (err) {
                    return reject(err);
                }

                return resolve(response);
            });
        });
    }
}

module.exports = MBClient;

