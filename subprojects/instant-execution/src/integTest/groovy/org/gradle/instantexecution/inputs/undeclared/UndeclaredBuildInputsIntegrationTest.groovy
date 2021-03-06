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

package org.gradle.instantexecution.inputs.undeclared


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.instantexecution.AbstractInstantExecutionIntegrationTest
import spock.lang.Unroll

class UndeclaredBuildInputsIntegrationTest extends AbstractInstantExecutionIntegrationTest {
    @Unroll
    def "reports build logic reading a system property set #mechanism.description via the Java API"() {
        buildFile << """
            // not declared
            System.getProperty("CI")
        """

        when:
        mechanism.setup(this)
        instantFails(*mechanism.gradleArgs)

        then:
        // TODO - use problems fixture, however build script class is generated
        failure.assertThatDescription(containsNormalizedString("- unknown location: read system property 'CI' from 'build_"))
        failure.assertHasFileName("Build file '${buildFile.absolutePath}'")
        failure.assertHasLineNumber(3)
        failure.assertThatCause(containsNormalizedString("Read system property 'CI' from 'build_"))

        where:
        mechanism << SystemPropertyInjection.all("CI", "false")
    }

    @Unroll
    def "build logic can read system property with no value without declaring access"() {
        file("buildSrc/src/main/java/SneakyPlugin.java") << """
            import ${Project.name};
            import ${Plugin.name};

            public class SneakyPlugin implements Plugin<Project> {
                public void apply(Project project) {
                    System.out.println("CI = " + System.getProperty("CI"));
                }
            }
        """
        buildFile << """
            apply plugin: SneakyPlugin
        """
        def fixture = newInstantExecutionFixture()

        when:
        instantRun()

        then:
        outputContains("CI = null")

        when:
        instantRun()

        then:
        fixture.assertStateLoaded()
        noExceptionThrown()

        when:
        mechanism.setup(this)
        instantFails(*mechanism.gradleArgs)

        then:
        failure.assertThatDescription(containsNormalizedString("- unknown location: read system property 'CI' from '"))

        where:
        mechanism << SystemPropertyInjection.all("CI", "false")
    }

    @Unroll
    def "build logic can read standard system property #prop without declaring access"() {
        file("buildSrc/src/main/java/SneakyPlugin.java") << """
            import ${Project.name};
            import ${Plugin.name};

            public class SneakyPlugin implements Plugin<Project> {
                public void apply(Project project) {
                    System.out.println("$prop = " + System.getProperty("$prop"));
                }
            }
        """
        buildFile << """
            apply plugin: SneakyPlugin
        """
        def fixture = newInstantExecutionFixture()

        when:
        instantRun()

        then:
        outputContains("$prop = ")

        when:
        instantRun()

        then:
        fixture.assertStateLoaded()
        noExceptionThrown()

        where:
        prop << [
            "os.name",
            "os.version",
            "os.arch",
            "java.version",
            "java.vm.version",
            "java.specification.version",
            "line.separator",
            "user.name",
            "user.home"
        ]
    }
}
