# sphairas-desktop
This is the desktop client for [sphairas server](https://github.com/sphairas/sphairas-server) used for all database and server administration tasks. It also provides comprehensive data views not available in the web frontend.

This client is built on top of the NetBeans platform. To compile and run this project you need the [Apache NetBeans IDE](https://netbeans.apache.org/). The currently supported version is 12. The features "Java SE" and "Developing NetBeans" must be enabled in the IDE. The root directories in this repository are all NetBeans modules suites. The required Java version is 11.

The project is ant-based but uses maven to download required external libraries. Before building the project for the first time, open [sphairas-admin-client](sphairas-admin-client) in your NetBeans IDE 12 and then open "Important Files" > "Build script". In the navigator window, right-click the "resolve dependencies" ant task and choose "Run Target". This will download all required external libraries. 

To compile and run the client open [sphairas-admin-client](sphairas-admin-client) and right-click "build" and then "run". The build process will also install several shared libraries, which are required to build the server, in your local maven repository. Therefore, building the client is a requirement for building the server locally.

This project contains icons licensed under various Creative Commons licenses. Please see [sphairas-admin-client/release/README](sphairas-admin-client/release/README) for detailed information.
