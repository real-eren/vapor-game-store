package edu.cwru.csds341.vapor.common;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class ActionTest {

    @Test
    public void shortNamesAreUnique() {
        Map<String, Action> actionShortNames = new TreeMap<>();
        for (Action action : Action.values()) {
            assertFalse(
                    actionShortNames.containsKey(action.shortName),
                    String.format(
                            "short name '%s' is used for both %s and %s.",
                            action.shortName,
                            action,
                            actionShortNames.get(action.shortName)
                    )
            );
            actionShortNames.put(action.shortName, action);
        }
    }


}