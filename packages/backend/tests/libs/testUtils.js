const fs = require('fs');
const path = require('path');
const os = require('os');
const _ = require('underscore');

/**
 * It copies an MP3 in a temporary directory with a random name, appending
 * a random buffer so that its MD5 it's unique
 * @param {Object} options it contains: 
 * - {String} filePath: file's absolute path (required)
 * - {Number} numCopies: how many copies of the specified file to be put in the temp folder
 * @returns {Object} whose fields are:
 * - mainFolder: the temporary folder created for the test
 * - files: an array of files names
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

/**
 * Moves a file to another temp directory
 * @param {String} filePath
 * @returns {String} the new path 
 */
function moveFile(filePath) {
    if (_.isEmpty(filePath)) {
        throw new Error('moveFile: filePath missing');
    }

    const folder = fs.mkdtempSync(path.join(os.tmpdir(), 'mm-'));
    const newPath = `${folder}/copyfile.mp3`;
    fs.copyFileSync(filePath, newPath);
    return newPath;
}

module.exports = {
    copyFile,
    moveFile,
};
