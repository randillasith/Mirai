#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <SPI.h>
#include <MFRC522.h>
#include <ESP32Servo.h>  // SERVO
#include <Wire.h>
#include <U8g2lib.h>  // OLED
unsigned long writeModeStart = 0;
const unsigned long WRITE_TIMEOUT_MS = 15000;  // 40 seconds

// ---------------- RFID PINS ----------------
#define SS_PIN 5   // RC522 SDA/SS
#define RST_PIN 4  // RC522 RST

MFRC522 rfid(SS_PIN, RST_PIN);
MFRC522::MIFARE_Key key;

// ---------------- WIFI ----------------
const char* ssid = "mirai";
const char* password = "11111112";

// IP of your PC where Java server runs
String serverIP = "mirai.cyanworks.org";  // <= CHANGE IF NEEDED

byte blockNumber = 1;  // RFID data block

bool writePending = false;
String pendingWriteText = "";

// ---------------- SERVO (standard 180°) ----------------
Servo gateServo;
const int SERVO_PIN = 13;  // signal pin to servo

const int GATE_CLOSED_ANGLE = 0;     // barrier down
const int GATE_OPEN_ANGLE = 90;      // barrier up
const int GATE_OPEN_WAIT_MS = 3000;  // gate stay open (3s)

void openGate() {
  Serial.println(">> OPENING GATE");
  gateServo.write(GATE_OPEN_ANGLE);  // move to open position (90°)
  delay(GATE_OPEN_WAIT_MS);          // wait while car passes

  Serial.println(">> CLOSING GATE");
  gateServo.write(GATE_CLOSED_ANGLE);  // move back to closed (0°)
  Serial.println(">> GATE CLOSED");
}

// ---------------- ULTRASONIC – SLOT 1 & SLOT 2 ----------------

// HC-SR04 #1 : Slot 1
const int SLOT1_TRIG_PIN = 27;
const int SLOT1_ECHO_PIN = 14;

// HC-SR04 #2 : Slot 2
const int SLOT2_TRIG_PIN = 25;
const int SLOT2_ECHO_PIN = 26;

bool slot1Occ = false;  // true = occupied, false = free
bool slot2Occ = false;

// Read distance in cm from HC-SR04
long readDistanceCm(int trigPin, int echoPin) {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  long duration = pulseIn(echoPin, HIGH, 30000);  // timeout 30ms
  if (duration == 0) {
    return 999;  // no echo
  }
  long distance = duration * 0.0343 / 2;  // sound speed ~343 m/s
  return distance;
}

// Send slot status to server: /api/slot?slot=1&occ=0/1
void sendSlotToServer(int slot, bool occ) {
  if (WiFi.status() != WL_CONNECTED) return;

  WiFiClientSecure client;
  client.setInsecure();  // <-- THIS is REQUIRED for HTTPS

  HTTPClient http;
  String url = "https://" + serverIP + "/api/slot?slot=" + String(slot) + "&occ=" + (occ ? "1" : "0");

  Serial.print("Slot update request: ");
  Serial.println(url);

  http.begin(client, url);
  int httpCode = http.GET();

  Serial.print("Slot HTTP code: ");
  Serial.println(httpCode);

  http.end();
}


// Measure and update any slot (generic)
void updateSlot(int slot, int trigPin, int echoPin, bool& occState) {
  long d = readDistanceCm(trigPin, echoPin);
  bool newOcc;

  // Threshold: < 30 cm = occupied (tune this per your sensor mounting)
  if (d > 0 && d < 30) {
    newOcc = true;
  } else {
    newOcc = false;
  }

  if (newOcc != occState) {
    occState = newOcc;
    Serial.print("Slot");
    Serial.print(slot);
    Serial.print(" changed: ");
    Serial.print(occState ? "OCCUPIED" : "FREE");
    Serial.print("  (");
    Serial.print(d);
    Serial.println(" cm)");
    sendSlotToServer(slot, occState);
  }
}

// ---------------- OLED (DM0051 SH1107 128x128) ----------------
// SDA -> 21, SCL -> 22
U8G2_SH1107_PIMORONI_128X128_F_HW_I2C u8g2(U8G2_R0, U8X8_PIN_NONE);

// ---------------- HELPER: UID STRING ----------------
String uidToString() {
  String s = "";
  for (byte i = 0; i < rfid.uid.size; i++) {
    if (rfid.uid.uidByte[i] < 0x10) s += "0";
    s += String(rfid.uid.uidByte[i], HEX);
    if (i != rfid.uid.size - 1) s += ":";
  }
  s.toUpperCase();
  return s;
}

