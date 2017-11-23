module.exports = function(config) {
	config.set({

		frameworks: ["jasmine", "karma-typescript"],

		files: [
			{ pattern: "base.spec.ts" },
			{ pattern: "binding/**/*.+(ts|html)" },
			{ pattern: "expression/**/*.+(ts|html)" },
			{ pattern: "meta/**/*.+(ts|html)" },
			{ pattern: "operations/**/*.+(ts|html)" },
			{ pattern: "query/**/*.+(ts|html)" },
			{ pattern: "stub/**/*.+(ts|html)" },
			{ pattern: "test/**/*.+(ts|html)" }
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
				lib: ["ES2015", "DOM"],
				sourceMap: true
			}
		},

		singleRun: true,

		reporters: ["dots", "karma-typescript"],

		browsers: ["Chrome"]
	});
};
