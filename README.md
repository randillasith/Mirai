# Mirai – Smart Parking Management System

Mirai is an IoT-based smart parking management system developed as a **mini-project** for the **1st Year, 1st Semester** module **Fundamentals of Computing**, under the **Information Technology degree pathway** at **Sri Lanka Institute of Information Technology (SLIIT)**.

The project focuses on monitoring parking slot availability in real time using sensors, microcontrollers, and a backend server. Mirai helps drivers quickly identify available parking spaces while assisting administrators in efficiently managing parking resources.

---

## Live Website
🔗 https://mirai.cyanworks.org/

---

## Project Objectives
- Detect vehicle presence in parking slots using sensors  
- Send real-time parking data to a backend server  
- Display parking availability through a web dashboard  
- Reduce time spent searching for parking spaces  
- Demonstrate practical use of IoT and embedded systems  

---

## System Overview
1. Sensors detect whether a parking slot is occupied or free  
2. A microcontroller (ESP32/Arduino) processes the sensor data  
3. Data is sent to the backend via HTTP 
4. The backend stores and processes the data  
5. The web dashboard displays real-time parking status  

---


##  System Architecture Diagram

![Mirai System Architecture](Progress%20Report/System%20Arch.png)

This diagram illustrates the overall architecture of the Mirai Smart Parking
Management System, showing how sensors and actuators are connected to the ESP32,
which communicates with the backend server, database, and web interface via the internet.


