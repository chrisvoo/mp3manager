const { MongoDataSource } = require('apollo-datasource-mongodb');
const mongoose = require('mongoose');
const fs = require('fs');
const util = require('util');
const metadata = require('node-id3');
const isEmpty = require('lodash.isempty');
const { base64_encode, base64_decode } = require('../../libs/utils/base64');
const logger = require('../../libs/utils/logger');

const unlink = util.promisify(fs.unlink);

/**
 * Datasource available from the context of the resolvers.
 */
class MusicFileDataSource extends MongoDataSource {
    /**
     * Deletes a file from the database and from the file system.
     * @param {string} cursor A base64 cursor
     * @returns {Promise<Boolean>}
     */
    async deleteFile(cursor) {
        const file = await this.getFile(cursor);
        const { filename } = file;
        await Promise.all([
            unlink(filename),
            this.model.findByIdAndDelete(file.id),
        ]);
        return true;
    }

    /**
     * Updates the info about a file in the database and also some basic metatags
     * @param {Object} a MusicFile instance
     * @returns {Promise<MusicFile>} The updated MusicFile
     */
    async updateFile(file) {
        const id = file.id || new mongoose.Types.ObjectId();
        const newFile = await this.model.findByIdAndUpdate({ id }, file, {
            new: true,
            upsert: true,
            runValidators: true,
            setDefaultsOnInsert: true,
        });

        const { filename, album_title, bitrate, artist, title, year } = newFile;
        const tags = {};

        
        if (!isEmpty(album_title)) {
            tags.album = album_title;
        }

        if (!isEmpty(artist)) {
            tags.artist = artist;
        }

        if (!isEmpty(bitrate)) {
            tags.bpm = bitrate;
        }

        if (!isEmpty(title)) {
            tags.title = title;
        }

        if (!isEmpty(year)) {
            tags.year = year;
        }

        metadata.update(tags, filename, (err) => { throw new Error(err.message); });
        return newFile;
    }

    /**
     * Returns a file by its ID converted in base64
     * @param {string} cursor ObjectId in base64, used for pagination
     * @returns {Promise<File>} A File object
     */
    getFile(cursor) {
        return this.model.findById(base64_decode(cursor));
    }

    /**
     * 
     * @param {Object} It may contain the following fields:
     * - first: it's the limit for the query
     * - after: it's a cursor
     * - sort: an object which specified which fields to use for sorting
     * - fileSearch: a string, which could also be a RegExp pattern without '/' as delimiters
     * @returns {Promise<FilesConnection>} A pagination object
     */
    async getFiles({ first, after, sort, fileSearch }) {
        const conditions = {};
        logger.info('getFiles requst', {
            metadata: {
                first,
                after,
                sort,
                fileSearch,
            },
        });

        if (!isEmpty(fileSearch)) {
            conditions.filename = new RegExp(`/${fileSearch}/`, 'gi');
        }

        if (!isEmpty(after)) {
            conditions._id = { $gt: base64_decode(after) };
        }

        const sortBy = {};
        if (!isEmpty(sort)) {
            sort.forEach((i) => {
                sortBy[i.field] = i.direction.toLowerCase();
            });
        }

        logger.info('getFiles conditions', {
            metadata: {
                conditions,
            },
        });

        const files = await this.model
            .find(conditions)
            .sort(sortBy)
            .limit(first + 1)
            .exec();

        logger.info('getFiles files found', {
            metadata: {
                found: files.length,
            },
        });

        const hasNextPage = files.length > first;
        const nodes = hasNextPage ? files.slice(0, -1) : files;

        const edges = nodes.map((node) => ({
            cursor: base64_encode(node._id.toString()),
            node,
        }));

        return {
            edges,
            pageInfo: {
                hasNextPage,
                lastCursor: files.length
                    ? base64_encode(files[files.length - 2]._id.toString())
                    : null,
            },
        };
    }
}

module.exports = MusicFileDataSource;
