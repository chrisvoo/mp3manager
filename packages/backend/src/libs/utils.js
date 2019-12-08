const util = require('util');
const path = require('path');
const os = require('os');
const fs = require('fs');
const crypto = require('crypto');
const _ = require('underscore');
const cp = require('child_process');

const execFile = util.promisify(cp.execFile);

/**
 * Executes an external program passing some arguments to it
 * @param {String} cmd command's name
 * @param {Array|String} args arguments to be passed to the command
 * @param {Object} options execFile's options
 * @returns {Promise<Object>} it returns an object with stdout, stderr and error keys
 * @see https://nodejs.org/docs/latest-v10.x/api/child_process.html#child_process_child_process_execfile_file_args_options_callback
 */
async function execute(cmd, args, options = {}) {
    let finalArgs;

    if (_.isArray(args)) {
        finalArgs = args;
    } else if (_.isString(args)) {
        finalArgs = args.split(/\s/);
    }

    return execFile(cmd, finalArgs, options);
}

/**
 * It prints a string representation of an object that is intended for debugging
 * @param {Object} obj Any JavaScript primitive or Object
 * @see https://nodejs.org/dist/latest-v10.x/docs/api/util.html#util_util_inspect_object_options
 */
function inspect(obj) {
    console.log(util.inspect(obj, {
        showHidden: true, // includes the object's non-enumerable symbols and properties
        depth: null, // make it recurse up to the maximum call stack size
        colors: true, // output styled with ANSI color codes
    }));
}

/**
 * Escapes all quotes chars and wrap the path between quotes. This is useful only
 * when using child_process' functions to pass paths as argumentsl
 * @param {string} path the file's path
 */
function safePath(filePath) {
    if (_.isEmpty(filePath)) {
        return '';
    }

    return `"${filePath.replace(/("+)/g, '\\$1')}"`;
}

/**
 * Just blocks the code's execution through a `setTimeout` call.
 * @param {Promise<Number>} ms The number of milliseconds to block the execution.
 */
function sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * It creates a random temporary directory in the predefined OS' TEMP_DIR_PATH.
 * @example: /tmp/foo-itXde2 or C:\Users\...\AppData\Local\Temp\foo-itXde2
 * @returns {Promise<string>} The path of the temp dir created.
 */
function createTempDir() {
    return new Promise((resolve, reject) => {
        // eslint-disable-next-line consistent-return
        fs.mkdtemp(path.join(os.tmpdir(), 'foo-'), (err, folder) => {
            if (err) {
                return reject(err);
            }

            fs.mkdir(folder, { recursive: true }, (err) => {
                if (err) {
                    return reject(err);
                }
                return resolve(folder);
            });
        });
    });
}
  

/**
 * It calculates the SHA1 (by default) of the music file, ignoring the metadata
 * @param {string} also The hashing algorithm (SHA1 by default)
 * @param {Buffer} data Binary data of the MP3 file
 * @returns {string} The file's hash
 */
async function getHash(algo = 'sha1', data) {
    const hash = crypto.createHash(algo);
    hash.update(data);
    return hash.digest('hex');
}

module.exports = {
    inspect, execute, getHash, safePath, sleep,
    createTempDir,
};
