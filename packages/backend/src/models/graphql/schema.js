const { makeExecutableSchema } = require('graphql-tools');
const { mergeTypes, mergeResolvers } = require('merge-graphql-schemas');
const { resolvers: scalarsResolvers, typeDefs: scalarsTypeDefs } = require('./scalars');
// TODO: remove this as soon as you've something real
const { resolvers: dummyResolvers, typeDefs: dummyTypeDefs } = require('./dummy');

const schemaDefinition = `
  schema {
    query: Query
    mutation: Mutation
    subscription: Subscription
  }
`;

const resolvers = [scalarsResolvers, dummyResolvers];
const types = [schemaDefinition, scalarsTypeDefs, dummyTypeDefs];

module.exports = makeExecutableSchema({
    typeDefs: mergeTypes(types),
    resolvers: mergeResolvers(resolvers),
});
