const Merge = require("webpack-merge");
const path = require('path');
const webpack = require('webpack'); //to access built-in plugins
const CommonConfig = require("./webpack.common.js");

module.exports = Merge(CommonConfig, {
    output: {
        path: path.resolve(__dirname, "build/dev"),
        filename: "js/[name].js"
    },
    devtool: "source-map",
    stats: "verbose",
    plugins: [
        new webpack.LoaderOptionsPlugin({
            debug: true,
            sourceMap: true
        })
    ]
});
