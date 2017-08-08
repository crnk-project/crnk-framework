// Karma configuration file, see link for more information
// https://karma-runner.github.io/0.13/config/configuration-file.html

module.exports = function (config) {
	config.set({
		basePath: '',
		frameworks: ['jasmine'],

		plugins: [
			require('karma-jasmine'),
			//require('@angular/cli/plugins/karma'),
			require('karma-chrome-launcher'),
			require('karma-firefox-launcher'),
			require('karma-jasmine-html-reporter'),
			require('karma-coverage-istanbul-reporter'),
			require('karma-junit-reporter')
		],
		client: {
			// leave Jasmine Spec Runner output visible in browser
			clearContext: false
		},
		files: [
			{pattern: './src/test/typescript/test.bundle.ts', watched: false}
		],
		preprocessors: {
			//'./src/test.ts': ['@angular/cli']
		},
		mime: {
			'text/x-typescript': ['ts', 'tsx']
		},
		coverageIstanbulReporter: {
			reports: ['html', 'lcovonly'],
			fixWebpackSourcePaths: true
		},
		reporters: ['progress', 'junit'].concat(
			config.angularCli && config.angularCli.codeCoverage ? ['coverage-istanbul'] : ['kjhtml']),
		port: 9876,
		colors: true,
		logLevel: config.LOG_INFO,
		autoWatch: true,
		browsers: ['Firefox'],
		singleRun: false,
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
