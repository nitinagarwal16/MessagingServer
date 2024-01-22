# Messaging Server

This is a simple peer-to-peer messaging server where users can register with unique usernames, send messages, and fetch chat history.

## Prerequisites

- [Docker](https://www.docker.com/get-started)

## Getting Started

1. Clone the repository and go inside the project directory:

    ```bash
    # Option 1: Clone using Git
    git clone https://github.com/nitinagarwal16/MessagingServer.git
    
    # Option 2: Download and unzip the zip file
    - Visit the repository on GitHub: https://github.com/nitinagarwal16/MessagingServer
    - Click on the "Code" button and select "Download ZIP"
    - Extract the downloaded ZIP file to your desired location
    
   cd MessagingServer
    ```

2. Build and run the Docker container (Make sure you have docker installed before running these commands):

    ```bash
    docker buildx build -t messagingserver .
   docker run -p 8080:8080 messagingserver
    ```

   This command will build the Docker image and start the application. You should see the server running at `http://localhost:8080`.


   - Note: 'messagingserver' is the docker image name. You can use whatever name you like.

3. Test the API

   Use tools like `curl` or [Postman](https://www.postman.com/) to test the provided endpoints.

## API Endpoints

- **Create User:**
    ```bash
    curl --request POST 'http://localhost:8080/user' \
    --header 'Content-Type: application/json' \
    --data-raw '{"username":"your-username","passcode":"your-password"}'
    ```

- **Login User:**
    ```bash
    curl --request POST 'http://localhost:8080/login' \
    --header 'Content-Type: application/json' \
    --data-raw '{"username":"your-username","passcode":"your-password"}'
    ```

- **Send Message:**
    ```bash
    curl --request POST 'http://localhost:8080/user/<your-username>/message' \
    --header 'Content-Type: application/json' \
    --data-raw '{"to":"recipient-username","text":"your-message"}'
    ```

- **Fetch Unread Messages:**
    ```bash
    curl --request GET 'http://localhost:8080/user/your-username/message'
    ```

- **Get Chat History:**
    ```bash
    # Without markAsRead option
    curl --request GET 'http://localhost:8080/user/your-username/message/history?friend=friend-username'

    # With markAsRead option
    curl --request GET 'http://localhost:8080/user/your-username/message/history?friend=friend-username&markAsRead=true'
    ```
- **Block User:**
    ```bash
    curl --request POST 'http://localhost:8080/user/<your-username>/block' \
    --header 'Content-Type: application/json' \
    --data-raw '{"username":"blocked-username"}'
    ```

- Note: When a user 'A' blocks a user 'B', neither of them will be able to send messages to each other


- **Logout User:**
    ```bash
    curl --request POST 'http://localhost:8080/logout' \
    --header 'Content-Type: application/json' \
    --data-raw '{"username":"your-username"}'
    ```

## Notes

- The application uses Docker for easy deployment.
- The login functionality has been kept simple with no security features or access token. The user will be able to perform send and receive actions only if he/she is logged in.


---
