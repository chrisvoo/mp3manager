// Same error levels used by NPM
const ERROR_LEVELS = {
    error: { name: 'error', code: 0 },
    warn: { name: 'warn', code: 1 },
    info: { name: 'info', code: 2 },
    verbose: { name: 'verbose', code: 3 },
    debug: { name: 'debug', code: 4 },
    silly: { name: 'silly', code: 5 },
};
  
const { createLogger, format, transports } = require('winston');
require('winston-mongodb');

const { combine, timestamp, json, colorize,
    prettyPrint, simple, errors,
} = format;

const finalMongoOptions = {
    db: process.env.MONGO_URI,
    level: process.env.LOGGING_LEVEL || ERROR_LEVELS.debug.name,
    name: 'mongodb',
    collection: process.env.MONGO_LOGGING_COLLECTION || 'logs',
    decolorize: true,
    tryReconnect: true,
    options: {
        useUnifiedTopology: true,
        useNewUrlParser: true,
    },
};

if (Number.isInteger(process.env.LOGS_EXPIRE_AFTER_DAYS)) {
    finalMongoOptions.expireAfterSeconds = process.env.LOGS_EXPIRE_AFTER_DAYS;
}

const logger = createLogger({
    // https://github.com/winstonjs/winston#logging-levels
    level: process.env.LOGGING_LEVEL,
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
            level: process.env.LOGGING_LEVEL || ERROR_LEVELS.debug.name,
            format: combine(
                colorize(),
                simple()
            ),
        }),
        new transports.MongoDB(finalMongoOptions),
    ],
    // https://github.com/winstonjs/winston#to-exit-or-not-to-exit
    exitOnError: false,
});

module.exports = logger;
