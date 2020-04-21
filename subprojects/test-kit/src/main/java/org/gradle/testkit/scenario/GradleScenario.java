/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.testkit.scenario;

import org.gradle.api.Incubating;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.scenario.internal.DefaultGradleScenario;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * Runs a scenario of several Gradle builds, allowing inspection of the outcome.
 *
 * @since 6.5
 */
@Incubating
public abstract class GradleScenario {

    public static GradleScenario create() {
        return new DefaultGradleScenario();
    }

    protected GradleScenario() {
    }

    public abstract GradleScenario withBaseDirectory(File temporaryDirectory);

    public abstract GradleScenario withWorkspace(Consumer<File> workspaceBuilder);

    public abstract GradleScenario withRunnerFactory(Supplier<GradleRunner> runnerFactory);

    public abstract GradleScenario withSteps(Consumer<GradleScenarioSteps> steps);

    public abstract ScenarioResult run();
}
