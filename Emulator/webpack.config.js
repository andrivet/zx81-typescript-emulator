
const path = require('path');
const webpack = require('webpack'); //to access built-in plugins
const HtmlWebpackPlugin = require('html-webpack-plugin');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');
const ExtractTextPlugin = require("extract-text-webpack-plugin");

module.exports = {
    entry: {
        polyfill: "./src/polyfill/polyfill.ts",
        app: "./src/zx81emulator/ZX81Emulator.ts"
    },
    output: {
        path: path.resolve(__dirname, "build"),
        filename: "[name].js"
    },
    watch: false,
    devtool: "source-map",
    stats: "detailed",
    resolve: {
        extensions: ['.ts', '.css', '.js']
    },
    module: {
        rules: [
            {test: /\.ts$/, use: 'awesome-typescript-loader'},
            {
                test: /\.css$/, use: ExtractTextPlugin.extract({
                    fallback: "style-loader",
                    use: "css-loader"
                })
            }
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
            template: './src/index.ejs',
            inject: true,
            filename: '../index.html'
        }),
        new UglifyJsPlugin(),
        new ExtractTextPlugin("styles.css")
    ]
};

