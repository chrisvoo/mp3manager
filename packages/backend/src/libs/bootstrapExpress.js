const express = require('express');
const isEmpty = require('lodash.isempty');
const { ApolloServer } = require('apollo-server-express');
const cors = require('cors');
const http = require('http');
const url = require('url');
const fs = require('fs');


const MusicFileDataSource = require('../models/db/MusicFileDataSource');
const MusicFile = require('../models/db/MusicFile');
const logger = require('./utils/logger');
const schema = require('../models/graphql/schema');

/**
 * It creates the Express app
 * @returns {Promise<Express>}
 */
function bootstrapExpress() {
    return new Promise((resolve, reject) => {
        try {
            const app = express();
            app.use(cors({ credentials: true, origin: process.env.FRONTEND_URL }));
            app.use(express.json());

            app.get('/streaming', (request, response) => {
                const queryData = url.parse(request.url, true).query;
        
                let skip = 0;
                
                if (queryData.skip) {
                    const parsed = parseInt(queryData.skip, 10);
                    if (!Number.isNaN(parsed)) {
                        skip = parsed;
                    }
                }
        
                logger.debug('Streamer', {
                    metadata: {
                        file: queryData.file, skip,
                    },
                });
        
                if (isEmpty(queryData.file)) {
                    response.writeHead(400, {
                        'Content-Type': 'application/json',
                    });
                    response.end(JSON.stringify({ error: 'Missing file parameter' }));
                } else {
                    // eslint-disable-next-line max-len
                    const filePath = '/home/ccastelli/Documents/GitHub/mp3manager/packages/backend/tests/resources/Under The Ice (Scene edit).mp3';
                    const stat = fs.statSync(filePath);
                    const startByte = skip;
        
                    response.writeHead(200, {
                        'Content-Type': 'audio/mpeg',
                        'Content-Length': stat.size - startByte,
                    });
        
                    fs.createReadStream(filePath, { start: startByte }).pipe(response);
                }
            });

            const httpServer = http.createServer(app);

            const server = new ApolloServer({
                debug: true,
                schema,
                dataSources: () => ({
                    files: new MusicFileDataSource(MusicFile),
                }),
                playground: { version: '1.7.25' },
            });

            server.applyMiddleware({ app });
            httpServer.listen({ port: process.env.SERVER_PORT }, () => {
                console.info(`🎸 Server ready at http://localhost:${process.env.SERVER_PORT}${server.graphqlPath}`);
                resolve(app);
            });
        } catch (error) {
            console.error(error);
            reject(error);
        }
    });
}

module.exports = bootstrapExpress;
