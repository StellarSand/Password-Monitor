# CONTRIBUTING

### Translations
- Remember to insert a backslash (`\`) before any apostrophe (`'`).
- Make sure the characters are properly encoded when translating strings (example: `ä` as `\u00E4`, `é` as `\u00E9` etc).
  <br>You can use websites like [Compart](https://www.compart.com/en/unicode), [Symbl](https://symbl.cc/en/unicode/table/) or something else.
  <br>Example: `é` would be shown as `U+00E9` on these websites, so just convert it to `\u00E9` for the android strings.
  <br>The strings can be found here:
  - [English](https://github.com/StellarSand/Password-Monitor/blob/main/app/src/main/res/values/strings.xml)
  - [Chinese](https://github.com/StellarSand/Password-Monitor/blob/main/app/src/main/res/values-zh/strings.xml)
  - [Dutch](https://github.com/StellarSand/Password-Monitor/blob/main/app/src/main/res/values-nl/strings.xml)
  - [French](https://github.com/StellarSand/Password-Monitor/blob/main/app/src/main/res/values-fr/strings.xml)
  - [German](https://github.com/StellarSand/Password-Monitor/blob/main/app/src/main/res/values-de/strings.xml)
  - [Italian](https://github.com/StellarSand/Password-Monitor/blob/main/app/src/main/res/values-it/strings.xml)
  - [Japanese](https://github.com/StellarSand/Password-Monitor/blob/main/app/src/main/res/values-ja/strings.xml)
  - [Spanish](https://github.com/StellarSand/Password-Monitor/blob/main/app/src/main/res/values-es/strings.xml)
  - [Swedish](https://github.com/StellarSand/Password-Monitor/blob/main/app/src/main/res/values-sv/strings.xml)
  - [Turkish](https://github.com/StellarSand/Password-Monitor/blob/main/app/src/main/res/values-tr/strings.xml)

### Code
- Do not submit pull requests to update gradle, dependencies or SDK.
- Try not to use any deprecated libraries, dependencies or methods, if other alternatives are available.
- Please test your changes before submitting a pull request.
