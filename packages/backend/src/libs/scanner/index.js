const path = require('path');
const _ = require('underscore');
const fs = require('graceful-fs');
const mm = require('music-metadata');
const readline = require('readline');

const { promisify } = require('util');
const MusicFile = require('./../../models/db/mongo/music_files');

const readdir = promisify(fs.readdir);

class MusicScanner {
    /**
     * @param {Object} options it may contain:
        - paths (Array): a list of URI to be scanned, every item could be a file or a directory
        - keepInMemory (Boolean): if to store the results in the DB or to keep them in memory,
            returning the results (just for tests, sort of dry run)
     */
    constructor(options) {
        if (_.isEmpty(options) || _.isEmpty(options.paths) || !_.isArray(options.paths)) {
            throw new Error('You must specify the `paths` property (array)');
        }

        const { paths, keepInMemory, printProgress } = options;

        this.paths = paths;
        this.mapPaths();

        this.keepInMemory = keepInMemory === true;
        this.printProgress = printProgress || false;
        this.processResult = {
            totFiles: 0,
            totBytes: 0,
            dirQueue: [],
        };
    }

    /**
     * It returns the directories' queue. Elements inside of it get removed following the LIFO
     * pattern.
     * @returns {Array}
     */
    getQueue() {
        return this.queue;
    }

    /**
     * It returns the paths to be scanned.
     * @returns {Array}
     */
    getPaths() {
        return this.paths;
    }

    /**
     * It checks that all items specified are absolute paths to directories and
     * filters out paths included by other specified paths.
     * @returns void.
     */
    mapPaths() {
        this.paths = this.paths
            .reduce((accumulator, currentValue) => {
                const stats = fs.statSync(path.normalize(currentValue));
                if (!stats.isDirectory()) {
                    throw new Error(`Path ${currentValue} is not a directory`);
                }
      
                if (!path.isAbsolute(currentValue)) {
                    throw new Error(`Passing a relative path: ${currentValue}`);
                }
          
                // this first check gets rid of duplicates
                if (!accumulator.includes(currentValue)) {
                    // filters paths which include current value and are shorter
                    const res = this.paths.filter((el) => currentValue.startsWith(el) && currentValue.length > el.length);
                    // if no path includes the current dir, we select it
                    if (!res.length) {
                        accumulator.push(currentValue);
                    }
                }
      
                return accumulator;
            }, []);
    }

    /**
     * It process a single file, extracting its metadata and storing them inside Mongo.
     * @param {string} resource Absolute path to an MP3 file.
     * @returns void
     */
    async processFile(resource) {
        if (path.extname(resource).toLowerCase() !== '.mp3') {
            return;
        }

        this.processResult.totFiles += 1;
        const metadata = await mm.parseFile(resource, { duration: true });
        const { format, common } = metadata;
        const fileSize = fs.statSync(resource).size;

        const fileInstance = {
            path: resource,
            fileSize,
            metadata: {
                format,
                common,
            },
        };

        if (!this.keepInMemory) {
            MusicFile.create(fileInstance);
        }
        
        this.processResult.totBytes += fileSize;
    }

    async processDirectory() {
        let filesPromises = [];

        while (this.processResult.dirQueue.length > 0) {
            const dir = this.processResult.dirQueue.shift();
            // eslint-disable-next-line no-await-in-loop
            const dirents = await readdir(dir, { withFileTypes: true });
            
            for (const dirent of dirents) {
                const resource = path.resolve(dir, dirent.name);
                if (dirent.isDirectory()) {
                    this.processResult.dirQueue.push(resource);
                } else if (dirent.isFile()) {
                    filesPromises.push(this.processFile(resource));
                }
            }

            if (filesPromises.length >= 100) {
                // eslint-disable-next-line no-await-in-loop
                await Promise.all(filesPromises);
                filesPromises = [];
            }
        }
    }

    /**
     * It prints on the same line some stats about the processed files.
     * @param {Number} totFiles Total files currently processed.
     * @returns void
     */
    logStats(totFiles) {
        readline.moveCursor(process.stdout, 0, -1);
        readline.clearLine(process.stdout, 0);
        console.log(`Scanned file count: ${totFiles}.`,
            `Heap total: ${parseInt(process.memoryUsage().heapTotal / 1024, 10)} KB, `,
            `used: ${parseInt(process.memoryUsage().heapUsed / 1024, 10)} KB`);
    }

    /**
     * Main function called to scan the paths passed as argument to the constructor.
     * @returns {Object} An object with the final stats (total files, total bytes and 
     * processing time).
     */
    async scan() {
        const promises = [];

        const start = Date.now();

        for (const thePath of this.paths) {
            this.processResult.dirQueue.push(thePath);
            promises.push(this.processDirectory());
        }

        if (this.printProgress) {
            this.interval = setInterval(() => this.logStats(this.processResult.totFiles), 5000);
        }
        
        await Promise.all(promises);
        const end = new Date(Date.now() - start);
        const humandate = `${end.getUTCHours()} hours, ${end.getUTCMinutes()} minutes and ${end.getUTCSeconds()} second(s)`;

        this.processResult.executionTime = humandate;
        if (this.printProgress) {
            clearInterval(this.interval);
        }

        return this.processResult;
    }
}

module.exports = MusicScanner;
