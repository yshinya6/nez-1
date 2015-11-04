package nez.schema;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { TYPE, FIELD })
public @interface Schematic {
}

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { TYPE })
@interface Ordered {
}

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { TYPE })
@interface Unordered {
}

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { FIELD })
@interface Option {
}

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { FIELD })
@interface Range {
	int max = 0;
	int min = 0;
}

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { FIELD })
@interface Length {
	int max = 0;
	int min = 0;
}

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { FIELD })
@interface Enumeration {
	String[] value();
}
