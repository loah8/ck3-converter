module.exports = {
  publicPath: process.env.GITHUB_ACTIONS === 'true'
    ? '/ck3-converter/'
    : '/'
}
