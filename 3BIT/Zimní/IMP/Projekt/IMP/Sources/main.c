/* Header file with all the essential definitions for a given type of MCU */
#include "MK60D10.h"
#include <stdio.h>
#include <string.h>


#define BTN_SW6 0x800     // Port E, bit 11

#define GPIO_PIN_MASK	0x1Fu
#define GPIO_PIN(x)		(((1)<<(x & GPIO_PIN_MASK)))
#define MSGCOUNT 4

int msgIndex = 0;
int runCounter = 0;

struct character {
	char code;
	char value[5][8];
};
struct message {
	int length;
	char message[512][8];
};
struct message myMessages[MSGCOUNT];
struct character alphabet[28] =
{
		{'A', {"01111111", "10010000", "10010000", "10010000", "01111111"}},
		{'B', {"11111111", "10010001", "10010001", "10010001", "01101110"}},
		{'C', {"01111110", "10000001", "10000001", "10000001", "01000010"}},
		{'D', {"11111111", "10000001", "10000001", "10000001", "01111110"}},
		{'E', {"11111111", "10001001", "10001001", "10001001", "10000001"}},
		{'F', {"11111111", "10001000", "10001000", "10001000", "10000000"}},
		{'G', {"01111110", "10000001", "10000001", "10000101","01000111"}},
		{'H', {"11111111", "00001000", "00001000", "00001000", "11111111"}},
		{'I', {"00000000", "00000000", "11111111", "00000000", "00000000"}},
		{'J', {"00000110", "00000001", "00000001", "10000001", "11111110"}},
		{'K', {"11111111", "00001000", "00010100", "00100010", "01000001"}},
		{'L', {"11111111", "00000001", "00000001", "00000001", "00000001"}},
		{'M', {"11111111", "00100000", "00010000", "00100000", "11111111"}},
		{'N', {"11111111", "00100000", "00010000", "00001000","11111111"}},
		{'O', {"01111110", "10000001", "10000001", "10000001","01111110"}},
		{'P', {"11111111", "10001000", "10001000", "10001000","01110000"}},
		{'Q', {"01111110", "10000001", "10000001", "10000010","01111101"}},
		{'R', {"11111111", "10001000", "10001000", "10001000","01110111"}},
		{'S', {"01110001", "10001001", "10001001", "10001001","10000110"}},
		{'T', {"10000000", "10000000", "11111111", "10000000","10000000"}},
		{'U', {"11111110", "00000001", "00000001", "00000001","11111110"}},
		{'V', {"11100000", "00011100", "00000111", "00011100","11100000"}},
		{'W', {"11111111", "00000010", "00111100", "00000010","11111111"}},
		{'X', {"11000011", "00100100", "00011000", "00100100","11000011"}},
		{'Y', {"11000000", "00100000", "00011111", "00100000","11000000"}},
		{'Z', {"10000011", "10000101", "10011001", "10100001","11000001"}},
		{'!', {"00000000", "00000000", "11111101", "00000000","00000000"}},
		{' ', {"00000000", "00000000", "00000000", "00000000","00000000"}},
		{'_', {"11111111", "11111111", "11111111", "11111111","11111111"}}
};
/* Configuration of the necessary MCU peripherals */
void SystemConfig() {
	/* Turn on all port clocks */
	SIM->SCGC5 = SIM_SCGC5_PORTA_MASK | SIM_SCGC5_PORTE_MASK;

	/* Set corresponding PTA pins (column activators of 74HC154) for GPIO functionality */
	PORTA->PCR[8] = ( 0|PORT_PCR_MUX(0x01) );  // A0
	PORTA->PCR[10] = ( 0|PORT_PCR_MUX(0x01) ); // A1
	PORTA->PCR[6] = ( 0|PORT_PCR_MUX(0x01) );  // A2
	PORTA->PCR[11] = ( 0|PORT_PCR_MUX(0x01) ); // A3

	/* Set corresponding PTA pins (rows selectors of 74HC154) for GPIO functionality */
	PORTA->PCR[26] = ( 0|PORT_PCR_MUX(0x01) );  // R0
	PORTA->PCR[24] = ( 0|PORT_PCR_MUX(0x01) );  // R1
	PORTA->PCR[9] = ( 0|PORT_PCR_MUX(0x01) );   // R2
	PORTA->PCR[25] = ( 0|PORT_PCR_MUX(0x01) );  // R3
	PORTA->PCR[28] = ( 0|PORT_PCR_MUX(0x01) );  // R4
	PORTA->PCR[7] = ( 0|PORT_PCR_MUX(0x01) );   // R5
	PORTA->PCR[27] = ( 0|PORT_PCR_MUX(0x01) );  // R6
	PORTA->PCR[29] = ( 0|PORT_PCR_MUX(0x01) );  // R7

	/* Set corresponding PTE pins (output enable of 74HC154) for GPIO functionality */
	PORTE->PCR[28] = ( 0|PORT_PCR_MUX(0x01) ); // #EN


	/* Change corresponding PTA port pins as outputs */
	PTA->PDDR = GPIO_PDDR_PDD(0x3F000FC0);

	/* Change corresponding PTE port pins as outputs */
	PTE->PDDR = GPIO_PDDR_PDD( GPIO_PIN(28) );


	PORTE->PCR[11] =
			( PORT_PCR_ISF(0x01)
				| PORT_PCR_IRQC(0x0A)
				| PORT_PCR_MUX(0x01)
				| PORT_PCR_PE(0x01)
				| PORT_PCR_PS(0x01)
			);



	NVIC_ClearPendingIRQ(PORTE_IRQn);
	NVIC_EnableIRQ(PORTE_IRQn);
}


