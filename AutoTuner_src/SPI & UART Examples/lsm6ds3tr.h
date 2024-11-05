#ifndef LSM6DS3TR_H_  // Header guard.
#define LSM6DS3TR_H_

/*------------------------------------------------------------------------------
  lsm6ds3tr.h --
  
  Description:
    Provides custom data types to make it easier to handle any data
    read from the LSM6DS3TR IMU. 
	  
	  The LSM6DS3TR can output accelerometer and gyroscope data. Data from both
    of these sensors is represented in a 16-bit signed format. 
  
  Author(s): Wesley Piard & Leslye Castillo & Dr. Eric M. Schwartz
  Last modified by: Dr. Eric M. Schwartz
  Last modified on: 7 July 2023
------------------------------------------------------------------------------*/


/***********************************MACROS*************************************/

#define LSM6DS3TR_SPI_READ_STROBE_bm 				0x80
#define LSM6DS3TR_SPI_WRITE_STROBE_bm				0x00

/********************************END OF MACROS*********************************/


/*******************************CUSTOM DATA TYPES******************************/

/* Used to differentiate the accelerometer and gyroscope within the LSM6DS3TR. */
typedef enum {LSM6DS3TR_ACCEL, LSM6DS3TR_GYRO} lsm6ds3tr_module_t;

/* Can be used to contain the separated bytes of data as they are read from
 * the LSM6DS3TR. */
typedef struct lsm6ds3tr_data_raw
{
  uint8_t accel_x_low, accel_x_high;
  uint8_t accel_y_low, accel_y_high;
  uint8_t accel_z_low, accel_z_high;

  uint8_t gyro_x_low, gyro_x_high;
  uint8_t gyro_y_low, gyro_y_high;
  uint8_t gyro_z_low, gyro_z_high;
}lsm6ds3tr_data_raw_t;

/* Contains the full concatenated signed 16-bit words of data. */
typedef struct lsm6ds3tr_data_full
{
  int16_t accel_x, accel_y, accel_z;
  int16_t gyro_x, gyro_y, gyro_z;
}lsm6ds3tr_data_full_t;

/* Provides the ability to choose how to access the LSM6DS3TR data. */
typedef union lsm6ds3tr_data
{
  lsm6ds3tr_data_full_t  word;
  lsm6ds3tr_data_raw_t   byte;
}lsm6ds3tr_data_t;

/***************************END OF CUSTOM DATA TYPES***************************/


/*****************************FUNCTION PROTOTYPES******************************/

/* INSERT YOUR LSM6DS3TR FUNCTION PROTOTYPES HERE. */
void LSM_init(void);
void LSM_write(uint8_t reg_addr, uint8_t data);
uint8_t LSM_read(uint8_t reg_addr);

/**************************END OF FUNCTION PROTOTYPES**************************/

#endif // End of header guard.