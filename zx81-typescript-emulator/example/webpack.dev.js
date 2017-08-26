const merge = require("webpack-merge");
const path = require('path');
const webpack = require('webpack'); //to access built-in plugins
const commonConfig = require("./webpack.common.js");

const config = {
    output: {
        path: path.resolve(__dirname, "dist/dev")
    },
    devtool: "source-map",
    stats: "verbose",
    plugins: [
        new webpack.LoaderOptionsPlugin({
            debug: true,
            sourceMap: true
        })
    ]
};

module.exports = merge(commonConfig, config);
