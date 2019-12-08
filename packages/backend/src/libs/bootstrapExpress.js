const express = require('express');
const bodyParser = require('body-parser');
const { execute, subscribe } = require('graphql');
const { ApolloServer } = require('apollo-server-express');
const { SubscriptionServer } = require('subscriptions-transport-ws');
const cors = require('cors');
const http = require('http');
const config = require('../config/getConfig');
const schema = require('../models/graphql/schema');

/**
 * It creates the Express app
 * @returns {Promise<Express>}
 */
function bootstrapExpress() {
    return new Promise((resolve, reject) => {
        try {
            const app = express();
            app.use(cors({ credentials: true, origin: config.frontendURL }));
            app.use(bodyParser.json());
            app.use(bodyParser.urlencoded({ extended: false }));

            app.get('/streaming', (request, response) => {
                const queryData = url.parse(request.url, true).query;
        
                let skip = 0;
                
                if (queryData.skip) {
                    const parsed = parseInt(queryData.skip, 10);
                    if (_.isNumber(parsed)) {
                        skip = parsed;
                    }
                }
        
                logger.debug('Streamer', { file: queryData.file, skip });
        
                if (_.isEmpty(queryData.file)) {
                    response.writeHead(400, {
                        'Content-Type': 'application/json',
                    });
                    response.end(JSON.stringify({ error: 'Missing file parameter' }));
                } else {
                    const filePath = 'D:/Musica/Compilations/AC-DC/Highway To Hell.mp3';
                    const stat = fs.statSync(filePath);
                    const startByte = skip;
        
                    response.writeHead(200, {
                        'Content-Type': 'audio/mpeg',
                        'Content-Length': stat.size - startByte,
                    });
        
                    fs.createReadStream(filePath, { start: startByte }).pipe(response);
                }
            })

            const httpServer = http.createServer();

            httpServer.listen(config.frontend.webSocketPort, '0.0.0.0', () => new SubscriptionServer({
                execute,
                subscribe,
                schema,
                onConnect: async (connectionParams) => {
                    console.log(connectionParams);
                    return {};
                },
            }, {
                server: httpServer,
                path: '/subscriptions',
            }));

            const server = new ApolloServer({
                debug: true,
                schema,
                formatError: (err) => { console.error(err); return err; },
            });

            server.applyMiddleware({ app });
            app.listen({ port: config.backend.apiPort }, () => {
                console.info(`🚀 Server ready at http://localhost:${config.backend.apiPort}${server.graphqlPath}`);
                resolve(app);
            });
        } catch (error) {
            console.error(error);
            reject(error);
        }
    });
}

module.exports = bootstrapExpress;
