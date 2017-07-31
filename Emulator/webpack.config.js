"use strict";

const path = require('path');
const webpack = require('webpack'); //to access built-in plugins
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ExtractTextPlugin = require("extract-text-webpack-plugin");
const CopyWebpackPlugin = require('copy-webpack-plugin');
const FaviconsWebpackPlugin = require('favicons-webpack-plugin');

module.exports = {
    entry: {
        vendor: ["bootstrap-loader"],
        polyfill: ["./src/polyfill/polyfill.ts", "whatwg-fetch"],
        app: "./src/Main.ts"
    },
    output: {
        path: path.resolve(__dirname, "build"),
        filename: "js/[name].js"
    },
    watch: true,
    devtool: "source-map",
    stats: "detailed",
    resolve: {
        alias: {
            ROM: path.resolve(__dirname, 'ROM/')
        },
        extensions: ['.ts', '.css', '.js', '.rom']
    },
    module: {
        rules: [
            { test: /\.ts$/, use: 'awesome-typescript-loader' },
            { test: /\.css$/, use: ExtractTextPlugin.extract({
                    fallback: "style-loader",
                    use: "css-loader"
            })},
            { test: /\.rom$/, use: { loader: 'file-loader', query: { name: '[path][name].[ext]' }}},
            { test: /bootstrap[\/\\]dist[\/\\]js[\/\\]umd[\/\\]/, loader: 'imports-loader?jQuery=jquery' },
            { test: /\.(woff2?|svg)$/, loader: 'url-loader?limit=10000' },
            { test: /\.(ttf|eot)$/, loader: 'file-loader' }
        ]
    },
    plugins: [
        new webpack.optimize.CommonsChunkPlugin({
            names: [
                "app",
                "polyfill",
                "manifest" // Extract the Webpack bootstrap logic into its own file by providing a name that wasn't listed in the "entry" file list.
            ]
        }),
        new HtmlWebpackPlugin({
            title: "ZX81 Emulator",
            template: './src/index.ejs',
            inject: true,
            filename: 'index.html'
        }),
        new ExtractTextPlugin("styles.css"),
        new CopyWebpackPlugin([{from: 'PROGS/', to: 'PROGS/'}, {from: 'robots.txt'}]),
        new FaviconsWebpackPlugin({logo: './logo.png', prefix: 'icons/', inject: true, background: '#FFF'}),
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery",
            "window.jQuery": "jquery",
            Tether: "tether",
            "window.Tether": "tether",
            Alert: "exports-loader?Alert!bootstrap/js/dist/alert",
            Button: "exports-loader?Button!bootstrap/js/dist/button",
            Carousel: "exports-loader?Carousel!bootstrap/js/dist/carousel",
            Collapse: "exports-loader?Collapse!bootstrap/js/dist/collapse",
            Dropdown: "exports-loader?Dropdown!bootstrap/js/dist/dropdown",
            Modal: "exports-loader?Modal!bootstrap/js/dist/modal",
            Popover: "exports-loader?Popover!bootstrap/js/dist/popover",
            Scrollspy: "exports-loader?Scrollspy!bootstrap/js/dist/scrollspy",
            Tab: "exports-loader?Tab!bootstrap/js/dist/tab",
            Tooltip: "exports-loader?Tooltip!bootstrap/js/dist/tooltip",
            Util: "exports-loader?Util!bootstrap/js/dist/util",
        })
    ]
};

