const FileType = `

`;

const typeDefs = `
    enum BitRate {
        VARIABLE
        CONSTANT
    }

    type File {
        """ Base64 coversion from/to backend """
        album_image: String
        album_image_mime_type: String
        album_title: String
        artist: String
        bitrate: Int
        bitrate_type: BitRate
        duration: Int
        filename: String!
        has_album_image: Boolean
        has_custom_tag: Boolean
        has_id3v1_tag: Boolean
        has_id3v2_tag: Boolean
        size: Int,
        title: String
    }

    type Query {
        dummy: Boolean
    }

    type Mutation {
        dummy: Boolean
    }

    type Subscription {
        dummy: Boolean
    }
`;

const resolvers = {
    Query: {
        dummy(o, args, params) {
            console.log(params);
            return true;
        },
    },
    Mutation: {
        dummy(o, args, params) {
            console.log(params);
            return true;
        },
    },
    Subscription: {
        dummy(o, args, params) {
            console.log(params);
            return true;
        },
    },
};

module.exports = {
    typeDefs,
    resolvers,
};
