package edu.cwru.csds341.vapor.cli;

/** A condition to enforce on a string, and a message to output when that condition is not met. */
public interface Requirement {
    boolean accepts(String str);

    String getMessage();
}
