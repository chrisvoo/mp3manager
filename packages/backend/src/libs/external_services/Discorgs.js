const { Client } = require('disconnect');
const config = require('../../config/getConfig');

class Discogs {
    constructor(options = {}) {
        this.useragent = options.useragent || config.apiIntegration.userAgent;
        let auth;

        if (options.hasOwnProperty('userToken')) {
            const { userToken } = options;
            auth = { userToken };
        } else if (
            options.hasOwnProperty('consumerKey') &&
            options.hasOwnProperty('consumerSecret')) {
            const { consumerKey, consumerSecret } = options;
            auth = { consumerKey, consumerSecret };
        } else {
            throw new Error('Authentication missing: https://www.discogs.com/developers/#page:authentication');
        }

        this.api = new Client(this.useragent, auth);
    }

    /**
     * The Release resource represents a particular physical or digital object released by one or more Artists.
     * @param {(number|string)} release - The Discogs release ID
     * @returns {Promise}
     */
    getRelease(release) {
        return this.api.database().getRelease(release);
    }

    /**
     * The Master resource represents a set of similar Releases. Masters (also known
     * as "master releases") have a "main release" which is often the chronologically
     * earliest.
     * @param {(number|string)} release - The Discogs release ID
     */
    getMasterRelease(release) {
        return this.api.database().getMasterRelease(release);
    }

    /**
     * Get an image
     * @param {string} url - The full image url
     */
    getImage(url) {
        return this.api.database().getImage(url);
    }

    /**
     * The Artist resource represents a person in the Discogs database who contributed to a
     * Release in some capacity.
     * @param {(number|string)} artist - The Discogs artist ID
     */
    getArtist(artist) {
        return this.api.database().getArtist(artist);
    }

    /**
     * Returns a list of Releases and Masters associated with the Artist.
     * @param {(number|string)} artist - The Discogs artist ID
     * @param {object} [params] - Optional pagination params
     * @see https://www.discogs.com/developers/#page:database,header:database-artist-releases
     */
    getArtistReleases(artist, params) {
        return this.api.database().getArtistReleases(artist, params);
    }

    /**
     * Queries the database
     * @param {(string|Object)} query
     * @param {Object} params
     * @see https://www.discogs.com/developers/#page:database,header:database-search
     */
    search(query, params) {
        return this.api.database().search(query, params);
    }
}

module.exports = Discogs;
