const fs = require('fs');
const _ = require('underscore');
const { promisify } = require('util');
const { getConnection } = require('../../libs/pool');
const { scanner: { batchSize } } = require('../../config/getConfig');
const { mp3hash } = require('../../libs/utils');
const FileMetadata = require('./FileMetadata');
const DbModel = require('./DbModel');

const getStats = promisify(fs.stat);

/**
 * It models the "file" table.
 */
class File extends DbModel {
    static get TABLE_NAME() {
        return 'file';
    }

    static get FIELDS() {
        return [
            'name', 'atime', 'mtime', 'size', 'path', 'md5_hash',
        ];
    }

    /**
     * It updates a record in file table by MD5
     * @param {Object} params file table fields
     * @returns {Promise<Object>} node.js mariadb's driver response
     */
    static async update(params) {
        if (!params || _.isEmpty(params.md5_hash)) {
            throw new Error('File.update: missing or null params (md5 required)');
        }

        const { md5_hash, atime, mtime, size, name, thePath } = params;
        const connection = await getConnection();
        try {
            const placeholders = this.getPlaceholders({
                filter_out: ['md5_hash'],
                forUpdate: true,
            });
            const queryResult = await connection.query(
                {
                    namedPlaceholders: true,
                    sql: `UPDATE ${File.TABLE_NAME}
                          SET ${placeholders}
                          WHERE md5_hash=:md5_hash`,
                },
                { name, atime, mtime, size, path: thePath, md5_hash },
            );

            if (queryResult.warningStatus !== 0) {
                await this.showWarnings(connection);
            }

            return queryResult;
        } finally {
            connection.end();
        }
    }

    /**
     * Inserts multiple files at once
     * @param {Array} files Array of objects which each of them represents a file
     * @returns {Number} the number of affected rows
     */
    static async batchInsert(files) {
        const connection = await getConnection();

        const batchSql = {
            namedPlaceholders: true,
            sql: `INSERT INTO ${File.TABLE_NAME}
                   (${this.getFields()})
                  VALUES (${this.getPlaceholders()})`,
        };
        let values = [];
        const promises = [];

        for (const file of files) {
            if (values.length === batchSize) {
                // no nested object so it results in a deep copy
                const sqlValues = Object.assign([], values);
                promises.push(connection.batch(batchSql, sqlValues));
                values = [];
            }

            const { name, path: thePath } = file;
            // eslint-disable-next-line
            const result = await Promise.all([
                mp3hash(thePath),
                getStats(thePath),
            ]);
            const [md5_hash, stat] = result;
            const { atime, mtime, size } = stat;

            values.push({
                name, atime, mtime, size, path: thePath, md5_hash,
            });
        }

        if (!_.isEmpty(values)) {
            promises.push(connection.batch(batchSql, values));
        }

        const res = await Promise.all(promises);
        return res.reduce((accumulator, currentValue) => accumulator + currentValue.affectedRows, 0);
    }

    /**
     * Inserts a new file, calculating the hashes
     * @param {Object} params fields to be saved inside the table
     * @returns {Promise<Object>} it returns the result of the insert operation and the
     * file's MD5 if everything goes fine, otherwise an object with the details of the failure
     */
    static async insert(params) {
        const { name, path: thePath } = params;

        if (_.isEmpty(name) || _.isEmpty(thePath)) {
            throw new Error("Missing name and/or file's path");
        }

        const result = await Promise.all([
            mp3hash(thePath),
            getStats(thePath),
        ]);
        const [md5_hash, stat] = result;
        const { atime, mtime, size } = stat;
        const connection = await getConnection();

        try {
            const queryResult = await connection.query(
                {
                    namedPlaceholders: true,
                    sql: `INSERT INTO ${File.TABLE_NAME}
                            (${this.getFields()})
                          VALUES (${this.getPlaceholders()})`,
                },
                { name, atime, mtime, size, path: thePath, md5_hash },
            );

            if (queryResult.warningStatus !== 0) {
                await this.showWarnings(connection);
            }

            queryResult.md5_hash = md5_hash;
            return queryResult;
        } catch (e) {
            const { code, message } = e;
            return {
                error: true,
                duplicated: e.code === 'ER_DUP_ENTRY',
                code,
                message,
            };
        } finally {
            connection.end();
        }
    }

