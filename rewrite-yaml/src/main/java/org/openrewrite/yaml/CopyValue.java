/*
 * Copyright 2020 the original author or authors.
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
package org.openrewrite.yaml;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.tree.Yaml;

@Value
@EqualsAndHashCode(callSuper = false)
public class CopyValue extends Recipe {
    @Option(displayName = "Old key path",
            description = "A [JsonPath](https://github.com/json-path/JsonPath) expression to locate a YAML key/value pair to copy.",
            example = "$.source.kind")
    String oldKeyPath;

    @Option(displayName = "New key path",
            description = "A [JsonPath](https://github.com/json-path/JsonPath) expression for where the new value should be copied to.",
            example = "$.dest.kind")
    String newKey;

    @Override
    public String getDisplayName() {
        return "Copy YAML value";
    }

    @Override
    public String getInstanceNameSuffix() {
        return String.format("`%s` to `%s`", oldKeyPath, newKey);
    }

    @Override
    public String getDescription() {
        return "Copies a YAML value from one key to another. " +
               "The existing key/value pair remains unaffected by this change. " +
               "If either the source or destination key path does not exist, no value will be copied. " +
               "Furthermore, copies are limited to scalar values, not whole YAML blocks.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JsonPathMatcher oldPathMatcher = new JsonPathMatcher(oldKeyPath);
        JsonPathMatcher newPathMatcher = new JsonPathMatcher(newKey);

        return new YamlIsoVisitor<ExecutionContext>() {
            @Override
            public Yaml.Mapping.Entry visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
                Yaml.Mapping.Entry source = super.visitMappingEntry(entry, ctx);
                if (oldPathMatcher.matches(getCursor()) && entry.getValue() instanceof Yaml.Scalar) {
                    doAfterVisit(new YamlIsoVisitor<ExecutionContext>() {
                        @Override
                        public Yaml.Mapping.Entry visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
                            Yaml.Mapping.Entry dest = super.visitMappingEntry(entry, ctx);
                            if (newPathMatcher.matches(getCursor())) {
                                dest = dest.withValue(source.getValue());
                            }
                            return dest;
                        }
                    });

                }
                return source;
            }
        };
    }
}
