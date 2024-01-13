/*
 * Copyright 2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.maven;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.Issue;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;

class ChangeDependencyGroupIdAndArtifactIdTest implements RewriteTest {

    @DocumentExample
    @Test
    void changeDependencyGroupIdAndArtifactId() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
            "javax.activation",
            "javax.activation-api",
            "jakarta.activation",
            "jakarta.activation-api",
            null,
            null
          )),
          pomXml(
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>javax.activation</groupId>
                          <artifactId>javax.activation-api</artifactId>
                      </dependency>
                  </dependencies>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>javax.activation</groupId>
                              <artifactId>javax.activation-api</artifactId>
                              <version>1.2.0</version>
                          </dependency>
                          <dependency>
                              <groupId>jakarta.activation</groupId>
                              <artifactId>jakarta.activation-api</artifactId>
                              <version>1.2.1</version>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """,
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>jakarta.activation</groupId>
                          <artifactId>jakarta.activation-api</artifactId>
                      </dependency>
                  </dependencies>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>javax.activation</groupId>
                              <artifactId>javax.activation-api</artifactId>
                              <version>1.2.0</version>
                          </dependency>
                          <dependency>
                              <groupId>jakarta.activation</groupId>
                              <artifactId>jakarta.activation-api</artifactId>
                              <version>1.2.1</version>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """
          )
        );
    }

    @Test
    @Issue("https://github.com/openrewrite/rewrite-java-dependencies/issues/55")
    void requireNewGroupIdOrNewArtifactId() {
        assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(() -> rewriteRun(
            spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
              "javax.activation",
              "javax.activation-api",
              null,
              null,
              null,
              null,
              null
            ))
          )).withMessageContaining("newGroupId OR newArtifactId must be different from before");
    }

    @Test
    @Issue("https://github.com/openrewrite/rewrite-java-dependencies/issues/55")
    void requireNewGroupIdOrNewArtifactIdToBeDifferentFromBefore() {
        assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(() -> rewriteRun(
            spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
              "javax.activation",
              "javax.activation-api",
              "javax.activation",
              null,
              null,
              null,
              null
            ))
          )).withMessageContaining("newGroupId OR newArtifactId must be different from before");
    }

    @Test
    void overrideManagedDependency() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
            "javax.activation",
            "javax.activation-api",
            "jakarta.activation",
            "jakarta.activation-api",
            "1.2.2",
            null,
            true
          )),
          pomXml(
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>javax.activation</groupId>
                          <artifactId>javax.activation-api</artifactId>
                      </dependency>
                  </dependencies>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>javax.activation</groupId>
                              <artifactId>javax.activation-api</artifactId>
                              <version>1.2.0</version>
                          </dependency>
                          <dependency>
                              <groupId>jakarta.activation</groupId>
                              <artifactId>jakarta.activation-api</artifactId>
                              <version>1.2.1</version>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """,
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>jakarta.activation</groupId>
                          <artifactId>jakarta.activation-api</artifactId>
                          <version>1.2.2</version>
                      </dependency>
                  </dependencies>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>javax.activation</groupId>
                              <artifactId>javax.activation-api</artifactId>
                              <version>1.2.0</version>
                          </dependency>
                          <dependency>
                              <groupId>jakarta.activation</groupId>
                              <artifactId>jakarta.activation-api</artifactId>
                              <version>1.2.1</version>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """
          )
        );
    }

    @Test
    void managedToUnmanaged() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
            "javax.activation",
            "javax.activation-api",
            "jakarta.activation",
            "jakarta.activation-api",
            "1.2.2",
            null,
            false
          )),
          pomXml(
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>javax.activation</groupId>
                          <artifactId>javax.activation-api</artifactId>
                      </dependency>
                  </dependencies>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>javax.activation</groupId>
                              <artifactId>javax.activation-api</artifactId>
                              <version>1.2.0</version>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """,
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>jakarta.activation</groupId>
                          <artifactId>jakarta.activation-api</artifactId>
                          <version>1.2.2</version>
                      </dependency>
                  </dependencies>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>javax.activation</groupId>
                              <artifactId>javax.activation-api</artifactId>
                              <version>1.2.0</version>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """
          )
        );
    }

    @Test
    void unmanagedToManaged() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
            "javax.activation",
            "javax.activation-api",
            "jakarta.activation",
            "jakarta.activation-api",
            "1.2.2",
            null,
            false
          )),
          pomXml(
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>javax.activation</groupId>
                          <artifactId>javax.activation-api</artifactId>
                          <version>1.2.0</version>
                      </dependency>
                  </dependencies>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>jakarta.activation</groupId>
                              <artifactId>jakarta.activation-api</artifactId>
                              <version>1.2.1</version>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """,
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>jakarta.activation</groupId>
                          <artifactId>jakarta.activation-api</artifactId>
                      </dependency>
                  </dependencies>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>jakarta.activation</groupId>
                              <artifactId>jakarta.activation-api</artifactId>
                              <version>1.2.1</version>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """
          )
        );
    }

    @Test
    void unmanagedToManagedWithOverrideManagedVersion() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
            "javax.activation",
            "javax.activation-api",
            "jakarta.activation",
            "jakarta.activation-api",
            "1.2.2",
            null,
            true
          )),
          pomXml(
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>javax.activation</groupId>
                          <artifactId>javax.activation-api</artifactId>
                          <version>1.2.0</version>
                      </dependency>
                  </dependencies>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>jakarta.activation</groupId>
                              <artifactId>jakarta.activation-api</artifactId>
                              <version>1.2.1</version>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """,
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>jakarta.activation</groupId>
                          <artifactId>jakarta.activation-api</artifactId>
                          <version>1.2.2</version>
                      </dependency>
                  </dependencies>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>jakarta.activation</groupId>
                              <artifactId>jakarta.activation-api</artifactId>
                              <version>1.2.1</version>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """
          )
        );
    }

    @Test
    void unmanagedToManagedWithOverrideManagedVersionNull() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
            "javax.activation",
            "javax.activation-api",
            "jakarta.activation",
            "jakarta.activation-api",
            "1.2.2",
            null,
            null
          )),
          pomXml(
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>javax.activation</groupId>
                          <artifactId>javax.activation-api</artifactId>
                          <version>1.2.0</version>
                      </dependency>
                  </dependencies>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>jakarta.activation</groupId>
                              <artifactId>jakarta.activation-api</artifactId>
                              <version>1.2.1</version>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """,
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>jakarta.activation</groupId>
                          <artifactId>jakarta.activation-api</artifactId>
                      </dependency>
                  </dependencies>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>jakarta.activation</groupId>
                              <artifactId>jakarta.activation-api</artifactId>
                              <version>1.2.1</version>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """
          )
        );
    }

    @Test
    void changeOnlyArtifactId() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
            "org.openrewrite",
            "rewrite-java-8",
            "org.openrewrite",
            "rewrite-java-11",
            null,
            null
          )),
          pomXml(
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>org.openrewrite</groupId>
                          <artifactId>rewrite-java-8</artifactId>
                          <version>7.20.0</version>
                      </dependency>
                  </dependencies>
              </project>
              """,
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>org.openrewrite</groupId>
                          <artifactId>rewrite-java-11</artifactId>
                          <version>7.20.0</version>
                      </dependency>
                  </dependencies>
              </project>
              """
          )
        );
    }

    @Test
    void doNotChangeUnlessBothGroupIdAndArtifactIdMatch() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
            "org.openrewrite.recipe",
            "rewrite-testing-frameworks",
            "org.openrewrite.recipe",
            "rewrite-migrate-java",
            null,
            null
          )),
          pomXml(
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>org.openrewrite.recipe</groupId>
                          <artifactId>rewrite-spring</artifactId>
                          <version>4.12.0</version>
                      </dependency>
                  </dependencies>
              </project>
              """
          )
        );
    }

    @Test
    void changeDependencyGroupIdAndArtifactIdAndVersion() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
            "javax.activation",
            "javax.activation-api",
            "jakarta.activation",
            "jakarta.activation-api",
            "2.1.0",
            null
          )),
          pomXml(
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>javax.activation</groupId>
                          <artifactId>javax.activation-api</artifactId>
                          <version>1.2.0</version>
                      </dependency>
                  </dependencies>
              </project>
              """,
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>jakarta.activation</groupId>
                          <artifactId>jakarta.activation-api</artifactId>
                          <version>2.1.0</version>
                      </dependency>
                  </dependencies>
              </project>
              """
          )
        );
    }

    @Test
    void changeDependencyGroupIdAndArtifactIdWithDeepHierarchy() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
              "io.quarkus",
              "quarkus-core",
              "io.quarkus",
              "quarkus-arc",
              null,
              null
            )
          ),
          pomXml(
            """
              <project>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>parent</artifactId>
                  <version>1</version>
                  <modules>
                      <module>child</module>
                  </modules>
              </project>
              """
          ),
          mavenProject("child",
            pomXml(
              """
                <project>
                        <parent>
                            <groupId>com.mycompany.app</groupId>
                            <artifactId>parent</artifactId>
                            <version>1</version>
                        </parent>
                        <groupId>com.mycompany.app</groupId>
                        <artifactId>child</artifactId>
                        <version>1</version>
                        <modules>
                            <module>subchild</module>
                        </modules>
                    </project>
                """
            ),
            mavenProject("subchild",
              pomXml(
                """
                      <project>
                          <parent>
                              <groupId>com.mycompany.app</groupId>
                              <artifactId>child</artifactId>
                              <version>1</version>
                          </parent>
                          <groupId>com.mycompany.app</groupId>
                          <artifactId>subchild</artifactId>
                          <version>1</version>
                          <dependencies>
                              <dependency>
                                  <groupId>io.quarkus</groupId>
                                  <artifactId>quarkus-core</artifactId>
                                  <version>2.8.0.Final</version>
                              </dependency>
                          </dependencies>
                      </project>
                  """,
                """
                      <project>
                          <parent>
                              <groupId>com.mycompany.app</groupId>
                              <artifactId>child</artifactId>
                              <version>1</version>
                          </parent>
                          <groupId>com.mycompany.app</groupId>
                          <artifactId>subchild</artifactId>
                          <version>1</version>
                          <dependencies>
                              <dependency>
                                  <groupId>io.quarkus</groupId>
                                  <artifactId>quarkus-arc</artifactId>
                                  <version>2.8.0.Final</version>
                              </dependency>
                          </dependencies>
                      </project>
                  """
              )
            )
          )
        );
    }

    @Test
    @Issue("https://github.com/openrewrite/rewrite/issues/1717")
    void changeDependencyGroupIdAndArtifactIdWithDependencyManagementScopeTest() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
            "io.quarkus",
            "quarkus-core",
            "io.quarkus",
            "quarkus-arc",
            null,
            null
          )),
          pomXml(
            """
              <project>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>parent</artifactId>
                  <version>1</version>
                  <modules>
                      <module>child</module>
                  </modules>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>io.quarkus</groupId>
                              <artifactId>quarkus-core</artifactId>
                              <version>2.8.0.Final</version>
                              <scope>test</scope>
                          </dependency>
                          <dependency>
                              <groupId>io.quarkus</groupId>
                              <artifactId>quarkus-arc</artifactId>
                              <version>2.8.0.Final</version>
                              <scope>test</scope>
                          </dependency>
                      </dependencies>
                  </dependencyManagement>
              </project>
              """
          ),
          mavenProject("child",
            pomXml(
              """
                    <project>
                        <parent>
                            <groupId>com.mycompany.app</groupId>
                            <artifactId>parent</artifactId>
                            <version>1</version>
                        </parent>
                        <groupId>com.mycompany.app</groupId>
                        <artifactId>child</artifactId>
                        <version>1</version>
                        <dependencies>
                            <dependency>
                                <groupId>io.quarkus</groupId>
                                <artifactId>quarkus-core</artifactId>
                            </dependency>
                        </dependencies>
                    </project>
                """,
              """
                    <project>
                        <parent>
                            <groupId>com.mycompany.app</groupId>
                            <artifactId>parent</artifactId>
                            <version>1</version>
                        </parent>
                        <groupId>com.mycompany.app</groupId>
                        <artifactId>child</artifactId>
                        <version>1</version>
                        <dependencies>
                            <dependency>
                                <groupId>io.quarkus</groupId>
                                <artifactId>quarkus-arc</artifactId>
                            </dependency>
                        </dependencies>
                    </project>
                """
            )
          )
        );
    }

    @Test
    @Issue("https://github.com/openrewrite/rewrite/issues/1751")
    void allowNewVersionToBeExpressedAsSemverSelector() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
            "com.google.guava",
            "guava-gwt",
            "com.google.guava",
            "guava",
            "30.1.x",
            "-jre"
          )),
          pomXml(
            """
              <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.mycompany.app</groupId>
                <artifactId>my-app</artifactId>
                <version>1</version>
                <dependencies>
                  <dependency>
                    <groupId>com.google.guava</groupId>
                    <artifactId>guava-gwt</artifactId>
                    <version>27.0-jre</version>
                  </dependency>
                </dependencies>
              </project>
              """,
            """
              <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.mycompany.app</groupId>
                <artifactId>my-app</artifactId>
                <version>1</version>
                <dependencies>
                  <dependency>
                    <groupId>com.google.guava</groupId>
                    <artifactId>guava</artifactId>
                    <version>30.1.1-jre</version>
                  </dependency>
                </dependencies>
              </project>
              """
          )
        );
    }

    @Test
    void changeGroupIdOnWildcardArtifacts() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupIdAndArtifactId(
            "org.apache.commons",
            "*",
            "commons-io",
            null,
            "2.11.0",
            null
          )),
          pomXml(
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>org.apache.commons</groupId>
                          <artifactId>commons-io</artifactId>
                          <version>1.3.2</version>
                      </dependency>
                  </dependencies>
              </project>
              """,
            """
              <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <version>1</version>
                  <dependencies>
                      <dependency>
                          <groupId>commons-io</groupId>
                          <artifactId>commons-io</artifactId>
                          <version>2.11.0</version>
                      </dependency>
                  </dependencies>
              </project>
              """
          )
        );
    }
}
