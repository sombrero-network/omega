http://erdgeist.org/arts/software/opentracker/#config-file

cvs -d :pserver:cvs@cvs.fefe.de:/cvs -z9 co libowfat
cd libowfat
make
cd ..
# LEGACY: cvs -d:pserver:anoncvs@cvs.erdgeist.org:/home/cvsroot co opentracker
git clone git://erdgeist.org/opentracker
cd opentracker
make

iptables -A INPUT -p tcp -m state --state NEW -m tcp --dport 6969 -j ACCEPT

./opentracker