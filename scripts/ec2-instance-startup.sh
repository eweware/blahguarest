# EC2 instance application tartup script
# rk 10/12/12

# TODO: check if each item is already started: now assuming everything's off

# Start ntpd
ntpd -u ntp:ntp -p /var/run/ntpd.pid -g

#Start DB
cd $MONGO_HOME
$MONGO_HOME/bin/mongod --port 21191 --dbpath $HOME/data/db  --smallfiles > $LOGS/mongo.log &

# Start memcached server
$MEMCACHED_HOME/memcached -d -p 11211

# Start stats app
cd $STATS_HOME
java -Xmx256m -jar Stats.jar > $LOGS/stats.log &

# Start REST service
cd $CATALINA/bin
./start.sh

