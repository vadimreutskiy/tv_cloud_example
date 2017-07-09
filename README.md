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
