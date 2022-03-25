// vue.config.js

/**
 * @type {import('@vue/cli-service').ProjectOptions}
 */
module.exports = {
    configureWebpack: {
        output: {
            libraryExport: 'default'
        }, performance: {
            maxAssetSize: 512000, maxEntrypointSize: 512000, hints: "warning"
        }
    }
}