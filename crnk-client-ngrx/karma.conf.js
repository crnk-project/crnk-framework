// Karma configuration file, see link for more information
// https://karma-runner.github.io/0.13/config/configuration-file.html

module.exports = function (config) {
	config.set({
		basePath: '',
		frameworks: ["jasmine", "karma-typescript"],

		plugins: [
			require('karma-jasmine'),
			//require('@angular/cli/plugins/karma'),
			require('karma-chrome-launcher'),
			require('karma-firefox-launcher'),
			require('karma-jasmine-html-reporter'),
			require('karma-coverage-istanbul-reporter'),
			require('karma-junit-reporter'),
			require('karma-typescript')
			//require('karma-typescript-preprocessor')
		],
		client: {
			// leave Jasmine Spec Runner output visible in browser
			clearContext: false
		},
		files: [
			{ pattern: "src/main/typescript/**/*.ts" },
			{ pattern: "src/test/typescript/**/*.ts" }

			// {pattern: './src/test/typescript/test.bundle.ts', watched: false}
		],
		preprocessors: {
			"src/main/typescript/**/*.ts": ["karma-typescript"],
			"test/test/typescript/**/*.ts": ["karma-typescript"]

			// "**/*.ts": ["karma-typescript"],
			// '**/*.ts': ['typescript']
		},
		mime: {
			'text/x-typescript': ['ts', 'tsx']
		},
		karmaTypescriptConfig: {
			bundlerOptions: {
				entrypoints: /\.spec\.ts$/,
				transforms: [
					require('karma-typescript-angular2-transform'),
					require("karma-typescript-es6-transform")()
				]
			},


			compilerOptions: {
				lib: ['ES2015', 'DOM'],
				module: "commonjs",
				emitDecoratorMetadata: true,
				experimentalDecorators: true,
				sourceMap: true,
				target: "ES5"
			}
		},



		coverageIstanbulReporter: {
			reports: ['html', 'lcovonly'],
			fixWebpackSourcePaths: true
		},
		// reporters: ['progress', 'junit'].concat(
		//	config.angularCli && config.angularCli.codeCoverage ? ['coverage-istanbul'] : ['kjhtml']),

		reporters: ['progress', 'karma-typescript'],

		port: 9876,
		colors: true,
		logLevel: config.LOG_INFO,
		autoWatch: true,
		browsers: ['Firefox'],
		singleRun: true,
		junitReporter: {
			outputDir: 'build/test/log/karma',
			outputFile: 'unit.xml',
			useBrowserName: false,
			suite: 'asap-resource-browser-module',
			nameFormatter: function classNameOmittingNameFormatter(browser, result) {
				var descriptions = result.suite.slice(1);
				descriptions.push(result.description);
				return descriptions.join(' ');
			}
		}
	});
};
