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
//#define SERVER_NODE(ipaddr)   uip_ip6addr(ipaddr, 0xfe80, 0, 0, 0, 0x0212, 0x7402, 0x0002, 0x0202)      /* cooja2 */

#define REMOTE_PORT  UIP_HTONS(COAP_DEFAULT_PORT)

#define TOGGLE_INTERVAL 10																//transmission interval

#define CT 50																			//content type


//----------------------------------PROCESSES--------------------------------------------------------------------------------------------------------------------//
PROCESS(client, "Client");																//client process
AUTOSTART_PROCESSES(&client);


//----------------------------------STATES AND VARIABLES---------------------------------------------------------------------------------------------------------//
static uip_ipaddr_t server_ipaddr;														//holds the server IP address

static struct etimer et;																//transmission periodicity

static int configured;																	//flag to decide if proceed with registration or subscription, 
																						//by default not configured
static int subscribed1;																	//flag to decide if proceed with subscription or topic creation, 
																						//by default not subscribed							
static int topiccreated;																//flag to decide if proceed with topic creation or sending data, 
																						//by default topic has not been created		
static int subscribed2;																	//flag to decide if sending data or not

static int working;


//----------------------------------MESSAGES AND URI CONFIGURATION-----------------------------------------------------------------------------------------------//
static char *registration_uri ="/ps/";													//standard topic path for registration, global identifier has to be 
																						//added to permit registration to the broker
static char *id="00-17-07-11-29-49";

static char *sensor="/actuator/sprinkler/";

//static char *subscribe_uri1 ="/ps/00-17-07-11-29-49";
static char *subscribe_uri1;

static char *zone;

static char *topic_uri;																	//uri da ricevere dal broker su cui poi creare il topic su cui pubblicare i dati

static char *subscribe_uri2;


//----------------------------------OBSERVING--------------------------------------------------------------------------------------------------------------------//
static coap_observee_t *obs1;															//observe relationship variable

static coap_observee_t *obs2;															//observe relationship variable


//----------------------------------RESPONSE CALLBACK HANDLERS---------------------------------------------------------------------------------------------------//
void reg_handler(void *response){														//response handler for registration, using the PUT method

	const uint8_t *chunk;
	
	int len = coap_get_payload(response, &chunk);
	printf("|%.*s", len, (char *)chunk);												//print of the response

	subscribe_uri1 = malloc(sizeof(char)*50);
	strcpy(subscribe_uri1, registration_uri);											// /ps/
	strcat(subscribe_uri1, id);															// /ps/00-17-07-11-29-49

	printf("CONFIGURED\n");																

	configured=1;																		//update og the state
	
}  


void topic_handler(void *response){														//response handler for topic creation, using the POST method

	const uint8_t *chunk;
	
	int len = coap_get_payload(response, &chunk);
	printf("|%.*s", len, (char *)chunk);												//print of the response

	subscribe_uri2 = malloc(sizeof(char)*100);
	strcpy(subscribe_uri2, topic_uri);													// /ps/sectorx/actuator/sprinkler/
	strcat(subscribe_uri2, id);															// /ps/sectorx/actuator/sprinkler/00-17-07-11-29-49
	
	printf("TOPIC ON WHICH RECEIVE DATA CREATED\n");

	topiccreated=1;
	

} 


