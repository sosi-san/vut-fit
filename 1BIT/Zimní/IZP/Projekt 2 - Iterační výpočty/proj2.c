/********************************************************           
* proj2 -- iteracni vypocty						       *   
*                                                      *   
* Author:  Adam, Woska		                           *   
*                                                      *   
* Purpose: Vytvoření vlastních funkcí na logaritmy 	   *   
*                                                      *    
*********************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#ifdef NAN
/* NAN is supported */
#endif
#ifdef INFINITY
/* INFINITY is supported */
#endif
double taylor_log(double x, unsigned int n);
double getDoubleFromChar(char *n, double *x);
int getIntFromChar(char *n);
double cfrac_log(double x, unsigned int n);
unsigned int getOddNumber(unsigned int n);
unsigned int getOddNumbersSum(unsigned int n);
double taylor_pow(double x, double y, unsigned int n);
double taylorcf_pow(double x, double y, unsigned int n);
void printErroMessage();
int main(int argc, char *argv[])
{
	if((argc == 4 ) && (strcmp(argv[1],"--log") == 0))
	{
		double x;
		unsigned int n = getIntFromChar(argv[3]);
		if(n <= 0 || getDoubleFromChar(argv[2], &x) == 1)
		{
			printErroMessage();
			return 1;
		}
		printf("       log(%g) = %.12g\n",x,log(x));
		printf(" cfrac_log(%g) = %.12g\n",x,cfrac_log(x,n));
		printf("taylor_log(%g) = %.12g\n",x,taylor_log(x,n));
		return 0;
	}
	if((argc == 5 ) && (strcmp(argv[1],"--pow") == 0))
	{
		
		double x;
		double y;
		unsigned int n = getIntFromChar(argv[4]);
		if(n <= 0 || getDoubleFromChar(argv[2],&x) == 1 || getDoubleFromChar(argv[3],&y) == 1)
		{
			printErroMessage();
			return 1;
		}
		printf("         pow(%g,%g) = %.12g\n",x,y,pow(x,y));
		printf("  taylor_pow(%g,%g) = %.12g\n",x,y,taylor_pow(x,y,n));
		printf("taylorcf_pow(%g,%g) = %.12g\n",x,y,taylorcf_pow(x,y,n));
		return 0;
	}
	printErroMessage();
	return 1;
}
void printErroMessage()
{
	printf("Chybně zadané argumenty!\n");
}
double taylor_log(double x, unsigned int n)
{
	if(x == 0) return -INFINITY;
	if(x < 0) return NAN;
	if(x == -INFINITY) return NAN;
	if(isinf(x)) return INFINITY;
	if(isnan(x)) return NAN;

	if(x >= 1)
	{
		double vrch = ((x-1.0)/x);
		double vysledek =  vrch;
		for(unsigned int i = 2; i <= n; i++)
		{
			vrch = vrch*((x-1.0)/x);
			vysledek = vysledek + (vrch/i);
		}
		return vysledek;
	}
	else
	{
		x = x-1;
		double vysledek = -x;
		double vrch = x;
		for(unsigned int i = 1; i < n; i++)
		{
			vrch = vrch * x;
			vysledek -= vrch/i;
		}
		return vysledek;
	}
}

