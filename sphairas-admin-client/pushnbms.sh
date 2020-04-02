#!/bin/bash

set -e

CLIENT_MODULE=sphairas-admin-client
SOURCES=(sphairas-admin-client sphairas-admin-libs sphairas-common sphairas-libs sphairas-service)
TARGET="gfserver@xxxxx.xxxx.xxxxxx.xxxxxx:/var/www/updates/sphairas-admin-client"
BUILD="$CLIENT_MODULE/build/nbms"

mkdir -p $BUILD
rm -rf $BUILD/*.xml

#Copy updates.xml from all clusters to build dir
for SOURCE in "${SOURCES[@]}"; do
    cp "$SOURCE/build/updates/updates.xml" $BUILD/updates-${SOURCE}.xml
done

#Extract latest timestamp from CLIENT_MODULE
TIMESTAMP=`sed 's:.* timestamp="\(.*\)".*:\1:;t;d' $BUILD/updates-$CLIENT_MODULE.xml`
printf "TIMESTAMP: $TIMESTAMP\n"

#Merge updates.xml from clusters
xml_grep --pretty_print indented --wrap module_updates --descr "timestamp=\"${TIMESTAMP}\"" --cond "module" $BUILD/*.xml > $BUILD/updates.xml

#Add xml declaration and DOCTYPE decl.
sed -i -e 's:\(<?xml.*\)\(\?>\):\1encoding="UTF-8" \2:' $BUILD/updates.xml
sed -i -e '2i<!DOCTYPE module_updates PUBLIC "-//NetBeans//DTD Autoupdate Catalog 2.6//EN" "http://www.netbeans.org/dtds/autoupdate-catalog-2_6.dtd">' $BUILD/updates.xml

#push nbm to target server
for SOURCE in "${SOURCES[@]}"; do
    rsync -rv --delete $SOURCE/build/updates/*.nbm $TARGET/
done

#push merged updates.xml to target server
rsync -rv --delete "$BUILD/updates.xml" "$TARGET/updates.xml" 
