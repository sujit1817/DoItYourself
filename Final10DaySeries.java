//synechron java buddy list
//reverse a String
public class Main
{
    public static void reverseString(String str){
        char[] arr = str.toCharArray();
        // String reversed = "";
        // for(int i = arr.length -1; i>=0 ; i--){
        //     reversed = reversed + charr;
        // }
        // System.out.println(reversed);
        
        int start = 0;
        int end = arr.length - 1;
        
        while(start < end){
            char temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
        String result = new String(arr);
        System.out.println(result);
    }
    
	public static void main(String[] args) {
	    String str =  "hello";
		reverseString(str);
	}
}
//synechron 
//Given a list of Integers, find the maximum value element present in 
//it using Stream functions
import java.util.*;

public class Main
{
	public static void main(String[] args) {
	    List<Integer> list = Arrays.asList(5,2,8,1);
	    int max = list.stream().max((a,b) -> a.compareTo(b)).get();
	    //list.stream().max(Integer::compare).get();
	    //get is used because max returns optional
	    
	    //or
	    //list.stream().max(Integer::compareTo).orElse(0); 
	    
	    System.out.println("max: "+max);
	    
	    int min = list.stream().min((a, b) -> a.compareTo(b)).get();
	    System.out.println("max: "+min);
	}
}


//Singleton ensures that only one instance of a class is created and provides a global access point to that instance.
//1. Logging
//2. Configuration classes
//3. Database connections

//Create singleton
//Eager
public class EagerSingleton{
	
	private static final EagerSingleton instance = new EagerSingleton();
	
	private EagerSingleton(){
	}
	
	public static EagerSingleton getInstance(){
		return instance;
	}
}

//Lazy
public class LazySingleton{
	private static LazySingleton instance;
	
	private LazySingleton(){		
	}
	
	public static LazySingleton getInstance(){
		if(instance==null){
			 instance = new LazySingleton();
		}
		return instance;
	}
}

//Thread safe
public class ThreadSafeSingleton{
	private static volatile ThreadSafeSingleton instance;
	
	private ThreadSafeSingleton(){}
	
	public static synchronized ThreadSafeSingleton getInstance() {
        if (instance == null) {
            instance = new ThreadSafeSingleton();
        }
        return instance;
    }
}

//Double checked
public class DoubleCheckedLocking{
	private static volatile DoubleCheckedLocking instance;
	
	private DoubleCheckedLocking(){}
	
	public static DoubleCheckedLocking getInstance() {
        if (instance == null) {
            synchronized (DoubleCheckedLocking.class) {
                if (instance == null) {
                    instance = new DoubleCheckedLocking();
                }
            }
        }
        return instance;
    }
}

//Remove Duplicates from Sorted Array : Use 2 Pointer 
import java.util.*;
public class Main {

    public static void removeDuplicates(int[] nums) {
        // Edge case
        if (nums == null || nums.length == 0) {
            System.out.println("empty array");
        }

       //
       int i = 0;
       for(int j = 1; j < nums.length; j++){
           if(nums[i]!=nums[j]){
               i++;
               nums[i]=nums[j];
           }
           
           
       }
       for(int k = 0; k<=i;k++){
            System.out.print(" "+nums[k]);
       }
      // System.out.println("Unique count: " +Arrays.toString(nums));
    }

