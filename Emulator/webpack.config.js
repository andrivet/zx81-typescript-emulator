
const path = require('path');
const webpack = require('webpack'); //to access built-in plugins
const HtmlWebpackPlugin = require('html-webpack-plugin');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');

module.exports = {
    entry: {
        polyfill: "./js/polyfill/polyfill.ts",
        app: "./js/zx81emulator/ZX81Emulator.ts"
    },
    output: {
        path: path.resolve(__dirname, "build"),
        filename: "[name].js"
    },
    watch: false,
    devtool: "source-map",
    resolve: {
        extensions: ['.ts']
    },
    module: {
        rules: [
            { test: /\.ts$/, exclude: /node_modules/, use: 'awesome-typescript-loader' }
        ]
    },
    plugins: [
        new webpack.optimize.CommonsChunkPlugin({
            names: [
                "main",
                "polyfill",
                "bootstrap" // Extract the Webpack bootstrap logic into its own file by providing a name that wasn't listed in the "entry" file list.
            ]
        }),
        new HtmlWebpackPlugin({
            title: "ZX81 Emulator",
            template: './js/index.ejs',
            inject: true,
            filename: '../index.html'
        }),
        new UglifyJsPlugin()
    ]
};

