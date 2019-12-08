const DbModel = require('./DbModel');
const { getConnection, showPoolInfo } = require('../../../libs/pool');
const logger = require('../../../libs/logger');

/**
 * It models the "file" table.
 */
class FileDuplicated extends DbModel {
    static get TABLE_NAME() {
        return 'file_duplicated';
    }

    static get FIELDS() {
        return [
            'path', 'md5_hash',
        ];
    }

    /**
     * It verifies if a file is already marked as duplicated
     * @param {string} md5_hash file's MD5 that we're currently examinating
     * @param {string} thePath file's absolute path
     * @returns {Promise<Boolean>} true if exists, false otherwise
     */
    static async isDuplicated(md5_hash, thePath) {
        const connection = await getConnection();
        try {
            const queryResult = await connection.query(
                {
                    namedPlaceholders: true,
                    sql: `SELECT EXISTS(
                            SELECT 1 FROM ${FileDuplicated.TABLE_NAME}
                            WHERE path = :thePath AND
                                  md5_hash = :md5_hash
                          )`,
                },
                { thePath, md5_hash },
            );

            if (queryResult.warningStatus !== 0) {
                await this.showWarnings(connection);
            }

            return queryResult[0];
        } catch (e) {
            logger.error('FileDuplicated', {
                message: e.message,
                stack: e.stack,
                thePath,
            });
            return null;
        } finally {
            logger.info('FileDuplicated: ', showPoolInfo());

            if (connection) {
                await connection.end();
                logger.debug('Connection ended');
            } else {
                logger.warn('connection was null');
            }
        }
    }

    /**
     * It just inserts a file in the table.
     * @param {Object} params it must contains the file's path
     * and its MD5. This fields constitute also a unique key
     */
    static async insert(params) {
        const { md5_hash, path: thePath } = params;
        const connection = await getConnection();

        try {
            const queryResult = await connection.query(
                {
                    namedPlaceholders: true,
                    sql: `INSERT INTO ${FileDuplicated.TABLE_NAME}
                            (${this.getFields()})
                          VALUES (${this.getPlaceholders()})`,
                },
                { path: thePath, md5_hash },
            );

            if (queryResult.warningStatus !== 0) {
                await this.showWarnings(connection);
            }

            return queryResult;
        } catch (e) {
            logger.error('Fileduplicated', {
                message: e.message,
                stack: e.stack,
                thePath,
            });
            return null;
        } finally {
            logger.info('FileDuplicated: ', showPoolInfo());

            if (connection) {
                await connection.end();
                logger.debug('Connection ended');
            } else {
                logger.warn('connection was null');
            }
        }
    }

    /**
     * Deletes a single duplicated file
     * @param {int} fileId integer identifier
     */
    static async delete(fileId) {
        const connection = await getConnection();

        try {
            const result = connection.query(
                {
                    namedPlaceholders: true,
                    sql: `DELETE FROM ${FileDuplicated.TABLE_NAME} WHERE id = :id`,
                },
                { fileId },
            );

            if (result.warningStatus !== 0) {
                await this.showWarnings(connection);
            }

            return result;
        } catch (e) {
            logger.error('Fileduplicated', {
                message: e.message,
                stack: e.stack,
                fileId,
            });
            return null;
        } finally {
            logger.info('FileDuplicated: ', showPoolInfo());

            if (connection) {
                await connection.end();
                logger.debug('Connection ended');
            } else {
                logger.warn('connection was null');
            }
        }
    }
}

module.exports = FileDuplicated;
