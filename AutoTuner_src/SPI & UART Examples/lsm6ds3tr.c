/*------------------------------------------------------------------------------
  lsm6ds3tr.c --
  
  Description:
    Brief description of file.
	  
	  Extended description, if appropriate.
  
  Author(s):
  Last modified by: Dr. Eric M. Schwartz
  Last modified on: 7 July 2023
------------------------------------------------------------------------------*/

/********************************DEPENDENCIES**********************************/

#include <avr/io.h>
#include "lsm6ds3tr.h"
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
void LSM_init(void){
	
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
	spi_write((uint8_t)(0x80 | reg_addr));
	uint8_t data = spi_read();
	PORTF.OUTSET = SS_bm;
	return data;
}
/***************************END OF FUNCTION DEFINITIONS************************/