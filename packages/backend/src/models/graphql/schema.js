const { makeExecutableSchema } = require('graphql-tools');
const { mergeTypes, mergeResolvers } = require('merge-graphql-schemas');
const { resolvers: scalarsResolvers, typeDefs: scalarsTypeDefs } = require('./scalars');
// TODO: remove this as soon as you've something real
const { resolvers: mmresolvers, typeDefs: mmtypes } = require('./music_manager');

const schemaDefinition = `
  schema {
    query: Query
    mutation: Mutation
    subscription: Subscription
  }
`;

const resolvers = [scalarsResolvers, mmresolvers];
const types = [schemaDefinition, scalarsTypeDefs, mmtypes];

module.exports = makeExecutableSchema({
    typeDefs: mergeTypes(types),
    resolvers: mergeResolvers(resolvers),
});
