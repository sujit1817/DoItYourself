1.Calculate the average of given array. 

import java.util.*;
public class ArrayAvg{
	
	public static void main(String args[])
	{
		//input
		Scanner sc=new Scanner(System.in);
		
		System.out.println("Enter how many elements you want? ");
		int n=sc.nextInt();
		
		// array declaration
		double[] arr =new double[n];
		System.out.println("Enter" +n+" elements in an array");
		double sum=0;
		
		for(int i=0; i<n;i++)
		{
			arr[i]=sc.nextDouble();
			sum=sum+arr[i];
		}
		System.out.println("Average of given array of "+n+" elements is "+sum/n);
		
	}
}
---------------------------------------------------------------------------
2.Find the second largest number in the given array. 

import java.util.Arrays;

public class SecondlargestNumberInArray {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
       int arr[] ={1,9,5,2,8,-1,3,55};
		
		int n=arr.length;
		
		//sort the array
		Arrays.sort(arr);	
		
		System.out.println("Second Highest number is "+arr[n-2]);
	}

}
-------------------------------------------------------------------------------
3.Find the second minimum number in the given array. 

// Find the second minimum number in the given array.
import java.util.Arrays;

public class SmallestNumberInAnArray {
   public static void main(String args[]){
   int array[]={20,40,25,50,60,30};
   int size=array.length;
   
   Arrays.sort(array);
   System.out.println("Sorted array "+Arrays.toString(array));
   
   int res=array[1];
   
    System.out.println("2nd smallest element is "+res);
   }
  }
-------------------------------------------------------------------------------
  import java.util.*;

public class SmallestNumberInAnArray {
	public static void main(String args[])
	{

		int temp,size;
		int array[]={10, 20, 15, 63, 96, 57};
		
		size=array.length;
		
		for(int i=0;i<size;i++)
		{
			for(int j=i+1;j<size;j++)
			{
				if(array[i]>array[j])
					{
				temp=array[i];
				array[i]=array[j];
				array[j]=temp;
					}
			}
			
		}
		System.out.println("2nd Smallest element of the array is:: "+array[1]);
	}
}
  
--------------------------------------------------------------------------------------
4.Find the missing Number in the given array of 1 to N. 
class MissingNumber{
	public static void main(String[] args)
	{
		int[] arr={3,4,5,1,6,7,9,8,10};
		
		int n=arr.length+1;
		
		int sum=(n*(n+1))/2;
		
		for(int i=0;i<arr.length;i++){
			sum=sum-arr[i];
			
		}
	System.out.println("Missing number is "+sum);
	
	
	}
	
}
--------------------------------------------------------------------------------------
5.Write a Java Program to find the Intersection of two arrays. 
class IntersectionOfTwoArrays{
	public static void main(String[] args){
		int[] arr1={23, 36, 96, 78, 55};
		int arr2[]={78, 45, 19, 73, 55};
		
		System.out.println("Intersection of the two arrays ");
		
		for(int i=0;i<arr1.length;i++){
			for(int j=0;j<arr2.length;j++){
				if(arr1[i]==arr2[j]){
					System.out.println(arr2[j]);
				}
			}
		}
	}
}
--------------------------------------------------------------------------------------
6.Write a Java Program to find the Intersection of Two Sorted arrays.
class IntersectionofTwo {
   public static void main(String args[]) {
	  int arr1[] = {2, 4, 6, 8, 9};
      int arr2[] = {1, 3, 4, 5, 6, 8, 9};
   int i = 0, j = 0;
  
System.out.print("Intersection of two arrays is: ");  
   while(i<arr1.length && j<arr2.length)
   {
	   if(arr1[i]<arr2[j])
		   i++;
	   else if(arr2[j]<arr1[i])
		   j++;
	   else{
		   System.out.print(arr2[j++]+" ");
	   }
     }
   }
}

--------------------------------------------------------------------------------------
7.Write a Java Program to find the Union of Two Arrays (UnSorted Array). 
import java.util.*;
class UnionofTwo{
	public static void main(String args[]){
		int a1[] = {2, 4, 6, 8, 9};
        int a2[] = {1, 10, 4, 5, 6, 8, 9};
		
		Set<Integer> set=new HashSet<>();
		
		for(int x:a1 ){
			set.add(x);	
		}
		
		for(int x:a2 ){
			set.add(x);	
		}
		System.out.print(set);

	}
}
--------------------------------------------------------------------------------------
8.Write a Java Program to find the Union of Two Arrays (Sorted Arrays). 


