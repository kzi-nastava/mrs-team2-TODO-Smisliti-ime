🚗 GetGo — Ride-Sharing Platform

SITI — 5th Semester Joint Project (2025/2026)
Software Engineering & IT — University Project

Overview
--------

GetGo is a full ride-sharing system inspired by Uber, developed as a collaborative project across five university courses:

- Software Development Methodologies (MRS)
- Mobile Applications (MA)
- Software Testing (TS)
- Client-Side Engineering (IKS)
- Server-Side Engineering (ISS)

The goal of the project is to design and implement a complete ecosystem consisting of:

- Web application (Angular)
- Android mobile application (Java)
- Backend system (Java Spring Boot)
- Relational database (H2 / PostgreSQL)
- Mapping & routing features (OpenStreetMap / Leaflet)

Main Features
-------------

For passengers:

- Account registration and email activation
- Requesting rides with multiple stops
- Viewing available vehicles on the map
- Ride scheduling and real-time notifications
- Live ride tracking
- PANIC alert system
- Ride cancellation

For drivers:

- Driver history, filtering, sorting
- Rating drivers and vehicles
- For Drivers: Automatic ride assignment
- Accept/cancel rides with required reason
- Availability control (active/inactive)
- Work-hour constraints (8h limit)
- Access to personal ride history
- PANIC alerts
- Profile editing with admin approval
- For Administrators: driver account creation & management
- Driver account creation & management
- Reviewing live ride status
- Handling PANIC events
- Blocking/unblocking users
- Global ride history with advanced filters
- Generating system-wide reports
- Live support chat with users
- Project architecture diagram

Project Architecture
--------------------

This project is split into three major components:

1. Backend (ISS — Server-Side Engineering)
   - Java + Spring Boot
   - RESTful API
   - JWT based authentication
   - Email service (SendGrid recommended)
   - Notification and ride state management
   - Database layer (H2/Postgres)
   - Scheduling and ride assignment logic
   - Mapping solutions (OpenStreetMap / Leaflet)
   - Fully synchronized with backend API

2. Web Client (IKS — Client-Side Engineering)
   - Angular
   - Angular Material / Bootstrap
   - Leaflet map integration
   - JWT-based authentication
   - Responsive UI (Figma-designed)
   - Live ride tracking + notifications

3. Mobile App (MA — Mobile Applications)
   - Java (Android Studio)
   - Material Design 3 components
   - Shared preferences for settings
   - Local notifications
   - Google/OSM map solutions
   - Backend integration (REST API)
   - Testing: unit tests, integration tests, end-to-end tests
   - Backend unit tests: JUnit / TestNG
   - Android tests: Jasmine / Karma
   - Manual test cases & acceptance tests

Testing & CI
------------

- Backend unit tests: JUnit / TestNG
- Spring Boot tests
- Spring Security (JWT)
- Postgres/H2 optional integration for CI
- Frontend: Angular 17 / Angular Material / Bootstrap / Leaflet
- Mobile: Android 17 / Material / Java
- Documentation: Figma UI/UX, design docs
- Acceptance criteria and test data kept up-to-date with MRS requirements

Usage & Docs
------------

- /client — Angular web application
- /mobile — Android application (Java)
- /server — Spring Boot backend
- /docs — MRS documents, sprint logs, retrospectives, mockups
- /tests — Selenium + Jasmine + JUnit/TestNG

Technologies Used
-----------------

- Backend: Java 17, Spring Boot, Spring Security, JWT, Postgres/H2, Spring REST
- Frontend: Angular, Angular Material, Leaflet
- Mobile: Android (Java), Material Design
- Email service: SendGrid (recommended)
- Maps: OpenStreetMap / Leaflet

Team
----

- Meris Bilalović SV1/2023
- Dalibor Nikolić SV13/2023
- Anastazija Petrović SV26/2023

Notes
-----

- Notifications, email activation, and real-time updates
- The platform connects passengers and drivers, enabling fast and safe urban transport.
