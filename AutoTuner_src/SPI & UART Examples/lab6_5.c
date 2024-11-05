/*
 * CFile1.c
 *
 * Created: 11/4/2023 10:54:53 AM
 *  Author: henoc
 */ 
#include <avr/io.h>
#include <avr/interrupt.h>
#include "spi.h"
#include "lsm6dsl.h"
#include "lsm6dsl_registers.h"
#include "usart.h"

volatile uint8_t x_axis_l;
volatile uint8_t x_axis_h;
volatile uint8_t y_axis_l;
volatile uint8_t y_axis_h;
volatile uint8_t z_axis_l;
volatile uint8_t z_axis_h;
volatile uint8_t accel_flag = 0;

int main()
{
	spi_init();
	LSM_init();
	usartd0_init();
	
	while(1){
		if (accel_flag)
		{
			x_axis_l = LSM_read(OUTX_L_XL);
			transmit(x_axis_l);
			x_axis_h = LSM_read(OUTX_H_XL);
			transmit(x_axis_h);
			y_axis_l = LSM_read(OUTY_L_XL);
			transmit(y_axis_l);
			y_axis_h = LSM_read(OUTY_H_XL);
			transmit(y_axis_h);
			z_axis_l = LSM_read(OUTZ_L_XL);
			transmit(z_axis_l);
			z_axis_h = LSM_read(OUTZ_H_XL);
			transmit(z_axis_h);
			accel_flag = 0;
			CPU_SREG = CPU_I_bm;
		}
	}
	
	return 0;
}

void transmit(uint8_t XL_data)
{
	usartd0_out_char(XL_data);
}

ISR(PORTC_INT0_vect)
{
  CPU_SREG = 0x00;
  accel_flag = 1;	
}