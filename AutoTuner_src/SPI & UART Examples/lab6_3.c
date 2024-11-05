///*
 //* CFile1.c
 //*
 //* Created: 10/30/2023 11:04:20 AM
 //*  Author: henoc
 //*/ 
#include <avr/io.h>
#include "spi.h"
#include "lsm6dsl.h"
#include "lsm6dsl_registers.h"

int main() {
	
	spi_init();
	
	volatile uint8_t temp_var = 0x37;
	
	while(1){
		
		temp_var = LSM_read(WHO_AM_I);
		asm volatile ("nop");
	}

	return 0;
}