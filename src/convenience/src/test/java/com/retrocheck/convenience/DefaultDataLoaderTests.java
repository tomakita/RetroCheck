package com.retrocheck.convenience;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.When;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.runner.RunWith;
import com.retrocheck.graph.Entity;
import com.retrocheck.graph.Workflow;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class DefaultDataLoaderTests {
    // property: orchestrate doesn't time out as long as there is a reachable outcome.
    // property: orchestrate returns a list of all assertion failures
    @Property
    public void dataLoader_onExpectedOutcome_stopsImmediately(
            @From(EventualWorkflowGenerator.class) @When(seed = 891989781395534172L) Workflow workflow,
            @From(TestListenerGenerator.class) @When(seed = 4932745122209453837L) TestListener listener) throws InterruptedException {
        Map<String, Function<?, ?>> inserts = workflow.getDataSetup().stream().map(Entity::getLoaderName).distinct().collect(Collectors.toMap(name -> name, name -> x -> x));
        Map<String, Function<?, ?>> deletes = workflow.getDataSetup().stream().map(Entity::getLoaderName).distinct().collect(Collectors.toMap(name -> name, name -> x -> x));

        DefaultDataLoader loader = new DefaultDataLoader(inserts, deletes);
        loader.setTimeoutMillis(1000);

        try {
            TestResult result = loader.orchestrateWithListener(workflow, listener, listener::start);
            long expectedFailureCount = listener.getTriggerSequence().stream().filter(trigger -> trigger == TestListener.FAILURE_EVENT_INDEX).count();
            assertEquals(result.getAssertionResults().size(), expectedFailureCount);
        } catch (ConditionTimeoutException ex) {
            List<String> assertionMethodNames = listener.getResultSequence().stream().map(result -> result.getMethodName()).collect(Collectors.toList());
            String outcome = workflow.getOutcome().getName();
            boolean isOutcomeAbsent = !assertionMethodNames.contains(outcome);

            boolean isContinuationHanging = true;
            if (!isOutcomeAbsent) {
                int finalOutcomeIndex = assertionMethodNames.lastIndexOf(outcome);
                String finalContinuation = listener.getResultSequence().get(finalOutcomeIndex).getContinuation();
                for (int i = finalOutcomeIndex + 1; i < assertionMethodNames.size(); i++) {
                    if (assertionMethodNames.get(i).equals(finalContinuation)) {
                        isContinuationHanging = false;
                        break;
                    }
                }
            }

            assertTrue(isOutcomeAbsent || isContinuationHanging);
        }
    }

    // property: all items inserted are also deleted
    @Property
    public void dataLoader_insertsAndDeletesInTheCorrectOrder(
            @From(ImmediateWorkflowGenerator.class)Workflow workflow,
            @From(TestListenerGenerator.class) TestListener listener) {
        // This guarantees that the data loader will complete.
        listener.getResultSequence().add(new AssertionResultStrings("", "", "", "", true));

        final AtomicLong state = new AtomicLong();
        Map<String, Function<?, ?>> inserts = workflow.getDataSetup().stream().map(Entity::getLoaderName).distinct().collect(Collectors.toMap(name -> name, name -> x -> state.addAndGet((int)x)));
        Map<String, Function<?, ?>> deletes = workflow.getDataSetup().stream().map(Entity::getLoaderName).distinct().collect(Collectors.toMap(name -> name, name -> x -> state.addAndGet(-((int)x))));
        DefaultDataLoader loader = new DefaultDataLoader(inserts, deletes);
        loader.setTimeoutMillis(1000);

        loader.orchestrateWithListener(workflow, listener, listener::start);

        assertEquals(state.get(), 0);
    }
}
