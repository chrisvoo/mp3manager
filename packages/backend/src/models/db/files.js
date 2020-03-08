const mongoose = require('mongoose');

const { Schema } = mongoose;
const modelName = 'MusicFiles';

const MusicFiles = new Schema({
    album_image: Buffer,
    album_image_mime_type: String,
    album_title: { type: String, trim: true },
    artist: { type: String, trim: true },
    bitrate: Number,
    bitrate_type: { type: String, enum: ['VARIABLE', 'CONSTANT'] },
    duration: Number,
    filename: { type: String, required: true, unique: true },
    has_album_image: Boolean,
    has_custom_tag: Boolean,
    has_id3v1_tag: Boolean,
    has_id3v2_tag: Boolean,
    size: Number,
    title: { type: String, trim: true },
});

module.exports = mongoose.model(modelName, MusicFiles, modelName);
