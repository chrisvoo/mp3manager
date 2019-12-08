
const path = require('path');

// eslint-disable-next-line
const config = require(path.join(__dirname, `./config.${process.env.NODE_ENV}.js`));
module.exports = config;
