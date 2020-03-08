/* eslint-disable no-param-reassign */
const _ = require('underscore');

const { execute } = require('../utils');

/**
 * Wrapper for `mediainfo` CLI app.
 * @see https://mediaarea.net/en/MediaInfo
 */
class MediaInfo {
    /**
     * It populates the object passed as argument with the generic info
     * @param {object} element The object part of `track` entries
     * @param {object} finalData The final object that will be returned by #getData
     * @returns void
     */
    static collectGeneralData(element, finalData) {
        for (const [key, value] of Object.entries(element)) {
            // eslint-disable-next-line default-case
            switch (key) {
                case 'FileSize':
                    finalData.size = parseInt(value, 10);
                    break;
                case 'Duration':
                    finalData.duration = parseFloat(value);
                    break;
                case 'OverallBitRate_Mode':
                    if (!finalData.hasOwnProperty('bitrate')) {
                        finalData.bitrate = {};
                    }
                    finalData.bitrate.mode = value;
                    break;
                case 'OverallBitRate':
                    if (!finalData.hasOwnProperty('bitrate')) {
                        finalData.bitrate = {};
                    }
                    finalData.bitrate.value = parseInt(parseInt(value, 10) / 1000, 10);
                    break;
                case 'Title':
                case 'Album':
                case 'Album_Performer':
                case 'Performer':
                case 'ISRC':  
                case 'Publisher':  
                case 'Genre': 
                case 'Encoded_Library':
                case 'Lyricist':  
                case 'extra':  
                    finalData[key.toLowerCase()] = value;
                    break;
                case 'Original_Released_Date':
                case 'Recorded_Date':
                    finalData[key.toLowerCase()] = new Date(value);
                    break;
                case 'File_Modified_Date_Local':
                    finalData.last_modified = new Date(value);
                    break;
                case 'Cover':
                    if (value === 'Yes') {
                        finalData.image = {};
                    }
                    break;
                case 'Cover_Mime':
                    if (!finalData.hasOwnProperty('image')) {
                        finalData.image = {};
                    }    
                    finalData.image.mime = value;
                    break;   
            }
        }
    }

    /**
     * It executes `mediainfo` spitting out relevant info in JSON format
     * @param {string} filePath the file's path
     */
    static async getData(filePath) {
        try {
            const process = await execute('mediainfo', ['-f', '--Output=JSON', filePath]);
            const { stdout, stderr } = process;

            if (!_.isEmpty(stderr)) {
                console.error(stderr);
                return {
                    error: true,
                    message: stderr,
                };
            }

            const { media } = JSON.parse(stdout);
            const finalData = {};

            if (_.isEmpty(media)) {
                return {};
            }

            media.track.forEach((element) => {
                if (element['@type'] === 'General') {
                    MediaInfo.collectGeneralData(element, finalData);
                }

                if (element['@type'] === 'Audio') {
                    if (finalData.hasOwnProperty('language')) {
                        finalData.language = element.Language;
                    }
                }
            });
            
            return finalData;
        } catch (e) {
            console.error(e);
            return {
                error: true,
                message: e.message,
            };
        }
    }
}

module.exports = MediaInfo;
