#!/bin/bash
BUILD_DIR=build/jdk
LINUX=amazon-corretto-11-x64-linux-jdk
WINDOWS=amazon-corretto-11-x64-windows-jdk.zip
WINDOWS_DIR=jdk11
UNZIPSFX=/usr/bin/unzipsfx
UNZ600XN=https://fossies.org/windows/misc/unz600xn.exe

set -e

rm -rf ${BUILD_DIR}
mkdir -p ${BUILD_DIR}
cd ${BUILD_DIR}

echo "Building linux jdk package..."
rm -rf tmp/
mkdir tmp/
cd tmp/
wget https://corretto.aws/downloads/latest/${LINUX}.tar.gz
tar -xzf ${LINUX}.tar.gz
rm amazon-corretto-11*-linux-x64/lib/src.zip
echo "...zipping..."
cd amazon-corretto-11*-linux-x64/ && zip -rq ../${LINUX}.zip ./* && cd ..
echo "...building self-extracting archive..."
cat ${UNZIPSFX} ${LINUX}.zip > ../${LINUX}.sh
rm ${LINUX}.zip
cd ..
echo "Done building linux jdk package."

echo "Building windows jdk package..."
rm -rf tmp/
mkdir tmp/
cd tmp/
wget https://corretto.aws/downloads/latest/${WINDOWS}
unzip -q ${WINDOWS}
rm jdk11*/lib/src.zip
echo "...zipping..."
cd jdk11*/ && zip -rq ../${WINDOWS} ./* && cd ..
echo "...building self-extracting archive..."
wget ${UNZ600XN}
cat unz600xn.exe ${WINDOWS} > ../${WINDOWS_DIR}.exe
rm ${WINDOWS}
cd ..
echo "Done building windows jdk package."

rm -rf tmp/
