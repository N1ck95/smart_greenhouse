//Greenhouse project, sensors part
//Author: Enrico F. Giannico

//----------------------------------INCLUDE----------------------------------------------------------------------------------------------------------------------
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "contiki-net.h"
#include "er-coap-engine.h"

//da rpl-node.c
#include "lib/random.h"
#include "sys/ctimer.h"
#include "sys/etimer.h"
#include "net/ip/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ip/uip-debug.h"
#include "node-id.h"
#include "simple-udp.h"   
//

//----------------------------------DEFINE-----------------------------------------------------------------------------------------------------------------------//
#define SERVER_NODE(ipaddr) uip_ip6addr(ipaddr,0xfd00,0,0,0,0,0,0,0x1) 					//broker ip address
#define REMOTE_PORT  UIP_HTONS(COAP_DEFAULT_PORT)
#define TX_INTERVAL 10																	//transmission interval
#define REG_INTERVAL 10																	//registration interval (in case of failures of the registration phase)
#define SUB_INTERVAL 10																	//subscription interval (in case of failures of the subscription phase)
#define TOPIC_INTERVAL 10																//topic interval (in case of failures of the subscription phase)

//static struct simple_udp_connection unicast_connection;								//struttura per gestire ipv6 (non dovrebbe servire)

PROCESS(client,"Client");																//client process
AUTOSTART_PROCESSES(&client);

uip_ipaddr_t server_ipaddr;



//----------------------------------TIMERS-----------------------------------------------------------------------------------------------------------------------//
static struct etimer regtimer;															//registration periodicity
static struct etimer subtimer;															//subscription periodicity
static struct etimer topictimer;														//topic creation periodicity
static struct etimer et;																//transmission periodicity



//----------------------------------STATES-----------------------------------------------------------------------------------------------------------------------//
static int configured=0;																//flag to decide if proceed with registration or subscription, 
																						//by default not configured
static int subscribed=0;																//flag to decide if proceed with subscription or topic creation, 
																						//by default not subscribed							
static int topiccreated=0;																//flag to decide if proceed with topic creation or sending data, 
																						//by default topic has not been created		
static int removed=0;																	//flag to decide if the sensor node has been removed from the deployment



//----------------------------------MESSAGES AND URI CONFIGURATION-----------------------------------------------------------------------------------------------//
static unsigned int message_number;														//for simulating a distribution of temperature during the day
static unsigned int temp;																//temperature variable
//char buf[180];																		//buffer for the temperature value+++++++++++++++++++++++++++++++++++++++

char *registration_url_without_MAC ="/ps/register/";									//standard topic path for registration, global identifier has to be 
																						//added to permit registration to the broker

static char *uri;																		//uri that i need to receive from the broker to create the topic

//----------------------------------OBSERVING--------------------------------------------------------------------------------------------------------------------//

static coap_observee_t *obs;															//observe relationship variable


//----------------------------------RESPONSE CALLBACK HANDLERS---------------------------------------------------------------------------------------------------//
void reg_handler(void *response){														//response handler for registration, using the PUT method

	const uint8_t *chunk;
	int len = coap_get_payload(response, &chunk);
	printf("|%.*s", len, (char *)chunk);												//print of the response

	if(response == "2.01 Created")														//da sistemare la sintassi ++++++++++++++++++++++++++++++++++++++++++++++
		configured=1;
	else
		configured=0;
}  

void sub_handler(void *response){														//response handler for subscription, using the GET method

	const uint8_t *chunk;
	int len = coap_get_payload(response, &chunk);
	printf("|%.*s", len, (char *)chunk);												//print of the response

	if(response == "2.05 Content")														//da sistemare la sintassi ++++++++++++++++++++++++++++++++++++++++++++++
		subscribed=1;

		obs_resource_uri="";															//mi copio il contenuto, cioè l'identificatore locale, su cui poi devo 
																						//andare a pubblicare, da aggiungere+++++++++++++++++++++++++++++++++++++

		printf("Starting observation\n");												//creation of the observing relationship, for receiving updates of the sensor
																						//position in the deployment

    	obs = coap_obs_request_registration(server_ipaddr, REMOTE_PORT, obs_resource_uri, notification_callback, NULL);


	else
		subscribed=0;
} 

void topic_handler(void *response){														//response handler for topic creation, using the POST method

	const uint8_t *chunk;
	int len = coap_get_payload(response, &chunk);
	printf("|%.*s", len, (char *)chunk);												//print of the response

	if(response == "2.01 Created")														//da sistemare la sintassi ++++++++++++++++++++++++++++++++++++++++++++++
		topiccreated=1;

		//potrei stampare il messaggio ricevuto******************************************************************************************************************

	else
		topiccreated=0;
} 

