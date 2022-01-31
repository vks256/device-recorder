### Overview
- I created the service using AWS API Gateway, which triggers a lambda.
- The lambda is a Java application and it is created manually in the AWS console, by deploying the jar file.
- API Gateway is also created manually in the AWS console, with two methods (one POST and one GET).
- The source code can be found at https://github.com/vks256/device-recorder
- PS: There is a limitation with the API responses. Currently, both the APIs return HTTP 200 status, even with the error responses. Please refer to the response payload for the correct status code and message.


### Deployment Instructions
- The API Gateway and lambda are already deployed to my AWS account.
- All you need to do is to call the endpoint "https://rxfht8al69.execute-api.us-east-1.amazonaws.com/beta"
- For the POST API, provide the payload in the JSON format.
- For the GET API, provide the id as query parameter. e.g. "https://rxfht8al69.execute-api.us-east-1.amazonaws.com/beta/device-readings?id=36d5658a-6908-479e-887e-a949ec199273"

### Assumptions:
- The requirement for duplicate payload was not clear. The problem mentioned, "Devices will occasionally send POST requests with duplicate payloads. Duplicate payloads should be ignored.". For the example in the problem, what can be considered as the duplicate payload?
  1. Is it the entire payload with the same id and same timestamps (T1 and T2) with the same count (2 and 15)?
  2. Is it a payload with the same id and any number of the duplicate timestamps (even with a different count)?
  3. Is it a payload with the same id and any number of the duplicate timestamps with the same count?

I went with the assumption for option 2 i.e. if I receive a payload with the previously recorded id and timestamp, I'll ignore it (irrespective of the count).


### Design Decisions
- Since there was a constraint that a database cannot be used, I needed something for storing the device data in-memory. 
  - One of the solutions I explored was to host the java service on localhost, but that required building my own API routes and would have required some ramp up time.
  - I wanted to save time by focussing on the backend solution and abstracting the APIs logic using the API Gateway. This required me to keep the service always running on a cloud server like EC2. This was a costly soultion, if the API calls are not frequent enough.
  - Instead, I went for a time and cost saving solution by building the API Gateway and lambda in AWS console and deploying the jar file directly to lambda.
- This solution has one big disadvantage that a lambda instance can not be running all the time. This means the service is not up all the time. Since I could not use an external memory, all the device data is stored in a single lambda instance memory. Once the lambda instance is killed (due to inactivity), the device data is also gone. Hence, you need to run all your tests in short time window so that the same lambda instance can be used for all the API calls.
  - It's hard to predict how long an idle lambda instance will remain active. The recommended time gap between the two API calls is < 45 seconds.
  - Of course, if we can use the external memory (like a database or file storage), we can use two independent lambdas, which will be invoked only when needed and will query direcly from the external memory.


### What can be improved:
- Authorization and Authentication for the APIs should be added.
- Currently, both the APIs return HTTP 200 status, even with the error responses. The lambda output response needed to be integrated with API method response, in order to display the correct HTTP status to the client.
- Better exception handling. Currently, I used generic exception classes, custom classes with different types status codes and appropriate error messages can be used.
- I stored the timestamp as a String, it would have been ideal to store it as long (epochTimeMillis). I couldn't figure out a quick way to convert offset date time format to epochTimeMillis.
- For a production code, more unit tests are a must.