double cfrac_log(double x, unsigned int n)
{
	if(x == 0) return -INFINITY;
	if(x < 0) return NAN;
	if(x == -INFINITY) return NAN;
	if(isinf(x)) return INFINITY;
	if(isnan(x)) return NAN;

	x = (x-1)/(x+1);
	double vysledek = 0;
	unsigned int oddNumber = getOddNumber(n);
	for(unsigned int i = n; i >= 1; i--)
	{
		if(i == 1) 
			vysledek = 2*x/(1-vysledek);
		else
		{
			vysledek = getOddNumbersSum(i-1)*(x*x)/(oddNumber-vysledek);
			oddNumber-=2;
		}
	}
	return vysledek;
}
double taylor_pow(double x, double y, unsigned int n)
{
	
	if(isinf(x) && y == -INFINITY) return 0;
	if(x == 0 && y == -INFINITY) return INFINITY;
	if((x < 0 && x > -1) && y == -INFINITY) return INFINITY;
	if((x > 0 && x < 1) && y == -INFINITY) return INFINITY;
	if((x < 0 && x > -1) && y == INFINITY) return 0;
	if((x > 0 && x < 1) && y == INFINITY) return 0;
	if(x > 1 && y == INFINITY) return INFINITY;

	if(x > 1 && y == -INFINITY) return 0;
	if(x < -1 && y == -INFINITY) return 0;
	if(isinf(x) && isinf(y)) return INFINITY;
	if(x == 1 && y == INFINITY) return 1;
	if(x == 1 && y == -INFINITY) return 1;
	if(x == 1 && isnan(y)) return 1;
	if(isnan(x) && isinf(y)) return NAN;
	if(x != 0 && isinf(y)) return 1;
	if(y != 0 && isnan(x)) return NAN;
	if(x == 0 && isinf(y)) return 0;
	if(y == 0) return 1;
	if(isnan(y)) return NAN;
	if(x == -INFINITY && y < 0) return (-0);
	if(x == -INFINITY && y > 0) return -INFINITY;
	if(x == INFINITY && y < 0) return 0;
	if(x == INFINITY && y > 0) return INFINITY;
	if(x == INFINITY) return INFINITY;
	if(x == 0) return 0;
	if(isnan(x)) return NAN;

	double vysledek = 1;
	double spodek = 1;
	double vrchLog = taylor_log(x,n);
	double vrchX = y;
	for(unsigned int i = 1; i <= n-1; i++)
	{
		vysledek = vysledek + (vrchX*vrchLog)/(spodek);
		vrchLog = vrchLog * taylor_log(x,n);
		vrchX = vrchX * y;
		spodek = i*spodek + spodek;
	}
	return (vysledek);
}
double taylorcf_pow(double x, double y, unsigned int n)
{

	if(isinf(x) && y == -INFINITY) return 0;
	if(x == 0 && y == -INFINITY) return INFINITY;
	if((x < 0 && x > -1) && y == -INFINITY) return INFINITY;
	if((x > 0 && x < 1) && y == -INFINITY) return INFINITY;
	if((x < 0 && x > -1) && y == INFINITY) return 0;
	if((x > 0 && x < 1) && y == INFINITY) return 0;
	if(x > 1 && y == INFINITY) return INFINITY;

	if(x > 1 && y == -INFINITY) return 0;
	if(x < -1 && y == -INFINITY) return 0;
	if(isinf(x) && isinf(y)) return INFINITY;
	if(x == 1 && y == INFINITY) return 1;
	if(x == 1 && y == -INFINITY) return 1;
	if(x == 1 && isnan(y)) return 1;
	if(isnan(x) && isinf(y)) return NAN;
	if(x != 0 && isinf(y)) return 1;
	if(y != 0 && isnan(x)) return NAN;
	if(x == 0 && isinf(y)) return 0;
	if(y == 0) return 1;
	if(isnan(y)) return NAN;
	if(x == -INFINITY && y < 0) return (-0);
	if(x == -INFINITY && y > 0) return -INFINITY;
	if(x == INFINITY && y < 0) return 0;
	if(x == INFINITY && y > 0) return INFINITY;
	if(x == INFINITY) return INFINITY;
	if(x == 0) return 0;
	if(isnan(x)) return NAN;

	double vysledek = 1;
	double spodek = 1;
	double vrchLog = cfrac_log(x,n);
	double vrchX = y;
	for(unsigned int i = 1; i <= n-1; i++)
	{
		vysledek = vysledek + (vrchX*vrchLog)/(spodek);
		vrchLog = vrchLog * cfrac_log(x,n);
		vrchX = vrchX * y;
		spodek = i*spodek + spodek;
	}
	return (vysledek);
}
//Vrací n-té sudé čislo;
unsigned int getOddNumber(unsigned int n)
{
	return (n*2)-1;
}
//Vrací součet sudých čísel po n-té sudé číslo
unsigned int getOddNumbersSum(unsigned int n)
{
	if(n == 0) return 1;
	unsigned int sum = 0;
	for(unsigned int i = 1; i <= n;i++)
		sum = getOddNumber(i)+ sum;
	return sum;
}
//Vytváří unsigned int ze stringu
int getIntFromChar(char *n)
{
	char *stringPart;
	int cisloN = strtod(n,&stringPart);
	if(strcmp(stringPart,"")) return 0;
	return cisloN;
}
//Vytváří double ze stringu
double getDoubleFromChar(char *n, double *doubleX)
{
	char *stringPart;
	*doubleX = strtod(n,&stringPart);
	if(strcmp(stringPart,"")) return 1;
	return 0;
}
