const fs = require('fs');
const MetaInfo = require('../../src/libs/scanner/mediainfo');
const EyeD3 = require('../../src/libs/eyeD3');

describe('ID3 Tags', () => {
    const resDir = `${process.cwd()}/packages/server/__tests__/resources`;
    const resFile = `${resDir}/Under The Ice (Scene edit).mp3`;

    it('Mediainfo', async () => {
        const metadata = await MetaInfo.getData(resFile);

        expect(metadata).not.toBeNull();
        expect(metadata.size).toBe(5235428);
        expect(metadata.duration).toBe(128.888);
        expect(metadata.bitrate.mode).toBe('CBR');
        expect(metadata.bitrate.value).toBe(320);
        expect(metadata.title).toBe('Under The Ice (Scene edit)');
        expect(metadata.album).toBe('Lives Of The Artists: Follow Me Down - Soundtrack');
        expect(metadata.album_performer).toBe('UNKLE');
        expect(metadata.performer).toBe('UNKLE');
        expect(metadata.recorded_date.toISOString().split('T')[0]).toBe('2010-01-01');
        expect(metadata.last_modified.toISOString()).toEqual('2019-03-25T11:25:56.000Z');
        expect(metadata.encoded_library).toBe('LAME3.99r');
        expect(metadata.image.mime).toBe('image/jpeg');
    });

    it('eyeD3 image extraction', async () => {
        const imgPath = await EyeD3.getCoverImage(resFile);
        expect(fs.existsSync(imgPath)).toBeTruthy();
        fs.unlinkSync(imgPath);
    });
});
