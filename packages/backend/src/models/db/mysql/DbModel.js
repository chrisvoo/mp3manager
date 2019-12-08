const _ = require('underscore');
const { getConnection, showPoolInfo } = require('../../../libs/pool');
const logger = require('../../../libs/logger');

class DbModel {
    /**
     * It mimics an abstract Java class design pattern, verifying that:
     * - this call is not instantiated
     * - the subclass implements the properties FIELDS and TABLE_NAME
     */
    static check() {
        const { TABLE_NAME, FIELDS } = this;

        if (TABLE_NAME === undefined) {
            throw new TypeError('You must implement the static property TABLE_NAME');
        }

        if (FIELDS === undefined) {
            throw new TypeError('You must implement the static property FIELDS');
        }
    }

    /**
     * It concatenates all the fields with a comma, useful for SELECT
     * @params {Object} it may have the following fields:
     * - alias: table's alias which prefixes the select fields
     * - filterOut: a list of fields not to be considered
     * @returns {String} a concatenation of the selected fields 
     */
    static getFields(options) {
        this.check();

        const { FIELDS } = this;
        let finalFields = FIELDS;

        if (_.isEmpty(options)) {
            return finalFields.join(',');
        }
        
        const { alias = '', filterOut = [] } = options;

        if (!_.isEmpty(filterOut)) {
            finalFields = finalFields.filter((f) => !filterOut.includes(f));
        }

        if (!_.isEmpty(alias)) {
            finalFields = finalFields.map((f) => `${alias}.${f}`);
        }

        return finalFields.join(',');
    }

    /**
     * It concatenates all the placeholders, whose names are equal to the fields.
     * @param {Object} options
     * ' {boolean} `forUpdate` if true, it uses the `=` 
     */
    static getPlaceholders(options = {}) {
        this.check();

        const { FIELDS } = this;
        const { forUpdate = false, filterOut = [] } = options;
        let finalFields = FIELDS;

        if (!_.isEmpty(filterOut)) {
            finalFields = finalFields.filter((f) => !filterOut.includes(f));
        }

        return finalFields.map((f) => (forUpdate ? `${f}=:${f}` : `:${f}`)).join(',');
    }

    /**
     * Usable just for the construct INSERT...ON DUPLICATE UPDATE.
     * @see https://mariadb.com/kb/en/library/values-value/
     */
    static getValuesExpressions() {
        this.check();

        const { FIELDS } = this;
        return FIELDS.map((f) => `${f}=VALUES(${f})`).join(',');
    }

    /**
     * @param {Connection} connection
     * @see https://github.com/MariaDB/mariadb-connector-nodejs/blob/master/documentation/promise-api.md#connection-api
     */
    static showWarnings(connection) {
        return connection
            .query('SHOW WARNINGS LIMIT 1')
            .then((rows) => {
                if (!_.isEmpty(rows)) {
                    const { Level, Code, Message } = rows[0];
                    logger.warn('DbModel', `Level: ${Level} [${Code}]: ${Message}`);
                }
            })
            .catch((err) => logger.error('DbModel', err));
    }

    /**
     * It retrieves records from a table
     * @param {Object} params can contains many fields:
     * `fields` as array or string, like `id, name` or `[id, name]`, default `*`
     * `conditions` must be a string (optional)
     * `pagination` an object with `limit` and `offset` (optional)
     * `sorting` has a `column` and `direction` fields (optional)
     * `namedPlaceholders`: placeholders for where conditions
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

        let sql = `SELECT ${chosenFields} FROM ${this.TABLE_NAME} `;
        if (!_.isEmpty(chosenConditions)) {
            sql += ` WHERE ${chosenConditions}`;
        }

        if (!_.isEmpty(chosenSorting)) {
            sql += chosenSorting;
        }

        if (!_.isEmpty(chosenLimit)) {
            sql += chosenLimit;
        }

        logger.debug('DbModel', { sql, namedPlaceholders });

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
        } catch (e) {
            logger.error('DbModel', {
                message: e.message,
                stack: e.stack,
                params,
                sql,
            });
            return null;
        } finally {
            logger.info('DbModel: ', showPoolInfo());

            if (connection) {
                logger.debug('Connection ended');
                await connection.end();
            } else {
                logger.warn('connection was null');
            }
        }
    }
}

module.exports = DbModel;
