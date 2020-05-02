package com.retrocheck.convenience;

import com.retrocheck.convenience.AssertionEvent;
import com.retrocheck.convenience.AssertionResultStrings;
import com.retrocheck.convenience.Listener;

import java.util.List;

public class TestListener implements Listener {
    public static int SUCCESS_EVENT_INDEX = 0;
    public static int FAILURE_EVENT_INDEX = 1;
    public static int COMPLETION_EVENT_INDEX = 2;

    private AssertionEvent[] assertionEvents = new AssertionEvent[] { new AssertionEvent(), new AssertionEvent(), new AssertionEvent() };
    private List<Integer> triggerSequence; // failure count is just this .filter(x -> x == 1)
    private List<AssertionResultStrings> resultSequence; // check to see if we used first instance of outcome is just checking observed sequence against prefix of this list

    public TestListener(List<Integer> triggerSequence, List<AssertionResultStrings> resultSequence) {
        this.triggerSequence = triggerSequence;
        this.resultSequence = resultSequence;
    }

    @Override
    public AssertionEvent getAssertionSuccessEvent() {
        return assertionEvents[0];
    }

    @Override
    public AssertionEvent getAssertionFailureEvent() {
        return assertionEvents[1];
    }

    @Override
    public AssertionEvent getInvocationCompletionEvent() {
        return assertionEvents[2];
    }

    @Override
    public void start() {
        for (int i = 0; i < triggerSequence.size(); i++) {
            int trigger = triggerSequence.get(i);
            AssertionResultStrings result = resultSequence.get(i);
            assertionEvents[trigger].trigger(result);
        }
    }

    @Override
    public void destroy() {

    }

    public List<Integer> getTriggerSequence() {
        return triggerSequence;
    }

    public List<AssertionResultStrings> getResultSequence() {
        return resultSequence;
    }
}
