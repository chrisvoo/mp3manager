const types = require('./commonTypes');

const { DateTime, GraphQLJSON } = types;

const typeDefs = `
    scalar DateTime
    scalar JSON
`;

const resolvers = {
    DateTime,
    JSON: GraphQLJSON,
};

module.exports = {
    typeDefs,
    resolvers,
};
