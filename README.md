# tv_cloud_example

## Setup
Run `javac BppServer.java` to compile

Run `java BppServer` to start HTTP server

## Run as service
Create startup file `sudo nano /etc/init.d/httppiserver`

Paste there following code (correct paths to scripts):
```bash
#!/bin/bash
case $1 in
    start)
        /bin/bash /home/ubuntu/tv_pi/server-start.sh
    ;;
    stop)
        /bin/bash /home/ubuntu/tv_pi/server-stop.sh
    ;;
    restart)
        /bin/bash /home/ubuntu/tv_pi/server-stop.sh
        /bin/bash /home/ubuntu/tv_pi/server-start.sh
    ;;
esac
exit 0
```

Start service by command `sudo service httppiserver start`

Stop it by command `sudo service httppiserver stop`

(Optional) Setup autostart by running `sudo update-rc.d httppiserver defaults`

## Usage

Go to page with url http://your-server-address:141459/100 to get 100th digit of Pi after comma,
where your-server-address should be equal to your server DNS name or `127.0.0.1` if you running it on local machine.
