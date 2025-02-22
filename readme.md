# CS2-Case-History-Analyzer

## 🎮 Overview

The CS2-Case-History-Analyzer is a Java application designed to provide CS2 players with deep insights into their case opening history. With this tool, you can analyze your inventory history, get detailed statistics about opened cases, and determine the total value of your case opening activities.

## ✨ Key Features

- **Detailed Analysis:** Gain insights into your CS2 case opening history
- **Cost Calculation:** Determine total costs for keys and cases
- **Rarity Breakdown:** See a detailed breakdown of received items by rarity
- **Case Statistics:** View statistics on the most frequently opened cases
- **Results Saving:** Save analysis results for later review

## 🛠 Installation & Usage

### Precompiled Jar (Recommended)

For the best user experience, download the precompiled jar directly from the [Releases](https://github.com/cla33ic/CS2_CaseFetcher/releases) page. This lets you run the tool without compiling it yourself.

> **Important:**  
> The precompiled jar is built with a target of Java 17 LTS (Long Term Support). Please ensure you have Java 17 or newer installed on your system.

#### Running the Tool

After downloading the jar, open your terminal or command prompt, navigate to the folder containing the jar, and execute:

```bash
java -jar CS2-Case-History-Analyzer.jar
```

### Building from Source

If you prefer to compile the project yourself, follow these steps:

1. **Ensure Java 17 or higher is installed.**

2. **Clone the repository:**
   ```bash
   git clone https://github.com/cla33ic/CS2_CaseFetcher.git
   ```

3. **Navigate to the project directory:**
   ```bash
   cd CS2_CaseFetcher
   ```

4. **Compile with Maven (targeting Java 8):**
   ```bash
   mvn clean install -Dmaven.compiler.source=17 -Dmaven.compiler.target=17
   ```

5. **Locate the Jar:** The compiled jar (e.g., `CS2-Case-History-Analyzer.jar`) will be in the `target` directory.

## 🚀 Usage

When you run the tool, you'll be prompted to enter:

1. **Your Steam Profile URL**
2. **Your Steam Login Cookie**

### 🍪 How to Obtain the Steam Login Cookie

1. Log in to steamcommunity.com
2. Open your browser's Developer Tools (F12 in most browsers)
3. Go to the "Network" tab
4. Refresh the page
5. Locate a request to "steamcommunity.com" (usually associated with your username)
6. In the Request Headers, find the `Cookie` value
7. Copy the entire value of the `Cookie`

**Warning:** Never share your Steam login cookie with anyone. It provides direct access to your account.

## 📊 Sample Output

After processing your inventory history, you might see an output like:

```yaml
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

Contributions are welcome! If you have suggestions, improvements, or bug reports, please open an issue or submit a pull request.

## 📜 License

This project is licensed under the MIT License. See the LICENSE file for details.

## 📞 Contact

For questions or feedback, reach out at:
- Email: cla33ic@cla33ic.de
- Twitter: @freundevniemand

## 🐛 Troubleshooting

If you experience character encoding issues (e.g., the € symbol appears as a question mark), run the application with the following JVM option:

```bash
-Dfile.encoding=UTF-8
```

For example:
```bash
java -Dfile.encoding=UTF-8 -jar CS2-Case-History-Analyzer.jar
```

By following these instructions, you'll be able to run and enjoy the CS2-Case-History-Analyzer with ease. Happy analyzing!