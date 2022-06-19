set DB_HOST=localhost
set DB_PORT=3306
set DB_USER=root
set DB_PASS='01676940253'
set DB_DATABASE=nja
set DEBUG=false
set REDIS_CACHE_DEFAULT=redis://redis:6379/1
set TOPUP_CARD_API=https://api.doicard68.com/api/card-auto
set TOPUP_CARD_API_KEY=C806331F-CC2A-4EAF-9148-B755DE567333
set NSO_MS_API=http://45.77.129.249:443/api/topup_card
java -server -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5000 -Xms1024M -Xmx1024M -XX:MaxHeapFreeRatio=50 -jar  target/NinjaServer.jar
pause