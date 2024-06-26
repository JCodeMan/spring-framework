/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.context.bean.override.mockito;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.bean.override.example.ExampleService;
import org.springframework.test.context.bean.override.example.RealExampleService;
import org.springframework.test.context.junit.EngineTestKitUtils;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

/**
 * {@link MockitoBean @MockitoBean} "by type" integration tests for failure scenarios.
 *
 * @author Simon Baslé
 * @author Sam Brannen
 * @since 6.2
 * @see MockitoBeanByTypeIntegrationTests
 */
class FailingMockitoBeanByTypeIntegrationTests {

	@Test
	void zeroCandidates() {
		Class<?> testClass = ZeroCandidatesTestCase.class;
		EngineTestKitUtils.executeTestsForClass(testClass).assertThatEvents().haveExactly(1,
			finishedWithFailure(
				cause(
					instanceOf(IllegalStateException.class),
					message("""
						Unable to select a bean definition to override: found 0 bean definitions \
						of type %s (as required by annotated field '%s.example')"""
							.formatted(ExampleService.class.getName(), testClass.getSimpleName())))));
	}

	@Test
	void tooManyCandidates() {
		Class<?> testClass = TooManyCandidatesTestCase.class;
		EngineTestKitUtils.executeTestsForClass(testClass).assertThatEvents().haveExactly(1,
			finishedWithFailure(
				cause(
					instanceOf(IllegalStateException.class),
					message("""
						Unable to select a bean definition to override: found 2 bean definitions \
						of type %s (as required by annotated field '%s.example'): %s"""
							.formatted(ExampleService.class.getName(), testClass.getSimpleName(), List.of("bean1", "bean2"))))));
	}


	@SpringJUnitConfig
	static class ZeroCandidatesTestCase {

		@MockitoBean
		ExampleService example;

		@Test
		void test() {
			assertThat(example).isNotNull();
		}

		@Configuration
		static class Config {
		}
	}

	@SpringJUnitConfig
	static class TooManyCandidatesTestCase {

		@MockitoBean
		ExampleService example;

		@Test
		void test() {
			assertThat(example).isNotNull();
		}

		@Configuration
		static class Config {

			@Bean
			ExampleService bean1() {
				return new RealExampleService("1 Hello");
			}

			@Bean
			ExampleService bean2() {
				return new RealExampleService("2 Hello");
			}
		}
	}

}
