
const base64_encode = (data) => Buffer.from(data).toString('base64');
const base64_decode = (data, toAscii = true) => {
    const buf = Buffer.from(data, 'base64');
    if (toAscii) {
        return buf.toString('ascii');
    }

    return buf;
};

module.exports = {
    base64_encode, base64_decode,
};
