
const path = require('path');

module.exports = {
    entry: "./js/zx81emulator/ZX81Emulator.ts",
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
            { test: /\.ts$/, use: 'awesome-typescript-loader' }
        ]
    }
};

