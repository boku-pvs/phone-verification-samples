# Phone Verification Samples

Sample code for clients and servers integrating with the Danal Phone Verification Service.

## Getting Started

Please take a look at the example server code which generates Danal API URLs to be used by the client.  Credentials are stored on the server, and URLs with Authorization Tokens are generated on demand for the client.  Please update the placeholder values for API Key, Developer ID, Encryption Type, and Danal API URL if you plan to run and test the code.

Once you have a server up and running, take a look at the client code for examples on client interaction with the server.  The client requests API URLs from the server once a Mobile Number is given by the user.  Since the API URLs contain Authorization Tokens (which expire) it is best to request the API URLs only immediately before they are needed by the client (not when the page or app loads).  Client examples support fallback to verify with SMS code, however this may not be required for all applications.  Resend SMS Code examples are not given, but should be considered as part of a complete client implementation that uses the fallback to verify with SMS code.
