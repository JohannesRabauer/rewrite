package org.openrewrite.ruby.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.ruby.Assertions.ruby;

public class ClassDeclarationTest implements RewriteTest {

    @Test
    void classDeclaration() {
        rewriteRun(
          ruby(
            """
              class Customer
              end
              """
          )
        );
    }

    @Test
    void classExtends() {
        rewriteRun(
          ruby(
            """
              class Box
              end
                            
              class BigBox < Box
              end
              """
          )
        );
    }

    @Test
    void initializeAndInstanceMethod() {
        rewriteRun(
          ruby(
            """
              class Box
                 def initialize(w,h)
                    @width, @height = w, h
                 end
                            
                 def getArea
                    @width * @height
                 end
              end
              """
          )
        );
    }
}