void delay(int t1, int t2)
{
	int i, j;

	for(i=0; i<t1; i++) {
		for(j=0; j<t2; j++);
	}
}
/* Conversion of requested column number into the 4-to-16 decoder control.  */
void column_select(unsigned int col_num)
{
	unsigned i, result, col_sel[4];

	for (i =0; i<4; i++) {
		result = col_num / 2;	  // Whole-number division of the input number
		col_sel[i] = col_num % 2;
		col_num = result;

		switch(i) {

			// Selection signal A0
		    case 0:
				((col_sel[i]) == 0) ? (PTA->PDOR &= ~GPIO_PDOR_PDO( GPIO_PIN(8))) : (PTA->PDOR |= GPIO_PDOR_PDO( GPIO_PIN(8)));
				break;

			// Selection signal A1
			case 1:
				((col_sel[i]) == 0) ? (PTA->PDOR &= ~GPIO_PDOR_PDO( GPIO_PIN(10))) : (PTA->PDOR |= GPIO_PDOR_PDO( GPIO_PIN(10)));
				break;


			case 2:
				((col_sel[i]) == 0) ? (PTA->PDOR &= ~GPIO_PDOR_PDO( GPIO_PIN(6))) : (PTA->PDOR |= GPIO_PDOR_PDO( GPIO_PIN(6)));
				break;

			// Selection signal A3
			case 3:
				((col_sel[i]) == 0) ? (PTA->PDOR &= ~GPIO_PDOR_PDO( GPIO_PIN(11))) : (PTA->PDOR |= GPIO_PDOR_PDO( GPIO_PIN(11)));
				break;


			default:
				break;
		}
	}
}
/* Zapání øádky ve které mají hodnotu 1  */
void row_select(char* row_select)
{
	for (int r=0; r<=7; r++) {
		switch(r) {

			// Selection signal R0
			case 0:
				((row_select[r]) == '0') ? (PTA->PDOR &= ~GPIO_PDOR_PDO( GPIO_PIN(26))) : (PTA->PDOR |= GPIO_PDOR_PDO( GPIO_PIN(26)));
				break;
			// Selection signal R1
			case 1:
				((row_select[r]) == '0') ? (PTA->PDOR &= ~GPIO_PDOR_PDO( GPIO_PIN(24))) : (PTA->PDOR |= GPIO_PDOR_PDO( GPIO_PIN(24)));
				break;
			// Selection signal R2
			case 2:
				((row_select[r]) == '0') ? (PTA->PDOR &= ~GPIO_PDOR_PDO( GPIO_PIN(9))) : (PTA->PDOR |= GPIO_PDOR_PDO( GPIO_PIN(9)));
				break;
			// Selection signal R3
			case 3:
				((row_select[r]) == '0') ? (PTA->PDOR &= ~GPIO_PDOR_PDO( GPIO_PIN(25))) : (PTA->PDOR |= GPIO_PDOR_PDO( GPIO_PIN(25)));
				break;
			// Selection signal R4
			case 4:
				((row_select[r]) == '0') ? (PTA->PDOR &= ~GPIO_PDOR_PDO( GPIO_PIN(28))) : (PTA->PDOR |= GPIO_PDOR_PDO( GPIO_PIN(28)));
				break;
			// Selection signal R5
			case 5:
				((row_select[r]) == '0') ? (PTA->PDOR &= ~GPIO_PDOR_PDO( GPIO_PIN(7))) : (PTA->PDOR |= GPIO_PDOR_PDO( GPIO_PIN(7)));
				break;
			// Selection signal R6
			case 6:
				((row_select[r]) == '0') ? (PTA->PDOR &= ~GPIO_PDOR_PDO( GPIO_PIN(27))) : (PTA->PDOR |= GPIO_PDOR_PDO( GPIO_PIN(27)));
				break;
			// Selection signal R7
			case 7:
				((row_select[r]) == '0') ? (PTA->PDOR &= ~GPIO_PDOR_PDO( GPIO_PIN(29))) : (PTA->PDOR |= GPIO_PDOR_PDO( GPIO_PIN(29)));
				break;

			default:
				break;
		}
	}
}
void msg_create(char* newMessage, int myMessageIndex)
{
	int len = strlen( newMessage ); // Delka zpravy
	int numberOfCharacters = 28; // Pocet znaku abecedy
	int counter = 0; // Pozici na kterou mame zapisovat
	int charNotFound = 0; // Pro kontrolu nenznamych znaku
	// Dává na zaèátek zprávy mezery
	for(int i = 0; i < 16; i++)
	{
		strcpy(myMessages[myMessageIndex].message[i], "00000000");
	}
	counter += 16;
	for (int i = 0; i < len; i++){
		charNotFound = 0;
		for (int j = 0; j < numberOfCharacters; j++){
			if(newMessage[i] == alphabet[j].code)
			{
				charNotFound = 1;
				strcpy(myMessages[myMessageIndex].message[counter + 0], alphabet[j].value[0]);
				strcpy(myMessages[myMessageIndex].message[counter + 1], alphabet[j].value[1]);
				strcpy(myMessages[myMessageIndex].message[counter + 2], alphabet[j].value[2]);
				strcpy(myMessages[myMessageIndex].message[counter + 3], alphabet[j].value[3]);
				strcpy(myMessages[myMessageIndex].message[counter + 4], alphabet[j].value[4]);
				strcpy(myMessages[myMessageIndex].message[counter + 5], "00000000");
				counter += 6;
			}
		}
		if(charNotFound == 0)
		{
			strcpy(myMessages[myMessageIndex].message[counter + 0], alphabet[28].value[0]);
			strcpy(myMessages[myMessageIndex].message[counter + 1], alphabet[28].value[1]);
			strcpy(myMessages[myMessageIndex].message[counter + 2], alphabet[28].value[2]);
			strcpy(myMessages[myMessageIndex].message[counter + 3], alphabet[28].value[3]);
			strcpy(myMessages[myMessageIndex].message[counter + 4], alphabet[28].value[4]);
			strcpy(myMessages[myMessageIndex].message[counter + 5], "00000000");
			counter += 6;
		}
	}
	// Dává na konec zprávy mezery
	for(int i = 0; i < 16; i++)
	{
		strcpy(myMessages[myMessageIndex].message[counter + i], "00000000");
	}
	// Poèítá délku zprávy
	// pocet znaku * 6 (znak ma sirku 5 plus jednu mezeru) + mezera pøed
	myMessages[myMessageIndex].length = len*6+1*(16);
}
void PORTE_IRQHandler(void) {

	delay(500,1);
	if (PORTE->ISFR & BTN_SW6)
	{
		runCounter = 0;
		msgIndex += 1;
		if(msgIndex > MSGCOUNT-1)
			msgIndex = 0;
	}
	PORTE->ISFR = BTN_SW6;
}
int main(void)
{
	SystemConfig();
	msg_create("TOHLE JE PRVNI ZPRAVA", 0);
	msg_create("| TOHLE JE ZPRAVA S NEZNAMYM ZNAKEM |", 1);
	msg_create("TOHLE JE VEEEEEEEEEEEEEEEEEELMI DLOUHA ZPRAVA", 2);
	msg_create("MALA", 3);
	int msgSpeed = 8; // Rychlost jakou se zprava posunuje
    for (;;) {
    	for (int x=0; x<msgSpeed; x++) {
			for (int c=0; c<16; c++) {
	    		delay(500,1);
				column_select(c);
				row_select(myMessages[msgIndex].message[c+runCounter]);
			}
    	}
    	runCounter++;
		if(runCounter >= myMessages[msgIndex].length)
		{
			// Jsme na konci zpravy tak se vracime na zacatek
			runCounter = 0;
		}
    }
    /* Never leave main */
    return 0;
}
