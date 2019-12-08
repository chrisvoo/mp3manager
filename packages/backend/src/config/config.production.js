module.exports = {
    backend: {
        apiPort: 3666, // graphql server
        streamingPort: 3667, // streaming server
    },
    frontend: {
        url: 'http://localhost',
        webSocketPort: 3668,
    },
    logging: {
        level: 'debug',
    },
    apiIntegration: { // shared properties between integrations
        userAgent: 'mp3manager/voodoo81people@gmail.com',
    },
    db: {
        mysql: {
            user: 'mp3admin',
            password: 'eR85/!sP$plO=',
            host: '127.0.0.1',
            database: 'mp3manager',
            connectionLimit: 50,
        },
        mongo: {
            dns: 'mongodb://localhost:27017/music_manager',
            mongooseDebug: false,
            mongooseOptions: {
                autoIndex: true,
                useNewUrlParser: true,
                useCreateIndex: true,
            },
        },
    },
    redis: {
        port: 6379,
        host: '127.0.0.1',
        family: 4,
        /* password: 'auth', */
        db: 0,
    },
};
