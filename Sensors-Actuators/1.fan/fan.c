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
#define SERVER_NODE(ipaddr) uip_ip6addr(ipaddr,0xfd00,0,0,0,0,0,0,0x2) 					//broker ip address

#define REMOTE_PORT  UIP_HTONS(6001)

#define TOGGLE_INTERVAL 10																//periodicity



//----------------------------------PROCESSES--------------------------------------------------------------------------------------------------------------------//
PROCESS(client, "Client");																//client process
AUTOSTART_PROCESSES(&client);


//----------------------------------STATES AND VARIABLES---------------------------------------------------------------------------------------------------------//
static uip_ipaddr_t server_ipaddr;														//holds the server IP address

static struct etimer et;																//timer declaration

static int configured;																	//configuration state flag 
																						
static int subscribed;																	//subscription state flag 
																												
static int working;																		//working state flag


//----------------------------------MESSAGES AND URI CONFIGURATION-----------------------------------------------------------------------------------------------//
static char *registration_uri ="/ps/";													//registration URI 
																						
static char *id="00-01-00-00-00-00";													//MAC

static char *subscribe_uri="/ps/00-01-00-00-00-00";										


//----------------------------------OBSERVING--------------------------------------------------------------------------------------------------------------------//
static coap_observee_t *obs;															//observe relationship variable


//----------------------------------RESPONSE CALLBACK HANDLERS---------------------------------------------------------------------------------------------------//
void reg_handler(void *response){														//callback function for registration

	const uint8_t *chunk;
	
	int len = coap_get_payload(response, &chunk);
	printf("|%.*s", len, (char *)chunk);												

	printf("RESPONSE FROM THE BROKER:%s\n", (char *) chunk);																

	configured=1;																		//flag updating
	
}  


//----------------------------------NOTIFICATION CALLBACK FOR OBSERVING------------------------------------------------------------------------------------------//


static void notification_callback(coap_observee_t *obs, void *notification, coap_notification_flag_t flag)
{
  int len = 0;
  const uint8_t *payload = NULL;

  printf("Notification handler\n");
  printf("Observee URI: %s\n", obs->url);
  
  if(notification) {
    len = coap_get_payload(notification, &payload);
  }

  //(void)len;

  switch(flag) {
  case NOTIFICATION_OK:
    printf("NOTIFICATION OK: %*s\n", len, (char *)payload);
    
   
    if (*payload == '1') {
        working=1;
      } else {
        working=0;
      }															
       
    break;
  
  case OBSERVE_OK: /* server accepted observation request */
    printf("OBSERVE_OK: %*s\n", len, (char *)payload);

    	 subscribed=1;

    if (*payload == '1') {

        working=1;
        printf("WORKING");
      
      } else {

        working=0;
        printf("NOT WORKING");
      
      }		

    break;
  
  case OBSERVE_NOT_SUPPORTED:
    printf("OBSERVE_NOT_SUPPORTED: %*s\n", len, (char *)payload);
    obs = NULL;
    break;
  
  case ERROR_RESPONSE_CODE:
    printf("ERROR_RESPONSE_CODE2: %*s\n", len, (char *)payload);
    obs = NULL;
    break;
  
  case NO_REPLY_FROM_SERVER:
    printf("NO_REPLY_FROM_SERVER: "
           "removing observe registration with token %x%x\n",
           obs->token[0], obs->token[1]);
    obs = NULL;
    break;
  }
}


//----------------------------------THREAD-----------------------------------------------------------------------------------------------------------------------//
PROCESS_THREAD(client, ev, data){													//client process start
	
	PROCESS_BEGIN();

	static coap_packet_t request[1];												//coap packet creation as elem. of an array in order to use it through a pointer
	
	SERVER_NODE(&server_ipaddr);
	
	coap_init_engine();																//coap engine initialization	

	etimer_set(&et, TOGGLE_INTERVAL * CLOCK_SECOND);								//setting transmission timer

	configured=0;																	
																					
	subscribed=0;																	 
																												
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
					
											
					char msg[25];
					sprintf(msg, "<%s>;ct=0", id);


					coap_set_payload(request, (uint8_t *)msg, sizeof(msg) - 1);
					
					COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, request, 
					reg_handler);													// POST /sectorx/sensor/temperature/sensor-local-id; ct=**
										
			}
			else if(subscribed==0){													
				
					obs = coap_obs_request_registration(&server_ipaddr, REMOTE_PORT,
					subscribe_uri, notification_callback, NULL);

			}

			etimer_reset(&et);
		}
	
	}

	PROCESS_END();
}
