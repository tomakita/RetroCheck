package com.retrocheck.assertion;

import org.aspectj.lang.Signature;
import java.util.List;

public interface AssertionExceptionCallback {
    void execute(Signature calledSignature, List<Tuple<String, Object>> signatureTypesAndValues, Object target, Exception ex);
}
