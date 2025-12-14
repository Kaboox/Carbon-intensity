Energy Dashboard - Backend

Backend application for the Energy Dashboard system. It fetches data from the UK Carbon Intensity API and calculates optimal EV charging windows.

Live Demo
The API is deployed on Render:
https://carbon-intensity-z1zs.onrender.com

📡 API Endpoints
GET /api/carbon/mix - Returns energy generation mix for the last 24h.

GET /api/carbon/optimal-charging?hours=X - Returns the best charging window for a specified duration (1-6h).

🛠️ Tech Stack
* **Java 21**
* **Spring Boot 3**
* **Maven**
* **Docker**
* **JUnit 5 / Mockito** (Testing)

### Prerequisites
* Java 21 SDK
* Maven

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Kaboox/Carbon-intensity.git
