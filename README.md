# CS258 Assignment 1: Leader Election Problem

## Code Explanation (MyProcess)

* Main Method
    * Reads in the connection details to connect to peer
    * Spawn a new process, and a separate thread that listens for new messages
    * Forward the initial message via the main thread after a second delay

* receiveMessage
    * Server Connection is established and starts the loop to find leader
    * Receive the message once the connection is accepted
    * Determine if the message has the leader flag
        * Print out if the message ID matches with current process (make it clear if you are the leader or not)
        * forward the message to other connected peer and exit (Leader was found)
    * If leader message was not received, check if the message does equal your own and declare yourself the leader and then forward the announcement message
    * If the ID does not match your own, check if the received ID is bigger than yours and then forward the message

* forwardMessage
    * establish a client connection to the other process's server
    * write out to bugger the class Message object
    * flush and send the serialized object

* outputToFile
    * prints out the message that was received, and the message that was sent to the next peer

## Acknowledgement
Used ChatGPT to better clarify how to create and use threads with my code to start the initial message exchange.