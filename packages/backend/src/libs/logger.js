const { createLogger, format, transports } = require('winston');
const path = require('path');
const config = require('../../src/config/getConfig');

const { combine, timestamp, json, colorize,
    prettyPrint, simple, errors,
} = format;
const { logging: { level } } = config;

const LOGS_DIR = path.join(`${__dirname}/../../logs`, process.env.NODE_ENV.toLowerCase());
const ERROR_LOG = `${LOGS_DIR}/error.log`;
const COMBINED_LOG = `${LOGS_DIR}/combined.log`;

// Default levels used: npm
/*
{ 
  error: 0, 
  warn: 1, 
  info: 2, 
  verbose: 3, 
  debug: 4, 
  silly: 5 
}
*/
const logger = createLogger({
    // https://github.com/winstonjs/winston#logging-levels
    level,
    // https://github.com/winstonjs/winston#formats
    format: combine(
        // https://github.com/taylorhakes/fecha format
        timestamp({ format: 'DD-MM-YY HH:mm:ss' }),
        prettyPrint({ depth: 5 }),
        json(),
        errors({ stack: true }),
    ),
    // https://github.com/winstonjs/winston#transports
    transports: [
        new transports.Console({
            level: 'warn',
            format: combine(
                colorize(),
                simple()
            ),
        }),
        new transports.File({
            filename: ERROR_LOG,
            level: 'error',
            handleExceptions: true,
            maxsize: 1024 * 1024 * 5,
            maxFiles: 3,
            zippedArchive: true,
            tailable: true,
        }),
        new transports.File({
            filename: COMBINED_LOG,
            maxsize: 1024 * 1024 * 5,
            maxFiles: 5,
            zippedArchive: true,
            tailable: true,
        }),
    ],
    // https://github.com/winstonjs/winston#to-exit-or-not-to-exit
    exitOnError: false,
});

module.exports = logger;