    public static void main(String[] args) {
        int[] nums = {1, 1, 2, 2, 3};
        removeDuplicates(nums);

        
    }
}


//deque implementation
import java.util.*;

public class Main
{
	public static void main(String[] args) {
		Deque<Integer> deque = new LinkedList<>();
		deque.addFirst(1);//[1]
		deque.addFirst(2);//[2,1]
		deque.addLast(3);//[2,1,3]
		deque.addLast(4);//[2,1,3,4]
		System.out.println(deque+" ");
		
		deque.removeFirst();// first element will be removed
		System.out.println(deque+" ");
		deque.removeLast();//Last element will be removed
		
		System.out.println(deque+" ");
		
		System.out.println("first el = "+deque.getFirst());
		System.out.println("last el = "+deque.getLast());
		
	}
}

//Binary format of 4 + count number of zeroes
import java.util.*;

public class Main
{
	public static void main(String[] args) {
	  int num = 4;
	  String binary = Integer.toBinaryString(num);
	  System.out.println("Binary: "+binary);
	  
	  int zeroCount = 0;
	  int oneCount = 0;
	  
	  for(char c : binary.toCharArray()){
	      if(c == '0'){
	          zeroCount++;
	      }else {
	      oneCount++;
	      }
	  }
	  
	  System.out.println("zeroCount : "+zeroCount);
	  System.out.println("oneCount : "+oneCount);
	}
}

//Palindrome of Given String
import java.util.*;

public class Main
{
	public static void main(String[] args) {
		String str = "madam";
		String reversed = new StringBuilder(str).reverse().toString();

		if(str.equals(reversed)) {
			System.out.println("Palindrome");
		} else {
			System.out.println("not Palindrome");
		}
	}
}

//static and instance counter
//static class level
//instance - per object level
//instance in heap
//static in method area
import java.util.*;

public class Main
{
	int instanceCounter = 0; //per Object
	static int staticCount = 0; //shared across all Objects

	Main() {
	//	instanceCounter++;
		staticCount++;
	}
	
	public static int getCount(){
	    return staticCount;
	}
	
	public int getInstanceCount(){
	    return instanceCounter;
	}
	
