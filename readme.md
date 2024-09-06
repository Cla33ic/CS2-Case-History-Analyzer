# Steam Inventory History Tool

## 🎮 Overview

The Steam Inventory History Tool is a Java application designed to provide CS2 players with deep insights into their case opening history. With this tool, you can analyze your inventory history, get detailed statistics about opened cases, and determine the total value of your case opening activities.

## ✨ Key Features

- 📊 Detailed analysis of your CS2 case opening history
- 💰 Calculation of total costs for keys and cases
- 🏆 Breakdown of received items by rarity
- 📈 Statistics on most frequently opened cases
- 💾 Saving of analysis results for later review

## 🛠 Installation

To use the Steam Inventory History Tool, please follow these steps:

1. Ensure that Java 11 or higher is installed on your system.
2. Clone this repository:
   ```
   git clone https://github.com/cla33ic/CS2_CaseFetcher.git
   ```
3. Navigate to the project directory:
4. Compile the project with Maven:
   ```
   mvn clean install
   ```

## 🚀 Usage

To run the tool, use the following command in the project directory:

```
java -jar .\target\CaseFetcher-1.0-SNAPSHOT-jar-with-dependencies.jar
```

You can also download the jar file from the releases page and run it directly.
```
java -jar CS2_CaseFetcher1.0.jar
```


You will be asked for the following information:

1. Your Steam profile URL
2. Your Steam login cookie (see below for instructions)

### 🍪 How to obtain the Steam login cookie:

1. Log in to [steamcommunity.com](https://steamcommunity.com).
2. Open your browser's Developer Tools (F12 in most browsers).
3. Go to the "Network" tab.
4. Refresh the page.
5. Look for a request to "steamcommunity.com", mostly your username.
6. In the Request Headers, find the "Cookie" value.
7. Copy the entire value of the "steamLoginSecure" cookie.

⚠️ **Important**: Never share your Steam login cookie with others. It grants access to your account!

## 📊 Sample Output

After analyzing your inventory history, you'll receive an output similar to this:

```
Case Opening Summary:

Total cases opened: 224
Total cost for keys: 526.40€
Total cost for cases: 215.69€
Total cost: 742.09€

Cases Opened:
Fracture Case: 19 (8.48%)
Spectrum 2 Case: 5 (2.23%)
Spectrum Case: 5 (2.23%)
Clutch Case: 7 (3.13%)
...

Items Received by Rarity:
Mil-Spec (Blue): 178 (79.46%)
Restricted (Purple): 37 (16.52%)
Classified (Pink): 8 (3.57%)
Covert (Red): 1 (0.45%)
```

## 🤝 Contributing

Contributions are welcome! If you'd like to propose an improvement or report a bug, please open an issue or submit a pull request.

## 📜 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## 📞 Contact

[cla33ic@cla33ic.de](mailto:cla33ic@cla33ic.de) | [@freundevniemand](https://x.com/FreundeVniemand) 

## 🐛 Troubleshooting

If you encounter issues with character encoding (e.g., € symbol appearing as ?), ensure you're using the `-Dfile.encoding=UTF-8`. This sets the file encoding to UTF-8, which should resolve any character display issues.
