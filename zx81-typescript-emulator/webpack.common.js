"use strict";

const path = require('path');
const webpack = require('webpack'); //to access built-in plugins
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ExtractTextPlugin = require("extract-text-webpack-plugin");
const CopyWebpackPlugin = require('copy-webpack-plugin');
const FaviconsWebpackPlugin = require('favicons-webpack-plugin');

const config =
    {
        output: {
            filename: "[name].js"
        },
        entry: {
            polyfill: ["./src/polyfill/polyfill.ts", "whatwg-fetch"],
            app: "./src/Main.ts"
        },
        resolve: {
            alias: {
                ROM: path.resolve(__dirname, 'ROM/')
            },
            extensions: ['.ts', '.css', '.js', '.rom', '.json']
        },
        module: {
            rules: [
                { test: /\.ts$/, use: 'awesome-typescript-loader' },
                { test: /\.css$/, use: ExtractTextPlugin.extract({
                        fallback: "style-loader",
                        use: "css-loader"
                    })},
                { test: /\.rom$/, use: { loader: 'file-loader', query: { name: '[path][name].[ext]' }}}
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
            new CopyWebpackPlugin([{from: 'resources/ROM/', to: 'ROM/'}, {from: 'resources/PROGS/', to: 'PROGS/'}, {from: 'resources/robots.txt', to:'.'}]),
            new FaviconsWebpackPlugin({logo: './resources/logo.png', prefix: 'icons/', inject: true, background: '#FFF'})
        ]
    };

module.exports = config;

