package edu.cwru.csds341.vapor.common;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/** A condition to enforce on a string, and a message to output when that condition is not met. */
public interface Requirement {
    boolean accepts(String str);

    String getMessage();


    class LengthReq implements Requirement {
        final int minLength;
        final int maxLength;

        public LengthReq(int minLength, int maxLength) {
            this.minLength = minLength;
            this.maxLength = maxLength;
        }

        @Override
        public boolean accepts(String str) {
            return false;
        }

        @Override
        public String getMessage() {
            return String.format("Input length must be between %d and %d characters long.", minLength, maxLength);
        }
    }

    /**
     * Requirements that may be common
     */
    enum SimpleReq implements Requirement {
        NONEMPTY(Predicate.not(String::isEmpty), "value cannot be empty"),
        ;

        SimpleReq(Predicate<String> predicate, String message) {
            this.predicate = predicate;
            this.message = message;
        }
        SimpleReq(Pattern pattern, String message) {
            this.predicate = pattern.asMatchPredicate();
            this.message = message;
        }

        private final Predicate<String> predicate;
        private final String message;


        @Override
        public boolean accepts(String str) { return predicate.test(str); }

        @Override
        public String getMessage() { return message; }
    }

}
