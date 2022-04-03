#include "Adafruit_Keypad.h"
#include "Nextion.h";
#include <ArduinoJson.h>

#include <Wire.h>
#include <PN532_I2C.h>
#include <PN532.h>   // The following files are included in the libraries Installed
#include <NfcAdapter.h>

#define debugPower 42
#define debugSignal 43
#define debugIn 44

PN532_I2C pn532_i2c(Wire);
NfcAdapter nfc = NfcAdapter(pn532_i2c);  // Indicates the Shield you are using

int debugState = LOW;
int buttonCurrent;
int buttonPrevious = HIGH;

String currentPayloadString = "";

StaticJsonDocument<1000> doc;

String endChar = "\xFF\xFF\xFF";

boolean onPage3 = false;
unsigned long page3Millis;
unsigned long nfcMillis;

boolean onPage1 = false;
int numPos = 0;
String code;

const byte ROWS = 4; // rows
const byte COLS = 3; // columns
//define the symbols on the buttons of the keypads
char keys[ROWS][COLS] = {
  {'1','2','3'},
  {'4','5','6'},
  {'7','8','9'},
  {'*','0','#'}
};
byte rowPins[ROWS] = {5, 4, 3, 2}; //connect to the row pinouts of the keypad
byte colPins[COLS] = {8, 7, 6}; //connect to the column pinouts of the keypad

//initialize an instance of class NewKeypad
Adafruit_Keypad customKeypad = Adafruit_Keypad( makeKeymap(keys), rowPins, colPins, ROWS, COLS);

NexText t0 = NexText(1, 12, "t0");
NexPicture p0 = NexPicture(0, 1, "p0");
NexNumber n0 = NexNumber(1, 5, "n0");
NexNumber n1 = NexNumber(1, 7, "n1");
NexNumber n2 = NexNumber(1, 3, "n2");
NexNumber n3 = NexNumber(1, 9, "n3");

NexTouch *nex_listen_list[] = 
{
  &t0,
  &p0,
  &n0,
  &n1,
  &n2,
  &n3,
  NULL
};

void setup() {
  Serial2.begin(38400); //NodeMCU
  Serial1.begin(38400); //Nextion
  Serial.begin(38400); //Use for debugging
  
  //nfc.begin();
  
  customKeypad.begin();

  t0.attachPop(t0PopCallback);
  p0.attachPop(p0PopCallback);
  n0.attachPop(n0PopCallback);
  n1.attachPop(n1PopCallback);
  n2.attachPop(n2PopCallback);
  n3.attachPop(n3PopCallback);

  pinMode(debugPower, OUTPUT);
  digitalWrite(debugPower, HIGH);

  pinMode(debugSignal, OUTPUT);

  pinMode(debugIn, INPUT);
  pinMode(13, INPUT);

  pinMode(28, OUTPUT);
  pinMode(10, OUTPUT);
}

