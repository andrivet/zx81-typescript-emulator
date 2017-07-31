const Merge = require("webpack-merge");
const webpack = require('webpack'); //to access built-in plugins
const CommonConfig = require("./webpack.common.js");

module.exports = Merge(CommonConfig, {
    watch: true,
    devtool: "cheap-module-eval-source-map",
    plugins: [
        new webpack.LoaderOptionsPlugin({
            debug: true,
            sourceMap: true
        })
    ]
});
