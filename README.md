# sphairas-desktop
This is the desktop client for [sphairas server](https://github.com/sphairas/sphairas-server) used for all database and server administration tasks. It also provides comprehensive data views not available in the web frontend.

This client is built on top of the NetBeans platform. To compile and run this project you need the [Apache NetBeans IDE](https://netbeans.apache.org/). The currently only supported version is 11.3 The features "Java SE" and "Developing NetBeans" must be enabled in the IDE. The root directories in this repository are all NetBeans modules suites. The required Java version is 11.

To compile and run the client open [sphairas-admin-client](sphairas-admin-client) in NetBeans IDE 11.3 and right-click "build" and then "run". Several shared libraries, which are required to build the server, are installed in your local maven repository during the build process. 

This project contains icons licensed under various Creative Commons licenses. Please see [sphairas-admin-client/release/README](sphairas-admin-client/release/README) for detailed information.
