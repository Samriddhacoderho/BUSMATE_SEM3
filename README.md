# BUSMATE_SEM3

## Overview

BUSMATE_SEM3 is a smart bus management and tracking system designed for schools. It facilitates efficient monitoring, collection of attendance, route planning, and emergency notifications, ensuring the safety and well-being of students during their commute.

The system provides robust features for admins, drivers, and parents, and offers real-time data integration, user-friendly interfaces, and secure communication. 

## Developed By: SSK Tech
**Keshab Bhattarai**

**Sandip Bhandari**

**Samriddha Raj Satyal**

## Features

### Key Functionalities
- **Admin Tools:**
  - Bus registration, data updates, and live tracking.
  - Driver-bus assignments.
  - Global message broadcasts.
  
- **Driver Dashboard:**
  - Manage and update location details.
  - Monitor student attendance.
  
- **Parent and Student Features:**
  - Real-time location updates.
  - Alerts for arrival and emergencies.

### Technical Capabilities
- Real-time database integration using Firebase.
- Google Maps' LatLng for route planning.
- Material Design for mobile-friendly user interfaces.
- Custom QR Code generation for student identification.

## Architecture

The repository adheres to a clean Model-View-ViewModel (MVVM) architecture with clearly defined interfaces and implementations for each feature.

### Directory Structure
- `data/`: Repository interfaces and implementations.
- `viewmodel/`: View models handling live data integration across the app.
- `view/`: UI components written in Compose.
- `utils/`: Utility scripts such as QR Code generation.

## Getting Started

### Prerequisites
- Android Studio Bumblebee (or later).
- Java 8 or higher.
- Google Maps and Firebase API keys.

### Installation
1. Clone the repository:
    ```
    git clone https://github.com/Samriddhacoderho/BUSMATE_SEM3.git
    ```
2. Open in Android Studio.
3. Configure API keys in `local.properties`:
   ```properties
   firebase.apiKey=YOUR_API_KEY
   googleMaps.apiKey=YOUR_MAPS_KEY
   ```
4. Build and run the app.

## Contribution Guidelines

Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a feature branch:
   ```
   git checkout -b feature-branch-name
   ```
3. Commit your changes and submit a pull request.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.

## Contact

For inquiries or suggestions:
- [GitHub Issues](https://github.com/Samriddhacoderho/BUSMATE_SEM3/issues)
- Email: satyalsamriddha@gmail.com
