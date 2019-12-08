const mongoose = require('mongoose');
const config = require('../../../config/getConfig');

/**
 * It bootstraps mongoose connection
 */
async function bootstrapMongoose() {
    const { dns, mongooseOptions, mongooseDebug } = config.db.mongo;
    console.log('Trying to connect to ', dns);

    mongoose.set('useFindAndModify', false);
    mongoose.set('useUnifiedTopology', true);
    mongoose.set('useCreateIndex', true);
    mongoose.set('debug', mongooseDebug);
    mongoose.Promise = global.Promise;

    return new Promise((resolve, reject) => {
        mongoose.connect(dns, mongooseOptions, (err) => {
            if (err) {
                return reject(err);
            }
        
            console.log("Mongoose connection established")
            return resolve(true);
        });
    });
}

module.exports = bootstrapMongoose;
