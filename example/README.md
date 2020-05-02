# Example

As an example of pbm's style of testing, we'll do a test of a small Spring Boot application called UserStatusService.

## Running the Example System

Requirements:
- Docker
- Docker Compose
- JRE 1.8

From `/example`, run `docker-compose up -d`.  This starts the memcached and Redis containers that the example system depends on.  Then start `example-app` by running `./gradlew bootRun` (or in an IDE), and run the test in `example-testdriver` by running `./gradlew test` (or in an IDE).

## Overview of `example-app`

On `/userstatus`, UserStatusService serves requests for a user's status -- whether or not a user is logged in.  UserStatusService does this by querying memcached with the user's ID.

We'll write a pbm assertion for `/userstatus`.  It only requires two changes to the code under test (not including the assertion code itself).  The first change is an annotation that we add to the method we want to test, and the second change is a method call that configures the application to use pbm.

SNIPPET FROM example-app/build.gradle GOES HERE

Here is the controller that implements `/userstatus`:

UserStatusService/UserStatusController.java:
```java
@RestController
public class UserStatusController {

    private MemcachedClient memcached;

    public UserStatusController(@Autowired MemcachedClient memcached) {
        this.memcached = memcached;
    }

    @RequestMapping("/userstatus")
    // The MonitorWith annotation tells pbm where to find the assertion for
    // the userStats method.
    @MonitorWith(UserStatusControllerAssertions.class)
    public UserStatus userStatus(@RequestParam(value="userId") Integer userId)
            throws InterruptedException, MemcachedException, TimeoutException {

        return findUserStatus(userId);
    }

    // This method queries for the status of a user, by keying into
    // memcached with the user's ID.  If the user isn't found in memcached,
    // this method returns null.  Otherwise, it returns the user's status
    // wrapped in a UserStatus object.
    private UserStatus findUserStatus(Integer userId)
            throws InterruptedException, MemcachedException, TimeoutException {

        Boolean isLoggedIn  = memcached.get(userId.toString());
        if (isLoggedIn != null) {
            return new UserStatus(userId, isLoggedIn, Instant.now());
        } else {
            return null;
        }
    }
}
```

Here is the assertion that checks to make sure `/userstatus` is working correctly:

UserStatusService/UserStatusControllerAssertions.java:
```java
@Component
public class UserStatusControllerAssertions {

    private MemcachedClient memcached;

    public UserStatusControllerAssertions(@Autowired MemcachedClient memcached) {
        // You can use objects from Spring's IoC container in assertion code.
        this.memcached = memcached;
    }

    // This is an assertion for the UserStatusController.userStatus method.
    // It returns a boolean, and has access to UserStatusController.userStatus's
    // argument list, as well as the value it returned, and the UserStatusController
    // instance on which it was invoked.
    // Assertions are invoked by pbm as the system under test runs, and their
    // return values are sent to Redis for use by test running code.
    public boolean userStatus(Integer userId,
                              UserStatus result,
                              UserStatusController instance)
            throws InterruptedException, MemcachedException, TimeoutException {

        // We check to see if the user has a status in memcached.  If it does,
        // we make sure that status matches what is being returned by /userstatus.
        // Otherwise, we make sure that /userstatus is returning null.
        Boolean isLoggedIn  = memcached.get(userId.toString());
        if (isLoggedIn != null) {
            return result.isLoggedIn() == isLoggedIn;
        } else {
            return result == null;
        }
    }
}
```

And here is the code that configures the application to use pbm (the code in this file also configures memcached):

UserStatusService/MemcachedConfig.java:
```java
@Configuration
public class MemcachedConfig {

    public MemcachedConfig(@Autowired ApplicationContext context) {

        // This configures pbm to emit metadata to Redis as assertions
        // succeed and fail.  This metadata will be used by our test driver,
        // which will also be connected to Redis.
        ResultEmitter.connect(
                "redis://localhost:6379", // Address of Redis.
                false,
                context::getBean); // Service locator method.
    }

    @Bean
    public MemcachedClient memcached() throws IOException {

        return new XMemcachedClient("localhost",11211);
    }
}
```

## Overview of `example-testdriver`

Here is the test driver code that invokes the `/userstatus` end point in UserStatusService, which will invoke its assertion code, as well.  When the assertion we've written in UserStatusService succeeds or fails, messages will be put onto Redis pub-sub channels, and our test driver will be notified.  This test driver code runs as a junit test.

SNIPPET FROM example-testdriver/build.gradle GOES HERE

TestDriver/UserStatusServiceTests.java:
```java
@SpringBootTest
class UserStatusServiceTests {

	// Redis allows pbm to know when assertions in the system under test have failed,
	// and when tests have ended.
	private Redis redis = new Redis("redis://localhost:6379");

	// This is a dependency of the system under test.  It's where the status (logged in
	// or not) of users is stored.
	private MemcachedClient memcached = new XMemcachedClient("localhost",11211);
	// This is an object that the tests use to send HTTP requests to the system under test.
	private TestAppService service = new TestAppService(new RestTemplateBuilder());

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
						x -> x,
						generator, // This is how the Node type knows how to generator Integer instances.
						"http", // Tells the DataLoader how to load instances of this entity.
						true, // Is this entity used to invoke the system?
						Probability.ALWAYS, // With what probability should this entity be present in tests?
						true);
		Node<UserStatus> userStatus =
				new Node<>(
						"user status",
						UserStatus.class,
						generator,
						"memcached");

		List<NodeShape<?>> nodes = new ArrayList<>(Arrays.asList(requestId,	userStatus));

		List<EdgeShape<?, ?>> edges =
				new ArrayList<>(
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
										new Probability(50))));

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
		loader.put("http", (Integer entity) -> service.get(entity));

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

		DataLoader dataLoader = new DataLoader(loader, unloader, redis);

		Tester<NodeShape<?>, EdgeShape<?, ?>> tester = new Tester<>("pbm example");
		Graph<NodeShape<?>, EdgeShape<?, ?>> graph = new Graph<>().withNodes(nodes).withEdges(edges);
		tester.preprocess(graph);

		// You can call this method in a loop, because each time it's called, it will generate
		// a new data model, load it into the system under test (using DataLoader), invoke the
		// system under test (using Nodes marked as entry points), wait for the test to end (using
		// Redis), and unload the data model from the system under test.
		for (int i = 0; i < 99; i++) {
			// userStatus is the name of the assertion whose execution signifies the end of a test.
			tester.process(dataLoader::orchestrateConveniently, "userStatus");
		}

		tester.postprocess();
		dataLoader.destroy();
	}
}
```