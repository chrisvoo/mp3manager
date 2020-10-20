/* Jest file configuration.
 * It keeps package.json cleaner and permits to load
 * ES6 setup/teardown scripts before/after the tests */
module.exports = {
    verbose: true,
    automock: false,
    testMatch: ['**/*.test.js'],
    testEnvironment: 'node',
};