--------------------------------------------------------------------------------------
9.Write a Java Program to find the Union of Tow Arrays using HashSet. 
import java.util.*;
class UnionofTwo{
	public static void main(String args[]){
		int a1[] = {2, 4, 6, 8, 9};
        int a2[] = {1, 10, 4, 5, 6, 8, 9};
		
		Set<Integer> set=new HashSet<>();
		
		for(int x:a1 ){
			set.add(x);	
		}
		
		for(int x:a2 ){
			set.add(x);	
		}
		System.out.print(set);

	}
}
--------------------------------------------------------------------------------------
10.Write a Java Program to Move all Zero to End of the Array. 
class MoveAllZerosToEnd{
	public static void main(String[] args){
	int arr[]={1,6,0,3,8,9,0,2};
int len=arr.length;
int count=0;

for(int i=0;i<len;i++){
	if(arr[i]!=0){
		arr[count]=arr[i]; 
		
		count++;
	}
}	
while(count<len){
	arr[count]=0;
	count++;
}

for(int j=0;j<len;j++){
System.out.print(arr[j]+" ");
}
	}
}
--------------------------------------------------------------------------------------
11.Write a Java Program to Move all Zeros to Start of the Array. 
import java.util.*;
public class MoveAllZerosToStart{
	public static void main(String[] args){
	
	Scanner sc=new Scanner(System.in);
	System.out.println("Enter the size of the array : ");
	
	int size=sc.nextInt();
	int[] arr=new int[size];

	System.out.println("Enter array elements : ");
	
	// for taking array elements
	for(int i=0;i<size;i++){
		arr[i]=sc.nextInt();
	}
	
	// counter variable which starts from size-1
	//suppose size is 5 then counter starts from 4
	int counter=size-1;
	
	// for array of size five  index starts from 0 to 4
	// i=size-1=5-1=4 i.e. index 4 means last location so elements will be inserted from last index and condition is i>=0
	// then i-- means reverse elements insertion
	// If statement inside for loop
	for(int i=size-1;i>=0;i--)
	{
		// if not zero then put arr[i] in arr[counter]
		// and counter-- 
	if(arr[i]!=0){
		arr[counter]=arr[i];
		counter--;
	}	
	}
	
	// suppose all non-zero elements inserted from last. and counter still have spaces means zeros are there so in the remaining spaces we have to put zeroes
	//so in while loop check if counter is greater than zero or not. If greater then there is a space and we can put o  at that places and decrement counter
	// until the space gets occupied with zero and this is how we move all zeros at start
	while(counter>=0){
		arr[counter]=0;
		counter--;
	}
	
	System.out.println(Arrays.toString(arr));
	// to print all array elements above line
	sc.close();

	}
}

--------------------------------------------------------------------------------------
12.Write a Java Program to Reverse the given array without using additional Array. 
import java.util.*;
class ArrayReverse{
	public static void main(String[] args){
	Scanner sujit=new Scanner(System.in);

	System.out.println("Enter the size of array ");
	int size=sujit.nextInt();
	
	// declare an array
	int arr[]=new int[size];
	
	System.out.println("Enter an array values ");
	
	// Input array values
	for(int i=0;i<arr.length;i++){
		arr[i]=sujit.nextInt();

	}
	
	int temp;
	int start=0;
	int end=size-1;
	
		// 1,3,5,6,9
	while(start<end)
	{
		// 0<4
		//so store element at start in temp i.e. element at 0 is 1 store in start
		// element at end i.e. index 4 is 9.store it in arr[start]
		// and store temp in arr[end]
		// replaced
		// then start++ will move index of start to 1 and 
		// end-- will move index of end which 4 to index 3 and continues in same manner
		
		temp=arr[start];
		arr[start]=arr[end];
		arr[end]=temp;
		
		start++;
		end--;
	}
System.out.print("Array after reverse ");

for(int i=0;i<arr.length;i++){
		
System.out.print( arr[i]+" ");
	}
	/*
	for(int j=0;j<arr.length;j++)
	{
		System.out.print( arr[j]);
	}*/
	}
}
--------------------------------------------------------------------------------------
13.Write a program to find the Most Frequent Element in an given array. 

--------------------------------------------------------------------------------------
14.Write a program to find the Most Frequent Element using Sorting. 

--------------------------------------------------------------------------------------
15.Write a program to find the Most Frequent Element using HashTable. 

--------------------------------------------------------------------------------------
16.Write a java program to Rotate the Given Array d times. 

--------------------------------------------------------------------------------------
17.Find the pair of elements(X+Y) in the array whose sum is equal to given number Z. 

--------------------------------------------------------------------------------------
18.Write a java program to check Given String is Palindrome or NOT.

import java.util.*;
class PalindromeString{
	public static void main(String[] args){
		Scanner sc=new Scanner(System.in);
		
		System.out.println("Enter a String : ");
		String str=sc.next();
		String org_str=str;
		
		String rev="";
		
		int len=str.length();
		
		for(int i=len-1;i>=0;i--){
			rev=rev+str.charAt(i);
		}
		
	
		if(org_str.equals(rev)){
			System.out.println(org_str+" is Palindrome String");
		}
	
		else
		{
			System.out.println(org_str+" is not a Palindrome String");
		}
}
}
--------------------------------------------------------------------------------------
19.How to Swap two Strings without using third (temporary) variable? 

--------------------------------------------------------------------------------------
20.Write a java program to Reverse a String without using in-build function.

--------------------------------------------------------------------------------------
