# Revolut Coding Challenge

Design and implement a RESTful API (including data model and the backing implementation)
for money transfers between accounts.
Explicit requirements:
1. You can use Java, Scala or Kotlin.
2. Keep it simple and to the point (e.g. no need to implement any authentication).
3. Assume the API is invoked by multiple systems and services on behalf of end users.
4. You can use frameworks/libraries if you like (except Spring), but don't forget about
requirement #2 – keep it simple and avoid heavy frameworks.
5. The datastore should run in-memory for the sake of this test.
6. The final result should be executable as a standalone program (should not require
a pre-installed container/server).
7. Demonstrate with tests that the API works as expected.

Implicit requirements:
1. The code produced by you is expected to be of high quality.
2. There are no detailed requirements, use common sense.
Please put your work on github or bitbucket.

## Solution

The solution pictures around amount transfers across multiple banks.
To demonstrate multiple banks, I have introduced supplementary endpoints beyond actual requirement of amount transfer.
- Add Bank - source of bank code, first 4 letters of IBAN
    - Publish createBankEvent to let the BankOperationTemplate to execute set of operations,
    1. Create bank exchange of type DIRECT
    2. Subscribe to global exchange with bank code as routing key.
- Add Branch to bank - source of BIC
- Add Account to bank - source of IBAN which is associated with bank and bic

I introduced concept of central bank which orchestrates flow when there is inter bank transfer request.

 1. Both sender and receiver accounts belong to same bank. (intra-bank transfer)
 2. Sender and receiver belong to 2 different banks. (inter-bank transfer)
    - Validate sender iban belonging to sender bank
    - Immediately deduct the amount from sender account if he has sufficient balance
    - Route the request to appropriate exchange for asynchronous processing
       - internal bank exchange (direct) for intra bank transfer
       - global central exchange (topic) for inter bank transfer
           - all internal bank queues subscribe to global queue with their respective bankcode as routing key
    - If unable to enqueue the request, revert the transaction
    
- Application uses Jersey Framework to support REST endpoints. Http is served by standard JDK HttpServer making application 
free from heavy embedded third party servers/containers like Tomcat, Jetty etc.
- I introduced a queuing mechanism to orchestrate the flow. To make everything in single jar, I was looking for embedded solutions 
and choices I considered were ActiveMQ and Qpid. But that ruled out my intention of making application light weight, making me 
implement my own solution.
- It is designed as a multi module maven application, with `revolut-commons` holding DTOs, `revolut-central` supposed to 
be representing a central bank employs message broker, explicitly designed based on requirements. While `revolut-core` demonstrates 
distributed banking, `revolut-release` bundles all into a single jar.

## Diagrams
### Flow Diagram
![Flow Diagram](https://raw.githubusercontent.com/mavaze/revolut-banking/master/FlowDiagram.jpg)

### Activity Diagram
![Activity Diagram](https://raw.githubusercontent.com/mavaze/revolut-banking/master/ActivityDiagram.jpg)
## How to compile and run
```
mvn clean package
sh start.sh
```

Server starts listening on port 9998 on localhost

## Request/Responses

##### Create Bank
```
1 > POST http://localhost:9998/banks
1 > Accept: application/json
1 > Content-Type: application/json
1 > Body: {"code":"GB96","name":"HSBC UK"}

Response: {"code":"GB96","name":"HSBC UK","branches":[],"accounts":[]}
```

##### Add Branch to Bank
```
2 > POST http://localhost:9998/banks/GB96/branches
2 > Accept: application/json
2 > Content-Type: application/json
2 > Body: {"bic":"JDJ333XXX"}

Response: {"bic":"JDJ333XXX"}
```

##### Create Account
```
3 > POST http://localhost:9998/banks/GB96/accounts
3 > Accept: application/json
3 > Content-Type: application/json
3 > Body: {"initialBalance":"1000","name":"Sam","bic":"JDJ333XXX"}

Response: {"accountId":881012626,"name":"Sam","iban":"GB96JDJ333XXX08445857","bic":"JDJ333XXX","balance":1000.0}
```

##### Amount Transfer Request
```
4 > POST http://localhost:9998/banks/GB96/accounts/881012626/transactions
4 > Accept: application/json
4 > Content-Type: application/json
4 > Body: {"sender":"GB96JDJ333XXX08445857","receiver":"DE26HD72SSXXX08565288","description":"inter bank transfer","amount":200}

Response: {"balance":800}
```

##### Get Account Transactions
```
5 > GET http://localhost:9998/banks/GB96/accounts/881012626/transactions
5 > Accept: application/json

Response: {"balance":1000.0,"transactions":[{"transactionId":881012629,"referenceId":"881012628","involvedAccount":"DE26HD72SSXXX08565288","amount":200,"status":"REVERTED","description":"No bank with the given code subscribed to topic.","transactionDate":1570372410201},{"transactionId":881012628,"referenceId":null,"involvedAccount":"DE26HD72SSXXX08565288","amount":200,"status":"DEBITED","description":"inter bank transfer","transactionDate":1570372410199},{"transactionId":881012627,"referenceId":null,"involvedAccount":null,"amount":1000,"status":"CREDITED","description":"Opening Balance","transactionDate":1570372410189}]}
```
