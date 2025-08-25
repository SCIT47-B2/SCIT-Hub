# SCIT-Hub
SCIT Master 47th B Class Group 2 Team Project

---

## 🖥 Development Environment
<p align="center">
  <img src="https://img.shields.io/badge/VS%20Code-007ACC?style=flat-square&logoColor=white"/>
  <img src="https://img.shields.io/badge/STS-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
  <img src="https://img.shields.io/badge/Git-F05032?style=flat-square&logo=git&logoColor=white"/>
  <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white"/>
  <img src="https://img.shields.io/badge/MySQL%20Workbench-4479A1?style=flat-square&logo=mysql&logoColor=white"/>
</p>

## 💻 Languages & Database
<p align="center">
  <img src="https://img.shields.io/badge/Java-007396?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5&logoColor=white"/>
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=css3&logoColor=white"/>
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black"/>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white"/>
</p>

## ⚙ Frameworks
<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Data%20JPA-59666C?style=flat-square&logo=spring&logoColor=white"/>
</p>

## 📚 Libraries & Tools
<p align="center">
  <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=flat-square&logo=thymeleaf&logoColor=white"/>
  <img src="https://img.shields.io/badge/jQuery-0769AD?style=flat-square&logo=jquery&logoColor=white"/>
  <img src="https://img.shields.io/badge/Lombok-CA4245?style=flat-square&logoColor=white"/>
</p>

---
# Setting Up the Development Environment
To run this project locally, you need to configure your own environment settings.
We provide an `application-example.properties` file as a template.
Copy it and adjust the values according to your local setup.

```bash
# Copy the example configuration
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

Then, open ```application.properties``` and update the following fields:

<hr>

## 🗄️Server Settings
* **Server port**
```server.port=YOUR_PORT_NUMBER```

* **Context path**
```server.servlet.context-path=YOUR_CONTEXT_PATH```

## 🗄 Database (MySQL)
* **JDBC URL**
```spring.datasource.url=jdbc:mysql://localhost:(YOUR_CONNECTION_PORT)/(YOUR_CONNECTION_NAME)?useSSL=false&serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=UTF-8```

* **Username**
```spring.datasource.username=YOUR_DB_USERNAME```

* **Password**
```spring.datasource.password=YOUR_DB_PASSWORD```

## 📂 File Upload
* **Upload directory**
```spring.servlet.multipart.location=YOUR_UPLOAD_PATH```

## 📝 Custom Properties
* **File upload path:**
```board.uploadPath=YOUR_UPLOAD_PATH```

<hr>

## ▶ Running the Application

Once you have updated ```application.properties```, run the project:

```bash
./gradlew bootRun      # macOS/Linux
gradlew.bat bootRun    # Windows
```

The application will be available at:
```http://localhost:<YOUR_PORT>/<YOUR_CONTEXT_PATH>```