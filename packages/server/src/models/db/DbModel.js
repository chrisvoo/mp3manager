const _ = require('underscore');

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
                    console.warn(`Level: ${Level} [${Code}]: ${Message}`);
                }
            })
            .catch((err) => console.error(err));
    }
}

module.exports = DbModel;
