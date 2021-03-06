Assignment 1

Date : 21.02.2022
----------------------------

13.Write a Java Program to find the smallest of 3 numbers (a,b,c) without using < or > symbol? 

18.Write a java program to LCM of TWO given number using Prime Factors method. 
20.Write a Java Program to print all the Prime Factors of the Given Number.

------------------------------------------------------------------------------
// Qn.1) Java Program to Check Whether a Number is Even or Odd
----------------------------------------------------------
import java.util.Scanner;

public class EvenOdd {
	  public static void main(String[] args) {

Scanner sc=new Scanner(System.in);

System.out.print("Enter any Number : ");

int n1 = sc.nextInt();

if(n1==0)

System.out.println(n1+ " is neither even nor odd");

else
	if(n1%2==0)

System.out.println(n1+ " is even");

else

System.out.println(n1+ " is odd");
}
}

--------------------------------------------------------------------------------------------------------------------------------------------------------------
Qn2. Write a Java Program to find the Factorial of given number.
--------------------------------------------------------------------------------------------------------------------------------------------------------------
public class Factorial{
	
	public static void main(String[] args)
	{
		Scanner sc=new Scanner(System.in);
		
		int no;
		System.out.print("Enter any Number : ");
		no=sc.nextInt();
        
		int fact =1;

		for(int i=1;i<=no;i++)
		{
		fact=fact*i	;
		}
		System.out.print("Factorial of "+no+" is "+fact);
		
	}
	
}
----------------------------------------------------------------
/*
public class Factorial{
	
	public static void main(String[] args)
	{
		
		int no =10;
int fact =1;

		for(int i=1;i<=no;i++)
		{
		fact=fact*i	;
		}
		System.out.print("Factorial of "+no+" is "+fact);
		
	}
	
}
*/
-----------------------------------------------------------------------------------------------------------------------------------------
3.Find the Factorial of a number using Recursion. 
import java.util.*;

class FactorialByRec{
	public static void main(String[] args){
	
	System.out.println("Enter a number to find factorial : ");
	Scanner sc=new Scanner(System.in);

	int n=sc.nextInt();
	
	System.out.println(factorial(n));	
	}
	
	public static int factorial(int n)
	{
		if(n==1){
			return 1;
		}
		else
		{
			return n*factorial(n-1);
		}
	}
}
-----------------------------------------------------------------------------------------------------------------------------------------
Q4,5,6. Swap two numbers without using third variable approach 1. 
-----------------------------------------------------------------------------------------------------------------------------------------
public class Swapping2Numbers{
	public static void main(String args[])
	{
/*	Practice
	int a=10,b=20;
		System.out.println("Before Swapping values are "+a+" "+b);
		
// logic1= third variable		
int t=a;
a=b;
b=t;

System.out.println("After swapping values "+a+" "+b);	
*/
----------------------------------------------------------------

/*Q4
----------------------------------------------------------------
//logic 2- use + and  - without using third variable
int a=10,b=20;
	System.out.println("Before Swapping values are "+a+" "+b);	
		
a=a+b; //10+20=30
b=a-b;// 30-20=10
a=a-b;// 30-10=20

System.out.println("After swapping values "+a+" "+b);	
*/
----------------------------------------------------------------

/* Q5
----------------------------------------------------------------
// logic 3  a and b should not be zero
int a=10,b=20;
System.out.println("Before Swapping values are "+a+" "+b);
	a=a*b; //10*20=200
b=a/b; //200/20=10
a=a/b;	//200/10=20
System.out.println("After swapping values "+a+" "+b);	
*/
----------------------------------------------------------------

// Q6
----------------------------------------------------------------
//  logic 4    bitwise XOR (^)
int a=10,b=20;
System.out.println("Before Swapping values are "+a+" "+b);
a=a^b;
b=a^b;
a=a^b;
System.out.println("After swapping values "+a+" "+b);	

	}
}

------------------------------------------------------------------------------------
Qn.7.How to check the given number is Positive or Negative in Java? 
------------------------------------------------------------------------------------

import java.util.Scanner;  
 public class PosOrNeg  
 {  
   public static void main(String[] args)   
 {  
    int n1;  

    Scanner sc = new Scanner(System.in);  
    System.out.print("Enter a number: ");  

n1 = sc.nextInt();  

if(n1>0)  
{  
    System.out.println("The number is positive.");  
}  

else if(n1<0)  
 {  
     System.out.println("The number is negative.");  
 }  
  
else  
 {  
     System.out.println("The number is zero.");  
 }  
}  
} 

----------------------------------------------------------------------------------------------------------------------------------------------------------------------
8.Write a Java Program to find whether given number is Leap year or NOT? 
----------------------------------------------------------------------------------------------------------------------------------------------------------------------
class LeapYear{
	public static void main(String args[])
	{
		int year=2016;
		
		if(year%4==0)
		{
			if(year%100==0)
			{
				if(year%400==0)
				{
					System.out.println(year+" is Leap year");	
				}
				else
				{
					System.out.println(year+" is not a Leap year");
				}
			}
			else
			{
				System.out.println(year+" is Leap year");
			}				
		}
		else
		{
			System.out.println(year+" is not a Leap year");
		}
	}
}
----------------------------------------------------
class LeapYear{
	public static void main(String args[])
	{
		int year=2016;
		if((year%400==0) || (year%100!=0 && year%4==0))
		{
		System.out.println(year+" is Leap year");	
		}
		else
			System.out.println(year+" is not Leap year");
	}
}

