🚗 GetGo – Ride-Sharing Platform

SIIT – 5th Semester Joint Project (2025/2026)
Software Engineering & IT – University Project

📌 Overview

GetGo is a full ride-sharing system inspired by Uber, developed as a collaborative project across five university courses:

Software Development Methodologies (MRS)

Mobile Applications (MA)

Software Testing (TS)

Client-Side Engineering (IKS)

Server-Side Engineering (ISS)

The goal of the project is to design and implement a complete ecosystem consisting of:

🌐 Web application (Angular)

📱 Android mobile application (Java)

🖥️ Backend system (Java Spring Boot)

🗄️ Relational database (H2 / PostgreSQL)

🗺️ Mapping & routing features (OpenStreetMap / Leaflet)

🎯 Main Features
For Passengers

Account registration and email activation

Requesting rides with multiple stops

Viewing available vehicles on the map

Ride scheduling and real-time notifications

Live ride tracking

PANIC alert system

Ride cancellation

Ride history, filtering, sorting

Rating drivers and vehicles

For Drivers

Automatic ride assignment

Accept/cancel rides with required reason

Availability control (active/inactive)

Work-hour constraints (8h limit)

Access to personal ride history

PANIC alerts

Profile editing with admin approval

For Administrators

Driver account creation & management

Reviewing live ride status

Handling PANIC events

Blocking/unblocking users

Global ride history with advanced filters

Generating system-wide reports

Live support chat with users

🏛️ Project Architecture

This project is split into three major components:

1. Backend (ISS – Server-Side Engineering)

Java + Spring Boot

RESTful API

JWT authentication

Email service (SendGrid recommended)

Notification and ride state management

Database layer (H2/PostgreSQL)

Scheduling and ride assignment logic

2. Web Client (IKS – Client-Side Engineering)

Angular

Angular Material / Bootstrap

Leaflet map integration

JWT-based authentication

Responsive UI (Figma-designed)

Live ride tracking + notifications

3. Mobile App (MA – Mobile Applications)

Java (Android Studio)

Material Design 3 components

SharedPreferences for settings

Local notifications

Google/OSM map solutions

Fully synchronized with backend API

🧪 Testing (TS – Software Testing)

Testing is implemented at multiple levels:

Backend unit tests: JUnit / TestNG

Angular tests: Jasmine / Karma

End-to-End tests: Selenium (web automation)

Manual test cases & acceptance tests

All critical features—ride flow, login, registration, PANIC events, ride history—must be covered.

🧩 Development Methodology (MRS – Software Development Methodologies)

The project follows an Agile/Scrum workflow:

Sprint planning & sprint goals

Role rotation (Product Owner / Scrum Master)

Trello board for tasks and progress tracking

Sprint Review & Retrospective documents

Burndown charts

Acceptance criteria written for every user story

Figma UI/UX documentation

All documents are kept up-to-date in accordance with MRS requirements.

📁 Repository Structure
/client     → Angular web application
/mobile     → Android application (Java)
/server     → Spring Boot backend
/docs       → MRS documents, sprint logs, retrospectives, mockups
/tests      → Selenium + Jasmine + JUnit/TestNG

🛠️ Technologies Used
Backend

Java 17+

Spring Boot

Spring Security (JWT)

JPA / Hibernate

PostgreSQL / H2

SendGrid (email service)

Frontend

Angular 17

Angular Material / Bootstrap

Leaflet (OpenStreetMap)

Figma UI/UX Design

Mobile

Android Studio (Java)

Material Design 3

SQLite / Firebase (optional)

Testing

JUnit / TestNG

Jasmine / Karma

Selenium WebDriver

🚀 Project Goals

Deliver a fully functional ride-sharing ecosystem

Demonstrate teamwork across five coordinated courses

Implement industry-level architecture and best practices

Present the system with test data and demo recordings

Produce complete documentation for all subjects

👥 Team

SIIT – 5th Semester
Students working jointly across five integrated courses.

Meris Bilalović SV1/2023
Dalibor Nikolić SV13/2023
Anastazija Petrov SV26/2023

🔔 Notifications, email activation, and real-time updates

The platform connects passengers and drivers, enabling fast and safe urban transportation.
