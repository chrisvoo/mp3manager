const os = require('os');
const _ = require('underscore');
const MusicScanner = require('../../src/libs/scanner');
const logger = require('../../src/libs/logger');

const resDir = `${process.cwd()}/packages/server/__tests__/resources`;

describe('Music scanner functions', () => {
    afterAll((done) => {
        logger.close();
        setTimeout(() => { done(); }, 500);
    });

    it('can scan directories recursively', async () => {
        const scanner = new MusicScanner({
            paths: [resDir],
            keepInMemory: true,
        });

        const res = await scanner.scan();
        const { totFiles, totBytes, dirQueue, executionTime } = res;
        expect(totFiles).toBe(6);
        expect(totBytes).toBe(5902138);
        expect(dirQueue.length).toBe(0);
        expect(executionTime).toEqual('0 hours, 0 minutes and 0 second(s)');
    });

    it('can validate paths passed to the constructor', () => {
        expect(() => new MusicScanner({
            paths: [os.homedir(), __filename],
            keepInMemory: true,
        })).toThrowError(/is not a directory/);

        const RES_DIR = `${__dirname}/../../`;

        const paths = [
            os.homedir(),
            `${RES_DIR}/__tests__/resources/tree/level1.1`,
            RES_DIR,
        ];

        const scanner = new MusicScanner({ paths, keepInMemory: true });
        expect((_.difference(scanner.getPaths(), [RES_DIR, os.homedir()])).length === 0).toBeTruthy();
    });
});