	public static void main(String[] args) {
		new Main();
		new Main();
    Main m = new Main();

    System.out.println(getCount()); 
    System.out.println(m.getInstanceCount()); 
	}
}



//singly : last node
class Node{
	int data;
	Node next = null;
}


//Circular : last node points back to head
Node head = new Node(1);
Node second = new Node(2);
head.next = second;
second.next = head;//circular


nth highest sal

select salary 
from (
select salary,
		  DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk
	FROM employees
	) ranked
where rnk = N; --replace N with desired rank (2, 3, etc.)

//Check if array is sorted (no inbuilt functions)
import java.util.*;

public class Main
{
	public static boolean isSorted(int[] arr) {
		if(arr == null || arr.length <= 1) {
			return true;
		}
		for(int i = 0; i<arr.length-1; i++) {
			if(arr[i]>arr[i+1]) {
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		int[] a = {1, 3, 5, 7};    // isSorted  true
		int[] b = {1, 5, 3, 7};    // isSorted  false
		System.out.println(isSorted(a));
		System.out.println(isSorted(b));
	}
}


write a java program that takes list of integers as input and returns the squares of all the odd numbers in the list using stream api
import java.util.Arrays;
import java.util.List;

public class OddSquares {
    public static void main(String[] args) {
        
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        nums.stream()
            .filter(n -> n % 2 != 0)   // filter odd numbers
            .map(n -> n * n)           // square them
            .forEach(System.out::println); // print result
    }
}
nums.stream().filter(n -> n%2!=0).map(n -> n*n).forEach(System.out::println);

/*Zemoso*/
//first non repeating char
import java.util.*;
import java.util.stream.*;

public class FirstNonRepeatingCharms {
    public static void main(String[] args) {
        String str = "character";

        Map<Character, Long> result = str.chars() //instream  need to convert to obj
            .mapToObj(c -> (char)c)
            .collect(Collectors.groupingBy(c -> c, LinkedHashMap::new, Collectors.counting()));
            
            System.out.println(result);
            //.stream().filter(c -> )
        
        for(Map.Entry<Character, Long> zemo : result.entrySet()){
            if(zemo.getValue() == 1){
                System.out.print(zemo.getKey());
                break;
            }
        }
    }
}

import java.util.*;
import java.util.stream.*;
public class FirstNonRepeatingCharStreams {
    public static void main(String[] args) {
        String str = "character";

        //Map<Character, Long> result =
        char ch =
        str.chars() //instream  need to convert to obj
            .mapToObj(c -> (char)c)
            .collect(Collectors.groupingBy(
                c -> c, 
                LinkedHashMap::new, 
                Collectors.counting()))
            .entrySet()
            .stream()
            .filter(e -> e.getValue() == 1)
            .map(Map.Entry::getKey)
            .findFirst().get();
            System.out.println(ch);
    }
}

JVM Heap Memory Flags: -Xmx110000m -Xms2048m

What These Flags Mean
FlagFull Form Value Meaning -Xms2048m Xmemory start 2048 MB = 2 GB Initial heap size when JVM starts-Xmx110000m Xmemory max110000 MB ≈ 107 GB Maximum heap size JVM can grow to

JVM Starts
    └─ Heap allocated = 2 GB  (-Xms2048m)
           │
           │  (app runs, creates objects, heap fills up)
           │
           ▼
    JVM requests more memory from OS
           │
           │  (keeps growing as needed)
           │
           ▼
    Maximum heap = ~107 GB  (-Xmx110000m)
           │
           ▼
    If exceeded → OutOfMemoryError: Java heap space

# ❌ Bad — large gap causes heap resizing overhead
-Xms2048m -Xmx110000m

# ✅ Good — fixed heap, no resizing at runtime
-Xms110000m -Xmx110000m

# Or use reasonable values based on server RAM
-Xms4g -Xmx4g      # 4 GB server
-Xms8g -Xmx8g      # 8 GB server
-Xms16g -Xmx16g    # 16 GB server



	
@Service
public class OrderService {
	//producer side

	@Autowired
	private KafkaTemplate<String, OrderCreatedEvent> kafkatemplate;

	public Order createOrder(OrderRequest request) {
		Order order = saveOrder(request);
		OrderCreatedEvent event = new OrderCreatedEvent(order);
		kafkatemplate.send(
		    "order-event"//topic name
		    ,order.getId().toString() //message key : same key same partition
		    ,event)//message value : OrderCreatedEvent Object

		return order;
	}

}


@Service
//consure
public class EmailService {
	@KafkaListner(topic = "order-events",
	              groupId = "email-service")
	public void handleOrderCreated(OrderCreatedEvent event) {
		sendOrderConfirmattion(event);
	}
}

@Configuration
public class KafkaConfig {
	@Bean
	public ProduceFactory<String, OrderCreatedEvent> produceFactory() {
		Map<String, Object> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	public KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}
}

Java Streams:
1. Sum of even numbers
Given a list of integers, return the sum of all even numbers using streams.
Given
List nums = List.of(1, 2, 3, 4, 5, 6, 7, 8);

nums.stream().filter(n -> n%2==0).reduce((a,b) -> a+b).orElse(0);
nums.stream().filter(n -> n % 2 == 0).reduce(0, (a, b) -> a+b);
nums.stream().filter(n -> n % 2 == 0).mapToInt(Integer::intValue).sum();

2. List of uppercase strings
Convert all strings in a list to uppercase and collect them into a new list.
Given
List words = List.of("hello", "world", "java");

words.stream().map(String::toUpperCase).collect(Collectors.toList());

3. Count strings longer than 3 chars
Count how many strings in a list have more than 3 characters.
Given
List words = List.of("hi", "java", "ok", "streams", "is");
long count = words.stream()
                  .filter(s -> s.length() > 3)
                  .count();

4. Find first name starting with 'A'
Return the first name in the list that starts with the letter 'A', or empty if none.
Given
List names = List.of("Bob", "Alice", "Anna", "Charlie");
Optional<String> result = names.stream().filter(s -> s.charAt(0)=='A').findFirst();

5. Flatten a list of lists
Flatten a List> into a single List using streams.
Given
List> nested = List.of(
List.of(1, 2), List.of(3, 4), List.of(5));
nested.stream().flatMap(List::stream).collect(Collectors.toList());

6.Group words by their length
Group a list of strings by their character length into a Map>.
Given
List words = List.of("hi", "hey", "yo", "java", "bye");
words.stream().collect(Collectors.groupingBy(s -> s.length()))

7. Get distinct sorted numbers
From a list with duplicates, return a sorted list of distinct numbers.
Given
List nums = List.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3);
nums.stream().distinct().sorted().forEach(System.out::println);

8. Max salary from employee list
Find the employee with the highest salary. Return an Optional.
Given
record Employee(String name, int salary) {}
List emps = List.of(
    new Employee("Ana", 70000),
    new Employee("Bob", 95000),
    new Employee("Cara", 80000));
Optional<Employee> maxEmp = emps.stream().max(Comparator.comparingInt(Employee::salary));

import java.util.*;

public class Main {
//     public static void moveZeros(int[] nums) {
//   int j = 0;
//   for(int i=0; i< nums.length;i++){
//       if(nums[i]!=0){
//           int temp = nums[i];
//           nums[i]=nums[j];
//           nums[j]=temp;
//           j++;

//       }
//   }
//   System.out.println(Arrays.toString(nums));
// }
// 	public static int storeWater(ArrayList<Integer> height) {
// 		int maxWater=0;

// 		for(int i=0; i<height.size(); i++) {
// 			for(int j=i+1; j<height.size(); j++) {
// 				int ht=Math.min(height.get(i),height.get(j));
// 				int width=j-i;
// 				//area
// 				int currWater=ht*width;
// 				maxWater = Math.max(maxWater, currWater);
// 			}
// 		}
// 		return maxWater;
// 	}

