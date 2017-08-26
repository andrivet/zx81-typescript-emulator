"use strict";

const path = require('path');
const webpack = require('webpack'); //to access built-in plugins
const CopyWebpackPlugin = require('copy-webpack-plugin');

const config =
    {
        output: {
            filename: "[name].js",
            library: "zx81-emulator",
            libraryTarget: "umd"
        },
        entry: {
            index: "./src/zx81emulator/ZX81Emulator.ts"
        },
        resolve: {
            alias: {
                ROM: path.resolve(__dirname, 'ROM/')
            },
            extensions: ['.ts', '.css', '.js']
        },
        module: {
            rules: [
                { test: /\.ts$/, use: 'awesome-typescript-loader' },
            ]
        },
        plugins: [
            new CopyWebpackPlugin([{from: 'src/index.d.ts'}])
        ]
    };

module.exports = config;

