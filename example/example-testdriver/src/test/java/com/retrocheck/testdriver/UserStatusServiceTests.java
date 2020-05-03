package com.retrocheck.testdriver;

import com.retrocheck.convenience.*;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.retrocheck.graph.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@SpringBootTest
class UserStatusServiceTests {

	// Redis allows RetroCheck to know when assertions in the system under test have failed,
	// and when tests have ended.
	private Redis redis = new Redis("redis://localhost:6379");

	// This is a dependency of the system under test.  It's where the status (logged in
	// or not) of users is stored.
	private MemcachedClient memcached = new XMemcachedClient("localhost",11211);
	// This is an object that the tests use to send HTTP requests to the system under test.
	private TestAppClient testAppClient = new TestAppClient();

	UserStatusServiceTests() throws IOException {}

	@Test
	void test() {

		// We use Generators to generate (pseudorandomly) instances of various types.
		// DefaultGenerator has generators for primitive types, and others can be added
		// by using the ".with()" method.
		DefaultGenerator generator =
				new DefaultGenerator()
						.with(
								UserStatus.class,
								(random, status) -> new UserStatus(random.nextInt(), random.nextBoolean()));

		// Each entity in the system is represented by a Node.
		Node<Integer> requestId =
				new Node<>(
						"request id", // Used to identify each entity.
						Integer.class, // The type of each entity -- required due to type erasure.
						generator, // This is how the Node type knows how to generator Integer instances.
						"http", // Tells the DataLoader how to load instances of this entity.
						true, // Is this entity used to invoke the system?
						true); // Does this entity need to be deleted at the end of each test?
		Node<UserStatus> userStatus =
				new Node<>(
						"user status",
						UserStatus.class,
						generator,
						"memcached");

		List<Node<?>> nodes = Arrays.asList(requestId,	userStatus);

		List<Edge<?, ?>> edges =
					Arrays.asList(
							new Edge<>(
									// This edge goes from the requestId node...
									requestId,
									// ...to the userStatus node.
									userStatus,
									// A constraint that's placed on our two nodes:
									// v (userStatus) must have the same Id as u (requestId).
									(u, v) -> v.withId(u),
									// The probability with which the above constraint is satisfied.
									new Probability(50)));

		Map<String, Function<?, ?>> loader = new HashMap<>();

		// This tells the DataLoader how to load UserStatus entities into memcached.
		loader.put("memcached", (UserStatus entity) -> {
			try {
				return memcached.set(entity.getUserId().toString(), 60, entity.isLoggedIn());
			} catch (TimeoutException | InterruptedException | MemcachedException e) {
				throw new RuntimeException(e);
			}
		});

		// This tells the DataLoader how to execute HTTP requests.
		loader.put("http", (Integer entity) -> testAppClient.get(entity));

		// This tells the DataLoader how to unload UserStatus entities from memcached
		// at the end of each test.
		Map<String, Function<?, ?>> unloader = new HashMap<>();
		unloader.put("memcached", (UserStatus entity) -> {
			try {
				return memcached.delete(entity.getUserId().toString());
			} catch (TimeoutException | InterruptedException | MemcachedException e) {
				throw new RuntimeException(e);
			}
		});

		DefaultDataLoader dataLoader = new DefaultDataLoader(loader, unloader, redis);

		// This object orchestrates the entire test.  Note that its preprocess, process, and postprocess
		// methods must all be called, and in that order.  Preprocess and postprocess should be called
		// once per test, whereas process may be called an arbitrary number of times per test.
		DefaultTester tester = new DefaultTester("RetroCheck Example");
		DefaultGraph graph = new DefaultGraph().withNodes(nodes).withEdges(edges);
		tester.preprocess(graph);

		// You can call this method in a loop, because each time it's called, it will generate
		// a new data model, load it into the system under test (using DataLoader), invoke the
		// system under test (using Nodes marked as entry points), wait for the test to end (using
		// Redis), and unload the data model from the system under test.
		for (int i = 0; i < 99; i++) {
			System.out.println("Test iteration: " + i);

			TestResult result =
					tester.process(
							dataLoader::orchestrate, // Lambda defining how data is loaded into and unloaded
							// from the system under test.
							new Outcome("userStatus")); // This Outcome object tells DefaultTester
							// the name of the assertion which indicates the end of a test.

			// DefaultTester.process returns a TestResult object, which contains a list of assertions
			// which failed during the test (if any), and the seed used to generate data during the test.
			if (result.hasFailures()) {
				System.out.println("Test failed with seed: " + result.getSeed());
			}
		}

		// This generates visualization files, for use in understanding and debugging your data model and various instances of it.
		tester.postprocess();
		// This disposes of the test driver's connection to Redis.
		dataLoader.destroy();
	}
}