//----------------------------------NOTIFICATION CALLBACK FOR OBSERVING------------------------------------------------------------------------------------------//
static void notification_callback1(coap_observee_t *obs, void *notification, coap_notification_flag_t flag)
{
  int len = 0;
  const char *payload = NULL;

  printf("Notification handler\n");
  printf("Observee URI: %s\n", obs->url);
  
  if(notification) {
    len = coap_get_payload(notification, &payload);
  }

  //(void)len;

  switch(flag) {
  case NOTIFICATION_OK:
    printf("NOTIFICATION OK: %*s\n", len, (char *)payload);
    															
       	zone = malloc(sizeof(char)*15);													//buffer su cui memorizzare il settore ricevuto dal broker
		strcpy(zone, payload); 															//copio il settore ricevuto dal server, sectorx 
		topic_uri = malloc(sizeof(char)*100);
		strcpy(topic_uri, registration_uri);											// /ps/
		strcat(topic_uri, zone);														// /ps/sectorx
		strcat(topic_uri, sensor);   													// /ps/sectorx/actuator/sprinkler/

    	printf("SUBSCRIBED");

    	subscribed1=1; 																	//subscribed state updated
    
    break;
  
  case OBSERVE_OK: /* server accepted observation request */
    printf("OBSERVE_OK: %*s\n", len, (char *)payload);

    	zone = malloc(sizeof(char)*15);													//buffer su cui memorizzare il settore ricevuto dal broker
		strcpy(zone, payload); 															//copio il settore ricevuto dal server, sectorx 
		topic_uri = malloc(sizeof(char)*100);
		strcpy(topic_uri, registration_uri);											// /ps/
		strcat(topic_uri, zone);														// /ps/sectorx
		strcat(topic_uri, sensor);   													// /ps/sectorx/sensor/temperature/

    	printf("SUBSCRIBED");

    	subscribed1=1; 

    break;
  
  case OBSERVE_NOT_SUPPORTED:
    printf("OBSERVE_NOT_SUPPORTED: %*s\n", len, (char *)payload);
    obs = NULL;
    break;
  
  case ERROR_RESPONSE_CODE:
    printf("ERROR_RESPONSE_CODE: %*s\n", len, (char *)payload);
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

static void notification_callback2(coap_observee_t *obs, void *notification, coap_notification_flag_t flag)
{
  int len = 0;
  const char *payload = NULL;

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

    	 subscribed2=1;

    if (*payload == '1') {
        working=1;
      } else {
        working=0;
      }		

    break;
  
  case OBSERVE_NOT_SUPPORTED:
    printf("OBSERVE_NOT_SUPPORTED: %*s\n", len, (char *)payload);
    obs = NULL;
    break;
  
  case ERROR_RESPONSE_CODE:
    printf("ERROR_RESPONSE_CODE: %*s\n", len, (char *)payload);
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
PROCESS_THREAD(client, ev, data){														//client process start
	
	PROCESS_BEGIN();

	static coap_packet_t request[1];													//coap packet creation as elem. of an array in order to use it through a pointer
	
	SERVER_NODE(&server_ipaddr);
	
	coap_init_engine();																	//coap engine initialization	

	etimer_set(&et, TOGGLE_INTERVAL * CLOCK_SECOND);									//setting  transmission timer

	configured=0;																		//flag to decide if proceed with registration or subscription, 
																						//by default not configured
	subscribed1=0;																		//flag to decide if proceed with subscription or topic creation, 
																						//by default not subscribed							
	topiccreated=0;																		//flag to decide if proceed with topic creation or sending data, 
																						//by default topic has not been created		
	subscribed2=0;

	working=0;																			//flag to decide if sending data or not

	printf("PROCESS STARTED\n");

			
	while(1){

		printf("WHILE STARTED\n");
					
		PROCESS_YIELD();

				
		if(etimer_expired(&et)){

			printf("TIMER EXPIRED\n");
			
			if(configured==0){																//not yet configured

					printf("CONFIGURATION STARTED\n");
					
					coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);				//packet creation
					coap_set_header_uri_path(request, registration_uri);							//topic_url da comporre +++++++++++++++++++++++++++++++++++++++++++++++++
					
					//char msg[]="<00-17-07-11-29-49>;ct=50";									//messaggio da inviare dopo l'uri,
					char msg[26];
					sprintf(msg, "<%s>;ct=%u", id, CT);


					coap_set_payload(request, (uint8_t *)msg, sizeof(msg) - 1);
					
					COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, request, reg_handler);// POST /sectorx/sensor/temperature/sensor-local-id; ct=**
										
			}
			else if(subscribed1==0){															//already configured, subscription phase DA SISTEMARE /SOSTITUIRE CON COAP REGISTRATION coap_obs_request_registration(server_ipaddr, REMOTE_PORT, OBS_RESOURCE_URI, notification_callback, NULL);
				
					obs1 = coap_obs_request_registration(&server_ipaddr, REMOTE_PORT, subscribe_uri1, notification_callback1, NULL);

			}
			else if(topiccreated==0){														//already subscribed, topic creation phase
				
					coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);				//packet creation
					coap_set_header_uri_path(request, topic_uri);							//topic_uri: /ps/sectorx/sensor/temperature/
					
					//char msg[]="<00-17-07-11-29-49>;ct=50";									//messaggio da inviare dopo l'uri, il MAC del sensore e il content type
					char msg[26];
					sprintf(msg, "<%s>;ct=%u", id, CT);

					coap_set_payload(request, (uint8_t *)msg, sizeof(msg) - 1);
					COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, request, topic_handler);// POST /ps/sectorx/sensor/temperature/ <MAC>; ct=**
																							//impostando come callback topic_handler
			}
			else if(subscribed2==0){														//ready to send data
				
					obs2 = coap_obs_request_registration(&server_ipaddr, REMOTE_PORT, subscribe_uri2, notification_callback2, NULL);																
					
			}

			etimer_reset(&et);
		}
	
	}

	PROCESS_END();
}