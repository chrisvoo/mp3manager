const types = require('./commonTypes');

const { DateTime, GraphQLJSON } = types;

const typeDefs = `
    scalar DateTime
    scalar GraphQLJSON
`;

const resolvers = {
    DateTime,
    GraphQLJSON,
};

module.exports = {
    typeDefs,
    resolvers,
};
