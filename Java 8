Functional Interfaces:
A functional interface is an interface that contains exactly one abstract method (SAM - Single Abstract Method). It can have multiple default or static methods, but only one abstract method.

@FunctionnalInterface
public interface Calculator{
	int calculate(int a, int b);
	
	//default methods are allowed
	default void printResult(int result){
		System.out.println("result : " + result);
	}
	
	//static methods are allowed
	static String getDescription(){
		return "Simple calculator";
	}
}

Built-in Functional Interfaces (java.util.function package):
// 1. Predicate<T> - Takes one argument, returns boolean
Predicate<Integer> isEven = num -> num % 2 == 0;
System.out.println(isEven.test(4)); // true

// 2. Function<T, R> - Takes one argument, returns result
Function<String, Integer> stringLength = str -> str.length();
System.out.println(stringLength.apply("Hello")); // 5

// 3. Consumer<T> - Takes one argument, returns nothing
Consumer<String> printer = msg -> System.out.println(msg);
printer.accept("Hello World");

// 4. Supplier<T> - Takes no argument, returns result
Supplier<Double> randomValue = () -> Math.random();
System.out.println(randomValue.get());

// 5. BiFunction<T, U, R> - Takes two arguments, returns result
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
System.out.println(add.apply(5, 3)); // 8

// 6. UnaryOperator<T> - Special Function where input and output are same type
UnaryOperator<String> toUpperCase = str -> str.toUpperCase();
System.out.println(toUpperCase.apply("hello")); // HELLO

// 7. BinaryOperator<T> - Special BiFunction where both inputs and output are same type
BinaryOperator<Integer> multiply = (a, b) -> a * b;
System.out.println(multiply.apply(4, 5)); // 20
```

**Lambda Expressions:**

Lambda expressions provide a concise way to represent anonymous functions (methods without names).

**Syntax:**
```
(parameters) -> expression
or
(parameters) -> { statements; }

Lambda expressions: -> used to write anaonymous function
provides an concise way/proper way to write/represent anonymous function
// 1. No parameters
Runnable runnable = () -> System.out.println("Running");

// 2. Single parameter (parentheses optional)
Consumer<String> print = message -> System.out.println(message);
// or with parentheses
Consumer<String> print2 = (message) -> System.out.println(message);

// 3. Multiple parameters
BiFunction<Integer, Integer, Integer> sum = (a, b) -> a + b;

// 4. With type declarations
BiFunction<Integer, Integer, Integer> sum2 = (Integer a, Integer b) -> a + b;

// 5. Multi-line lambda with return statement
Function<List<Integer>, Integer> sumList = (numbers) -> {
    int total = 0;
    for (int num : numbers) {
        total += num;
    }
    return total;
};

// 6. Method reference (shorthand for lambda)
// Lambda: str -> System.out.println(str)
// Method reference: System.out::println
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
names.forEach(System.out::println);

// Static method reference
Function<String, Integer> parser = Integer::parseInt;

// Instance method reference
String str = "Hello";
Supplier<Integer> lengthSupplier = str::length;

// Constructor reference
Supplier<List<String>> listSupplier = ArrayList::new;

// Example 1: Filtering and processing employees
class Employee {
    private String name;
    private double salary;
    private String department;
    
    // Constructor, getters, setters...
}

List<Employee> employees = getEmployees();

// Find high-earning engineers
List<Employee> highEarningEngineers = employees.stream()
    .filter(emp -> "Engineering".equals(emp.getDepartment()))
    .filter(emp -> emp.getSalary() > 100000)
    .collect(Collectors.toList());

// Calculate average salary using lambda
double avgSalary = employees.stream()
    .mapToDouble(Employee::getSalary)
    .average()
    .orElse(0.0);

// Example 2: Custom comparator with lambda
List<Employee> sortedByName = employees.stream()
    .sorted((e1, e2) -> e1.getName().compareTo(e2.getName()))
    .collect(Collectors.toList());

// Or using method reference
List<Employee> sortedBySalary = employees.stream()
    .sorted(Comparator.comparing(Employee::getSalary))
    .collect(Collectors.toList());

// Example 3: Event handling
button.addActionListener(event -> {
    System.out.println("Button clicked!");
    processClick();
});

// Example 4: Thread creation
Thread thread = new Thread(() -> {
    for (int i = 0; i < 5; i++) {
        System.out.println("Thread running: " + i);
    }
});
thread.start();

// Example 5: Custom functional interface
@FunctionalInterface
interface Validator<T> {
    boolean validate(T value);
}

Validator<String> emailValidator = email -> 
    email != null && email.contains("@") && email.contains(".");

System.out.println(emailValidator.validate("test@example.com")); // true

// Example 6: Combining predicates
Predicate<Integer> isPositive = num -> num > 0;
Predicate<Integer> isEven = num -> num % 2 == 0;
Predicate<Integer> isPositiveAndEven = isPositive.and(isEven);

System.out.println(isPositiveAndEven.test(4));  // true
System.out.println(isPositiveAndEven.test(-4)); // false
