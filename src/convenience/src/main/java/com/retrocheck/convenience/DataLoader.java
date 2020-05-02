package com.retrocheck.convenience;

import com.retrocheck.graph.Workflow;

public interface DataLoader {
    TestResult orchestrate(Workflow workflow);
    void start(Workflow workflow);
    void insert(Workflow workflow);
    void delete(Workflow workflow);
}
