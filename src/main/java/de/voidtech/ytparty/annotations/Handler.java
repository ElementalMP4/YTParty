package main.java.de.voidtech.ytparty.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component //Tells spring that our new annotation will be a Component stereotype.
@Target({ElementType.TYPE}) //This allows the annotation to be used by types (Classes!)
@Retention(RetentionPolicy.RUNTIME) //This tells the compiler to
									//record annotations in the class file so they can be found by spring.
public @interface Handler { //This tells the java compiler that this is an annotation, not a class.

}

