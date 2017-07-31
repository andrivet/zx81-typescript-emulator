const Merge = require("webpack-merge");
const webpack = require('webpack'); //to access built-in plugins
const CommonConfig = require("./webpack.common.js");

module.exports = Merge(CommonConfig, {
    watch: false,
    devtool: "hidden-source-map",
    plugins: [
        new webpack.DefinePlugin({ "process.env": {"NODE_ENV": JSON.stringify("production") }}),
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
});