String urlEncode(const String& text) {
  String encoded = "";
  for (int i = 0; i < text.length(); i++) {
    char c = text[i];
    if (c == ' ') encoded += "%20";
    else encoded += c;
  }
  return encoded;
}

// ---------------- OLED SCREENS ----------------

// Idle screen: show parking info (2 slots)
void drawParkingScreen() {
  u8g2.clearBuffer();
  u8g2.setFont(u8g2_font_6x13_tr);
  u8g2.setFontPosCenter();

  // Title
  const char* title = "MIRAI Parking";
  u8g2.drawStr((128 - u8g2.getStrWidth(title)) / 2, 20, title);

  // Slot 1 status
  const char* s1Label = "Slot 1:";
  const char* s1State = slot1Occ ? "OCCUPIED" : "FREE";
  u8g2.drawStr(10, 55, s1Label);
  u8g2.drawStr(70, 55, s1State);

  // Slot 2 status
  const char* s2Label = "Slot 2:";
  const char* s2State = slot2Occ ? "OCCUPIED" : "FREE";
  u8g2.drawStr(10, 75, s2Label);
  u8g2.drawStr(70, 75, s2State);

  // Summary line
  int occupied = (slot1Occ ? 1 : 0) + (slot2Occ ? 1 : 0);
  int total = 2;
  int free = total - occupied;

  char buf[32];
  snprintf(buf, sizeof(buf), "Free: %d  Occ: %d", free, occupied);
  u8g2.drawStr((128 - u8g2.getStrWidth(buf)) / 2, 105, buf);

  u8g2.sendBuffer();
}

void drawWelcomeScreen(const char* name) {
  u8g2.clearBuffer();
  u8g2.setFont(u8g2_font_ncenB14_tr);
  u8g2.setFontPosCenter();
  u8g2.drawStr((128 - u8g2.getStrWidth("WELCOME")) / 2, 40, "WELCOME");

  u8g2.setFont(u8g2_font_6x13_tr);
  u8g2.setFontPosCenter();
  u8g2.drawStr((128 - u8g2.getStrWidth(name)) / 2, 70, name);

  u8g2.sendBuffer();
}

void drawGoodbyeScreen(const char* name) {
  u8g2.clearBuffer();
  u8g2.setFont(u8g2_font_ncenB14_tr);
  u8g2.setFontPosCenter();
  u8g2.drawStr((128 - u8g2.getStrWidth("GOOD BYE")) / 2, 40, "GOOD BYE");

  u8g2.setFont(u8g2_font_6x13_tr);
  u8g2.setFontPosCenter();
  u8g2.drawStr((128 - u8g2.getStrWidth(name)) / 2, 70, name);

  u8g2.sendBuffer();
}

void drawGateOpening(const char* text) {
  u8g2.clearBuffer();
  u8g2.setFont(u8g2_font_6x13_tr);
  u8g2.setFontPosCenter();
  u8g2.drawStr((128 - u8g2.getStrWidth(text)) / 2, 60, text);
  u8g2.sendBuffer();
}

void drawGateClosed() {
  u8g2.clearBuffer();
  u8g2.setFont(u8g2_font_6x13_tr);
  u8g2.setFontPosCenter();
  u8g2.drawStr((128 - u8g2.getStrWidth("Gate closed")) / 2, 60, "Gate closed");
  u8g2.sendBuffer();
}

void drawAccessDenied() {
  u8g2.clearBuffer();
  u8g2.setFont(u8g2_font_6x13_tr);
  u8g2.setFontPosCenter();
  u8g2.drawStr((128 - u8g2.getStrWidth("ACCESS DENIED")) / 2, 50, "ACCESS DENIED");
  u8g2.drawStr((128 - u8g2.getStrWidth("Unknown card")) / 2, 75, "Unknown card");
  u8g2.sendBuffer();
}

