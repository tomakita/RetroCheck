package com.retrocheck.assertion;

import org.aspectj.lang.Signature;
import java.util.ArrayList;

public interface GeneralExceptionCallback {
    void execute(Signature calledSignature, ArrayList<Object> calledArgumentsWithReturnValue, Object target, Exception ex);
}
