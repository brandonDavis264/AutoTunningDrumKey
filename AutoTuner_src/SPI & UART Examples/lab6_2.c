/*
 * CFile1.c
 *
 * Created: 10/28/2023 10:23:59 PM
 *  Author: 
 */ 
#include <avr/io.h>
#include "spi.h"

int main() {
	
	spi_init();
	
	while(1){
		
		PORTF.OUTCLR = SS_bm;
		spi_write(0x4c);
		spi_read();
		PORTF.OUTSET = SS_bm;
	}

	return 0;
}