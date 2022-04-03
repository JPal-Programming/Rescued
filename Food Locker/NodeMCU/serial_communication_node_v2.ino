#include <Arduino.h>

#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>

#include <ESP8266HTTPClient.h>

#include <WiFiClientSecureBearSSL.h>

#include <ArduinoJson.h>

#define ARRAY_SIZE(x) sizeof(x)/sizeof(x[0])

StaticJsonDocument<1000> doc;

const char *ssid     = "Samsung Galaxy S10e_4235"; //"ATT4YIh3Cc";
const char *password = "xctu3046"; //"7fd#nzc?99qw";

uint8_t firebaseFingerprint[20] = {0x4b, 0xd6, 0x90, 0x9f, 0x6d, 0x0e, 0x83, 0x7f, 0x8d, 0xf2, 0xfc, 0xa7, 0x2d, 0x01, 0x7d, 0xfb, 0x7c, 0xf2, 0x98, 0xb6};
uint8_t hashifyFingerprint[20] = {0x75, 0xb3, 0x0a, 0x0c, 0xc0, 0xc2, 0x1e, 0x38, 0x26, 0x6f, 0x9e, 0x65, 0x56, 0xb7, 0xa3, 0x7f, 0xb4, 0x8b, 0x1f, 0x74};
uint8_t fingerprints[2][20] = {{0x4b, 0xd6, 0x90, 0x9f, 0x6d, 0x0e, 0x83, 0x7f, 0x8d, 0xf2, 0xfc, 0xa7, 0x2d, 0x01, 0x7d, 0xfb, 0x7c, 0xf2, 0x98, 0xb6}, {0x75, 0xb3, 0x0a, 0x0c, 0xc0, 0xc2, 0x1e, 0x38, 0x26, 0x6f, 0x9e, 0x65, 0x56, 0xb7, 0xa3, 0x7f, 0xb4, 0x8b, 0x1f, 0x74}}; //firebase fingerprint, hashify fingerprint

String firebaseDemoUrl = "https://firestore.googleapis.com/v1beta1/projects/foodlocker-5341f/databases/(default)/documents/users/cookerturtle?key=KEY";

ESP8266WiFiMulti WiFiMulti;

void setup() {
  Serial.begin(38400);

  WiFi.mode(WIFI_STA);
  WiFiMulti.addAP(ssid, password);
}

void loop() {
  String request = Serial.readStringUntil('|');
  if (request != "") {
    Serial.println("REQUEST: " + request);
    
    if (request == "GET") {
      String getUrl = Serial.readStringUntil('|');      
      int fingerprint = Serial.readStringUntil('|').toInt();
      int returnLevels = Serial.readStringUntil('|').toInt();
      String returnValues[returnLevels] = {};
      for (int i = 0; i < returnLevels; i++)
      {
        returnValues[i] = Serial.readStringUntil('|');
      }
  
      getRequest(getUrl, fingerprints[fingerprint], returnValues, returnLevels);
    } 
    else if (request = "POST") {
      String postPath = Serial.readStringUntil('|');
      String postField = Serial.readStringUntil('|');
      String postType = Serial.readStringUntil('|');
      String postValue = Serial.readStringUntil('|');
      int fingerprint = Serial.readStringUntil('|').toInt();
      int returnLevels = Serial.readStringUntil('|').toInt();
      String returnValues[returnLevels] = {};
      for (int i = 0; i < returnLevels; i++)
      {
        returnValues[i] = Serial.readStringUntil('|');
      }

      postRequest(postPath, postField, postType, postValue, fingerprints[fingerprint], returnValues, returnLevels);
    } 
  }
}

