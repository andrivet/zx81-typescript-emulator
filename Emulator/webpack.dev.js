const Merge = require("webpack-merge");
const CommonConfig = require("./webpack.common.js");

module.exports = Merge(CommonConfig, {
    watch: true,
    devtool: "cheap-module-eval-source-map",
    plugins: [
    ]
});
