
const path = require('path');
const webpack = require('webpack'); //to access built-in plugins

module.exports = {
    entry: {
        app: [
            'es6-promise',
            'whatwg-fetch',
            "./js/zx81emulator/ZX81Emulator.ts"

        ]
    },
    output: {
        path: path.resolve(__dirname, "build"),
        filename: "bundle.js"
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
        new webpack.ProvidePlugin({'Promise': 'es6-promise'})
    ]
};

