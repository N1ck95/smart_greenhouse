//Greenhouse project, sensors part
//Author: Enrico F. Giannico

//----------------------------------INCLUDE----------------------------------------------------------------------------------------------------------------------
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "contiki-net.h"
#include "er-coap-engine.h"
#include "dev/button-sensor.h"


//----------------------------------DEFINE-----------------------------------------------------------------------------------------------------------------------//
#define SERVER_NODE(ipaddr) uip_ip6addr(ipaddr,0xfd00,0,0,0,0,0,0,0x2) 				//broker ip address

#define REMOTE_PORT  UIP_HTONS(6001)

#define TOGGLE_INTERVAL 5															//transmission interval

#define CT 0																		//content type


//----------------------------------PROCESSES--------------------------------------------------------------------------------------------------------------------//
PROCESS(client, "Client");															//client process
AUTOSTART_PROCESSES(&client);


//----------------------------------STATES AND VARIABLES---------------------------------------------------------------------------------------------------------//
static uip_ipaddr_t server_ipaddr;													//holds the server IP address

static struct etimer et;															//transmission periodicity

static int configured;																	 
																						
static int publish;																		 
																													
static int working;


//----------------------------------MESSAGES AND URI CONFIGURATION-----------------------------------------------------------------------------------------------//
static char *registration_uri ="/ps/";													 
																						
static char *publish_uri="/ps/00-00-00-00-00-06";

static unsigned int message_number;														

static unsigned int temp;																




//----------------------------------RESPONSE CALLBACK HANDLERS---------------------------------------------------------------------------------------------------//
void reg_handler(void *response){													//response handler for registration, using the PUT method

	const uint8_t *chunk;
	
	int len = coap_get_payload(response, &chunk);
	printf("|%.*s", len, (char *)chunk);											//print of the response

	printf("RESPONSE FROM THE BROKER:%s\n", (char *) chunk);																

	configured=1;
	publish=1;																		//update og the state
	
}  



void tx_handler(void *response){													//response handler for data trasmission

	const uint8_t *chunk;
	
	int len = coap_get_payload(response, &chunk);
	printf("|%.*s", len, (char *)chunk);											//print of the response
	
	printf("DATA PUBLISHED ON BROKER\n");
}


//----------------------------------NOTIFICATION CALLBACK FOR OBSERVING------------------------------------------------------------------------------------------//




//----------------------------------THREAD-----------------------------------------------------------------------------------------------------------------------//
PROCESS_THREAD(client, ev, data){													//client process start
	
	PROCESS_BEGIN();

	static coap_packet_t request[1];												//coap packet creation as elem. of an array in order to use it through a pointer
	
	SERVER_NODE(&server_ipaddr);
	
	coap_init_engine();																//coap engine initialization	

	etimer_set(&et, TOGGLE_INTERVAL * CLOCK_SECOND);								//setting  transmission timer

	configured=0;																	 
																					
	publish=0;																		 
																												
	working=0;																		

	printf("PROCESS STARTED\n");

			
	while(1){

		printf("WHILE STARTED\n");
					
		PROCESS_YIELD();

				
		if(etimer_expired(&et)){

			printf("TIMER EXPIRED\n");
			
			if(configured==0){														//not yet configured

					printf("CREATION OF THE TOPIC STARTED\n");
					
					coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);		//packet creation
					coap_set_header_uri_path(request, registration_uri);					
					
					const char msg[] = "<00-00-00-00-00-06>;ct=0";

      				coap_set_payload(request, (uint8_t *)msg, sizeof(msg) - 1);
					
					COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, 
					request, reg_handler);											// POST /sectorx/sensor/temperature/sensor-local-id; ct=**
										
			}
			else if(publish==1){													
				
					

					if (message_number>=0 && message_number<5) 						//morning
				      temp=5;

				    else if (message_number>5 && message_number<10)					//afternoon
				      temp=15;

				    else if (message_number>10 && message_number<15)				//evening
				      temp=13;

				 	else if (message_number>15 && message_number<=19)				//night
				      temp=4;

				    char msg[3];													

				    sprintf(msg, "%u", temp);
				    printf("SMOKE:%s\n",msg);
				    
				    message_number++;

				    if(message_number==19)
				      message_number=0;

					coap_init_message(request, COAP_TYPE_CON, COAP_PUT, 0);			//packet creation
					coap_set_header_uri_path(request, publish_uri);
					coap_set_payload(request, (uint8_t *)msg, sizeof(msg) - 1);
					coap_set_header_content_format(request, CT);
					
					COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, request, 	// PUT /sectorx/sensor/temperature/id tempdata
					tx_handler);														
																					
					
			}

			etimer_reset(&et);
		}
	
	}

	PROCESS_END();
}
