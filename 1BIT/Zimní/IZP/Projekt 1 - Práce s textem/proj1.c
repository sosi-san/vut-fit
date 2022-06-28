/********************************************************           
* proj1 -- jednoduchý řádkový editor textu		       *   
*                                                      *   
* Author:  Adam, Woska		                           *   
*                                                      *   
* Purpose: Dokáže urychlit úpravu textového souboru    *   
*                                                      *    
*********************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
//Maximální délka řádku
#define LENGTHOFLINE 1002 

int checkArgumentCount(int argc);
void printInfo();
int openComandFile(char *filename, int numOfComands, char comands[numOfComands][LENGTHOFLINE]);
int openTextFile(int numOfComands, char comands[numOfComands][LENGTHOFLINE]);
int getNumOfComands(char *filename);
int getCommandNumber(char *command);
void removeEOL(char str[LENGTHOFLINE]);
void getCommandText(char *comands, char tmp[LENGTHOFLINE]);
int substitude(char *comands, char *str);
int main(int argc, char *argv[])
{
	if(checkArgumentCount(argc) == 1)return 0;
	//Zjistíme počet příkatů v souboru
	int numOfComands = getNumOfComands(argv[1]);
	if(numOfComands == -1) return -1;
	char comands[numOfComands][LENGTHOFLINE];
	//Načteme příkazy ze souboru
	if(openComandFile(argv[1], numOfComands, comands) == -1) return -1;
	//Načítáme textový soubor a podle příkazů upravujeme
	if(openTextFile(numOfComands,comands) == -1) 
	{
		printf("Nastala chyba!\n");
		return -1;
	}
	return 0;
}
int checkArgumentCount(int argc)
{
	if(argc == 2) return 0;
	printInfo();
	return 1;
}
void printInfo()
{
	printf("Jednoduchy textovy editor\n");
}
int getNumOfComands(char *filename)
{
	FILE *file = fopen (filename, "r");
	int numOfComands = 0;
	if(file == NULL) 
	{
		return -1;
	}
	if (file != NULL)
	{
		while(!feof(file))
		{
  			if(fgetc(file) == '\n')
  			{
  				numOfComands++;
  			}
		}
	}
	fclose(file);
	return numOfComands;
}
//Načítání příkazů do pole
int openComandFile(char *filename, int numOfComands, char comands[numOfComands][LENGTHOFLINE])
{
	FILE *file = fopen (filename, "r");
	rewind(file);
	if(file == NULL) 
	{
		return -1;
	}
	if (file != NULL)
	{
		int lengthOfCommand = LENGTHOFLINE-1;
	  	char line [lengthOfCommand];	
	  	for (int i = 0;fgets (line, sizeof line, file) != NULL;i++)
	  	{
	  		strcpy(comands[i],line);
	  	}
	 	fclose (file);
	}
	return 0;
}
int openTextFile(int numOfComands, char comands[numOfComands][LENGTHOFLINE])
{
	FILE *fp;
	char str[LENGTHOFLINE];
	
	fp = stdin;
	if(fp == NULL) 
	{
	  perror("Error opening file");
	  return -1;
	}
	int lineNum = 0;
	int commandNum = 0;
	//Dokud jsou nějaké řádky na úpravu tak upravujem
	while(fgets (str, LENGTHOFLINE, fp)!=NULL) 
	{
		//Už není žádný příkaz vytiskni zbytek textu
		if(numOfComands == commandNum)fputs(str,stdout);
		else
		{		
			while(comands[commandNum][0] != 'n')
			{

				if(commandNum == numOfComands-1) return 0;
				char tmp[LENGTHOFLINE];
				switch(comands[commandNum][0])
				{
					case 'q': ;
					return 0;

					case 'a': ;
						memset(tmp,'\0',LENGTHOFLINE);
						char *enter;
						enter = strchr(str,'\n');
						*enter = '\0';
						getCommandText(comands[commandNum],tmp);
						strcat(str,tmp);
						strcat(str,"\n");
						commandNum++;
					break;

					case 'b': ;
						memset(tmp,'\0',LENGTHOFLINE);
						getCommandText(comands[commandNum],tmp);
						strcat(tmp,str);
						strcpy(str,tmp);
						commandNum++;
					break;

					case 'i': ;
						memset(tmp,'\0',LENGTHOFLINE);
						getCommandText(comands[commandNum],tmp);
						strcat(tmp,"\n");
						fputs(tmp,stdout);
						commandNum++;
					break;

					case 's': ;
						substitude(comands[commandNum],str);
						commandNum++;
					break;

					case 'S': ;
						while(substitude(comands[commandNum],str) != -1) lineNum++;
						commandNum++;
					break;

					case 'r': ;
						removeEOL(str);
						commandNum++;
					break;

					case 'e': ;
						strcpy(str,"\n");
						commandNum++;
					break;

					case 'd': ;
						if(fgets (str, LENGTHOFLINE, fp)!=NULL){}
						for (int i = 1; (i < getCommandNumber(comands[commandNum])) && (fgets (str, LENGTHOFLINE, fp)!=NULL); i++){}
						commandNum++;
					break;

					case 'g': ;
						commandNum = getCommandNumber(comands[commandNum])-1;
					break;

					default: return -1;
				}
		
			}
			if(comands[commandNum][0] == 'n')
			{
				fputs(str,stdout);
				int numOfLinesToBePrinted = getCommandNumber(comands[commandNum]);
				for(int i = 1; (i < numOfLinesToBePrinted) && (fgets(str,LENGTHOFLINE,fp)!= NULL);i++)	fputs(str,stdout);
				commandNum++;
			}
		}
	}
	fclose(fp);
	return 0;
}
int getCommandNumber(char *command)
{
	int commandNumber = 0;
	for(int i = 1; command[i] != '\n'; i++)
	{
		commandNumber = commandNumber*10+(command[i]-48);
	}
	return commandNumber;
}
void removeEOL(char str[LENGTHOFLINE])
{
	char *enter;
	enter = strchr(str,'\n');
	*enter = '\0';
}
void getCommandText(char *comands, char tmp[LENGTHOFLINE])
{
	for(int i = 1; comands[i] != '\n'; i++)
	{
		tmp[i-1] = comands[i];
	}
}
int substitude(char *comands, char *str)
{
	char tmp[LENGTHOFLINE] = {0};
	//Co budeme nahrazovat
	char substitudeWhat[LENGTHOFLINE]  = {0};
	char separator  = comands[1];

	int i;
	//Získaváme řetězec který se bude nahrazovat
	for(i = 2; comands[i] != separator; i++)	substitudeWhat[i-2] = comands[i];
	//Za co budeme nahrazovat
	char substitudeWhatFor[LENGTHOFLINE];
	//Od kama pokračovat
	int positionInChar = i;

	for(i = i+1; comands[i] != '\0'; i++)	substitudeWhatFor[i-positionInChar-1] = comands[i];
	//Podíváeme se jestli tam vůbec je co nahradit
	char *pch;
  	pch = strstr(str,substitudeWhat);
  	if(pch == NULL) return -1;
  	//Text který je před řetězcem který máme nahradit
  	int pos = pch - str;
  	for(int i = 0; i != pos; i++)
  	{
  		tmp[i] = str[i];
  	}
  	removeEOL(substitudeWhatFor);
  	strcat(tmp,substitudeWhatFor);

  	char restOfLine[LENGTHOFLINE]  = {0};
  	pos += strlen(substitudeWhat);
  	//Přidání zbylého textu
  	for(int i = pos; str[i+1] != '\0'; i++)	restOfLine[i-pos] = str[i];
 
  	strcat(tmp,restOfLine);
  	strcat(tmp,"\n");
  	strcpy(str,tmp);
  	return 0;
}
