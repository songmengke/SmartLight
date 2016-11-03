#include<stdio.h>
#include<stdlib.h>
#include<malloc.h>
#include<ev.h>
#include<netinet/in.h>
#include<arpa/inet.h>
#include<sys/socket.h>
#include<sys/types.h>
#include<string.h>
#define PORT_RPI        9999
#define PORT_CELLPHONE  8888
#define BUFFER_SIZE     1024
int total_clients=0;
char buffer[BUFFER_SIZE] = "welcome string";
char last_buffer[BUFFER_SIZE];
char total_buffer[BUFFER_SIZE];
void accept_cellphone_cb(struct ev_loop *loop, struct ev_io *watcher, int revents);
//void accept_rpi_cb(struct ev_loop *loop, struct ev_io *watcher, int revents);
void read_cb(struct ev_loop *loop, struct ev_io *watcher, int revents);
//void send_cb(struct ev_loop *loop, struct ev_io *watcher, int revents);
int main()
{
		int cellphone_sd;

    struct ev_loop *loop=ev_default_loop(0);
		struct sockaddr_in cellphone_addr;
		//int addr_len=sizeof(addr);
		struct ev_io cellphone_accept;

		if((cellphone_sd=socket(AF_INET, SOCK_STREAM, 0))<0) {
				printf("socket error");
				return -1;
		}

		bzero(&cellphone_addr, sizeof(cellphone_addr));

		cellphone_addr.sin_family = AF_INET;
		cellphone_addr.sin_port = htons(PORT_CELLPHONE);
		cellphone_addr.sin_addr.s_addr = INADDR_ANY;

		if(bind(cellphone_sd,(struct sockaddr*)&cellphone_addr, sizeof(cellphone_addr))!=0) {
				printf("bind error");
		}
    
		if(listen(cellphone_sd, 0) < 0) {
				printf("listen error");
				return -1;
		}

		ev_io_init(&cellphone_accept, accept_cellphone_cb, cellphone_sd, EV_READ);
		ev_io_start(loop, &cellphone_accept);
		while(1){
				ev_loop(loop,0);
		}
		return 0;
}
void accept_cellphone_cb(struct ev_loop *loop, struct ev_io *watcher, int revents)
{
		//struct sockaddr_in client_addr;
		int client_sd;
		struct ev_io *w_client = (struct ev_io*)malloc(sizeof(struct ev_io));
		if(EV_ERROR & revents) {
				printf("error event in accept");
				return ;
		}
		//client_sd=accept(watcher->fd,(struct sockaddr *)&client_addr,&client_len);
		client_sd = accept(watcher->fd, NULL, NULL);
		if(client_sd<0) {
				printf("accept error");
				return;
		}
		total_clients++;
		printf("successfully connected with client.\n");
		printf("%d client connected .\n", total_clients);
		ev_io_init(w_client,read_cb, client_sd, EV_READ);
		ev_io_start(loop,w_client);
}
//void accept_rpi_cb(struct ev_loop *loop, struct ev_io *watcher, int revents)
//{
//		//struct sockaddr_in client_addr;
//		int client_sd;
//		struct ev_io *w_client = (struct ev_io*)malloc(sizeof(struct ev_io));
//		if(EV_ERROR & revents) {
//				printf("error event in accept");
//				return ;
//		}
//		//client_sd=accept(watcher->fd,(struct sockaddr *)&client_addr,&client_len);
//		client_sd = accept(watcher->fd, NULL, NULL);
//		if(client_sd<0) {
//				printf("accept error");
//				return;
//		}
//		total_clients++;
//		printf("successfully connected with client.\n");
//		printf("%d client connected .\n", total_clients);
//		ev_io_init(w_client,send_cb, client_sd, EV_WRITE);
//		ev_io_start(loop,w_client);
//}
void read_cb(struct ev_loop *loop, struct ev_io *watcher, int revents)
{
		int read;
    int send_num;
		if(EV_ERROR & revents) {
				printf("error event in read");
				return;
		}
		read = recv(watcher->fd, buffer, BUFFER_SIZE, 0);
		if( read == 0 ) {
				ev_io_stop(loop,watcher);
				perror("peer might closing");
				total_clients--;
				printf("%d client connected .\n",total_clients);
				return;
		}
		else {
				buffer[read] = '\0';
				printf("get the message: %s\n",buffer);
        strcpy(last_buffer,buffer);
		}
    strcat(total_buffer,last_buffer);
    send_num = send(watcher->fd, total_buffer,BUFFER_SIZE, 0);
		if( send_num == 0 ) {
				ev_io_stop(loop,watcher);
				perror("peer might closing");
				total_clients--;
				printf("%d client connected .\n",total_clients);
				return;
		}
		else {
				total_buffer[send_num] = '\0';
				printf("send the message: %s\n",total_buffer);
		}
}
//void send_cb(struct ev_loop *loop, struct ev_io *watcher, int revents)
//{
//	int send_num;
//  if(EV_ERROR & revents) {
//      printf("error event in send");
//      return;
//  }
//
//  if(strlen(buffer)!=0) {
//      send_num = send(watcher->fd, buffer, BUFFER_SIZE, 0);
//  }
//
//	if( send_num == 0 ) {
//			ev_io_stop(loop,watcher);
//			perror("peer might closing");
//			total_clients--;
//			printf("%d client connected .\n",total_clients);
//			return;
//	}
//	else {
//			buffer[send_num] = '\0';
//			printf("send the message: %s\n",buffer);
//	}
//}
