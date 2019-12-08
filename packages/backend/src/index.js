const bootstrapExpress = require('./libs/bootstrapExpress');
const bootstrapMongoose = require('./models/db/mongo');

console.time('boostrap');
Promise.all([
    bootstrapMongoose(),
    bootstrapExpress(),
]).then(() => console.timeEnd('boostrap'))
    .catch((e) => console.error(e));