    /**
     * Deletes a single file
     * @param {int} fileId integer identifier
     */
    static async delete(fileId) {
        const connection = await getConnection();

        try {
            const result = await connection.query(
                {
                    namedPlaceholders: true,
                    sql: `DELETE FROM ${File.TABLE_NAME} WHERE id = :id`,
                },
                { fileId },
            );

            if (result.warningStatus !== 0) {
                await this.showWarnings(connection);
            }

            return result;
        } finally {
            connection.end();
        }
    }

    /**
     * It gets all the info from the db about a music file performing a JOIN
     * with the metadata's table
     * @param {string} md5 file's MD5
     */
    static async getFileAndMetadata(md5) {
        const connection = await getConnection();

        // LEFT JOIN because if for whatever reason we have a record in file table but
        // we don't have any metadata for it, this still works
        const sql = `
            SELECT ${this.getFields({ alias: 'f' })}, ${FileMetadata.getFields({ alias: 'm' })}
            FROM ${File.TABLE_NAME} f
                LEFT JOIN ${FileMetadata.TABLE_NAME} m
                ON f.md5_hash = m.md5_hash
            WHERE f.md5_hash = :md5_hash
        `;

        try {
            const result = await connection.query(
                {
                    namedPlaceholders: true,
                    sql,
                },
                { md5_hash: md5 },
            );

            if (result.warningStatus !== 0) {
                await this.showWarnings(connection);
            }

            return result;
        } finally {
            connection.end();
        }
    }

    /**
     * It gets a file by its ID
     * @param {Number} id Shortcut for retrieving a file by its ID
     */
    static async getById(id) {
        const res = await this.get({
            where: 'id = :id',
            namedPlaceholders: { id },
        });

        if (!_.isEmpty(res)) {
            return res[0];
        }

        return [];
    }

    /**
     * It retrieves a music file
     * @param {Object} params can contains many fields:
     * `fields` as array or string, like `id, name` or `[id, name]`, default `*`
     * `conditions` must be a string (optional)
     * `pagination` an object with `limit` and `offset` (optional)
     * `sorting` has a `column` and `direction` fields (optional)
     */
    static async get(params) {
        let chosenFields = '*';
        let chosenConditions;
        let chosenLimit;
        let chosenSorting;

        const { fields, where, namedPlaceholders, pagination, sorting } = params;

        if (!_.isEmpty(fields)) {
            chosenFields = _.isArray(fields) ? fields.join(',') : fields;
        }

        if (!_.isEmpty(where) && _.isString(where)) {
            chosenConditions = where;
        }

        if (!_.isEmpty(sorting)) {
            const { column, direction } = sorting;

            if (!_.isEmpty(column)) {
                chosenSorting = column;
            }

            if (!_.isEmpty(direction) && !_.isEmpty(column)) {
                chosenSorting += ` ORDER BY ${direction}`;
            }
        }

        if (!_.isEmpty(pagination)) {
            const { limit, offset } = pagination;
            const theLimit = parseInt(limit, 10);
            const theOffset = parseInt(offset, 10);

            if (_.isNumber(theLimit) && theLimit >= 0) {
                chosenLimit = `LIMIT ${theLimit}`;
            }

            if (_.isNumber(theOffset)) {
                chosenLimit += `OFFSET ${theOffset}`;
            }
        }

        const connection = await getConnection();

        let sql = `SELECT ${chosenFields} FROM ${File.TABLE_NAME} `;
        if (!_.isEmpty(chosenConditions)) {
            sql += ` WHERE ${chosenConditions}`;
        }

        if (!_.isEmpty(chosenSorting)) {
            sql += chosenSorting;
        }

        if (!_.isEmpty(chosenLimit)) {
            sql += chosenLimit;
        }

        try {
            const result = await connection.query(
                {
                    namedPlaceholders: true,
                    sql,
                },
                namedPlaceholders,
            );

            if (result.warningStatus !== 0) {
                await this.showWarnings(connection);
            }

            return result;
        } finally {
            connection.end();
        }
    }
}

module.exports = File;