void loop() {
  nexLoop(nex_listen_list);  // Check for any touch event
  
  customKeypad.tick();

  while(customKeypad.available() && onPage1){
    keypadEvent e = customKeypad.read();
//    Serial.print((char)e.bit.KEY);
//    if(e.bit.EVENT == KEY_JUST_PRESSED) Serial.println(" pressed");
//    else if(e.bit.EVENT == KEY_JUST_RELEASED) Serial.println(" released");
    char num = (char)e.bit.KEY;
    
    if(e.bit.EVENT == KEY_JUST_RELEASED && isDigit(num))
    {      
      Serial2.print("vis n");
      Serial2.print(numPos);
      Serial2.print(",1");
      endNextionCommand();

      Serial2.print("n");
      Serial2.print(numPos);
      Serial2.print(".val=");
      Serial2.print(num);
      endNextionCommand();

      code += num;
      numPos++;

      changeNumColor();

      if (numPos == 4)
      {
        beginVerification(code);
      }
    }

  }

  if (onPage1 && millis() - nfcMillis >= 500) {
    //NfcTag tag = nfc.read();

    nfcMillis = millis();

//    if (tag.hasNdefMessage() && tag.getUidString() == "A3 00 8E 06")
//    {
//
//      NdefMessage message = tag.getNdefMessage();
//      
//      NdefRecord record = message.getRecord(0);
//
//      int payloadLength = record.getPayloadLength();
//      byte payload[payloadLength];
//      record.getPayload(payload);
//
//
//      String payloadAsString = ""; // Processes the message as a string vs as a HEX value
//      for (int c = 0; c < payloadLength; c++) {
//        payloadAsString += (char)payload[c];
//      }
//
//      payloadAsString = payloadAsString.substring(3);
//
//      Serial.println(payloadAsString);
//
//      if (payloadAsString != currentPayloadString && payloadAsString != "") {
//        Serial.println(payloadAsString);
//        verifyCode(payloadAsString);
//        currentPayloadString = payloadAsString;
//      }
//    }
  }
  
  buttonCurrent = digitalRead(debugIn);

  if (buttonCurrent == HIGH && buttonPrevious == LOW)
  {
    if (debugState == HIGH)
    {
      debugState = LOW;
    }
    else {
      debugState = HIGH;
    }
  }

  digitalWrite(debugSignal, debugState);

  buttonPrevious = buttonCurrent;

  if (digitalRead(13) == HIGH && !onPage3) {
    unlockDoor();
  }
  
  if(Serial1.available())
  {
    char serialValue = Serial1.read();

    if (serialValue == '`') {
      Serial.println("RESULT RECEIVED: ");
      
      String result = Serial1.readStringUntil('~');

      Serial.println(result);

      verifyCode(result);

//      const char* errorCode = doc["error"]["status"];
//      Serial.print("DETECTED ERROR: ");
//      Serial.println(errorCode);
      
      //verifyCode(doc);
    
      // Fetch values.
      //
      // Most of the time, you can rely on the implicit casts.
      // In other case, you can do doc["time"].as<long>();
//      const char* name = doc["name"];
//      boolean active = doc["fields"]["active"]["booleanValue"];
//      const char* createTime = doc["createTime"];
//    
//      // Print values.
//      Serial.println(name);
//      Serial.println(createTime);
//      if (active) {
//        Serial.println("true");
//      } else {
//        Serial.println("false");
//      }
      
    }
    Serial.print(serialValue);
  }

  if (onPage3 && millis() - page3Millis >= 10000)
  {
    clearNextion();
  }


}

void writeToBuffer(String msg)
{
    NdefMessage message = NdefMessage();
    message.addTextRecord(msg); // Text Message you want to Record
    
    NfcTag tag = nfc.read();

    if (tag.getUidString() == "A3 00 8E 06")
    {
      boolean success = nfc.write(message); // Write OTP to RFID buffer
    }
    else
    {
      Serial.println("Invalid tag. Please recalibrate");
    }
}

void array_to_string(byte array[], unsigned int len, char buffer[])
{
   for (unsigned int i = 0; i < len; i++)
   {
      byte nib1 = (array[i] >> 4) & 0x0F;
      byte nib2 = (array[i] >> 0) & 0x0F;
      buffer[i*2+0] = nib1  < 0xA ? '0' + nib1  : 'A' + nib1  - 0xA;
      buffer[i*2+1] = nib2  < 0xA ? '0' + nib2  : 'A' + nib2  - 0xA;
   }
   buffer[len*2] = '\0';
}

void postValue(String path, String field, String valueType, String value, int fingerprintNumber, int returnLevels, String returnValues[])
{
  Serial1.print("POST"); // USE SERIAL.PRINT (SERIAL.PRINTLN ADDS A NEWLINE CHARACTER)
  Serial1.print("|"); // Separator character
  Serial1.print(path); // GET URL
  Serial1.print("|");
  Serial1.print(field);
  Serial1.print("|");
  Serial1.print(valueType);
  Serial1.print("|");
  Serial1.print(value);
  Serial1.print("|");
  Serial1.print(fingerprintNumber);
  Serial1.print("|");
  Serial1.print(returnLevels);
  Serial1.print("|");
  for (int i = 0; i < returnLevels; i++)
  {
    Serial1.print(returnValues[i]);
    Serial1.print("|");
  }
}

