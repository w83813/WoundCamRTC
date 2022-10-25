docker rm -f apprtc
docker run -d --name apprtc -e PUBLIC_IP=140.96.170.75 -p 8180:8080 -p 8089:8089 -p 3478:3478 -p 3478:3478/udp -p 3033:3033 --expose=59000-65000 -v ~/apprtc_configs -t -i piasy/apprtc-server
ping 127.0.0.1 -n 3 
docker cp %USERPROFILE%/apprtc/index_template.html apprtc:"/apprtc/out/app_engine/"
docker cp %USERPROFILE%/apprtc/apprtc.debug.js apprtc:"/apprtc/out/app_engine/js/"
docker cp %USERPROFILE%/apprtc/apprtc.py apprtc:"/apprtc/out/app_engine/"
rem please remember modify PUBLIC_IP
pause