---------------------------------------------------------------------------------------------------------------------------------------------
Qn.9. Write a Java Program to Print 1 To 10 Without Using Loop. 
--------------------------------------------------------------------------------------------------------------------------------------------
public class Main
{
    public static void printNum(int num){
        if(num<=10)
        {
            System.out.println(num);
            printNum(num+1);
        }
    }
    
	public static void main(String[] args) {
	    
	printNum(1);
	}
}
--------------------------------------------------------------------------------------------------------------------
// 10.Write a Java Program to print the digits of a Given Number. 
--------------------------------------------------------------------------------------------------------------------
import java.util.*;

class PrintDigit{
	public static void main(String[] args)
	{
		Scanner sc=new Scanner(System.in);
		
		int num;
		
		System.out.print("Enter a number : ");
		num=sc.nextInt();
		while(num!=0)
		{
			int digit=num%10;
			
			System.out.println("The digit is "+digit);
			num=num/10;
		}
	}
}
--------------------------------------------------------------------------------------------------------------------------------------

// 11.Write a Java Program to print all the Factors of the Given number. 
------------------------------------------------------------------------------
import java.util.*;
public class FindFactorsOfNumber{
	public static void main(String[] args){
		int num;
		
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter a number whose factors you want : ");
		num=sc.nextInt();
		
		for(int i=1;i<=num;i++)
		{
			if(num%i==0){
				System.out.println(i);
			}
		}
	}
}

-----------------------------------------------------------------------------------------------
12.Write a Java Program to find sum of the digits of a given number. 
-----------------------------------------------------------------------------------------------
public class SumOfDigits{
	public static void main(String[] args)
	{
		int num=1503;
		int rem=0;
		int sum=0;
		
		while(num>0)
		{
			rem=num%10;
			sum=sum+rem;
			num=num/10;
			
		}
		
		System.out.println(sum);
	}
	
}
-----------------------------------------------------------------------------------------------
14.How to add two numbers without using the arithmetic operators in Java? 
-----------------------------------------------------------------------------------------------
import java.util.Scanner;
public class addWithoutOperator{
     public static void main(String[] arg) 
	 {
	   int x, y ;
	   Scanner in = new Scanner(System.in);	
	   System.out.print("Input first number: ");
	   x = in.nextInt(); 
	   System.out.print("Input second number: ");
	   y = in.nextInt(); 
      while(y != 0){
            int carry = x & y;
            x = x ^ y;
           y = carry << 1;
        }
        System.out.print("Sum: "+x); 
		
		     	
	}	
}

--------------------------------------------------------------------------------------------------------------------------------------
// 15.Write a java program to Reverse a given number. 
--------------------------------------------------------------------------------------------------------------------------------------
public class Reversenumber{
	public static void main(String args[])
	{
	Scanner sc=new Scanner(System.in);
	System.out.println("Enter a Number : ");
	int num=sc.nextInt();  // 1234
	
	//1. using algorithm
	
	int rev=0;
	while(num!=0)
	{
		int rem=num%10;
		rev =rev*10+rem;
	//or
	// rev=rev*10+num%10; 
		num=num/10;	
	}
	System.out.println("Reverse Number is "+rev);
	}	
}

-------------------------------------------------------------------------------
16.Write a Java Program to find GCD of two given numbers. 
-------------------------------------------------------------------------------
// for HCG/GDD
import java.util.*;
public class GcdofTwo{
 public static void main(String[] args)
 {
	 Scanner sc=new Scanner(System.in);
	 
	 System.out.println("Enter any Two numbers : ");
	 
	 int a=sc.nextInt();
	 int b=sc.nextInt();
	 
	int g=0;
	
	 
	 for(int i=1;i<=a;i++){
		 if(a%i==0&&b%i==0)
			 
		 g=i;
		
	 }
	  System.out.print("GCD of "+a+" and "+b+" is "+g);
 }
}

/*
* Comman factors we get from here
import java.util.*;
public class GcdofTwo{
 public static void main(String[] args)
 {
	 Scanner sc=new Scanner(System.in);
	 
	 System.out.print("Enter any Two numbers : ");
	 
	 int a=sc.nextInt();
	 int b=sc.nextInt();
	 
	 
	 
	 for(int i=1;i<=a;i++){
		 if(a%i==0&&b%i==0)
			 System.out.println(i);
	 }
 }
}
*/
-------------------------------------------------------------------------------------------------
17.Write a java program to LCM of TWO given number.
//LCM
import java.util.*;
public class LcmofTwo{
 public static void main(String[] args)
 {
	 Scanner sc=new Scanner(System.in);
	 
	 System.out.println("Enter any Two numbers : ");
	 
	 int a=sc.nextInt();
	 int b=sc.nextInt();
	 
	int g=0;
	
	 
	 for(int i=1;i<=a;i++)
	 {
		 if(a%i==0&&b%i==0)
			 
		 g=i;
		
	 }
	 
	 int lcm=a*b/g;
	  System.out.print("LCM is "+lcm);
 }
}
-------------------------------------------------------------------------------------------------
// 19.Check whether the Given Number is a Palindrome or NOT. 
public class palindrome{
	public static void main(String args[])
	{
	Scanner sc=new Scanner(System.in);
	System.out.println("Enter a Number : ");
	int num=sc.nextInt();  // 1234
	
	//1. using algorithm
	int temp=num;
	int rev=0;
	while(temp!=0)
	{
		int rem=temp%10;
		rev=rev*10+rem;
	//or
	// rev=rev*10+num%10; 
		temp=temp/10;	
	}
	System.out.println("Reverse Number is "+rev);
	
	if(rev==num)
		System.out.println("is palindrome ");
	else
	System.out.println("not palindrome");
		
}
}
--------------------------------------------------------------------------------------------------------------------------

--------------------------------------------------------------------------------------------------------------------------