	public static void main(String[] args) {
		ArrayList<Integer> list = new ArrayList<>();
		//1,8,6,2,5,4,8,3,7
// 		list.add(1);
// 		list.add(8);
// 		list.add(6);
// 		list.add(2);
// 		list.add(5);
// 		list.add(4);
// 		list.add(8);
// 		list.add(3);
// 		list.add(7);
		int[] nums= {0, 1, 0, 3, 12};
		//System.out.println("Max water is : "+storeWater(list));
		moveZeros(nums);
	}
}

Mercedes-benz R&D
	1 Middle of LinkedList (missed: told normal approach)
	The best approach is to use Floyd's algorithm. fast and slow pointers
class Solution {
    public ListNode middleNode(ListNode head) {
       ListNode fast = head, slow = head;
        while(fast!=null && fast.next!=null){
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;//slow is at the middle  
    }
}
	2 Java streams 
		Find even numbers from a list of integers, square them, and sum them up.
		List nums = List.of(1, 2, 3, 4, 5, 6, 7, 8);
nums.stream().filter(n -> n % 2 == 0).mapToInt(n -> n*n).sum();

3. AI project explain
	How do you maintain security?
	How was the search done by LLM?
	Overall architecture from request to response
	What are Embeddings?
	what is vector db?
	Difference between Vector DB and NoSQL Db?
	security
4. Java latest version? It is 26, but not aware yet
	What is the higher version you are aware?
	Java 17, but mostly aware of java 8
	Java 17: record class, sealed classes, text blockc
5. Monitoring in Java applications?
6. Deployed where?

	***************************
## Types of Garbage Collectors: ->
	***************************
1. Serial GC (-XX:+UseSerialGC)
	Single-threaded collector
┌──────────────┐
│   GC Thread  │ → Stops application threads
└──────────────┘
Best for: Small applications, single-core systems
	
2. Parallel GC (-XX:+UseParallelGC)
Multiple threads for collection
┌───────┬───────┬───────┐
│  GC1  │  GC2  │  GC3  │ → Parallel minor GC
└───────┴───────┴───────┘
Best for: Throughput-oriented applications
Default: Java 8
# Heap size configuration
java -Xms512m -Xmx2g MyApp
# -Xms: Initial heap size (512 MB)
# -Xmx: Maximum heap size (2 GB)

3. G1 GC (-XX:+UseG1GC)
Heap divided into regions
┌───┬───┬───┬───┬───┬───┬───┬───┐
│ E │ S │ O │ E │ H │ O │ E │ S │
└───┴───┴───┴───┴───┴───┴───┴───┘
E=Eden, S=Survivor, O=Old, H=Humongous

Best for: Large heaps (>4GB), predictable pause times
Default: Java 9+

4. ZGC (-XX:+UseZGC)
Ultra-low latency (<10ms pauses)
Concurrent, scalable
Best for: Large heaps (TB), latency-sensitive apps
Available: Java 11+

	*************
#GC Configuration : 

	#Selection of GC
	java -XX:+UseG1GC MyApp
	java -XX:+UseZGC MyApp
	
# Heap size configuration
java -Xms512m -Xmx2g MyApp
# -Xms: Initial heap size (512 MB)
# -Xmx: Maximum heap size (2 GB)