// ---------------- SERVER COMMUNICATION ----------------
// Expect server to reply one of:
//   "OK_IN|Driver Name"   -> first scan (enter parking)
//   "OK_OUT|Driver Name"  -> leaving parking
//   "DENIED|Unknown"
//   "IGNORED|Bad read"
void sendScanToServer(const String& uid, const String& text, bool isWrite) {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi not connected, cannot send");
    return;
  }

  WiFiClientSecure client;
  client.setInsecure();

  HTTPClient http;
  String url = "https://" + serverIP + "/api/scan?uid=" + uid + "&reader=1&text=" + urlEncode(text) + "&mode=" + (isWrite ? "write" : "read");

  http.begin(client, url);

  Serial.print("Request: ");
  Serial.println(url);

  http.begin(url);
  int httpCode = http.GET();
  Serial.print("HTTP response code: ");
  Serial.println(httpCode);

  if (httpCode > 0) {
    String body = http.getString();
    Serial.print("Server reply: ");
    Serial.println(body);

    // Parse "STATUS|Name"
    String status = body;
    String driverName = "Guest";
    int sep = body.indexOf('|');
    if (sep != -1) {
      status = body.substring(0, sep);
      driverName = body.substring(sep + 1);
      status.trim();
      driverName.trim();
    } else {
      status.trim();
    }

    if (status == "OK_IN") {
      // First time: ENTER → welcome
      drawWelcomeScreen(driverName.c_str());
      delay(1500);

      drawGateOpening("Gate opening...");
      openGate();

      drawGateClosed();
      delay(1500);

      // After animation, refresh slots + screen
      updateSlot(1, SLOT1_TRIG_PIN, SLOT1_ECHO_PIN, slot1Occ);
      updateSlot(2, SLOT2_TRIG_PIN, SLOT2_ECHO_PIN, slot2Occ);
      drawParkingScreen();

    } else if (status == "OK_OUT") {
      // Leaving: EXIT → goodbye
      drawGoodbyeScreen(driverName.c_str());
      delay(1500);

      drawGateOpening("Gate opening...");
      openGate();

      drawGateClosed();
      delay(1500);

      updateSlot(1, SLOT1_TRIG_PIN, SLOT1_ECHO_PIN, slot1Occ);
      updateSlot(2, SLOT2_TRIG_PIN, SLOT2_ECHO_PIN, slot2Occ);
      drawParkingScreen();

    } else if (status == "FULL") {
      Serial.println("Parking is FULL. Gate will NOT open.");

      drawParkingFull();
      delay(2000);

      // Refresh slot status and go back to main screen
      updateSlot(1, SLOT1_TRIG_PIN, SLOT1_ECHO_PIN, slot1Occ);
      updateSlot(2, SLOT2_TRIG_PIN, SLOT2_ECHO_PIN, slot2Occ);
      drawParkingScreen();


    } else if (status == "DENIED") {
      Serial.println("Access denied: vehicle not registered.");
      drawAccessDenied();
      delay(1500);

      updateSlot(1, SLOT1_TRIG_PIN, SLOT1_ECHO_PIN, slot1Occ);
      updateSlot(2, SLOT2_TRIG_PIN, SLOT2_ECHO_PIN, slot2Occ);
      drawParkingScreen();

    } else if (status == "IGNORED") {
      Serial.println("Ignored scan: bad card read.");
    } else {
      Serial.println("Unknown status from server.");
    }
  } else {
    Serial.println("HTTP request failed");
  }

  http.end();
}




void drawParkingFull() {
  u8g2.clearBuffer();
  u8g2.setFont(u8g2_font_ncenB14_tr);
  u8g2.setFontPosCenter();
  u8g2.drawStr((128 - u8g2.getStrWidth("PARKING")) / 2, 40, "PARKING");
  u8g2.drawStr((128 - u8g2.getStrWidth("FULL")) / 2, 70, "FULL");
  u8g2.sendBuffer();
}


// ---------------- SETUP ----------------
void setup() {
  Serial.begin(115200);

  // RFID
  SPI.begin();  // SCK=18, MISO=19, MOSI=23
  rfid.PCD_Init();
  for (byte i = 0; i < 6; i++) key.keyByte[i] = 0xFF;

  // OLED
  Wire.begin(21, 22);
  u8g2.begin();

  // ULTRASONIC pin modes
  pinMode(SLOT1_TRIG_PIN, OUTPUT);
  pinMode(SLOT1_ECHO_PIN, INPUT);
  pinMode(SLOT2_TRIG_PIN, OUTPUT);
  pinMode(SLOT2_ECHO_PIN, INPUT);

  // SERVO
  gateServo.setPeriodHertz(50);
  gateServo.attach(SERVO_PIN, 500, 2400);
  gateServo.write(GATE_CLOSED_ANGLE);  // start CLOSED

  // WiFi
  Serial.println("Connecting to WiFi...");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.print("WiFi connected, IP: ");
  Serial.println(WiFi.localIP());

  // Initial slot check + draw parking screen
  updateSlot(1, SLOT1_TRIG_PIN, SLOT1_ECHO_PIN, slot1Occ);
  updateSlot(2, SLOT2_TRIG_PIN, SLOT2_ECHO_PIN, slot2Occ);
  drawParkingScreen();
}