void postRequest(String path, String field, String valueType, String value, uint8_t fingerprint[20], String returnValues[], int returnLevels) {
    // wait for WiFi connection
  if ((WiFiMulti.run() == WL_CONNECTED)) {

    String requestJSONFormatted = "{\"writes\":[{\"update\":{\"name\":\"" + path + "\",\"fields\":{\"" + field + "\":{\"" + valueType + "\":" + value + "}}}}]}";

    Serial.println(requestJSONFormatted);

    // EXAMPLE: "{\"writes\":[{\"update\":{\"name\":\"projects\/foodlocker-5341f\/databases\/(default)\/documents\/users\/cookerturtle\",\"fields\":{\"active\":{\"booleanValue\":false}}}}]}";

    std::unique_ptr<BearSSL::WiFiClientSecure>client(new BearSSL::WiFiClientSecure);

    client->setFingerprint(fingerprint);

    //client->setInsecure();

    HTTPClient https;

    String url = "https://firestore.googleapis.com/v1beta1/projects/foodlocker-5341f/databases/(default)/documents:commit?key=KEY";
    
    if (https.begin(*client, url)) {  // HTTPS

      Serial.println("[HTTPS] BEGIN POST OPERATION");
      // start connection and send HTTP header
//      int httpCode = https.GET();
      https.addHeader("Content-Type", "application/json");
 
      int httpCode = https.POST(requestJSONFormatted);

      String payload = https.getString();
      Serial.println(payload);
      processJson(payload, returnValues, returnLevels);
      
//      // httpCode will be negative on error
//      if (httpCode > 0) {
//        // HTTP header has been send and Server response header has been handled
//        Serial.printf("[HTTPS] POST RESPONSE CODE: %d\n", httpCode);
//
//        // file found at server
//        if (httpCode == HTTP_CODE_OK || httpCode == HTTP_CODE_MOVED_PERMANENTLY) {
//          String payload = https.getString();
//          processJson(payload, returnValues);
//        } else {
//          Serial.println(https.getString());
//        }
//      } else {
//        Serial.printf("[HTTPS] POST FAILED, ERROR: %s\n", https.errorToString(httpCode).c_str());
//      }

      https.end();
    } else {
      Serial.printf("[HTTPS] UNABLE TO CONNECT...");
    }
  }
}

void getRequest(String url, const uint8_t fingerprint[20], String returnValues[], int returnLevels) {
    // wait for WiFi connection
  if ((WiFiMulti.run() == WL_CONNECTED)) {

    std::unique_ptr<BearSSL::WiFiClientSecure>client(new BearSSL::WiFiClientSecure);

    client->setFingerprint(fingerprint);

    //client->setInsecure();

    HTTPClient https;

    Serial.println(url);

    if (https.begin(*client, url)) {  // HTTPS

      Serial.print("HTTPS[] BEGIN GET OPERATION\n");
      // start connection and send HTTP header
//      int httpCode = https.GET();
 
      int httpCode = https.GET();

      String payload = https.getString();
      Serial.println(payload);
      processJson(payload, returnValues, returnLevels);
          
//      // httpCode will be negative on error
//      if (httpCode > 0) {
//        // HTTP header has been send and Server response header has been handled
//        Serial.printf("[HTTPS] GET RESPONSE CODE: %d\n", httpCode);
//
//        // file found at server
//        if (httpCode == HTTP_CODE_OK) {
//          String payload = https.getString();
//          processJson(payload, returnValues);
//        } else {
//          Serial.println(https.getString());
//        }
//      } else {
//        Serial.printf("[HTTPS] GET FAILED, ERROR: %s\n", https.errorToString(httpCode).c_str());
//      }

      https.end();
    } else {
      Serial.printf("[HTTPS] UNABLE TO CONNECT...");
    }
  }
}

void processJson(String json, String returnValues[], int returnLevels)
{  
  // Deserialize the JSON document
  DeserializationError error = deserializeJson(doc, json);
  
  Serial.print('`');
  if (returnLevels == 1)
  {
    const char* returnValue = doc[returnValues[0]];
    Serial.print(returnValue); 
  } else if (returnLevels == 2)
  {
    const char* returnValue = doc[returnValues[0]][returnValues[1]];
    Serial.print(returnValue); 
  } else if (returnLevels == 3)
  {
    const char* returnValue = doc[returnValues[0]][returnValues[1]][returnValues[2]];
    Serial.print(returnValue); 
  } else if (returnLevels == 4)
  {
    const char* returnValue = doc[returnValues[0]][returnValues[1]][returnValues[2]][returnValues[3]];
    Serial.print(returnValue);
  } else if (returnLevels == 5)
  {
    const char* returnValue = doc[returnValues[0]][returnValues[1]][returnValues[2]][returnValues[3]][returnValues[4]];
    Serial.print(returnValue);
  }
  Serial.println('~');
}
