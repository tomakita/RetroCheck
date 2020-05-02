import com.retrocheck.mock.Actions;
import com.retrocheck.mock.MockException;
import com.retrocheck.mock.MockWith;
import com.retrocheck.mock.Tuple;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public aspect MockAspect {
    pointcut mockable(MockWith mockWith): execution(* *.*(..)) && @annotation(mockWith);

    Object around(MockWith mockWith) : mockable(mockWith) {
        if (!Actions.areMocksEnabled) {
            return proceed(mockWith);
        }

        Signature calledSignature = thisJoinPoint.getSignature();
        String mockMethodName = calledSignature.getName();
        Class mockClass = mockWith.value();

        MethodSignature methodUnderTestSignature = (MethodSignature)calledSignature;
        Class[] signatureTypes = methodUnderTestSignature.getParameterTypes();

        try {
            Method mockMethod = mockClass.getMethod(mockMethodName, signatureTypes);
            Object[] calledArguments = thisJoinPoint.getArgs();
            List<Tuple<String, Object>> signatureTypesAndValues = IntStream.range(0, calledArguments.length).mapToObj(i -> new Tuple<String, Object>(signatureTypes[i].getName(), calledArguments[i])).collect(Collectors.toList());

            try {
                return mockMethod.invoke(Actions.getServiceLocator().apply(mockClass), calledArguments);
            }
            catch (Exception ex) {
                throw new MockException(calledSignature.toLongString(), signatureTypesAndValues.toArray(), ex);
            }
        } catch (MockException ex) {
            System.out.println("Error during execution of mock: " + ex.signature + ", inputs: " + Arrays.toString(ex.parametersWithValues));
        }
        catch (Exception ex) {
            System.out.println("An exception was encountered during mock execution that was not handled gracefully: " + Arrays.toString(ex.getStackTrace()));
        }

        return null;
    }
}