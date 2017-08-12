module.exports = function(config) {
	config.set({

		frameworks: ["jasmine", "karma-typescript"],

		files: [
			{ pattern: "base.spec.ts" },
			{ pattern: "src/main/typescript/**/*.+(ts|html)" }
		],

		preprocessors: {
			"**/*.ts": ["karma-typescript"]
		},

		karmaTypescriptConfig: {


			bundlerOptions: {
				entrypoints: /\.spec\.ts$/,
				transforms: [
					require("karma-typescript-es6-transform")({presets: ["es2015"]}),
					require("karma-typescript-angular2-transform")

				]
			},
			compilerOptions: {
				lib: ["ES2015", "DOM"]
			}
		},

		singleRun: false,

		reporters: ["dots", "karma-typescript"],

		browsers: ["Chrome"]
	});
};
