package org.metafacture.metafix;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.opentest4j.TestAbortedException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(MetafixToDo.Handler.class)
public @interface MetafixToDo {

    String value();

    class Extension implements AfterAllCallback, InvocationInterceptor {

        private static final boolean DISABLE_TO_DO = Boolean.parseBoolean(System.getProperty("org.metafacture.metafix.disableToDo"));

        private boolean annotationPresent;

        private Extension() {
        }

        @Override
        public void afterAll(final ExtensionContext context) {
            if (!annotationPresent) {
                for (final Method method : context.getTestClass().get().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Test.class) && method.isAnnotationPresent(MetafixToDo.class)) {
                        return;
                    }
                }

                Assertions.fail("Unused extension (no annotations present): " + Handler.EXTENSION_NAME);
            }
        }

        @Override
        public void interceptTestMethod(final InvocationInterceptor.Invocation<Void> invocation, final ReflectiveInvocationContext<Method> invocationContext, final ExtensionContext extensionContext) throws Throwable {
            if (DISABLE_TO_DO) {
                handleAnnotation(invocationContext, a -> {
                    throw new TestAbortedException(a.value());
                });

                invocation.proceed();
                return;
            }

            try {
                invocation.proceed();
            }
            catch (final Throwable e) { // checkstyle-disable-line IllegalCatch
                handleAnnotation(invocationContext, a -> {
                    throw new TestAbortedException(a.value(), e);
                });

                throw e;
            }

            handleAnnotation(invocationContext, a -> Assertions.fail("Marked as " + a + ", but passed."));
        }

        private void handleAnnotation(final ReflectiveInvocationContext<Method> invocationContext, final Consumer<MetafixToDo> consumer) {
            final MetafixToDo annotation = invocationContext.getExecutable().getAnnotation(MetafixToDo.class);

            if (annotation != null) {
                annotationPresent = true;
                consumer.accept(annotation);
            }
        }

    }

    class Handler implements ExecutionCondition {

        private static final Class<Extension> EXTENSION_CLASS = Extension.class;
        private static final String EXTENSION_NAME = EXTENSION_CLASS.getTypeName();

        private static final Map<Class<?>, Boolean> EXTENSION_PRESENT = new HashMap<>();

        private Handler() {
        }

        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
            final boolean extensionPresent = EXTENSION_PRESENT.computeIfAbsent(context.getTestClass().get(), k -> {
                for (final ExtendWith annotation : k.getAnnotationsByType(ExtendWith.class)) {
                    for (final Class<? extends org.junit.jupiter.api.extension.Extension> extensionClass : annotation.value()) {
                        if (extensionClass.isAssignableFrom(EXTENSION_CLASS)) {
                            return true;
                        }
                    }
                }

                return false;
            });

            if (extensionPresent) {
                return ConditionEvaluationResult.enabled("Extension present: " + EXTENSION_NAME);
            }
            else {
                Assertions.fail("Extension missing: " + EXTENSION_NAME);
                return null; // not reached
            }
        }

    }

}
