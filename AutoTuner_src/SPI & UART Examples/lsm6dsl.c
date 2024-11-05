/*------------------------------------------------------------------------------
  lsm6dsl.c --
  
  Description:
    Brief description of file.
	  
	  Extended description, if appropriate.
  
  Author(s):
  Last modified by: Dr. Eric M. Schwartz
  Last modified on: 7 July 2023
------------------------------------------------------------------------------*/

/********************************DEPENDENCIES**********************************/

#include <avr/io.h>
#include "lsm6dsl.h"
#include "lsm6dsl_registers.h"
#include "spi.h"

/*****************************END OF DEPENDENCIES******************************/


/*****************************FUNCTION DEFINITIONS*****************************/

/* INSERT YOUR LSM6DSL FUNCTION DEFINITIONS BELOW. */
/*------------------------------------------------------------------------------
  LSM_init -- 
  
  Description:
    Configure IMU accelerometer

  Input(s): N/A
  Output(s): N/A
------------------------------------------------------------------------------*/
void LSM_init(void)
{
	//interrupt source
	PORTC_INT0MASK = INT1;
	
	//set interrupt as an input
	PORTC_DIRCLR = INT1;
	
	//set priority low
	PORTC_INTCTRL = PORT_INT0LVL_LO_gc;
	
	//sense falling edges
	PORTC_PIN2CTRL = PORT_ISC_FALLING_gc;
	
	//enable low level interrupts
	PMIC_CTRL = PMIC_LOLVLEN_bm;
	
	//turn on global interrupts
	CPU_SREG = CPU_I_bm;
	
	int16_t wakeup_x = LSM_read(OUTX_L_XL | OUTX_H_XL);
	int16_t wakeup_y = LSM_read(OUTY_L_XL | OUTY_H_XL);
	int16_t wakeup_z = LSM_read(OUTZ_L_XL | OUTZ_H_XL);
	
	//software reset
	LSM_write(CTRL3_C, SW_RESET);
	
	//enable X, Y, and Z coordinates
	LSM_write(CTRL9_XL, DEN_XYZ);
	
	//full scale selection & output data rate
	LSM_write(CTRL1_XL, (ORD_XL_208Hz | FS_XL_2g));
	
	//enable accelerometer interrupt
	LSM_write(INT1_CTRL, INT1_DRDY_XL);
	
	}
/*------------------------------------------------------------------------------
  LSM_write -- 
  
  Description:
    Writes a single byte of data, 'data', to the address 'reg_addr', which is
	meant to be associated with an LSM register

  Input(s): Two 8-bit values read from the relevant SPI module.
  Output(s): N/A
------------------------------------------------------------------------------*/
void LSM_write(uint8_t reg_addr, uint8_t data)
{
	PORTF.OUTCLR = SS_bm;
	spi_write(reg_addr);
	spi_write(data);
	PORTF.OUTSET = SS_bm;
}
/*------------------------------------------------------------------------------
  LSM_read -- 
  
  Description:
    Returns a single byte of data from an LSM register associated with the 
	address 'reg_addr'

  Input(s): N/A
  Output(s): 8-bit value read from the relevant SPI module.
------------------------------------------------------------------------------*/
uint8_t LSM_read(uint8_t reg_addr)
{
	PORTF.OUTCLR = SS_bm;
	spi_write((uint8_t)(LSM6DSL_SPI_READ_STROBE_bm | reg_addr));
	uint8_t data = spi_read();
	PORTF.OUTSET = SS_bm;
	return data;
}
/***************************END OF FUNCTION DEFINITIONS************************/