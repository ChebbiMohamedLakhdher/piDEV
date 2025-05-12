# LegalLink - Bridging the Gap Between Individuals and Legal Resources

![LegalLink Logo](https://looka.com/s/211159504)

LegalLink is a comprehensive platform designed to connect individuals with legal resources, offering a centralized space for justice-related services. Developed by a team of 3 students at ESPRIT School of Engineering.

## Table of Contents
- [Project Description](#project-description)
- [Team Members](#team-members)
- [Topics](#topics)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Technologies](#technologies)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgments](#acknowledgments)

## Project Description
LegalLink was developed as part of the coursework at **ESPRIT School of Engineering** to bridge the gap between individuals and legal resources. Our platform provides:

- A centralized hub for legal services
- Tools for case management and tracking
- Community forums for legal discussions
- Direct connections with legal professionals
- Appointment scheduling system

## Team Members
This project was developed by:
- [Mohamed Iheb Dahmen](https://github.com/ceno-wp) - Developed the Task Of Cases Management  
- [Mohamed Lakhdher Chebbi](https://github.com/ChebbiMohamedLakhdher) - Developed the Task Of Forum (Web) and User (JavaFX) Management 
- [Karim Gazzah](https://github.com/karimgazzah) - Developed the Task Of User (Web) and Appointment Management 

## Topics
`legal-tech` `javafx` `symfony` `case-management` `forum` `appointment-system` `esprit-school` `java` `php` `mysql`

## Features
### User Management
- Secure registration and authentication
- Profile customization
- Role-based access control

### Case Management
- Case creation and tracking
- Case Claims
- Document upload and management
- Status updates and notifications
- Real-time Chat between users

### Forum System
- Topic creation and discussion threads
- Categorized legal discussions
- Community support system

### Appointment System
- Professional directory
- Calendar integration
- Notification system

## Installation


### Prerequisites
- Java 17+ (for JavaFX version)
- PHP 8.1+ (for Symfony version)
- MySQL 8.0+
- Node.js 16+ (for Symfony frontend dependencies)

### JavaFX Desktop Version
1. Clone the repository:
   ```bash
   git clone https://github.com/ChebbiMohamedLakhdher/piDEV.git
   cd piDEV
2. Set up MySQL database:
   CREATE DATABASE legallink_db;
   CREATE USER 'legaluser'@'localhost' IDENTIFIED BY 'legalpass123';
   GRANT ALL PRIVILEGES ON legallink_db.* TO 'legaluser'@'localhost';
   FLUSH PRIVILEGES;
3. Build and run:
   mvn clean javafx:run

### Symfony Web Application
1. Clone the repository:
   git clone https://github.com/ceno-wp/LegalLinkCase.git
2. Install dependencies:
   composer install
   npm install
   npm run build 
3. Configure environment:
   cp .env .env.local
4. Set up database:
   php bin/console doctrine:database:create
   php bin/console doctrine:migrations:migrate
   php bin/console doctrine:fixtures:load
5. Start development server:
   symfony serve -d

##Usage

###Desktop Application
1.Run the JavaFX application
2.Create and Use test accounts:
   Admin: admin@legallink.com / Admin123
   User: user@legallink.com / User123
   Lawyer: lawyer@legallink.com / Lawyer123

###Web Application
1.Access at http://127.0.0.1:8000/home
2.Create and Use test accounts:
   Admin: admin@legallink.com / Admin123
   User: user@legallink.com / User123

## Technologies
### JavaFX Version

1.Frontend: JavaFX, CSS
2.Backend: Java 17, Spring Boot
3.Database: MySQL
4.Build Tool: Maven

###Symfony Web Version

1.Frontend: Twig, Bootstrap, JavaScript
2.Backend: PHP 8.1, Symfony 6.4
3.Database: MySQL
4.Other: Doctrine ORM, Webpack Encore
## Contributing
We welcome contributions from the community. To contribute:

1.Fork the repository
2.Create a feature branch (git checkout -b feature/AmazingFeature)
3.Commit your changes (git commit -m 'Add some AmazingFeature')
4.Push to the branch (git push origin feature/AmazingFeature)
5.Open a Pull Request

Please ensure your code follows our coding standards and includes appropriate tests.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


## Acknowledgments

We would like to express our sincere gratitude to:

- **ESPRIT School of Engineering** for providing the academic framework and resources that made this project possible
- Our professors and advisors at ESPRIT for their invaluable guidance and continuous support throughout the development process
- The open-source community for providing essential tools and libraries that powered our development:
  - [JavaFX](https://openjfx.io/) team for the robust desktop application framework
  - [Symfony](https://symfony.com/) contributors for the powerful PHP framework
  - [MySQL](https://www.mysql.com/) developers for the reliable database system
- Our colleagues and peers at ESPRIT who provided feedback during testing phases
- The countless developers who shared their knowledge through tutorials, forums, and documentation
- The GitHub Education program for providing student developer tools

---

*This project was developed as part of the coursework at ESPRIT School of Engineering (2024-2025 academic year).*
