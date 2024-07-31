About
=====

This repository contains a proof of concept for easily establishing 
client-server communication using ~~Spring Remoting~~ CBOR in Java.

Originally this repository contained a convenient approach to leveraging Springs HTTP Invoker for client-server communication. However, with the advent of Spring Framework 6 the underlying infrastructure was removed for security purposes.

Now this repository contains a similar client-server remoting interface, but instead of Java serialization it is now utilising CBOR for data exchange. CBOR is similar in characteristics to JSON, but more efficient because it is using binary encoding for data representation.

Generally speaking, this approach works quite well, but has some caveats with regard to the objets that are being transferred. The approach works best with simple POJOs. With other, more complex Java-classes custom serialization hints might be needed. Such can be supplied using mixins for Jackson.

Basic Concept
=============

1. Mark the interfaces of services that you wish to export as remote service using the `@RemoteExport` annotation
2. On the server-side, use the `@EnableHttpInvokerAutoExport` annotation in your Spring configuration class
  * A Spring bean implementing the interface **must** be available in the application context
  * By default the service will be exported with the mapping path `/remoting/<InterfaceName>`
  * The default mapping path can be overridden using the `mappingPath` property
   of the `@RemoteExport` annotation
3. On the client-side, use the `@EnableHttpInvokerAutoProxy` annotation in your Spring configuration class
  * Proxies will be automatically created for every annotated interface that is 
   found in the classpath using the package of the annotated configuration class as base package by default
  * The base package can also be explicitly specified using the `basePackages` property
   of the `@EnableHttpInvokerAutoProxy` annotation
  * For remote invocation the service URL is build from a base URL and the individual mapping path of the service
  * The base URL defaults to *http://localhost:8080* and can be overridden using the system property *remote.baseUrl*

Sample Application
==================

A sample application providing a simple time service is supplied in this repository.

To run it, simply perform a Maven build, then open a terminal and execute the following command:

```
java -jar sample/server/target/sample-server.jar
```

Now open another terminal and execute this command:
```
java -jar sample/client/target/sample-client.jar
```

If everything worked out as expected, you should see the client printing out the 
current server time every 5 seconds as pictured in the screenshot below.

![Alt Screenshot](screenshot.png)
