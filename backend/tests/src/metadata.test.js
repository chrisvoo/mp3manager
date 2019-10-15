const mm = require('music-metadata');
const path = require('path');

describe('ID3 Tags', () => {
  const resDir = path.join(__dirname, '..', 'resources');
  const resFile = path.join(resDir, 'Under The Ice (Scene edit).mp3');

  it('Mediainfo', async () => {
    const metadata = await mm.parseFile(resFile);

    expect(metadata).not.toBeNull();
    // expect(metadata.size).toBe(5235428);
    expect(metadata.format.duration).toBeCloseTo(128.888);
    expect(metadata.format.container).toBe('MPEG');
    expect(metadata.format.codec).toBe('MP3');
    expect(metadata.format.codecProfile).toBe('CBR');
    expect(metadata.format.bitrate).toBe(320000);
    expect(metadata.common.title).toBe('Under The Ice (Scene edit)');
    expect(metadata.common.album).toBe('Lives Of The Artists: Follow Me Down - Soundtrack');
    expect(metadata.common.artist).toBe('UNKLE');
    expect(metadata.common.date).toBe('2010');
    expect(metadata.format.tool).toBe('LAME3.99r');
  });

  it('eyeD3 image extraction', async () => {
    const metadata = await mm.parseFile(resFile);
    expect(metadata.common.picture).toBeDefined();
    expect(metadata.common.picture[0].format).toBe('image/jpeg');
  });
});
