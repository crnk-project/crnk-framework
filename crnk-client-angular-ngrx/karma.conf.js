module.exports = function(config) {
	config.set({

		frameworks: ["jasmine", "karma-typescript"],

		files: [
			{ pattern: "base.spec.ts" },
			{ pattern: "src/main/typescript/**/*.+(ts|html)" },
			{ pattern: "src/test/typescript/**/*.+(ts|html)" }
		],

		preprocessors: {
			"**/*.ts": ["karma-typescript"]
		},

		karmaTypescriptConfig: {
			//tsconfig: 'tsconfig.spec.json',

			exclude: [
				"node_modules",
				"build"
			],

			bundlerOptions: {
				entrypoints: /\.spec\.ts$/,
				transforms: [
					require("karma-typescript-es6-transform")({presets: ["es2015"]}),
					require("karma-typescript-angular2-transform")

				]
			},
			compilerOptions: {
				skipLibCheck: true,
				lib: ["ES2015", "DOM"]
			}
		},

		singleRun: true,

		reporters: ["dots", "karma-typescript"],

		browsers: ["Chrome"]
	});
};
