// vue.config.js

/**
 * @type {import('@vue/cli-service').ProjectOptions}
 */
module.exports = {
    configureWebpack: {
        output: {
            libraryExport: 'default'
        }, performance: {
            maxAssetSize: 5512000, maxEntrypointSize: 5512000, hints: "warning"
        }
    }
}