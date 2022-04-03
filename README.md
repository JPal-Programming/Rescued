# Rescued
An innovative take on an age-old problem.

## Inspiration

Rescued is an innovative solution to the food supply crisis caused by the pandemic. According to the USDA's latest “Household Food Insecurity in the United States” report, more than 38 million people in the United States experienced hunger in 2020. In addition, the USDA estimates that food waste is estimated at between 30-40 percent of the total food supply. My app helps bridge this gap between businesses that have excess food and people in need by giving stores the ability to donate their unsold food to the people who need it. 

During the pandemic, many families struggled to afford food due to rising local unemployment rates. After speaking with a local food shelter and talking to members of my community, I noticed that a startling number of people were experiencing the effects of food insecurity. At the same time, many small businesses saw a decline in customers and therefore an increase in food waste. Upon identifying this problem, I felt the need to create an app that would solve both food insecurity and food waste. 

## What it does

The first part of the project is a prototype food locker that provides a secure and refrigerated location for food storage starting at its Sell-By date and ending when it is picked up by someone who needs it. The prototype I made consists of electrical components attached to a chest refrigerator. The ultimate purpose of the electrical components is to operate a solenoid lock that can be used to bolt the lid shut. See [How I built it](#how-i-built-it) for more information about the electronics.

The mobile application is the main user interface, with usage of the app required to access the Food Locker. Specifically, creating an account with the mobile app gives users the credentials required to unlock the locker. The main page of the app shows all the Food Lockers near the user. The app is able to determine this by taking the user's location and displaying the Food Lockers within a 50-mile radius. By clicking on a Food Locker (delineated by a custom map location pin), users can see what food is available at each locker, the locker's distance, and request directions through the integrated directions information. The secondary page gives credentials to unlock the Food Locker, including a numeric code as well as NFC Contactless verification.

## How I built it

The electronics can be split into 2 parts - the Arduino and the NodeMCU. The Arduino was responsible for handling all the physical IO (e.g. sensor data, controlling the solenoid), and the NodeMCU was responsible for all the wireless communication (e.g. writing to the server, validating against server data, etc.). And I could have used an Arduino Uno WiFi, but I didn't have one, so I chose to suffer. I had to code drivers for each board so they could exchange data through Serial communication. And I've used NFC before, so I thought it would be easy, but 1-way communication is very different from 2-way communication, as I soon found out ([see Challenges I ran into](#challenges-i-ran-into)). Finally, I had to make sure the server reads and writes could happen smoothly, since doing so was a critical portion of the hardware capabilities.

## Challenges I ran into

I encountered several obstacles during the production of my app. One such roadblock that I faced was integrating near-field communication. Specifically, I needed to incorporate host-based card emulation, a technology that had minimal documentation to assist me. However, I got around this by utilizing a buffer RFID chip. This works because the android app is able to write to the buffer RFID, which then relays the message to the PN532 NFC module. This allowed me to incorporate the NFC and contactless check-in capability into the hardware and the app.

## Accomplishments that I'm proud of

I'm proud I made it this far without collapsing out of exhaustion. But all jokes aside, I am happy to see that I was able to combine various independent technologies to create a coherent final product. Although I had previously worked with most of the components individually, this was the first time I had combined them.

## What I learned

* Get a full night of sleep before starting a 24-hour hackathon
* Keep a stash of granola bars at all times
* Avocados are fruits, not vegetables

Oh... you mean about my project?
Yeah, that was pretty straightforward as you can see from my coherent and composed thoughts while writing this.

## What's next for Rescued

As of right now, the app is limited to android-enabled devices. However, depending on the statistic, iOS controls anywhere from 10-30% of the global market share, including a ~57% market share in the US. Given more time, the next course of action will be to expand into iOS, making my app much more accessible. In addition, we would like to make a sturdier version of the hardware. Due to time and budget constraints, the hardware we used was nowhere near the strength and reliability needed for a real-life application.

## Notice - IMPORTANT

The API keys have been removed for security. If you would like to request the API keys for demo purposes, [please reach out to me by email.](mailto:bbobjoeyguy@gmail.com)