void tx_handler(void *response){														//response handler for data trasmission

	const uint8_t *chunk;
	int len = coap_get_payload(response, &chunk);
	printf("|%.*s", len, (char *)chunk);												//print of the response

	if(response == "2.01 Created" || response == "2.04 Changed")						//da sistemare la sintassi ++++++++++++++++++++++++++++++++++++++++++++++
		printf("The broker has received the temperature", len, (char *)chunk);			//print of the response
		
	else
		printf("The broker has NOT received the temperature", len, (char *)chunk);		//print of the response
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
  switch(flag) {
  case NOTIFICATION_OK:
    printf("NOTIFICATION OK: %*s\n", len, (char *)payload);
    															
    if(payload==NULL)																
    	removed=1;
    else{
    	//remove the previous topic 													//e.g using the DELETE method++++++++++++++++++++++++++++++++++++++++++++ 

    	//uri=(char *)payload)															//acquisition of the uri on which i will publish my data+++++++++++++++++

    	//topiccreated=0; 																//reset topiccreated, so, when restart the while, the thread will create
    																					//a new topic with the new uri recived+++++++++++++++++++++++++++++++++++
    }
    break;
  case OBSERVE_OK: /* server accepeted observation request */
    printf("OBSERVE_OK: %*s\n", len, (char *)payload);
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



//----------------------------------MAC/IPV6 CONFIGURATION-------------------------------------------------------------------------------------------------------//
static void set_global_address(void)													//ipv6 address setting 
{
  uip_ipaddr_t ipaddr; 																	//struttura fornita dal s.o. per far si che un nodo sensore utilizzi
  																						//capacità ipv6, in particolare rappresenta un indirizzo ipv6
  int i;
  uint8_t state;

  uip_ip6addr(&ipaddr, 0xaaaa, 0, 0, 0, 0, 0, 0, 0);  									//per settare un indirizzo ipv6
  uip_ds6_set_addr_iid(&ipaddr, &uip_lladdr);
  uip_ds6_addr_add(&ipaddr, 0, ADDR_AUTOCONF);        									//queste due funzioni servono ad assegnare un indirizzo ipv6 basato 
  																						//sul mac dello specifico nodo sensore

  printf("IPv6 addresses: ");                         									//stampa tutti ind.ipv6 assegnati fino ad ora al wireless tranceiver
  for(i = 0; i < UIP_DS6_ADDR_NB; i++) {
    state = uip_ds6_if.addr_list[i].state;
    if(uip_ds6_if.addr_list[i].isused &&
       (state == ADDR_TENTATIVE || state == ADDR_PREFERRED)) {
      uip_debug_ipaddr_print(&uip_ds6_if.addr_list[i].ipaddr);
      printf("\n");
      /* hack to make address "final" */
      if (state == ADDR_TENTATIVE) {
	uip_ds6_if.addr_list[i].state = ADDR_PREFERRED;
      }
    }
  }
}															

//----------------------------------THREAD-----------------------------------------------------------------------------------------------------------------------//

PROCESS_THREAD(client, ev, data){														//client process start
	
	PROCESS_BEGIN();

	uip_ipaddr_t addr;																	//da controllare

	static coap_packet_t request[1];													//coap packet creation as elem. of an array in order to use it through a pointer
	SERVER_NODE(&server_ipaddr);
	coap_init_engine();																	//coap engine initialization
	
	etimer_set(&et,TX_INTERVAL*CLOCK_SECOND);											//setting  transmission timer
	etimer_set(&regtimer,REG_INTERVAL*CLOCK_SECOND);									//setting  registration timer
	etimer_set(&subtimer,SUB_INTERVAL*CLOCK_SECOND);									//setting  subscription timer
	etimer_set(&topictimer,TOPIC_INTERVAL*CLOCK_SECOND);								//setting  topic timer

	set_global_address();                                 								//settaggio indirizzo ipv6 in base al mac




	while(1){

		if(configured==0){																//not yet configured

			if(etimer_expired(&regtimer)){												//if registration timer expired
				coap_init_message(request, COAP_TYPE_CON, COAP_PUT, 0);					//packet creation
				coap_set_header_uri_path(request, registration_url);					//registration_url da comporre a partire da quello senza MAC ++++++++++++

				char msg[]="ind. nodo sensore(da riempire al posto di questo testo)";	//creazione del messaggio da inviare, cioè l'identificatore globale del nodo sensore

				coap_set_payload(request, (uint8_t *)msg, sizeof(msg) - 1);				//creazione del payload da inviare

				COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, request, reg_handler);//invio del pacchetto coap, impostando come callback reg_handler
				
				etimer_reset(&regtimer);												//resetto il timer
			}
		}
		else if(subscribed==0){															//already configured, subscription phase
			if(etimer_expired(&subtimer)){												//if timer expired
				coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);					//packet creation
				coap_set_header_uri_path(request, subscribe_url);						//subscribe_url da comporre a partire da quello senza MAC +++++++++++++++
				COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, request, sub_handler);//invio del pacchetto coap, impostando come callback sub_handler
				etimer_reset(&subtimer);												//resetto il timer

			}
		}
		else if(topiccreated==0){														//already subscribed, topic creation phase
			if(etimer_expired(&topictimer)){											//if timer expired
				coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);				//packet creation
				coap_set_header_uri_path(request, topic_url);							//topic_url da comporre +++++++++++++++++++++++++++++++++++++++++++++++++
				
				char msg[]="ct=**";														//messaggio da inviare dopo l'uri,

				COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, request, topic_handler);// POST /sectorx/sensor/temperature/sensor-local-id; ct=**
																						//impostando come callback topic_handler
				etimer_reset(&topictimer);												//resetto il timer

			}
		}
		else if{																		//ready to send data
			if(etimer_expired(&et)){													//if timer expired transmit the temperature
				
																						//daily distribution of the data acquisition  
				if (message_number>=0 && message_number<5) 								//morning
			      temp=22;

			    else if (message_number>5 && message_number<10)							//afternoon
			      temp=26;

			    else if (message_number>10 && message_number<15)						//evening
			      temp=24;

			 	else if (message_number>15 && message_number<=20)						//night
			      temp=21;

			    //sprintf(buf, "Temperature %d %d", message_number, temp);
			    char msg[]="%u",temp;													

			    message_number++;

			    if(message_number==20)
			      message_number=0;

				coap_init_message(request, COAP_TYPE_CON, COAP_PUT, 0);					//packet creation
				coap_set_header_uri_path(request, topic_url);
				COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, request, tx_handler);// PUT /sectorx/sensor/temperature/sensor-local-id temp
																						//impostando come callback tx_handler
				etimer_reset(&et);

			}
		}

	PROCESS_END();
}

