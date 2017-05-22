package io.crnk.gen.runtime.deltaspike;

import java.io.File;
import java.util.List;

import io.crnk.gen.typescript.TSGeneratorConfiguration;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.InitializationError;

public class DeltaspikeRunner {

	public void run(File outputDir, TSGeneratorConfiguration config) {
		DeltaspikeTypescriptGenerator.setOutputDir(outputDir);
		DeltaspikeTypescriptGenerator.setConfig(config);
		Runner runner;
		try {
			runner = new CdiTestRunner(DeltaspikeTypescriptGenerator.class);
		}
		catch (InitializationError e) {
			throw new IllegalStateException(e);
		}

		JUnitCore c = new JUnitCore();
		Result result = c.run(Request.runner(runner));
		List<Failure> failures = result.getFailures();
		if (!failures.isEmpty()) {
			Failure failure = failures.get(0);
			Throwable exception = failure.getException();
			if (exception instanceof RuntimeException) {
				throw (RuntimeException) exception;
			}
			else {
				throw new IllegalStateException(exception);
			}
		}
	}
}