void getValue(String url, int fingerprintNumber, int returnLevels, String returnValues[])
{
  Serial1.print("GET"); // USE SERIAL.PRINT (SERIAL.PRINTLN ADDS A NEWLINE CHARACTER)
  Serial1.print("|"); // Separator character
  Serial1.print(url); // GET URL
  Serial1.print("|");
  Serial1.print(fingerprintNumber);
  Serial1.print("|");
  Serial1.print(returnLevels);
  Serial1.print("|");
  for (int i = 0; i < returnLevels; i++)
  {
    Serial1.print(returnValues[i]);
    Serial1.print("|");
  }
}

void resetNextion(String objectType)
{
  for (int i=0;i<4;i++)
  {
    int j;
    if (objectType == "p") j=i+1;
    else j=i;
    Serial2.print("vis " + objectType);
    Serial2.print(j);
    Serial2.print(",0");
    endNextionCommand();
  }
}

void endNextionCommand()
{
  Serial2.print(endChar);
}

void beginVerification(String newCode)
{
  String returnValues[] = {"error", "status"};
  getValue("https://firestore.googleapis.com/v1beta1/projects/foodlocker-5341f/databases/(default)/documents/codes/" + newCode + "?key=AIzaSyDu0llzElloDrp0Zdev3gJ4rzXIKtsQg3A", 0, 2, returnValues);
  //postValue("projects\/foodlocker-5341f\/databases\/(default)\/documents\/users\/cookerturtle", "active", "booleanValue", "false", 0);

  Serial2.print("page 2");
  endNextionCommand();
}

void verifyCode(String result)
{
  Serial.println("Verifying code");
  
  if (result == "NOT_FOUND") //code isn't in firestore
  {
    Serial2.print("page 1");
    endNextionCommand();
    Serial2.print("vis p5,1");
    endNextionCommand();
    Serial2.print("vis p6,1");
    endNextionCommand();
    Serial2.print("vis t0,1");
    endNextionCommand();
    Serial2.print("vis t1,1");
    endNextionCommand();

    code = "";
    numPos = 0;

    Serial.println("Incorrect Code");
  }
  else 
  {
    unlockDoor();
  }
}

void unlockDoor() {
    Serial2.print("page 3");
    endNextionCommand();
    tone(10, 500, 2000);

    //writeToBuffer("");

    onPage3 = true;
    onPage1 = false;
    page3Millis = millis();

    digitalWrite(28, HIGH);

    Serial.println("Verified");
}

void changeNumColor()
{
  resetNextion("p");
  
  Serial2.print("vis p");
  Serial2.print(numPos+1);
  Serial2.print(",1");
  endNextionCommand();
}

void clearNextion()
{
  digitalWrite(28, LOW);
  Serial2.print("page 0");
  endNextionCommand();
  code = "";
  numPos = 0;
  onPage1 = false;
  onPage3 = false;
  currentPayloadString = "";
}

// Touch Events: //
void t0PopCallback(void *ptr)
{
  Serial2.print("vis p5,0");
  endNextionCommand();
  Serial2.print("vis p6,0");
  endNextionCommand();
  Serial2.print("vis t0,0");
  endNextionCommand();
  Serial2.print("vis t1,0");
  endNextionCommand();
}

void p0PopCallback(void *ptr)
{
  onPage1 = true;
  Serial2.print("page 1");
  endNextionCommand();
  nfcMillis = millis();
}

void n0PopCallback(void *ptr)
{
  code = "";
  numPos = 0;
  changeNumColor();
}

void n1PopCallback(void *ptr)
{
  code = code[0];
  numPos = 1;
  changeNumColor();
}

void n2PopCallback(void *ptr)
{
  code = code.substring(0,2);
  numPos = 2;
  changeNumColor();
}

void n3PopCallback(void *ptr)
{
  code = code.substring(0,3);
  numPos = 3;
  changeNumColor();
}
