/* Put here every custom type reusable in other definitions, so this
 * becomes the single access point for them */
const GraphQLJSON = require('graphql-type-json');
const { GraphQLScalarType, Kind } = require('graphql');

/* Custom scalar type for representing a Date, which
 * is serialized and parsed as Unix timestamp in milliseconds */
const DateTime = new GraphQLScalarType({
    name: 'DateTime',
    description: 'Date parsed and returned as unix timestamp in milliseconds',
    parseValue(value) {
        return new Date(value); // value from the client
    },
    serialize(value) {
        return value; // value sent to the client
    },
    parseLiteral(ast) {
        if (ast.kind === Kind.INT) {
            return parseInt(ast.value, 10); // ast value is always in string format
        }
        return null;
    },
});

module.exports = {
    DateTime,
    GraphQLJSON,
};
