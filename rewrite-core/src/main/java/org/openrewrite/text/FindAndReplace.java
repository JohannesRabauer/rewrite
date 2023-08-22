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
package org.openrewrite.text;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.openrewrite.*;
import org.openrewrite.binary.Binary;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Marker;
import org.openrewrite.quark.Quark;
import org.openrewrite.remote.Remote;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.openrewrite.Tree.randomId;

@Value
@EqualsAndHashCode(callSuper = true)
public class FindAndReplace extends Recipe {

    @Option(displayName = "Find",
            description = "The text to find (and replace).",
            example = "blacklist")
    String find;

    @Option(displayName = "Replace",
            description = "The replacement text for `find`.",
            example = "denylist")
    String replace;

    @Option(displayName = "Regex",
            description = "Default false. If true, `find` will be interpreted as a Regular Expression, and capture group contents will be available in `replace`.",
            required = false)
    @Nullable
    Boolean regex;

    @Option(displayName = "Case sensitive",
            description = "If `true` the search will be sensitive to case. Default `false`.",
            required = false)
    @Nullable
    Boolean caseSensitive;

    @Option(displayName = "Regex multiline mode",
            description = "When performing a regex search setting this to `true` allows \"^\" and \"$\" to match the beginning and end of lines, respectively. " +
                          "When performing a regex search when this is `false` \"^\" and \"$\" will match only the beginning and ending of the entire source file, respectively." +
                          "Has no effect when not performing a regex search. Default `false`.",
            required = false)
    @Nullable
    Boolean multiline;

    @Option(displayName = "Regex dot all",
            description = "When performing a regex search setting this to `true` allows \".\" to match line terminators." +
                          "Has no effect when not performing a regex search. Default `false`.",
            required = false)
    @Nullable
    Boolean dotAll;

    @Option(displayName = "File pattern",
            description = "A glob expression that can be used to constrain which directories or source files should be searched. " +
                          "Multiple patterns may be specified, separated by a semicolon `;`. " +
                          "If multiple patterns are supplied any of the patterns matching will be interpreted as a match. " +
                          "When not set, all source files are searched. ",
            example = "**/*.java")
    @Nullable
    String filePattern;

    @Override
    public String getDisplayName() {
        return "Find and replace";
    }

    @Override
    public String getDescription() {
        return "Simple text find and replace. When the original source file is a language-specific Lossless Semantic " +
               "Tree, this operation irreversibly converts the source file to a plain text file. Subsequent recipes " +
               "will not be able to operate on language-specific type.";
    }


    /**
     * Ensure that a file is not find-and-replaced twice in the same recipe run.
     * Used to avoid the situation where replacing "a" with "ab" results in something like "abb".
     */
    @Value
    @With
    static class AlreadyReplaced implements Marker {
        UUID id;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        TreeVisitor<?, ExecutionContext> visitor = new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                SourceFile sourceFile = (SourceFile) requireNonNull(tree);
                if (sourceFile instanceof Quark || sourceFile instanceof Remote || sourceFile instanceof Binary) {
                    return sourceFile;
                }
                if(sourceFile.getMarkers().findFirst(AlreadyReplaced.class).isPresent()) {
                    return sourceFile;
                }
                String searchStr = find;
                if (!Boolean.TRUE.equals(regex)) {
                    searchStr = Pattern.quote(searchStr);
                }
                int patternOptions = 0;
                if(!Boolean.TRUE.equals(caseSensitive)) {
                    patternOptions |= Pattern.CASE_INSENSITIVE;
                }
                if(Boolean.TRUE.equals(multiline)) {
                    patternOptions |= Pattern.MULTILINE;
                }
                if(Boolean.TRUE.equals(dotAll)) {
                    patternOptions |= Pattern.DOTALL;
                }
                PlainText plainText = PlainTextParser.convert(sourceFile);
                Pattern pattern = Pattern.compile(searchStr, patternOptions);
                Matcher matcher = pattern.matcher(plainText.getText());

                if (!matcher.find()) {
                    return sourceFile;
                }
                String newText = matcher.replaceAll(replace);
                return plainText.withText(newText)
                        .withMarkers(sourceFile.getMarkers().add(new AlreadyReplaced(randomId())));
            }
        };
        //noinspection DuplicatedCode
        if(filePattern != null) {
            //noinspection unchecked
            TreeVisitor<?, ExecutionContext> check = Preconditions.or(Arrays.stream(filePattern.split(";"))
                    .map(HasSourcePath<ExecutionContext>::new)
                    .toArray(TreeVisitor[]::new));

            visitor = Preconditions.check(check, visitor);
        }
        return visitor;
    }
}
