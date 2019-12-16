const mongoose = require('mongoose');

/**
 * It bootstraps mongoose connection
 */
async function bootstrapMongoose() {
    const options = {
        useFindAndModify: true,
        useUnifiedTopology: true,
        useCreateIndex: true,
        useNewUrlParser: true,
    };

    mongoose.Promise = global.Promise;

    return new Promise((resolve, reject) => {
        mongoose.connect(process.env.MONGO_URI, options, (err) => {
            if (err) {
                return reject(err);
            }
        
            console.log('Mongoose connection established');
            return resolve(true);
        });
    });
}

module.exports = bootstrapMongoose;
