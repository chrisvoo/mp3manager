const { MongoDataSource } = require('apollo-datasource-mongodb');
const isEmpty = require('lodash.isempty');
const { base64_encode, base64_decode } = require('../../libs/utils/base64');
const logger = require('../../libs/utils/logger');

/**
 * Datasource available from the context of the resolvers.
 */
class MusicFilesDataSource extends MongoDataSource {
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

module.exports = MusicFilesDataSource;
