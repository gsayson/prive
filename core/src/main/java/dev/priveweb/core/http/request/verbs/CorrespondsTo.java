package dev.priveweb.core.http.request.verbs;

import dev.priveweb.core.http.request.RequestMethod;

import java.lang.annotation.*;

/**
 * Marks a {@code [XXX]Request} annotation as corresponding
 * to a particular HTTP verb, listed in {@link RequestMethod}.
 * <p>
 *      This does not have any semantic meaning;
 * 		This is only a marker annotation.
 * </p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
@Repeatable(CorrespondsToContainer.class)
@interface CorrespondsTo {

	RequestMethod value();

}

// internal
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
@interface CorrespondsToContainer {
	CorrespondsTo[] value();
}