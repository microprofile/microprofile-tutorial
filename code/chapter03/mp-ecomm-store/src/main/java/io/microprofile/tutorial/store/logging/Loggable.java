package io.microprofile.tutorial.store.logging;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Target;

@InterceptorBinding
@Retention(RUNTIME)
@Target({METHOD})
public @interface Loggable {

}
