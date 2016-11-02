#include<stdio.h>
#include<stdlib.h>
#include<malloc.h>
#include<ev.h>
#include<netinet/in.h>
#include<arpa/inet.h>
#include<sys/socket.h>
#include<sys/types.h>
#include<string.h>
#define PORT 9999
#define BUFFER_SIZE 1024
int total_clients=0;
void accept_cb(struct ev_loop *loop,struct ev_io *watcher,int revents);
void read_cb(struct ev_loop *loop,struct ev_io *watcher,int revents);
int main()
{
		struct ev_loop *loop=ev_default_loop(0);
		int sd;
		struct sockaddr_in addr;
		//int addr_len=sizeof(addr);
		struct ev_io socket_accept;

		if((sd=socket(AF_INET,SOCK_STREAM,0))<0){
				printf("socket error");
				return -1;
		}
		bzero(&addr,sizeof(addr));
		addr.sin_family=AF_INET;
		addr.sin_port=htons(PORT);
		addr.sin_addr.s_addr=INADDR_ANY;
		if(bind(sd,(struct sockaddr*)&addr,sizeof(addr))!=0){
				printf("bind error");
		}
		if(listen(sd,0)<0){
				printf("listen error");
				return -1;
		}
		ev_io_init(&socket_accept,accept_cb,sd,EV_READ);
		ev_io_start(loop,&socket_accept);
		while(1){
				ev_loop(loop,0);
		}
		return 0;
}
void accept_cb(struct ev_loop *loop,struct ev_io *watcher,int revents)
{
		//struct sockaddr_in client_addr;
		int client_sd;
		struct ev_io *w_client=(struct ev_io*)malloc(sizeof(struct ev_io));
		if(EV_ERROR & revents){
				printf("error event in accept");
				return ;
		}
		//client_sd=accept(watcher->fd,(struct sockaddr *)&client_addr,&client_len);
		client_sd=accept(watcher->fd,NULL,NULL);
		if(client_sd<0){
				printf("accept error");
				return;
		}
		total_clients++;
		printf("successfully connected with client.\n");
		printf("%d client connected .\n",total_clients);
		ev_io_init(w_client,read_cb,client_sd,EV_READ);
		ev_io_start(loop,w_client);
}
void read_cb(struct ev_loop *loop,struct ev_io *watcher,int revents)
{
		char buffer[BUFFER_SIZE];
		int read;
		if(EV_ERROR & revents){
				printf("error event in read");
				return;
		}
		read=recv(watcher->fd,buffer,BUFFER_SIZE,0);
		if(read==0){
				ev_io_stop(loop,watcher);
				free(watcher);
				perror("peer might closing");
				total_clients--;
				printf("%d client connected .\n",total_clients);
				return;
		}
		else{
				buffer[read]='\0';
				printf("get the message: %s\n",buffer);
		}
		
		send(watcher->fd,buffer,read,0);
		bzero(buffer,read);

}
