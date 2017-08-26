const merge = require("webpack-merge");
const path = require('path');
const webpack = require('webpack'); //to access built-in plugins
const commonConfig = require("./webpack.common.js");

const config = {
    output: {
        path: path.resolve(__dirname, "dist/prod")
    },
    devtool: "hidden-source-map",
    stats: "normal",
    plugins: [
        new webpack.DefinePlugin({ "process.env": {"NODE_ENV": JSON.stringify("production") }}),
        new webpack.LoaderOptionsPlugin({
            debug: false,
            sourceMap: true
        }),
        new webpack.optimize.UglifyJsPlugin({
            beautify: false,
            mangle: {
                screw_ie8: true,
                keep_fnames: true
            },
            compress: { screw_ie8: true }, // We do not support IE 8
            comments: false
        })
    ]
};

module.exports = merge(commonConfig, config);
