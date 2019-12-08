const mongoose = require('mongoose');

const { Schema } = mongoose;
const modelName = 'MusicFiles';

const MusicFiles = new Schema({
    path: { type: String, required: true },
    fileSize: Number,
    coverImage: Buffer,
    metadata: Schema.Types.Mixed,
    createdAt: { type: Date, default: Date.now },
    updatedAt: { type: Date },
});

module.exports = mongoose.model(modelName, MusicFiles, modelName);
