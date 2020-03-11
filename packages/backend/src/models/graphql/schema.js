const { makeExecutableSchema } = require('graphql-tools');
const { mergeTypes, mergeResolvers } = require('merge-graphql-schemas');
const { resolvers: scalarsResolvers, typeDefs: scalarsTypeDefs } = require('./scalars');
const { resolvers: mmresolvers, typeDefs: mmtypes } = require('./music_manager');

const schemaDefinition = `
  schema {
    query: Query
    mutation: Mutation
  }
`;

const resolvers = [scalarsResolvers, mmresolvers];
const types = [schemaDefinition, scalarsTypeDefs, mmtypes];

module.exports = makeExecutableSchema({
    typeDefs: mergeTypes(types),
    resolvers: mergeResolvers(resolvers),
});
