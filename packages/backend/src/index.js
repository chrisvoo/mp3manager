const dotenv = require('dotenv');
const dotenvExpand = require('dotenv-expand');

const myEnv = dotenv.config();
dotenvExpand(myEnv);

const bootstrapExpress = require('./libs/bootstrapExpress');
const bootstrapMongoose = require('./models/db');

console.time('boostrap');
Promise.all([
    bootstrapMongoose(),
    bootstrapExpress(),
]).then(() => console.timeEnd('boostrap'))
    .catch((e) => console.error(e));
