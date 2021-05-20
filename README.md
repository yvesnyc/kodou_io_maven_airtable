kodou_io_maven_airtable

# Background
kodou.io allows functions found in existing software repositories to be used and composed into services without downloading anything. Any function
becomes active behind an API endpoint as a service that accepts the function's arguments.

As the [kodou.io](https://kodou.io) system expands it lets you use quality software from across languages, open source and proprietary, to solve your problems.

The requirements for a software repository is high quality and a build system (CMake/Make, Maven, etc.).

It is easy to create functions just for using kodou.io, simplifying the developer experience. These user generated functions are also part of the
kodou.io pool of code, helping other developers.

# Introduction
This is an example for using kodou.io with the Airtable API. In this example our code is written in Java and uses the Maven build system. 

After this repository is ingested by kodou.io the public static methods will be available for anyone to use. 

Our interest is in making functions that perform the operations we need. We can create one function that performs what we need 
or follow the recommended approach of using many functions and call them inside a few functions. This expansion into additional functions
is not just good software engineering but also contributes code to kodou.io.

The code in this repository are examples of operations for converting web forms into Airtable records.
## Web forms to Airtable
The use case is a service that processes and converts web form data into a properly formatted record for an Airtable.
The action for the web form points to the [kodou.io](https://api.kodou.io) API endpoint and a token for the provisioned functions for handling the form data.