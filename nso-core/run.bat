@REM set DB_HOST=localhost
@REM set DB_PORT=3306
@REM set DB_USER=root
@REM set DB_PASS='01676940253'
@REM set DB_DATABASE=nja
@REM set DEBUG=false
@REM set REDIS_CACHE_DEFAULT=redis://redis:6379/1
@REM set TOPUP_CARD_API=https://api.doicard68.com/api/card-auto
@REM set TOPUP_CARD_API_KEY=C806331F-CC2A-4EAF-9148-B755DE567333
@REM set NSO_MS_API=http://45.77.129.249:443/api/topup_card
java -server -Xms192M -Xmx192M -XX:MaxHeapFreeRatio=50 -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5000 target/NinjaServer.jar
pause