module.exports = {
  extends: ['@commitlint/config-conventional'],
  ignores: [
    (commit) => commit.startsWith('Merge pull request'),
    (commit) => commit.includes('dependabot')
  ],
  rules: {
    'body-max-line-length': [0, 'always', 300],
  },
};