// ---------------- LOOP ----------------
unsigned long lastSlotUpdate = 0;

void loop() {

  // ⏱️ Auto-exit write mode after timeout
  if (writePending && millis() - writeModeStart > WRITE_TIMEOUT_MS) {
    Serial.println("WRITE MODE TIMEOUT → exiting");
    writePending = false;
    pendingWriteText = "";

    // optional: notify backend
    WiFiClientSecure client;
    client.setInsecure();
    HTTPClient http;
    http.begin(client, "https://" + serverIP + "/api/rfid/clear");
    http.POST("");
    http.end();
  }

  // 1) Periodically update slot 1 & 2 and redraw parking info when idle
  unsigned long now = millis();
  if (now - lastSlotUpdate >= 500) {  // every 500 ms
    lastSlotUpdate = now;
    updateSlot(1, SLOT1_TRIG_PIN, SLOT1_ECHO_PIN, slot1Occ);
    updateSlot(2, SLOT2_TRIG_PIN, SLOT2_ECHO_PIN, slot2Occ);
    drawParkingScreen();
  }

  // 2) Poll server for pending write (if you ever implement /api/getWrite)
  checkWriteFromServer();

  // 3) Check for card
  if (!rfid.PICC_IsNewCardPresent() || !rfid.PICC_ReadCardSerial()) {
    delay(20);
    return;
  }

  String uidStr = uidToString();
  Serial.print("Card UID: ");
  Serial.println(uidStr);

  MFRC522::StatusCode status;
  byte buffer[18];
  byte size = sizeof(buffer);
  String text = "";

  // Authenticate
  status = rfid.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A,
                                 blockNumber, &key, &(rfid.uid));
  if (status != MFRC522::STATUS_OK) {
    Serial.print("Auth failed: ");
    Serial.println(rfid.GetStatusCodeName(status));
    rfid.PICC_HaltA();
    rfid.PCD_StopCrypto1();
    delay(1000);
    return;
  }

  bool didWrite = false;

  if (writePending) {
    // ----- WRITE MODE -----
    String t = pendingWriteText;
    if (t.length() > 16) t = t.substring(0, 16);

    byte block[16];
    for (byte i = 0; i < 16; i++) block[i] = 0x20;  // spaces
    for (byte i = 0; i < t.length(); i++) block[i] = t[i];

    status = rfid.MIFARE_Write(blockNumber, block, 16);
    if (status == MFRC522::STATUS_OK) {
      Serial.print("Write OK: ");
      Serial.println(t);
      text = t;
      didWrite = true;
      writePending = false;
      pendingWriteText = "";
    } else {
      Serial.print("Write failed: ");
      Serial.println(rfid.GetStatusCodeName(status));
      text = "WRITE_ERROR";
    }

  } else {
    // ----- READ MODE -----
    status = rfid.MIFARE_Read(blockNumber, buffer, &size);
    if (status == MFRC522::STATUS_OK) {
      for (byte i = 0; i < 16; i++) {
        if (buffer[i] >= 32 && buffer[i] <= 126) {
          text += char(buffer[i]);
        } else {
          text += ' ';
        }
      }
      text.trim();
      Serial.print("Block text (vehicle code): ");
      Serial.println(text);
    } else {
      Serial.print("Read failed: ");
      Serial.println(rfid.GetStatusCodeName(status));
      text = "READ_ERROR";
    }
  }

  rfid.PICC_HaltA();
  rfid.PCD_StopCrypto1();

  // 4) Send result to server
  sendScanToServer(uidStr, text, didWrite);

  delay(500);  // small delay so it doesn't spam
}
void checkWriteFromServer() {
  if (WiFi.status() != WL_CONNECTED) return;
  if (writePending) return;

  WiFiClientSecure client;
  client.setInsecure();

  HTTPClient http;
  String url = "https://" + serverIP + "/api/rfid/pending";

  http.begin(client, url);
  int code = http.GET();

  if (code == 200) {
    String body = http.getString();

    if (body.startsWith("WRITE:")) {
      pendingWriteText = body.substring(6);
      writePending = true;
      writeModeStart = millis();
      Serial.print("WRITE MODE ENABLED → ");
      Serial.println(pendingWriteText);
    }
    if (body == "NONE") {
  if (writePending) {
    Serial.println("WRITE MODE CANCELLED FROM UI");
    writePending = false;
    pendingWriteText = "";
  }
}

  }

  http.end();
}
