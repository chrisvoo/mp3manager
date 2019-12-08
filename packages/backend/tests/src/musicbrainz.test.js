// const util = require('util');
const MBClient = require('../../src/libs/external_services/MusicBrainz');

describe('MusicBrainz tests', () => {
    it('can search by artist', async () => {
        const client = new MBClient();
        const resp = await client.search('artist', { artist: 'pink floyd' });

        expect(resp.artists.length).toBe(resp.count);

        const group = resp.artists.filter((g) => g.score === 100)[0];
        expect(group.id).toEqual('83d91898-7763-47d7-b03b-b92132375c47');
        expect(group.type).toEqual('Group');
        expect(group['type-id']).toEqual('e431f5f6-b5d2-343d-8b36-72607fffb74b');
        expect(group.name).toEqual('Pink Floyd');
        expect(group['sort-name']).toEqual('Pink Floyd');
        expect(group.country).toEqual('GB');
        expect(group.area.id).toEqual('8a754a16-0027-3a29-b6d7-2b40ea0481ed');
        expect(group.area.name).toEqual('United Kingdom');
        expect(group.area['sort-name']).toEqual('United Kingdom');
        expect(group['begin-area'].name).toEqual('London');
        expect(group['begin-area'].type).toEqual('City');

        const { begin, end, ended } = group['life-span'];
        expect(begin).toEqual('1965');
        expect(end).toEqual('2014');
        expect(ended).toBeTruthy();

        const mostRelevantTags = group.tags.sort((a, b) => b.count - a.count).slice(0, 3);
        mostRelevantTags.includes({ count: 17, name: 'progressive rock' });
        mostRelevantTags.includes({ count: 13, name: 'rock' });
        mostRelevantTags.includes({ count: 13, name: 'psychedelic rock' });
        // console.log(util.inspect(group, { showHidden: false, depth: null }));
    });

    it('can search by release', async () => {
        const client = new MBClient();
        // normal search
        const resp = await client.search('release', { arid: '83d91898-7763-47d7-b03b-b92132375c47' });
        expect(resp.count >= 1436).toBeTruthy();
        expect(resp.releases.length).toBe(25);

        // console.log(util.inspect(resp, { showHidden: false, depth: null }));
    });

    it('can search by release-group', async () => {
        const client = new MBClient();
        // lucene search, I want all Pink Floyd's albums, whose types are not live, compilation, etc
        const resp = await client.search('release-group', {
            query: `
                arid:"83d91898-7763-47d7-b03b-b92132375c47" AND
                primarytype:"album" AND
                status:"official" AND
                -secondarytype:(
                  "compilation" OR
                  "live" OR
                  "remix" OR
                  "soundtrack" OR
                  "demo")
            `,
        });

        // console.log(util.inspect(resp, { showHidden: false, depth: null }));
    });
});
