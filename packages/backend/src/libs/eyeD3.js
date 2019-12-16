const _ = require('underscore');

const { execute, createTempDir } = require('./utils');

class EyeD3 {
    /**
     * 
     * @param {string} stderr Stadard error output
     * @param {Error} error 
     */
    static isError(stderr, error) {
        const err = stderr || error;

        if (_.isEmpty(err)) {
            return false;
        }

        if (err.includes('command not found')) {
            let warnMessage = "It seems you don't have eyeD3 installed.\n";
            warnMessage += 'Use pip install eyeD3 python-magic-bin: ';
            warnMessage += 'https://eyed3.readthedocs.io/en/latest/installation.html';
            console.error(warnMessage);
            return true;
        }

        // generic error
        if (err.includes('error')) {
            console.error(err);
            return true;
        }

        return false;
    }

    /**
     * Global method to run eyeD3
     * @param {Array|string} args arguments to be passed to eyeD3
     * @returns {Promise<string>} null if an error happens, default stderr otherwise
     */
    static async run(args) {
        const { stderr, stdout, error } = await execute('eyeD3', args);
        
        const result = `${stderr}${stdout}`;

        if (this.isError(result, error)) {
            return null;
        }

        return result;
    }

    /**
     * Returns the eyeD3 version
     */
    static async version() {
        return this.run('--version').then((result) => result.trim());
    }

    /**
     * It strips away all the metadata from the file
     * @param {string} filePath absolute URL of the music file
     * @returns the output of eyeD3's execution (never considered)
     */
    static async removeAllTags(filePath) {
        if (_.isEmpty(filePath)) {
            throw new Error('EyeD3.removeAllTags: passed an empty file path');
        }

        return this.run(['--remove-all', filePath]);
    }

    /**
     * It strips away all the comments from the file
     * @param {string} filePath absolute URL of the music file
     * @returns the output of eyeD3's execution (never considered)
     */
    static async removeAllComments(filePath) {
        if (_.isEmpty(filePath)) {
            throw new Error('EyeD3.removeAllComments: passed an empty file path');
        }

        return this.run(['--remove-all-comments', filePath]);
    }

    /**
     * It returns the front cover image
     * @TODO must be deeply tested.
     * - Sometimes there's no file in the temp dir
     * - Modify regexp to ignore the case of the extension
     * - Does the regexp work also on Windows?
     * @returns {Promise<string>} The image's path for the music file.
     */
    static async getCoverImage(filePath) {
        const tempDir = await createTempDir();
        const result = await this.run([`--write-images=${tempDir}`, filePath]);

        if (_.isEmpty(result)) {
            console.log(`EyeD3 returned null for file: ${filePath}`);
            return null;
        }

        const res = result.match(/^Writing \/([A-z0-9-_+]+\/)*([A-z0-9]+\.(jpeg|jpg|gif|png|\(null\)))/gm);

        if (!_.isEmpty(res)) {
            // EyeD3 outputs 'Writing <PATH>...\n'
            return res[0].split(' ')[1];
        }

        console.log(`${tempDir} must be checked. File: ${filePath}`);
        return null;
    }

    /**
     * Used to mark the file as scanner. It may optionally remove
     * all other comments (default true)
     * @param {string} filePath file's absolute path
     */
    static async addComment(filePath, comment) {
        if (_.isEmpty(filePath)) {
            throw new Error('EyeD3.addComment: passed an empty file path');
        }

        // description and lang are unique among comments. If there's already
        // another comment with the same values, it gets overwritten.
        const finalComment = `${comment}:${Date.now()}:eng`;
        
        const args = [];

        if (process.env.REMOVE_ALL_COMMENTS) {
            args.push('--remove-all-comments');
        }

        args.push('--add-comment');
        args.push(finalComment);
        args.push(filePath);
        
        return this.run(args);
    }
}

module.exports = EyeD3;
