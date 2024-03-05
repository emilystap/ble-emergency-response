# Bluetooth LE Emergency Response Application

Android application designed to send an SMS message containing the user's location to an emergency contact upon BLE trigger

## Description

Mobile application component of project submission for Senior Design 2024 at the University of Akron - the purpose of the design project is to create a wearable smart vest for pedestrians, which will detect on-coming vehicles and possible collisions. Application pairs with an RN4870 Bluetooth Low Energy (BLE) module connected to a PIC24F microcontroller. Microcontroller transmits an SOS signal to the mobile application through BLE when a collision is suspected. The application initiates an emergency response upon receiving an SOS signal, sending an SMS message containing the user's location to the user's emergency contact. The application attempts to establish a connection to the BLE module on startup, then continues to monitor for SOS signals by subscribing to BLE characteristic change notifications within a foreground service.

### Features

* Settings page with options to disable location and SMS access, and set emergency contact information
* Option within application and notification to manually trigger emergency response
* Ability to monitor and refresh BLE connection through home page

### Roadmap

- [ ] 30 second timer, to allow user to cancel in-progress emergency response
- [ ] Option within application and notification for user to stop or start BLE monitoring
- [ ] Option to add multiple emergency contacts

## Dependencies

* Android SDK 33 or greater

## Authors

* Emily Stapleton ([@emilystap](https://github.com/emilystap)) - eas173@uakron.edu

## Design Team

* Nicholas Huttinger
* Susannah Smith
* Emily Stapleton

## License

This project is licensed under the MIT License - see [LICENSE](https://github.com/emilystap/ble-emergency-response/tree/master?tab=MIT-1-ov-file#readme) for details
