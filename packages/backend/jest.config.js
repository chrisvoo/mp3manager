/* Jest file configuration.
 * It keeps package.json cleaner and permits to load
 * ES6 setup/teardown scripts before/after the tests */
const { join } = require('path');

const ROOT = `${process.cwd()}`;
const SERVER_ENV = join(ROOT, '/packages/server/__tests__');

module.exports = {
    verbose: true,
    automock: false,
    testMatch: [
        join(SERVER_ENV, '/src/**/*.test.js'),
    ],
    testEnvironment: 'node',
    // globalSetup: join(ROOT, 'scripts/jest/setup.js'),
    // globalTeardown: join(ROOT, 'scripts/jest/teardown.js'),
};
