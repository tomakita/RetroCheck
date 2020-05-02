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

	// [Redis]: TODO: not required, can also use for k/v storage
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

		// [Generators]: TODO: uniqueness, unification, Generator interface
		// We use Generators to generate (pseudorandomly) instances of various types.
		// DefaultGenerator has generators for primitive types, and others can be added
		// by using the ".with()" method.
		DefaultGenerator generator =
				new DefaultGenerator()
						.with(
								UserStatus.class,
								(random, status) -> new UserStatus(random.nextInt(), random.nextBoolean()));

		// [Nodes]: TODO: other overloads
		// Each entity in the system is represented by a Node.
		Node<Integer> requestId =
				new Node<>(
						"request id", // Used to identify each entity.
						Integer.class, // The type of each entity -- required due to type erasure.
						x -> x,
						generator, // This is how the Node type knows how to generator Integer instances.
						"http", // Tells the DataLoader how to load instances of this entity.
						true, // Is this entity used to invoke the system?
						Probability.ALWAYS, // TODO: get rid of this since it's the default val. With what probability should this entity be present in tests?
						true);
		Node<UserStatus> userStatus =
				new Node<>(
						"user status",
						UserStatus.class,
						generator,
						"memcached");

		List<Node<?>> nodes = Arrays.asList(requestId,	userStatus);

		// [Edges]: TODO: multiedges, other overloads
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
									// TODO: Probability.ALWAYS
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

		// [DataLoaders]: TODO: truncater, entry points, when data is inserted/deleted/truncated
		DefaultDataLoader dataLoader = new DefaultDataLoader(loader, unloader, redis);

		// [Testers]: TODO: orchestrates the entire test, pre/process/post must be run in that order.
		DefaultTester tester = new DefaultTester("RetroCheck Example");
		// [Graphs]: TODO: acyclic, subgraphs, subgraph archetypes, seeding
		DefaultGraph graph = new DefaultGraph().withNodes(nodes).withEdges(edges);
		tester.preprocess(graph);

		// You can call this method in a loop, because each time it's called, it will generate
		// a new data model, load it into the system under test (using DataLoader), invoke the
		// system under test (using Nodes marked as entry points), wait for the test to end (using
		// Redis), and unload the data model from the system under test.
		for (int i = 0; i < 99; i++) {
			System.out.println("Test iteration: " + i);
			// [TestResults]: TODO: use the return value of this method to check for failures -- actually add
			//       				   that to the code here.  show how to get the seed on failure!
			TestResult result = tester.process(dataLoader::orchestrate, new Outcome("userStatus"));
		}

		// [Visualization]: TODO: generates visualization files, where it's written to, how to use it
		tester.postprocess();
		// This disposes of the test driver's connection to Redis.
		dataLoader.destroy();
	}
}
