const fs = require('fs');
const path = require('path');
const os = require('os');
const _ = require('underscore');

/**
 * 
 * @param {Object} options it contains: 
 * - {String} filePath: file's absolute path (required)
 * - {Number} numCopies: how many copies of the specified file to be put in the temp folder
 */
function copyFile(options) {
    const { filePath, numCopies = 1 } = options;
    if (_.isEmpty(filePath)) {
        throw new Error('filePath is mandatory');
    }

    const folder = fs.mkdtempSync(path.join(os.tmpdir(), 'mm-'));
    const result = {
        mainFolder: folder,
        files: [],
    };

    for (let i = 0; i < numCopies; i++) {
        const fileName = `copy${i}.mp3`;
        result.files.push(fileName);
        fs.copyFileSync(filePath, `${folder}/${fileName}`);
        // the resulting MD5 should be different for every file
        fs.appendFileSync(`${folder}/${fileName}`, require('random-buffer')(3));
    }

    return result;
}

module.exports = {
    copyFile,
};