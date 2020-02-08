const util = require('util');
require('colors');
const childProcess = require('child_process');
const bootstrapMongoose = require('../src/models/db/mongo');
const MusicScanner = require('../src/libs/scanner');

const exec = util.promisify(childProcess.exec);

/**
 * Application used by the `mm` CLI. It just calls `run` inside of the
 * script, which automatically calls the proper method
 */
class App {
    constructor(options) {
        const { env, command, verbose, paths } = options;
        console.log(`Scanning ${paths.length} resources in ${process.env.NODE_ENV} mode`);
        this.env = env;
        this.paths = paths;
        this.command = command;
        this.verbose = verbose;
    }

    /**
     * Run one of the methods beloging to this class, using the command
     * parameter passed by `mm`
     * @returns {Promise} it returns the response of the corresponding command executed
     */
    run() {
        try {
            return this[`${this.command}`]();
        } catch (e) {
            console.error(`Run failed for ${this.command}: ${e.message}`);
            return false;
        }
    }

    /**
     * It runs a shell command returning stdout and stderr, if any
     * @param  {string} command one or more commands that can be run in a shell
     * @return {object}         stdout and stderr
     */
    static async runShellCommand(command) {
        let stdout;
        let stderr;
        try {
            ({ stdout, stderr } = await exec(command));
            return { stdout, stderr };
        } catch (e) {
            console.error('runShellCommand has failed', e);
            return { stdout: e.stdout, stderr: e.stderr };
        }
    }

    /**
     * Scans the directories passed as parameter
     */
    async scan() {
        await bootstrapMongoose();
        console.log('Connected to mongo...');

        const scanner = new MusicScanner({
            keepInMemory: this.dryRun,
            paths: this.paths,
        });

        return scanner.scan();
    }
}

module.exports = App;
